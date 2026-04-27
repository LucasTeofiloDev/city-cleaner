package citycleaner.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lightweight audio manager for background music.
 * MP3 playback uses JavaFX (when present). WAV playback uses Java Sound Clip.
 */
public final class AudioManager {
    private static Clip backgroundClip;
    private static String currentBackgroundPath;
    private static boolean muted;

    private static Object javafxMediaPlayer;
    private static Method javafxRunLaterMethod;
    private static Method javafxStopMethod;
    private static Method javafxDisposeMethod;
    private static volatile boolean javafxInitialized;

    private static Thread jLayerPlaybackThread;
    private static volatile int jLayerPlaybackGeneration;
    private static Object jLayerPlayer;
    private static Method jLayerCloseMethod;
    private static InputStream jLayerInputStream;

    private AudioManager() {
    }

    public static synchronized void playBackgroundMusic(String relativePath) {
        String previousBackgroundPath = currentBackgroundPath;
        currentBackgroundPath = relativePath;
        stopActivePlayback();

        if (muted) {
            return;
        }

        File audioFile = resolveResourceFile(relativePath);
        if (audioFile == null) {
            System.out.println("AudioManager: file not found: " + relativePath);
            restorePreviousBackground(previousBackgroundPath, relativePath);
            return;
        }

        if (playFile(audioFile)) {
            return;
        }

        System.out.println("AudioManager: could not play audio file: " + audioFile.getAbsolutePath());
        restorePreviousBackground(previousBackgroundPath, relativePath);
    }

    public static synchronized void stopBackgroundMusic() {
        currentBackgroundPath = null;
        stopActivePlayback();
    }

    public static synchronized boolean toggleMuted() {
        setMuted(!muted);
        return muted;
    }

    public static synchronized boolean isMuted() {
        return muted;
    }

    public static synchronized String getCurrentBackgroundPath() {
        return currentBackgroundPath;
    }

    public static synchronized void setMuted(boolean mutedValue) {
        if (muted == mutedValue) {
            return;
        }

        muted = mutedValue;

        if (muted) {
            stopActivePlayback();
            return;
        }

        if (currentBackgroundPath != null && !currentBackgroundPath.trim().isEmpty()) {
            playBackgroundMusic(currentBackgroundPath);
        }
    }

