$ErrorActionPreference = 'Stop'

$backend = 'C:\Users\Deevi Ujwal\OneDrive - K L University\Desktop\CIVIC VOTE\CivicVote\backend'
$logDir = Join-Path $env:TEMP 'CivicVote'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$env:DB_USERNAME = 'root'
$securePassword = Read-Host 'Enter MySQL root password (input is hidden)' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password

try {
    $java = (Get-Command java.exe).Source

    foreach ($service in @('auth-service', 'election-service', 'voting-service', 'result-service')) {
        $jar = Join-Path $backend ($service + '\target\' + $service + '-1.0.0.jar')
        if (-not (Test-Path $jar)) {
            throw "Service jar missing: $jar"
        }

        $stdout = Join-Path $logDir ($service + '.out.log')
        $stderr = Join-Path $logDir ($service + '.err.log')
        $process = Start-Process -FilePath $java `
            -ArgumentList @('-jar', ('"' + $jar + '"')) `
            -WorkingDirectory (Split-Path $jar) `
            -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr `
            -PassThru

        Write-Output "$service launched (PID $($process.Id))"
        Start-Sleep -Seconds 4
    }
}
finally {
    Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue
}

Write-Output "Waiting for service health checks. Logs: $logDir"
Start-Sleep -Seconds 25

foreach ($port in @(8081, 8082, 8083, 8084)) {
    try {
        $health = Invoke-RestMethod "http://127.0.0.1:$port/actuator/health" -TimeoutSec 3
        Write-Output "Port ${port} health: $($health.status)"
    }
    catch {
        Write-Output "Port ${port} did not pass health check: $($_.Exception.Message)"
    }
}
