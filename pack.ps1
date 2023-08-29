$7zip = "$env:ProgramFiles/7-Zip/7z.exe"
Set-Alias Start-SevenZip $7zip

$stuffToPack = ("LICENSE.md"),
               ("README.md"),
               ("mod_info.json"),
               ("EHM.version"),
               ("changelog.txt"),
               ("jars"),
               ("data")

foreach($line in Get-Content "./mod_info.json") {
    if($line -match '"version"') {
        $null = $line -match ':"(.*?)",'
        $version = $Matches[1]
        break
    }
}

Write-Host "Mod archiver" -ForegroundColor Green

$sourceDir = "$PSScriptRoot/Experimental Hull Modifications/"
$targetZip = "$PSScriptRoot/ExperimentalHullModifications $version.zip"

$null = New-Item $sourceDir -ItemType Directory
Write-Host "Copying items" -ForegroundColor Blue
foreach ($path in $stuffToPack) {
    Copy-Item -Path "./$path" -Destination $sourceDir -Recurse
}

if (Test-Path -Path $targetZip -PathType Leaf) {
    Write-Host "Deleting old archive" -ForegroundColor Red
    Remove-Item $targetZip -Force -Recurse
}

Write-Host "Packing mod" -ForegroundColor Blue
Compress-Archive -Path $sourceDir -DestinationPath $targetZip -Update
# $null = Start-SevenZip u -mx=9 $targetZip $sourceDir

Write-Host "Deleting copied items" -ForegroundColor Red
Remove-Item $sourceDir -Force -Recurse

if (Test-Path -Path $targetZip -PathType Leaf) {
    Write-Host "Mod archived!" -ForegroundColor Green
}