    private static void stopActivePlayback() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }

        stopJLayerPlayback();

        if (javafxMediaPlayer != null) {
            invokeOnJavaFxThread(() -> {
                try {
                    javafxStopMethod.invoke(javafxMediaPlayer);
                    javafxDisposeMethod.invoke(javafxMediaPlayer);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    // Ignore to keep shutdown safe.
                }
            });
            javafxMediaPlayer = null;
        }
    }

    private static boolean playWithClip(File audioFile) {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            backgroundClip = clip;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean playFile(File audioFile) {
        if (audioFile == null) {
            return false;
        }

        String lowerName = audioFile.getName().toLowerCase();
        if (lowerName.endsWith(".mp3")) {
            if (playMp3WithJavaFx(audioFile)) {
                return true;
            }
            if (playMp3WithJLayer(audioFile)) {
                return true;
            }
        }

        return playWithClip(audioFile);
    }

    private static void stopJLayerPlayback() {
        jLayerPlaybackGeneration++;

        if (jLayerPlayer != null && jLayerCloseMethod != null) {
            try {
                jLayerCloseMethod.invoke(jLayerPlayer);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // Keep stop resilient.
            }
        }

        if (jLayerInputStream != null) {
            try {
                jLayerInputStream.close();
            } catch (Exception ignored) {
                // Keep stop resilient.
            }
            jLayerInputStream = null;
        }

        jLayerPlayer = null;
        jLayerCloseMethod = null;

        if (jLayerPlaybackThread != null) {
            jLayerPlaybackThread.interrupt();
            jLayerPlaybackThread = null;
        }
    }

    private static boolean playMp3WithJLayer(File audioFile) {
        try {
            File jLayerJar = resolveJLayerJar();
            if (jLayerJar == null) {
                return false;
            }

            URLClassLoader classLoader = new URLClassLoader(
                new URL[] {jLayerJar.toURI().toURL()},
                AudioManager.class.getClassLoader()
            );
            Class<?> playerClass = Class.forName("javazoom.jl.player.Player", true, classLoader);
            Constructor<?> playerConstructor = playerClass.getConstructor(InputStream.class);
            Method playMethod = playerClass.getMethod("play");
            Method closeMethod = playerClass.getMethod("close");

            final int playbackGeneration = jLayerPlaybackGeneration;
            Thread playbackThread = new Thread(() -> {
                while (playbackGeneration == jLayerPlaybackGeneration) {
                    InputStream stream = null;
                    Object player = null;

                    try {
                        stream = new FileInputStream(audioFile);
                        player = playerConstructor.newInstance(stream);

                        synchronized (AudioManager.class) {
                            if (playbackGeneration != jLayerPlaybackGeneration) {
                                closeQuietly(stream);
                                break;
                            }
                            jLayerInputStream = stream;
                            jLayerPlayer = player;
                            jLayerCloseMethod = closeMethod;
                        }

                        playMethod.invoke(player);
                    } catch (Throwable t) {
                        System.out.println("AudioManager: JLayer MP3 playback failed. " + t.getClass().getSimpleName());
                        break;
                    } finally {
                        synchronized (AudioManager.class) {
                            jLayerPlayer = null;
                            jLayerCloseMethod = null;
                            if (jLayerInputStream != null) {
                                closeQuietly(jLayerInputStream);
                                jLayerInputStream = null;
                            }
                        }
                    }

                    if (playbackGeneration != jLayerPlaybackGeneration) {
                        break;
                    }
                }
            }, "citycleaner-jlayer-bg-music");

            playbackThread.setDaemon(true);
            jLayerPlaybackThread = playbackThread;
            playbackThread.start();
            return true;
        } catch (Throwable t) {
            System.out.println("AudioManager: JLayer unavailable for MP3. " + t.getClass().getSimpleName());
            return false;
        }
    }

    private static void closeQuietly(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();
        } catch (Exception ignored) {
            // Keep shutdown safe.
        }
    }

    private static File resolveJLayerJar() {
        String userDir = System.getProperty("user.dir");

        String[] candidates = new String[] {
            "lib/jlayer-1.0.1.jar",
            "libs/jlayer-1.0.1.jar",
            userDir + "/lib/jlayer-1.0.1.jar",
            userDir + "\\lib\\jlayer-1.0.1.jar"
        };

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists() && file.isFile()) {
                return file;
            }
        }

        return null;
    }

    private static void restorePreviousBackground(String previousPath, String requestedPath) {
        if (muted || previousPath == null || previousPath.trim().isEmpty()) {
            return;
        }

        if (requestedPath != null && previousPath.equalsIgnoreCase(requestedPath)) {
            return;
        }

        File previousAudioFile = resolveResourceFile(previousPath);
        if (previousAudioFile == null) {
            return;
        }

        if (playFile(previousAudioFile)) {
            currentBackgroundPath = previousPath;
            System.out.println("AudioManager: restored previous background track: " + previousPath);
        }
    }

    private static boolean playMp3WithJavaFx(File audioFile) {
        try {
            initializeJavaFx();

            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            invokeOnJavaFxThread(() -> {
                try {
                    Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                    Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");

                    Constructor<?> mediaConstructor = mediaClass.getConstructor(String.class);
                    Object media = mediaConstructor.newInstance(audioFile.toURI().toString());

                    Constructor<?> mediaPlayerConstructor = mediaPlayerClass.getConstructor(mediaClass);
                    Object mediaPlayer = mediaPlayerConstructor.newInstance(media);

                    Field indefiniteField = mediaPlayerClass.getField("INDEFINITE");
                    int indefinite = (Integer) indefiniteField.get(null);

                    Method setCycleCountMethod = mediaPlayerClass.getMethod("setCycleCount", int.class);
                    Method playMethod = mediaPlayerClass.getMethod("play");

                    setCycleCountMethod.invoke(mediaPlayer, indefinite);
                    playMethod.invoke(mediaPlayer);

                    javafxStopMethod = mediaPlayerClass.getMethod("stop");
                    javafxDisposeMethod = mediaPlayerClass.getMethod("dispose");
                    javafxMediaPlayer = mediaPlayer;
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    latch.countDown();
                }
            });

            latch.await();

            if (error.get() != null) {
                System.out.println("AudioManager: MP3 playback failed. " + error.get().getClass().getSimpleName());
                return false;
            }

            return true;
        } catch (Throwable t) {
            System.out.println("AudioManager: JavaFX media unavailable for MP3. " + t.getClass().getSimpleName());
            return false;
        }
    }

    private static void initializeJavaFx() throws Exception {
        if (javafxInitialized) {
            return;
        }

        Class<?> platformClass = Class.forName("javafx.application.Platform");
        javafxRunLaterMethod = platformClass.getMethod("runLater", Runnable.class);
        Method startupMethod = platformClass.getMethod("startup", Runnable.class);

        try {
            startupMethod.invoke(null, (Runnable) () -> {
            });
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null || !cause.getClass().getSimpleName().contains("IllegalStateException")) {
                throw e;
            }
        }

        javafxInitialized = true;
    }

    private static void invokeOnJavaFxThread(Runnable runnable) {
        if (javafxRunLaterMethod == null) {
            return;
        }

        try {
            javafxRunLaterMethod.invoke(null, runnable);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            // Ignore to keep audio failures non-fatal.
        }
    }

    private static File resolveResourceFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }

        String normalized = relativePath.replaceFirst("^/", "");
        String normalizedWithMusicFallback = normalized;
        if (normalized.startsWith("audio/") && !normalized.startsWith("audio/music/")) {
            normalizedWithMusicFallback = "audio/music/" + normalized.substring("audio/".length());
        }

        String userDir = System.getProperty("user.dir");

        String[] candidates = new String[] {
            normalized,
            normalizedWithMusicFallback,
            "resources/" + normalized,
            "resources/" + normalizedWithMusicFallback,
            userDir + "/resources/" + normalized,
            userDir + "/resources/" + normalizedWithMusicFallback,
            userDir + "\\resources\\" + normalized.replace('/', '\\'),
            userDir + "\\resources\\" + normalizedWithMusicFallback.replace('/', '\\')
        };

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists() && file.isFile()) {
                return file;
            }
        }

        return null;
    }
}