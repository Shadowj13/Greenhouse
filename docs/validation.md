# Rendering investigation and validation

## Evidence recovered

- Repository baseline: `f7c6823648698c1d8b06c0aeb26b023bfecb6092` (0.2.3).
- GitHub Actions runs 1â€“18 recovered: 15 succeeded; 1, 2 and 17 failed.
  Run 18 succeeded after a PowerShell exit-status correction. These builds do
  not establish that a graphical test was performed.
- Screenshots: `2026-10-17_15.42.48.png`, `16.02.00.png`, `16.03.02.png`,
  `16.03.04.png`, recovered from the referenced conversation.
- Actual 0.2.6 JAR recovered read-only from the user's Prism instance. It contains
  the flat roof and reduced held-item transforms absent from GitHub. Its assets
  are the baseline for this change, rather than reconstructing those revisions.
- Instance versions: Minecraft 26.3, NeoForge 26.3.0.85; Jade 26.3.5, JEI
  31.13.0.71, Mezz Config 0.6.8. No shader mod is present in that mods directory.

## Separate causes

| Layer | Finding | Evidence / limit |
| --- | --- | --- |
| Minecraft selection | Confirmed mismatch: old box `(0.45,0,0.45)â€“(15.55,15.55,15.55)` versus lid top `14.85` and inset posts `1â€“1.9` / `14.1â€“15`. | `javap` inspection of the actual 0.2.6 class; JSON geometry; suspended rectangle in the targeted screenshot. |
| Mesh | Old glass had opposing faces separated by 0.01 model units; no same-facing duplicate panel found. A hidden automatic underside face shared Y=0 with the base. | Geometry audit. Separation alone is not proof of visible z-fighting. Changed to true planes; hidden duplicate removed. |
| Alpha / texture | Light blue, partially transparent pixels; no black border. 26.3 material alpha chooses translucency per texture. | PNG channel inspection; official NeoForge model specification. Texture simplification cannot fix selection geometry. |
| Other mods | Jade provides the top information panel. Presence of Jade/JEI does not prove responsibility for wire edges. | Installed mod names and screenshot appearance. Any modpack compatibility result must be stated separately from clean-client results. |

## Automated regression checks

`tests/verify_models.py` checks all resource references, one-block bounds, flat
roof and crop clearance, retained pot and differentiated automatic base, four
single double-sided glass planes, translucency and absence of black border.
It also proves continuous opaque coverage along all twelve selection edges for
both models by merging exact geometric intervals. This catches the original
floating-outline bug without depending on screenshots or subjective judgment.

The selection still works as a solid target cuboid; it is not disabled or made
empty. Collision is independent and includes the base's decorative extent.

## Runtime verification

- Local Gradle build: **PASS**, including `verifyModels` and `verifyJar`.
- GitHub Actions build and PR checks for `d418fd0b1945ac92921c0dec63ca1d0fa67bbc59`:
  **PASS** ([build](https://github.com/Shadowj13/Greenhouse/actions/runs/39042797774),
  [PR check](https://github.com/Shadowj13/Greenhouse/actions/runs/39042801042)).
- Packaged artifact: CRC, all resources, expanded mod metadata and all twelve
  Java 25 class files verified. The eleven classes other than `GreenhouseBlock`
  are byte-for-byte identical to the recovered 0.2.6 JAR, including GUI, menus,
  crops, block entity and registry setup.
- Secondary compilation against the installed NeoForge **26.3.0.85**: **PASS**.
- Negative regression check: restoring the old selection cuboid in a temporary
  fixture causes the opaque-edge coverage test to **fail as expected**.
- Live, isolated Minecraft 26.3 / NeoForge **26.3.0.76** development server,
  loading the actual downloaded CI JAR: **PASS**. After 466 controlled ticks:
  1. The manual oak greenhouse reaches stage 3 and maturity (450-tick boosted cycle).
  2. Dirt and sapling remain in their input slots.
  3. Exactly one bone meal is consumed (64 to 63), with three boosted cycles left.
  4. The automatic greenhouse sends oak logs to the chest below it.
  5. Sand rejects an oak sapling; growth stays at zero and bone meal is not consumed.
  The server saves and shuts down normally. Save/reload persistence was not tested.

Client visual tests, targeting from different camera angles, GUI interaction,
Jade/JEI compatibility and new in-game screenshots: **NOT COMPLETED**. Windows
Computer Use was explicitly interrupted by the physical Escape key. No further
desktop input was issued after that interruption. These are not reported as
passed. The static geometric correction and server tests do not certify the
final appearance under shaders, resource packs or another mod's highlight renderer.

An initial direct server bootstrap using Prism's client-only patched JAR was
unsuitable for a joined development launch (missing `Minecraft-Dists` metadata).
The successful server test uses the correctly generated NeoGradle joined runtime.
An initial CI invocation of `clean build` deleted NeoGradle's configured temporary
patch bundle. CI now builds the fresh checkout without that destructive task order.

## Delivered artifact

`greenhouse-0.2.7-alpha.jar`, downloaded from successful GitHub Actions run
39042797774, artifact 12104991507. SHA-256:

```text
144107a709486e50f41db5f0d39f6c9f098c26af34db30ec320330c8a0e95175
```

The artifact ZIP digest and the JAR's accompanying `.sha256` file were both
verified before delivery. ZIP timestamps and checkout line endings can make a
separate local build have a different archive hash; packaged source resources
and preserved class bytecode were verified independently.

## Sources

- [Original repository and history](https://github.com/Shadowj13/Greenhouse)
- [Last historical successful build](https://github.com/Shadowj13/Greenhouse/actions/runs/39026004538)
- [NeoForge model specification](https://docs.neoforged.net/docs/resources/client/models/)
- [NeoForge development and test setup](https://docs.neoforged.net/docs/gettingstarted/)
