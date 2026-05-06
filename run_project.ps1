$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mvn = Join-Path $projectRoot 'tools\apache-maven-3.9.9\bin\mvn.cmd'
$pom = Join-Path $projectRoot 'java\pom.xml'

if (-not (Test-Path $mvn)) {
    throw "Maven local introuvable: $mvn"
}

if (-not (Test-Path $pom)) {
    throw "pom.xml introuvable: $pom"
}

Push-Location (Join-Path $projectRoot 'java')
try {
    & $mvn -f $pom clean compile exec:java
}
finally {
    Pop-Location
}