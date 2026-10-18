# 0.2.9-alpha crop and crafting acceptance

Minecraft Java 26.3, Java 25. The packaged JAR passes the isolated NeoForge
26.3.0.76 server suite. A full NeoForge 26.3.0.85 client separately checks the
three poplar colors with Jade 26.3.5, JEI 31.13.0.71 and MezzConfig 0.6.8.

## Reproduced defects

The exact delivered 0.2.8 JAR was tested before changing production code. Of
128 new checks, 122 passed and six failed:

- Orange and yellow poplar states were reset to red in normal and automatic
  machines (four failures).
- Forty-eight seeded natural selections only produced red poplar (one failure).
- A separate QA mod's `greenhouse_qa:oak_sapling` was mistaken for the vanilla
  item, because crop recognition compared only the identifier path (one failure).

All six failures pass in 0.2.9. The base crafting recipes needed no changes.

Minecraft 26.3's `TreeGrower.POPLAR` assigns equal weights to red, orange and
yellow from the same sapling. Greenhouse now chooses uniformly once when a
valid poplar is established. It retains that color across ticks, harvests and
reloads using the existing `crop` field. Existing red trees stay red, and already
serialized orange/yellow states are retained. Removing the valid plant/soil
combination resets selection in the usual way. Crop and substrate recognition
now requires the `minecraft` namespace; no new third-party crop support is implied.

The GUI, menus, client and registry bytecode (eight classes) is identical to
0.2.8. All models, textures, recipes, growth durations, fertilizer rules and
drop rolls are unchanged. The block entity selection and crop recognition code
change intentionally. Serialized field names and inventory layout are retained.

## Server matrix

The default `integrationTest` now runs 151 named checks: the previous 23 plus
122 crop checks and six crafting checks. All pass locally. This includes:

- All 30 crop/color enum entries: registry resolution, allowed substrate aliases
  and invalid combinations, exact unboosted maturity boundaries, four half-time
  cycles per bone meal, fifth-cycle fallback, one-time manual harvest, automatic
  export into a real chest and retention of plant/substrate inputs.
- Ten thousand seeded harvest rolls for each entry (300,000 total), checking
  main yield bounds, allowed bonuses and reachability of each possible yield and
  bonus. These are deterministic range/availability checks, not a statistical
  certification of probabilities. Production probability code is unchanged.
- Natural poplar selection across 48 seeds, color/progress retention through
  value reload and harvest, and rejection of QA items with colliding paths.
- Both recipes through the loaded Minecraft recipe manager, every item in the
  loaded wooden-slab tag and mixed slab species, missing/wrong ingredients at
  every grid position, then two actual result takes in server crafting menus.
  Each take consumes one of every ingredient and gives one result; a third take
  with no ingredients gives nothing. The crafting players are server fake
  players; this is not a separate network or GUI recipe-book test.

A separate migration run saves both checkpoint fixtures with 0.2.8, stops that
server, and reloads them in 0.2.9: in-progress growth/fertilizer and blocked mature
output both pass. JSON evidence is under `docs/evidence/`.

Most crop ticks are driven directly against block entities in a real loaded
server with world ticking frozen, allowing exact boundary assertions. The
earlier 0.2.8 natural-tick 256-machine run is documented separately and is not
claimed as repeated for 0.2.9.

## Client scope

The isolated full client displays red, orange and yellow poplars in both normal
and automatic greenhouses, with transparent glass, flat roof, internal pot and
distinct automatic base. Native F2 screenshots record the overview, targeted
variants and GUI. The visual fixture starts from controlled serialized states;
the independent server test verifies that all colors can also be chosen naturally.
An actual click on the orange poplar's harvest button delivers two poplar logs,
restarts growth and disables the button. The Italian GUI and JEI sidebar remain
visible and functional.
The 26.3.0.85 runtime uses the installed client's unchanged code/assets with only
the development-loader manifest adaptation described in the 0.2.8 report.

Shaders, alternate resource packs, public-server Internet latency, and a new
two-client network run are not tested for this version. The previous 0.2.8
network and rendering evidence remains historical evidence, not new 0.2.9 results.

## Reproduce

```powershell
./gradlew.bat build integrationTest
python tests/run_server_tests.py --crops-only --report-name crops-only
python tests/verify_jar.py build/libs/greenhouse-0.2.9-alpha.jar C:/path/to/greenhouse-0.2.8-alpha.jar
```

For visual fixtures, copy `tests/fixtures/poplar-visual/` into a disposable
creative world's `datapacks/` directory. It replaces the platform at coordinates
0..4, Y=120..121 and clears the test player's inventory. Never install this test
pack in a normal world. Slots 1/2/3 target red/orange/yellow, slot 4 shows all
six machines, and slot 5 targets the orange automatic greenhouse.
