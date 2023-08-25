$7zip = "$env:ProgramFiles\7-Zip\7z.exe"
Set-Alias Start-SevenZip $7zip

$stuffToPack = ("LICENSE.md"),
               ("README.md"),
               ("mod_info.json"),
               ("EHM.version"),
               ("changelog.txt"),
               ("jars"),
               ("data")

foreach($line in Get-Content ".\mod_info.json") {
    if($line -match '"version"') {
        $null = $line -match ':"(.*?)",'
        $version = $Matches[1]
    }
}

$sourceDir = "$PSScriptRoot\Experimental Hull Modifications\"
$targetZip = "$PSScriptRoot\ExperimentalHullModifications $version.zip"

$null = New-Item "$PSScriptRoot\Experimental Hull Modifications\" -ItemType Directory
Write-Host "Copying items" -ForegroundColor Yellow
foreach ($path in $stuffToPack) {
    Write-Host $path -ForegroundColor Blue
    Copy-Item -Path ".\$path" -Destination $sourceDir -Recurse
}

if (Test-Path -Path $targetZip -PathType Leaf) {
    Write-Host "Deleting old archive" -ForegroundColor Red
    Remove-Item $targetZip -Force -Recurse
}

# Compress-Archive -Path "$PSScriptRoot\Experimental Hull Modifications\" -DestinationPath "$PSScriptRoot\ExperimentalHullModifications $version.zip" -Update
$null = Start-SevenZip a -mx=9 $targetZip $sourceDir

Write-Host "Deleting copied items" -ForegroundColor Red
Remove-Item $sourceDir -Force -Recurse

if (Test-Path -Path $targetZip -PathType Leaf) {
    Write-Host "Mod packed!" -ForegroundColor Green
}