param([string]$Destination)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $Destination) {
    $Destination = Join-Path (Split-Path $repoRoot -Parent) 'riverfront-urban-design-release.zip'
}
$archivePath = [IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $archivePath) {
    throw "Archive already exists. Choose a new -Destination: $archivePath"
}
$allowedDirectories = @('src', 'lib', 'assets', 'docs', 'scripts', '.mvn')
$allowedFiles = @('pom.xml', 'README.md', 'THIRD_PARTY_NOTICES.md', '.gitignore', '.gitattributes', 'mvnw', 'mvnw.cmd')
$files = foreach ($directory in $allowedDirectories) {
    $candidate = Join-Path $repoRoot $directory
    if (Test-Path -LiteralPath $candidate) { Get-ChildItem -LiteralPath $candidate -Recurse -File -Force }
}
foreach ($name in $allowedFiles) {
    $candidate = Join-Path $repoRoot $name
    if (Test-Path -LiteralPath $candidate) { $files += Get-Item -LiteralPath $candidate -Force }
}
foreach ($file in $files) {
    $relative = $file.FullName.Substring($repoRoot.Length + 1).Replace('\', '/')
    if ($file.Extension -in @('.csv', '.archijson', '.3dmbak', '.class', '.log', '.zip')) {
        throw "Refusing to package local data/build output: $relative"
    }
    if ($file.Extension -eq '.3dm' -and $relative -ne 'assets/components/door-and-window.3dm') {
        throw "Only the door/window component model may be distributed: $relative"
    }
    if ($file.Length -ge 25MB) { throw "File exceeds the 25 MiB project packaging limit: $relative" }
}
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::Open($archivePath, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in ($files | Sort-Object FullName)) {
        $relative = $file.FullName.Substring($repoRoot.Length + 1).Replace('\', '/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $file.FullName,
            "riverfront-urban-design/$relative", [IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally { $archive.Dispose() }
$result = Get-Item -LiteralPath $archivePath
Write-Output ("Created {0} ({1:N2} MiB, {2} files)" -f $result.FullName, ($result.Length / 1MB), $files.Count)
Get-FileHash -LiteralPath $archivePath -Algorithm SHA256
