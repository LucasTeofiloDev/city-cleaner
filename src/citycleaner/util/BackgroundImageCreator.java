package citycleaner.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Criador de imagem de fundo para o jogo
 */
public class BackgroundImageCreator {

    public static void createBackgroundImage() {
        int width = 1152;
        int height = 576;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Anti-aliasing para melhor qualidade
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fundo - céu degradado
        drawSky(g2d, width, height);

        // Nuvens
        drawClouds(g2d, width, height);

        // Prédios ao fundo
        drawBuildings(g2d, width, height);

        // Árvores
        drawTrees(g2d, width, height);

        // Casa de reciclagem
        drawRecyclingCenter(g2d);

        // Ponte
        drawBridge(g2d, width, height);

        // Fumaça de poluição
        drawPollution(g2d, width, height);

        // Grama e chão
        drawGround(g2d, width, height);

        // Salvar imagem
        try {
            File outputFile = new File("resources/sprites/background.png");
            outputFile.getParentFile().mkdirs(); // Criar diretórios se não existirem
            ImageIO.write(image, "png", outputFile);
            System.out.println("Imagem de fundo criada com sucesso: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao salvar imagem de fundo: " + e.getMessage());
        }

        g2d.dispose();
    }

    private static void drawSky(Graphics2D g, int width, int height) {
        // Gradiente do céu (azul claro no topo, azul mais escuro embaixo)
        GradientPaint skyGradient = new GradientPaint(
            0, 0, new Color(135, 206, 250), // Azul céu claro
            0, height/2, new Color(70, 130, 180)  // Azul mais escuro
        );
        g.setPaint(skyGradient);
        g.fillRect(0, 0, width, height);
    }

    private static void drawClouds(Graphics2D g, int width, int height) {
        g.setColor(new Color(255, 255, 255, 220));

        // Nuvem 1 - esquerda
        drawCloud(g, 150, 80, 80, 50);

        // Nuvem 2 - centro
        drawCloud(g, 500, 120, 100, 60);

        // Nuvem 3 - direita
        drawCloud(g, 900, 100, 90, 55);

        // Nuvem 4 - pequena
        drawCloud(g, 300, 60, 60, 35);
    }

    private static void drawCloud(Graphics2D g, int x, int y, int width, int height) {
        // Desenhar várias elipses para formar uma nuvem
        g.fillOval(x, y, width, height);
        g.fillOval(x + width/3, y - height/3, width, height);
        g.fillOval(x + (width*2/3), y, width, height);
        g.fillOval(x + width/6, y + height/4, width*3/4, height*3/4);
    }

    private static void drawBuildings(Graphics2D g, int width, int height) {
        // Prédios de diferentes alturas
        int[] buildingX = {100, 280, 480, 680, 880, 1050};
        int[] buildingWidth = {80, 90, 100, 85, 95, 75};
        int[] buildingHeight = {180, 250, 200, 300, 220, 160};

        for (int i = 0; i < buildingX.length; i++) {
            int x = buildingX[i];
            int w = buildingWidth[i];
            int h = buildingHeight[i];
            int y = height/2 - h;

            // Corpo do prédio
            g.setColor(new Color(105, 105, 105)); // Cinza escuro
            g.fillRect(x, y, w, h);

            // Janelas
            g.setColor(new Color(255, 255, 150, 200)); // Amarelo claro translúcido
            int windowRows = h / 25;
            int windowCols = w / 20;

            for (int row = 0; row < windowRows; row++) {
                for (int col = 0; col < windowCols; col++) {
                    if (Math.random() > 0.3) { // Algumas janelas acesas
                        g.fillRect(x + 5 + col * 20, y + 10 + row * 25, 12, 15);
                    }
                }
            }

            // Borda do prédio
            g.setColor(new Color(70, 70, 70));
            g.setStroke(new BasicStroke(1));
            g.drawRect(x, y, w, h);
        }
    }

    private static void drawTrees(Graphics2D g, int width, int height) {
        // Árvores nas laterais
        drawTree(g, 30, height/2 + 50);
        drawTree(g, 80, height/2 + 30);
        drawTree(g, width - 80, height/2 + 40);
        drawTree(g, width - 130, height/2 + 60);
    }

