# 0.2.8-alpha consolidation

Minecraft Java 26.3, NeoForge 26.3.0.76, Java 25. Tests use the packaged mod on a
real, isolated headless development server, with a separate QA mod and fixture
world. No launcher profile, player account or user world is modified.

## Reproduced defects and corrections

The delivered 0.2.7 CI artifact was used as a negative baseline. Fifteen core
checks produced ten passes and five failures. The following corrections make
all five failing cases pass:

| Defect reproduced in 0.2.7 | Correction in 0.2.8 |
| --- | --- |
| Progress between visible stage transitions did not mark the chunk dirty. | Mark each growing tick dirty through `Level.blockEntityChanged`, without comparator notifications on every tick. |
| Empty automatic greenhouses marked their unchanged chunk dirty every eight ticks when above a container. | Skip transfers with no output and mark inventories changed only after actual movement. |
| A destination with a smaller stack limit could truncate items after the source had already lost them. | Calculate the actual destination capacity before moving the same quantity from the source. |
| Automatic insertion ignored the destination's exposed face and accessible slots. | Respect `WorldlyContainer` slots and insertion rules for its upper face; resolve holder containers and both chest halves. |
| Non-player removal lost the mature harvest stored separately from the output slots. | Drain inputs, outputs and pending harvest through the block entity removal lifecycle, once. |

Crop yields, timing, fertilizer rules, slot layout, recipes and GUI are preserved.
The nine GUI/menu, crop, registry and client classes are byte-identical to the
0.2.7 baseline. All model and texture source assets remain unchanged from 0.2.7.
The block and block entity classes intentionally change for the lifecycle fixes.
The serialized 27-slot layout and field names are retained.

## Verification

`build` runs the resource/model and packaged-JAR checks. `integrationTest` runs
23 named checks:

- Seventeen core cases: growth and single harvest; four fertilizer cycles;
  invalid substrate/fertilizer; growth and idle dirty flags; blocked output and
  resume; target stack limit; target upper face; full target; furnace slots;
  both halves of a double chest; non-player removal; survival break; player
  inventory overflow; two server-side menu listeners; shift-click rules; and
  value serialization.
- Two save checkpoints: a crop at tick 137/450 with three remaining boosted
  cycles, and a mature automatic greenhouse with full output and pending logs.
- Two assertions that those fixture chunks really unload. Merely asking to
  unload is not considered a passing result.
- Two disk reload checks in a new server process: exact progress/fertilizer and
  retained pending harvest, followed by successful completion/resume.

All 23 checks pass locally. A separate migration run writes the two checkpoint
states with the actual 0.2.7 CI JAR, stops the server, then loads and validates
them with 0.2.8. Both states pass without conversion. Evidence is under
[`evidence/`](evidence/).

The performance sample executes 51,200 machine ticks (256 active automatic
greenhouses over 200 controlled iterations). The recorded local run averaged
0.370 ms per 256-machine iteration. This measures the machine tick code only:
it excludes full world ticks, networking and rendering, has no universal timing
threshold, and is not a server TPS or modpack performance guarantee.

Archives use sorted entries, fixed timestamps and normalized JSON/TOML line
endings. The installable JAR is checked for CRC integrity, expanded metadata,
exactly twelve Java 25 classes, current resources and absence of QA classes.
CI executes the same build and live server suite and uploads its JSON and logs
separately from the installable artifact.

## Reproduction

```powershell
./gradlew.bat build integrationTest
python tests/verify_jar.py build/libs/greenhouse-0.2.8-alpha.jar C:/path/to/greenhouse-0.2.7-alpha.jar
```

For the two-version migration, prepare the QA runtime with
`./gradlew.bat prepareQaRuntime`, then run the following in sequence (Java 25
must be on PATH, or pass `--java C:/path/to/java.exe`):

```powershell
python tests/run_server_tests.py --jar C:/path/to/greenhouse-0.2.7-alpha.jar --checkpoint-only --report-name migration-from-027
python tests/run_server_tests.py --reload-only --report-name migration-to-028
```

## Limits

The two-viewer check uses fake players, real server menus and their data
listeners. It verifies server-side synchronization and prevents double harvest;
it does not test packets, latency, separate client processes or GUI rendering.
Client visuals, new in-game screenshots and Jade/JEI compatibility are not
reported as passed. The earlier desktop test was interrupted with Escape and
has not been repeated. No claim is made for shaders, resource packs or arbitrary
third-party storage capabilities beyond the tested vanilla container contracts.
