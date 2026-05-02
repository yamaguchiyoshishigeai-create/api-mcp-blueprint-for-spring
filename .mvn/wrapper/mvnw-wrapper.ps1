$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoDir = Split-Path -Parent (Split-Path -Parent $scriptDir)
$propertiesPath = Join-Path $scriptDir 'maven-wrapper.properties'

if (!(Test-Path $propertiesPath)) {
    Write-Error "Maven wrapper properties not found: $propertiesPath"
}

$properties = Get-Content -Raw $propertiesPath | ConvertFrom-StringData
$distributionUrl = $properties.distributionUrl
if ([string]::IsNullOrWhiteSpace($distributionUrl)) {
    Write-Error "distributionUrl is not defined in $propertiesPath"
}

$homeDir = [Environment]::GetFolderPath('UserProfile')
$mavenUserHome = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { Join-Path $homeDir '.m2' }
$distRoot = Join-Path $mavenUserHome 'wrapper\dists'
$fileName = Split-Path $distributionUrl -Leaf
$mainName = $fileName -replace '-bin\.zip$', ''
$targetRoot = Join-Path $distRoot $mainName
$mavenHome = Join-Path $targetRoot $mainName
$mvnCommand = Join-Path $mavenHome 'bin\mvn.cmd'

if (!(Test-Path $mvnCommand)) {
    New-Item -ItemType Directory -Force -Path $targetRoot | Out-Null
    $zipPath = Join-Path $targetRoot $fileName
    if (!(Test-Path $zipPath)) {
        Write-Host "Downloading Maven from $distributionUrl"
        Invoke-WebRequest -Uri $distributionUrl -OutFile $zipPath
    }

    $extractDir = Join-Path $targetRoot 'extract'
    if (Test-Path $extractDir) {
        Remove-Item $extractDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $extractDir | Out-Null
    Expand-Archive -Path $zipPath -DestinationPath $extractDir -Force

    $extracted = Get-ChildItem $extractDir -Directory | Select-Object -First 1
    if ($null -eq $extracted) {
        Write-Error "Could not find extracted Maven directory."
    }

    if (Test-Path $mavenHome) {
        Remove-Item $mavenHome -Recurse -Force
    }
    Move-Item $extracted.FullName $mavenHome
    Remove-Item $extractDir -Recurse -Force
}

& $mvnCommand @args
exit $LASTEXITCODE
