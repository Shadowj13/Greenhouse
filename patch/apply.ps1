$ErrorActionPreference = 'Stop'
$base = Join-Path $PSScriptRoot '..\project\BonsaiGreenhouse-26.3'
$base = [System.IO.Path]::GetFullPath($base)
if (!(Test-Path (Join-Path $base 'build.gradle'))) { throw "Missing extracted project: $base" }

# Apply complete Java overlays after the compatibility updates in the CI workflow.
$javaDir = Join-Path $base 'src\main\java\it\bonsaigreenhouse'
foreach ($name in @('BonsaiGreenhouse.java','GreenhouseMenu.java','GreenhouseScreen.java','ClientScreens.java')) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "java\$name") -Destination (Join-Path $javaDir $name) -Force
}

# Keep the author's Java package namespace for continuity; only mod/resource
# identifiers become 'greenhouse'. This intentionally does not migrate alpha worlds.
$gradleProps = Join-Path $base 'gradle.properties'
$p = [System.IO.File]::ReadAllText($gradleProps)
$p = $p.Replace('mod_id=bonsai_greenhouse','mod_id=greenhouse')
$p = $p.Replace('mod_name=Bonsai Greenhouse','mod_name=Greenhouse')
$p = $p.Replace('mod_version=0.1.0-alpha','mod_version=0.2.0-alpha')
[System.IO.File]::WriteAllText($gradleProps,$p,[System.Text.UTF8Encoding]::new($false))

# Replace the placeholder vanilla 27-slot chest menu with the new 3-input GUI
# (plus 9 outputs only for automatic greenhouses). Synced data powers the
# progress bar, readiness feedback, and fertilization indicator.
$beFile = Join-Path $javaDir 'GreenhouseBlockEntity.java'
$be = [System.IO.File]::ReadAllText($beFile)
$old = '    private boolean automatic() { return getBlockState().getBlock() == BonsaiGreenhouse.HOPPING.get(); }'
if (!$be.Contains($old)) { throw 'Expected isAutomatic anchor was not found in GreenhouseBlockEntity' }
$insert = @'
    public boolean isAutomatic() { return automatic(); }
    public net.minecraft.world.inventory.ContainerData dataAccess() {
        return new net.minecraft.world.inventory.ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> elapsed;
                    case 1 -> duration;
                    case 2 -> mature ? 1 : 0;
                    case 3 -> chargedCycles;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 4; }
        };
    }
'@
$be = $be.Replace($old, $old + [Environment]::NewLine + $insert)
$oldMenu = 'return ChestMenu.threeRows(windowId,inventory,this);'
if (!$be.Contains($oldMenu)) { throw 'Expected menu constructor was not found' }
$be = $be.Replace($oldMenu, 'return new GreenhouseMenu(windowId,inventory,this);')
[System.IO.File]::WriteAllText($beFile,$be,[System.Text.UTF8Encoding]::new($false))

# Rename both namespaces and both block ids within all assets, recipes,
# loot tables, models, and in-game item definitions.
$resources = Join-Path $base 'src\main\resources'
foreach ($kind in @('assets','data')) {
    $old = Join-Path $resources "$kind\bonsai_greenhouse"
    $new = Join-Path $resources "$kind\greenhouse"
    if (Test-Path $old) { Move-Item -LiteralPath $old -Destination $new }
}

# Rename filenames before textual replacement, keeping references consistent.
Get-ChildItem -LiteralPath $resources -Recurse -File | Where-Object { $_.Extension -eq '.json' } | ForEach-Object {
    $newName = $_.Name.Replace('hopping_greenhouse','automatic_greenhouse').Replace('bonsai_greenhouse','greenhouse')
    if ($newName -ne $_.Name) { Rename-Item -LiteralPath $_.FullName -NewName $newName }
}
Get-ChildItem -LiteralPath $resources -Recurse -File | Where-Object { $_.Extension -in @('.json','.toml') } | ForEach-Object {
    $raw = [System.IO.File]::ReadAllText($_.FullName)
    $raw = $raw.Replace('hopping_greenhouse','automatic_greenhouse').Replace('bonsai_greenhouse','greenhouse')
    [System.IO.File]::WriteAllText($_.FullName,$raw,[System.Text.UTF8Encoding]::new($false))
}

$en = @'
{
  "block.greenhouse.greenhouse": "Greenhouse",
  "block.greenhouse.automatic_greenhouse": "Automatic Greenhouse",
  "itemGroup.greenhouse": "Greenhouse",
  "gui.greenhouse.soil": "Soil",
  "gui.greenhouse.plant": "Plant",
  "gui.greenhouse.fertilizer": "Bone meal",
  "gui.greenhouse.harvest": "Harvest",
  "gui.greenhouse.storage": "Products",
  "gui.greenhouse.bonus": "Boost",
  "gui.greenhouse.missing_plant": "Insert a plant or seed",
  "gui.greenhouse.invalid_soil": "Invalid substrate for this crop",
  "gui.greenhouse.ready": "Ready to harvest"
}
'@
$it = @'
{
  "block.greenhouse.greenhouse": "Greenhouse",
  "block.greenhouse.automatic_greenhouse": "Automatic Greenhouse",
  "itemGroup.greenhouse": "Greenhouse",
  "gui.greenhouse.soil": "Terreno",
  "gui.greenhouse.plant": "Pianta",
  "gui.greenhouse.fertilizer": "Ossa",
  "gui.greenhouse.harvest": "Raccogli",
  "gui.greenhouse.storage": "Prodotti",
  "gui.greenhouse.bonus": "Bonus",
  "gui.greenhouse.missing_plant": "Inserisci un seme o pianta",
  "gui.greenhouse.invalid_soil": "Terreno non valido",
  "gui.greenhouse.ready": "Pronto da raccogliere"
}
'@
$languageDir = Join-Path $resources 'assets\greenhouse\lang'
[System.IO.File]::WriteAllText((Join-Path $languageDir 'en_us.json'),$en,[System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $languageDir 'it_it.json'),$it,[System.Text.UTF8Encoding]::new($false))
Write-Host 'Greenhouse 0.2: Java overlay, complete resource ID migration, and custom GUI applied.'
