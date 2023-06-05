$stuffToPack =  ("LICENSE.md"),
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

$null = New-Item "$PSScriptRoot\Experimental Hull Modifications\" -ItemType Directory
foreach($path in $stuffToPack)
{
    Write-Host $path -ForegroundColor Blue
    Copy-Item -Path ".\$path" -Destination "$PSScriptRoot\Experimental Hull Modifications\" -Recurse
}
Compress-Archive -Path "$PSScriptRoot\Experimental Hull Modifications\" -DestinationPath "$PSScriptRoot\ExperimentalHullModifications $version.zip" -Update
Remove-Item "$PSScriptRoot\Experimental Hull Modifications\" -Force -Recurse
Write-Host "Mod packed!" -ForegroundColor Green