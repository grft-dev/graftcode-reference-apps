$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

docker compose up --build -d customers vets

$customersGuid = $null
for ($i = 0; $i -lt 60; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8092/maven" -UseBasicParsing -TimeoutSec 5).Content
        $match = [regex]::Match($body, "https://grft.dev/maven2/([0-9a-f-]+)__free")
        if ($match.Success) {
            $customersGuid = $match.Groups[1].Value
            break
        }
    }
    catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $customersGuid) {
    throw "Graftcode Gateway did not publish a Maven repository at http://localhost:8092/maven"
}

Write-Output "customers.graft.guid=$customersGuid"

$env:GRAFT_GUID = $customersGuid
docker compose build visits
docker compose up -d visits

$visitsGuid = $null
for ($i = 0; $i -lt 60; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8096/npm" -UseBasicParsing -TimeoutSec 5).Content
        $match = [regex]::Match($body, "https://grft\.dev/([0-9a-f-]+)__free")
        if ($match.Success) {
            $visitsGuid = $match.Groups[1].Value
            break
        }
    }
    catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $visitsGuid) {
    throw "Graftcode Gateway did not publish an npm registry at http://localhost:8096/npm"
}

Write-Output "visits.graft.guid=$visitsGuid"
$npmrc = "@graft:registry=https://grft.dev/${visitsGuid}__free"
Set-Content -Path "ClinicWeb\.npmrc" -Value $npmrc -Encoding utf8

Push-Location ClinicWeb
try {
    npm install
    npm test
}
finally {
    Pop-Location
}

Push-Location visits
try {
    mvn -q test "-Dgraft.guid=$customersGuid"
}
finally {
    Pop-Location
}
