$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

docker compose up --build -d

$guid = $null
for ($i = 0; $i -lt 30; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8090/npm" -UseBasicParsing -TimeoutSec 5).Content
        $match = [regex]::Match($body, "https://grft\.dev/([0-9a-f-]+)__free")
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
    throw "Graftcode Gateway did not publish an npm registry at http://localhost:8090/npm"
}

Write-Output "graft.guid=$guid"
$npmrc = "@graft:registry=https://grft.dev/${guid}__free"
Set-Content -Path "ClinicWeb\.npmrc" -Value $npmrc -Encoding utf8

Push-Location ClinicWeb
try {
    npm install
    npm test
}
finally {
    Pop-Location
}
