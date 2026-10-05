$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

docker compose up --build -d

$guid = $null
for ($i = 0; $i -lt 30; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8090/maven" -UseBasicParsing -TimeoutSec 5).Content
        $match = [regex]::Match($body, "https://grft.dev/maven2/([0-9a-f-]+)__free")
        if ($match.Success) {
            $guid = $match.Groups[1].Value
            break
        }
    }
    catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $guid) {
    throw "Graftcode Gateway did not publish a Maven repository at http://localhost:8090/maven"
}

Write-Output "graft.guid=$guid"
Push-Location ClinicConsumer
try {
    mvn -q test "-Dgraft.guid=$guid"
}
finally {
    Pop-Location
}
