$ErrorActionPreference = 'Stop'
$taskProjectRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskProjectRoot
try {
    & (Join-Path $taskProjectRoot 'mvnw.cmd') -B -ntp clean verify
    if ($LASTEXITCODE -ne 0) { throw "Build failed (exit $LASTEXITCODE)." }
} finally {
    Pop-Location
}
