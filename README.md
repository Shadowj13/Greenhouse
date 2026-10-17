# Greenhouse

Minecraft Java **26.3**, NeoForge **26.3.0.76 or later**, Java **25**.

Version **0.2.8-alpha** consolidates persistence, automatic transfers and removal
drops. It retains the 0.2.7 selection correction, detailed flat-roof greenhouse,
transparent side panels, internal pot, distinct automatic base, menu and crop
mechanics. Existing 0.2.7 world data loads without a conversion.

## Build and verify

The canonical, editable project is now at the repository root. The original ZIP
and `patch/` are historical inputs, no longer applied by CI.

```powershell
./gradlew.bat build
./gradlew.bat integrationTest
./gradlew.bat runClient
```

Python 3 is required for the asset regression checks in `check` (no packages are
required). Override its location with `-PpythonExecutable=C:/path/to/python.exe`.
The installable file is `build/libs/greenhouse-0.2.8-alpha.jar`.
Use one Greenhouse JAR at a time in the instance's `mods` folder.

`integrationTest` launches the packaged JAR and a separate QA mod on an isolated,
headless NeoForge server. It checks growth, fertilizer, full inventories, sided
insertion, double chests, drops, server-side menu listeners, chunk unloading and
disk reload. Its world stays under `build/integration`; results and server logs
are written to `build/qa-results`. The QA mod is never part of the installable JAR.
The harness accepts the Minecraft EULA for this local test server.

See [0.2.8 consolidation](docs/consolidation-0.2.8.md) for reproduced defects,
test results and the limits of server-only validation. Client rendering and real
multiplayer network sessions still require separate verification.

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
