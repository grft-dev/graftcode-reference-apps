$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

docker compose up --build -d

$body = $null
for ($i = 0; $i -lt 60; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8092/npm" -UseBasicParsing -TimeoutSec 5).Content
        if ($body -match "grft\.dev") {
            break
        }
    }
    catch {
        $body = $null
    }
    Start-Sleep -Seconds 2
}

if (-not $body) {
    throw "Graftcode Gateway did not publish an npm install command at http://localhost:8092/npm"
}

$package = $null
$registry = $null
if ($body -match "npm install\s+(@graft/\S+)") {
    $package = $Matches[1]
}
if ($body -match "(https://grft\.dev/[^\s`"']+)") {
    $registry = $Matches[1].TrimEnd(")", ",", ";")
}

if (-not $package -or -not $registry) {
    throw "Could not read the graft package and registry from http://localhost:8092/npm:`n$body"
}

Write-Output "graft.package=$package"
Write-Output "graft.registry=$registry"

Push-Location ArticleConsumer
try {
    npm install $package --registry $registry
    $env:GRAFT_PACKAGE = $package
    npm test
}
finally {
    Pop-Location
    Remove-Item Env:GRAFT_PACKAGE -ErrorAction SilentlyContinue
}
