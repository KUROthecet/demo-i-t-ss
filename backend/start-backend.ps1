# start-backend.ps1
# Loads .env.local and starts the AIMS backend JAR with real email credentials.
# Usage: .\start-backend.ps1

param(
    [string]$EnvFile = ".env.local"
)

if (-not (Test-Path $EnvFile)) {
    Write-Host "[WARN] $EnvFile not found. Email will not be sent (no SMTP credentials)." -ForegroundColor Yellow
    Write-Host "       Copy .env.example to $EnvFile and fill in your credentials." -ForegroundColor Yellow
} else {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.+)$') {
            $key   = $Matches[1].Trim()
            $value = $Matches[2].Trim()
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "[ENV] $key = $('*' * [Math]::Min($value.Length, 8))..." -ForegroundColor DarkGray
        }
    }
    Write-Host "[OK] Environment loaded from $EnvFile" -ForegroundColor Green
}

$jar = Get-ChildItem "target\*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1
if (-not $jar) {
    Write-Host "[ERROR] No JAR found in target\. Run: .\mvnw.cmd package -DskipTests" -ForegroundColor Red
    exit 1
}

Write-Host "[START] Launching $($jar.Name)..." -ForegroundColor Cyan
java -jar $jar.FullName
