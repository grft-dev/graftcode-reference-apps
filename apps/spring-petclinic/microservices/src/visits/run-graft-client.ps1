# Requires customers host: docker compose up -d customers (microservices/src)
# Pass customers Maven GUID: -GraftGuid <guid> or env GRAFT_GUID

param(
    [string]$GraftGuid = $env:GRAFT_GUID
)

$ErrorActionPreference = "Stop"

$mavenUrl = if ($env:GRAFT_MAVEN_URL) { $env:GRAFT_MAVEN_URL } else { "http://localhost:8092/maven" }
try {
    $body = (Invoke-WebRequest -Uri $mavenUrl -UseBasicParsing -TimeoutSec 5).Content
    if (-not $GraftGuid) {
        $match = [regex]::Match($body, "https://grft.dev/maven2/([0-9a-f-]+)__free")
        if ($match.Success) {
            $GraftGuid = $match.Groups[1].Value
        }
    }
}
catch {
    throw "Customers gateway not reachable at $mavenUrl. Start: cd microservices\src; docker compose up -d customers"
}

if (-not $GraftGuid) {
    throw "Set -GraftGuid or GRAFT_GUID (from http://localhost:8092/maven)"
}

if (-not $env:CUSTOMERS_GRAFT_HOST) {
    $env:CUSTOMERS_GRAFT_HOST = "ws://localhost:8092/ws"
}

Write-Host "WebSocket host: $env:CUSTOMERS_GRAFT_HOST"
Write-Host "graft.guid=$GraftGuid"

mvn -q compile exec:java "-Dgraft.guid=$GraftGuid" "-Dexec.mainClass=org.springframework.samples.petclinic.visits.Main"
