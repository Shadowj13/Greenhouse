$ErrorActionPreference = 'Stop'
$base = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\project\BonsaiGreenhouse-26.3'))
$assets = Join-Path $base 'src\main\resources\assets\greenhouse'

# Decompose pane models into the two visible faces only. The old pane cubes
# rendered all six faces at a tiny thickness, causing repeated glass passes,
# intrusive edges and overlap with posts at some angles.
foreach ($name in @('greenhouse', 'automatic_greenhouse')) {
    $path = Join-Path $assets "models\block\$name.json"
    $model = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json -AsHashtable
    if ($model['elements'].Count -lt 26) { throw "Unexpected geometry in $path" }

    foreach ($index in @(15,16,17,18,23,24)) {
        $element = $model['elements'][$index]
        $faces = $element['faces']
        $names = if ($index -in @(15,16)) { @('north','south') }
                 elseif ($index -in @(17,18)) { @('west','east') }
                 else { @('up','down') }
        $visibleFaces = [ordered]@{}
        foreach ($side in $names) {
            if (!$faces.Contains($side)) { throw "Face $side missing on element $index" }
            $visibleFaces[$side] = $faces[$side]
        }
        $element['faces'] = $visibleFaces
    }

    $json = $model | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText($path, $json + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))
}

$softGlass = (Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'textures\soft_glass.png.b64')).Trim()
$target = Join-Path $assets 'textures\block\glass.png'
[System.IO.File]::WriteAllBytes($target, [Convert]::FromBase64String($softGlass))
Write-Host 'Clean glass: reduced transparent overdraw, removed diagonal streaks.'
