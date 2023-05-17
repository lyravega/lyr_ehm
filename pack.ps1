$stuffToPack =  (".\LICENSE.md"),
                (".\README.md"),
                (".\mod_info.json"),
                (".\EHM.version"),
                (".\jars"),
                (".\data"),
                (".\customization")

foreach($line in Get-Content ".\mod_info.json") {
    if($line -match '"version"'){
        $line -match ':"(.*?)",'
        $version = $Matches[1]
        Write-Host $version
    }
}

foreach($path in $stuffToPack)
{
    Write-Host $path -ForegroundColor Red
    Compress-Archive -Path $path -DestinationPath "$PSScriptRoot\lyr_ehm $version.zip" -Update
}
Write-Host "Mod packed!" -ForegroundColor Green