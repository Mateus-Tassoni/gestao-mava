# Carrega o .env e inicia a aplicação
# Uso: .\run.ps1

if (-not (Test-Path .env)) {
    Write-Host "Arquivo .env nao encontrado. Copie .env.example para .env e configure as credenciais:" -ForegroundColor Red
    Write-Host "  copy .env.example .env" -ForegroundColor Yellow
    exit 1
}

Get-Content .env -Encoding UTF8 | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim().Trim('"').Trim("'")
        Set-Item -Path "Env:$name" -Value $value -Force
    }
}

Write-Host ">>> Clean install..." -ForegroundColor Cyan
.\mvnw.cmd clean install
if ($LASTEXITCODE -ne 0) {
    Write-Host "Falha no build." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ">>> Iniciando aplicacao..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run