    private static void drawTree(Graphics2D g, int x, int y) {
        // Tronco
        g.setColor(new Color(139, 69, 19)); // Marrom
        g.fillRect(x, y, 15, 40);

        // Folhagem
        g.setColor(new Color(34, 139, 34)); // Verde escuro
        g.fillOval(x - 20, y - 30, 55, 50);
        g.fillOval(x - 15, y - 40, 45, 45);
    }

    private static void drawRecyclingCenter(Graphics2D g) {
        int x = 350;
        int y = 280;

        // Base da casa
        g.setColor(new Color(200, 200, 200)); // Cinza claro
        g.fillRect(x, y, 180, 80);

        // Teto
        g.setColor(new Color(150, 50, 50)); // Vermelho escuro
        int[] roofX = {x - 10, x + 90, x + 190};
        int[] roofY = {y, y - 30, y};
        g.fillPolygon(roofX, roofY, 3);

        // Porta
        g.setColor(new Color(101, 67, 33)); // Marrom
        g.fillRect(x + 75, y + 50, 30, 30);

        // Janelas
        g.setColor(new Color(135, 206, 250)); // Azul céu
        g.fillRect(x + 20, y + 20, 25, 20);
        g.fillRect(x + 55, y + 20, 25, 20);
        g.fillRect(x + 110, y + 20, 25, 20);
        g.fillRect(x + 145, y + 20, 25, 20);

        // Símbolo de reciclagem
        drawRecycleSymbol(g, x + 190, y + 30, 20);
    }

    private static void drawRecycleSymbol(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(0, 128, 0)); // Verde
        g.setStroke(new BasicStroke(2));

        // Triângulo com setas de reciclagem
        for (int i = 0; i < 3; i++) {
            double angle = (i * Math.PI * 2) / 3 - Math.PI / 2;
            int x1 = (int)(x + Math.cos(angle) * size);
            int y1 = (int)(y + Math.sin(angle) * size);
            g.drawLine(x, y, x1, y1);
        }

        // Círculo
        g.drawOval(x - size/2, y - size/2, size, size);
    }

    private static void drawBridge(Graphics2D g, int width, int height) {
        int bridgeY = height/2 - 20;

        // Base da ponte
        g.setColor(new Color(139, 69, 19)); // Marrom
        g.fillRect(750, bridgeY, 200, 15);

        // Arcos da ponte
        g.setColor(new Color(105, 105, 105)); // Cinza
        for (int i = 0; i < 3; i++) {
            int arcX = 760 + i * 60;
            g.fillArc(arcX, bridgeY - 25, 50, 50, 0, 180);
        }

        // Cabos
        g.setColor(new Color(50, 50, 50)); // Cinza escuro
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 5; i++) {
            g.drawLine(750 + i * 40, bridgeY, 750 + i * 40, bridgeY - 40);
        }
    }

    private static void drawPollution(Graphics2D g, int width, int height) {
        g.setColor(new Color(100, 100, 100, 180)); // Cinza translúcido

        // Fumaça saindo de chaminés
        drawSmoke(g, 180, height/2 - 180, 20);
        drawSmoke(g, 370, height/2 - 250, 25);
        drawSmoke(g, 570, height/2 - 200, 18);
        drawSmoke(g, 770, height/2 - 300, 22);
    }

    private static void drawSmoke(Graphics2D g, int x, int y, int size) {
        // Várias elipses para formar fumaça
        g.fillOval(x, y, size, (int)(size * 1.5));
        g.fillOval(x - size/3, y - size/2, size, size);
        g.fillOval(x + size/3, y - size/3, (int)(size * 0.8), (int)(size * 1.2));
    }

    private static void drawGround(Graphics2D g, int width, int height) {
        // Grama
        g.setColor(new Color(34, 139, 34)); // Verde grama
        g.fillRect(0, height - 80, width, 80);

        // Detalhes da grama
        g.setColor(new Color(50, 180, 50)); // Verde mais claro
        for (int i = 0; i < width; i += 15) {
            int grassHeight = 8 + (int)(Math.random() * 6);
            g.drawLine(i, height - 80, i + 5, height - 80 + grassHeight);
        }

        // Algumas flores
        g.setColor(Color.YELLOW);
        for (int i = 0; i < 5; i++) {
            int flowerX = 100 + i * 200;
            int flowerY = height - 90;
            g.fillOval(flowerX, flowerY, 4, 4);
        }
    }

    public static void main(String[] args) {
        createBackgroundImage();
    }
}