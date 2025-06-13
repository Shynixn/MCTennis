function Invoke-Name-Patch{
    $extensions = @(".md", ".kts", ".yml", ".kt", ".html")
    $rootPath = $PSScriptRoot

    Get-ChildItem -Path $rootPath -Recurse -File | Where-Object { $extensions -contains $_.Extension } | ForEach-Object {
        $filePath = $_.FullName
        $data = Get-Content -Path $filePath -Raw
        $data = $data.Replace("blockball", "mctennis").replace("BlockBall", "MCTennis").replace("Soccer", "Tennis").replace("soccer", "tennis").replace("/football", "")
        Set-Content -Value $data -Path $filePath

        $relativePath = $filePath.Substring($rootPath.Length).TrimStart("\\")
        $newRelativePath = $relativePath.Replace("blockball", "mctennis").replace("BlockBall", "MCTennis").replace("Soccer", "Tennis").replace("soccer", "tennis")
        $fullNewPath = "$PSScriptRoot\$newRelativePath"

        $fileDirectory = [System.IO.Path]::GetDirectoryName($fullNewPath )

        # Ensure the directory exists
        if (!(Test-Path -Path $fileDirectory)) {
            $null = New-Item -ItemType Directory -Path $fileDirectory -Force
        }

        Move-Item -Path $filePath -Destination $fullNewPath -Force
        Write-Host "Modified $filePath"
    }

    Get-ChildItem -Path $rootPath -Recurse -Directory |  ForEach-Object {
        $filePath = $_.FullName
        $relativePath = $filePath.Substring($rootPath.Length).TrimStart("\\")

        if($relativePath.contains("blockball") -or $relativePath.contains("BlockBall")){
            if ((Test-Path -Path $filePath)) {
                Remove-Item $filePath -Force -Recurse
                Write-Host "Deleted $filePath"
            }
        }
    }
}

Invoke-Name-Patch
