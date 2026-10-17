# Rendering investigation and validation

## Evidence recovered

- Repository baseline: `f7c6823648698c1d8b06c0aeb26b023bfecb6092` (0.2.3).
- GitHub Actions runs 1–18 recovered: 15 succeeded; 1, 2 and 17 failed.
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
| Minecraft selection | Confirmed mismatch: old box `(0.45,0,0.45)–(15.55,15.55,15.55)` versus lid top `14.85` and inset posts `1–1.9` / `14.1–15`. | `javap` inspection of the actual 0.2.6 class; JSON geometry; suspended rectangle in the targeted screenshot. |
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

Results are recorded after running the final build. Automated geometry checks
are not a substitute for client rendering, GUI interaction or production tests.

## Sources

- [Original repository and history](https://github.com/Shadowj13/Greenhouse)
- [Last historical successful build](https://github.com/Shadowj13/Greenhouse/actions/runs/39026004538)
- [NeoForge model specification](https://docs.neoforged.net/docs/resources/client/models/)
- [NeoForge development and test setup](https://docs.neoforged.net/docs/gettingstarted/)
