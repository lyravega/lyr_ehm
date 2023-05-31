$stuffToPack =  ("LICENSE.md"),
                ("README.md"),
                ("mod_info.json"),
                ("EHM.version"),
                ("changelog.txt"),
                ("jars"),
                ("data"),
                ("customization")

foreach($line in Get-Content ".\mod_info.json") {
    if($line -match '"version"') {
        $null = $line -match ':"(.*?)",'
        $version = $Matches[1]
    }
}

$null = New-Item "$PSScriptRoot\lyr_ehm\" -ItemType Directory
foreach($path in $stuffToPack)
{
    Write-Host $path -ForegroundColor Blue
    Copy-Item -Path ".\$path" -Destination "$PSScriptRoot\lyr_ehm\" -Recurse
}
Compress-Archive -Path "$PSScriptRoot\lyr_ehm\" -DestinationPath "$PSScriptRoot\lyr_ehm $version.zip" -Update
Remove-Item "$PSScriptRoot\lyr_ehm\" -Force -Recurse
Write-Host "Mod packed!" -ForegroundColor Green