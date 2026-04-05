# Script para compilar o projeto City Cleaner
$files = Get-ChildItem -Path src -Recurse -Include "*.java"
Write-Host "Compilando $($files.Count) arquivos Java..."
javac -d bin -sourcepath src $files.FullName

if ($?) {
    Write-Host "✓ Compilação concluída com sucesso!" -ForegroundColor Green
} else {
    Write-Host "✗ Erro na compilação!" -ForegroundColor Red
}
