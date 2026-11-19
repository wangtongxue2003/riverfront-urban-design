param(
    [double]$Width = 14,
    [double]$Depth = 18,
    [double]$Floors = 1.5
)
$ErrorActionPreference = 'Stop'
$taskProjectRoot = Split-Path -Parent $PSScriptRoot
$taskMavenArgs = @('-B', '-ntp', 'compile', 'exec:java')
if ($PSBoundParameters.Count -gt 0) {
    $taskCulture = [Globalization.CultureInfo]::InvariantCulture
    $taskDimensions = @($Width.ToString($taskCulture), $Depth.ToString($taskCulture), $Floors.ToString($taskCulture)) -join ' '
    $taskMavenArgs += "-Dexec.args=$taskDimensions"
}
Push-Location -LiteralPath $taskProjectRoot
try {
    & (Join-Path $taskProjectRoot 'mvnw.cmd') @taskMavenArgs
    if ($LASTEXITCODE -ne 0) { throw "Demo failed (exit $LASTEXITCODE)." }
} finally {
    Pop-Location
}
