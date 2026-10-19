# Greenhouse

Minecraft Java **26.3**, NeoForge **26.3.0.76 or later**, Java **25**.

## Download

[Download Greenhouse 0.3.0-beta.2](https://github.com/Shadowj13/Greenhouse/releases/tag/v0.3.0-beta.2)
(public beta; no GitHub account required).
Choose the [installable JAR](https://github.com/Shadowj13/Greenhouse/releases/download/v0.3.0-beta.2/greenhouse-0.3.0-beta.2.jar)
or the [ZIP with Italian guides and checksums](https://github.com/Shadowj13/Greenhouse/releases/download/v0.3.0-beta.2/Greenhouse-0.3.0-beta.2-Minecraft-26.3-NeoForge.zip).
Copy the JAR into your Minecraft 26.3 NeoForge instance's `mods` folder.

Version **0.3.0-beta.2** adds kelp to both greenhouses: one raw kelp every
30 seconds, using a reusable water bucket in the existing substrate slot.
Four animated growth stages and an aquatic pot preserve the detailed frame,
transparent panels, flat roof, automatic base and GUI layout. Other crops,
recipes, yields and timing retain the beta.1 implementation.

Start with [installation and updates](docs/INSTALLAZIONE.md) and
[recipes, crops and automation](docs/USO.md). Keep one Greenhouse JAR per instance;
install the same version on clients and dedicated servers.
See [kelp verification](docs/kelp-0.3.0-beta.2.md) for this update and the
[previous beta verification report](docs/beta-0.3.0.md) for the beta.1 two-client,
natural-tick load and migration scope, with explicit untested configurations.

## Build and verify

The canonical, editable project is now at the repository root. The original ZIP
and `patch/` are historical inputs, no longer applied by CI.

```powershell
./gradlew.bat build
./gradlew.bat integrationTest
./gradlew.bat releaseBundle
./gradlew.bat runClient
```

Python 3 is required for the asset regression checks in `check` (no packages are
required). Override its location with `-PpythonExecutable=C:/path/to/python.exe`.
The installable file is `build/libs/greenhouse-0.3.0-beta.2.jar`.
Use one Greenhouse JAR at a time in the instance's `mods` folder.

`releaseBundle` builds and runs all server checks, then creates a reproducible
install ZIP in `build/distributions/` with the JAR, SHA256, Italian guides,
changelog and release manifest. Packaging rejects a stale JAR/test receipt or
any failed test. CI uploads both the standalone JAR and the bundle.

`integrationTest` launches the packaged JAR and a separate QA mod on an isolated,
headless NeoForge server. It checks growth, fertilizer, full inventories, sided
insertion, double chests, drops, server-side menu listeners, chunk unloading,
disk reload, all 31 crop/color entries and both crafting recipes (156 checks).
Its world stays under `build/integration`; results and server logs
are written to `build/qa-results`. The QA mod is never part of the installable JAR.
The harness accepts the Minecraft EULA for this local test server.

See [0.2.9 crop acceptance](docs/crop-acceptance-0.2.9.md) for the new fixes,
negative baseline, recipe and crop matrix, and native client screenshots.
See [0.2.8 consolidation](docs/consolidation-0.2.8.md) for earlier defects and
server regression results, and [client acceptance](docs/client-acceptance-0.2.8.md)
for the subsequent graphics, GUI, Jade/JEI and two-client loopback checks.

When cleaning, run `clean` in a separate invocation before `build`: NeoGradle
7.1.39 expands its patch archive during configuration into `build/tmp`, so
`clean build` in one invocation deletes that input before `neoFormSetup`.

## Rendering correction

The 0.2.6 JAR retained a selection box reaching Y=15.55 although its flat lid
ended at Y=14.85 (model coordinates in sixteenths). Its outer footprint also
followed the broad base rather than the inset posts. Minecraft therefore drew
wire edges over the sky and through transparent space. The chest's outline
follows its opaque shell and has no comparable floating cage.

The new single selection cuboid follows the four posts and the completed opaque
roof rim. Collision remains a separate cuboid covering the decorative base.
The four glass panels use one geometric plane with opposite face normals each,
without thin side faces, near-coplanar depth separation, ambient occlusion or
directional darkening. Minecraft 26.3 selects translucent rendering from the PNG
alpha; no obsolete global cutout render setting is installed.

The original glass texture has translucent, light blue pixels and no black
border. The old underside plug also shared a coplanar down face with the base;
that fully hidden face is removed. This is a separate geometry issue and does
not explain the large rectangle above the roof.

See [0.2.7 rendering investigation](docs/validation.md) for the original evidence,
build history and the exact visual test limits.
