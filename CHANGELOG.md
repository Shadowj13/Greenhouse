# Changelog

## 0.3.0-beta.7

- Keep a ready harvest when the plant or substrate is removed or replaced.
  The harvest stays with the crop that produced it until it is collected or
  exported; the new crop starts afterwards. Previously the harvest was lost.
- Give back the bone meal charge of an accelerated cycle interrupted by a crop
  change, so one bone meal always yields four accelerated harvests.
- Automation (hoppers) fills an empty soil or plant slot with one item and no
  longer stacks extra copies, matching the menu limit.
- GUI: show "Insert soil" when the soil slot is empty and "Output full" when an
  automatic greenhouse is waiting for space; the title is now translated.
- Italian names: "Serra" and "Serra automatica".
- Tree bonuses use an explicit crop list instead of the enum position; remove an
  unused leaf mapping for fungi.
- Redstone: a powered greenhouse pauses growth (no new cycle, no bone meal
  spent); a ready harvest can still be collected or exported. The GUI shows
  "Paused".
- Comparator: the normal greenhouse outputs its growth progress (1-14) and 15
  when ready; the automatic greenhouse outputs the fill level of its nine
  output slots, like a chest.
- Optional JEI integration (JEI 31.13+ for 26.3): a "Greenhouse" category lists
  every crop with accepted soils, yield, bonus chances and cycle time; both
  greenhouses are catalysts and clicking the growth bar opens the catalog.
  Soils, yields and bonuses come from one table shared with the harvest code.
- Pipes and storage from other mods: both greenhouses expose NeoForge's item
  handler with the same face rules as hoppers (inputs from above, bone meal
  from the sides, products from below; soil and plant one item each). The
  automatic greenhouse also exports into non-vanilla storage below it.
- Axes break greenhouses faster (minecraft:mineable/axe).
- Both recipes unlock in the recipe book (glass pane or flower pot for the
  greenhouse; greenhouse or hopper for the automatic one).
- Breaking a greenhouse keeps its soil, plant, bone meal, products, ready
  harvest and growth progress inside the dropped item, like a shulker box
  (survival, creative, explosions and block breakers). Placing it restores
  everything, including unused bone meal charges. Greenhouses with no items
  or growth state drop plain, stackable items.
- Crafting an automatic greenhouse from a filled greenhouse keeps its contents
  (new recipe type greenhouse:keep_contents_shaped, same JSON as shaped).
- GUI: the growth line shows the remaining time in seconds next to the
  percentage while a crop is growing.
- Optional Jade integration (Jade 26.3.5+): looking at a greenhouse shows the
  crop, its state (growing with percentage and remaining time, ready, output
  full, paused) and the bone meal boost left. Without Jade on the server the
  tooltip still shows the crop.
- Normal greenhouse: keeps growing until it holds four ready harvests instead
  of stopping after each one. "Harvest n/4" hands them all over at once; the
  GUI and Jade show the count and "Greenhouse full" at 4/4. Stored harvests
  are not shown as slots and cannot be extracted by hoppers or pipes; the
  automatic greenhouse keeps its nine exportable slots. The comparator reads
  15 when the normal greenhouse is full.
- Add seventeen server checks (362 in total): ready harvest after input changes
  (manual and automatic), bone meal refund, single-item automation inputs,
  catalog consistency, comparator output (both machines), redstone pause,
  item handler face rules, the axe tag/recipe advancements, packed drops
  (survival, creative, destroy with drops, empty item), the upgrade recipe
  the remaining-time calculation shared by the GUI and Jade, and the normal
  greenhouse's four-harvest buffer. Tests that expected the normal greenhouse
  to stop at every harvest now check for an available harvest instead.

## 0.3.0-beta.6

- Grow weeping vines on crimson nylium and show it in the pot.
- Replace moss cultivation with shelf mushrooms on mycelium or podzol.
  An original shelf mushroom yields one or two mushrooms every 900 ticks.
- Preserve stored inputs and ready harvests when updating older greenhouses.
  Recover retired moss and replace the old weeping-vine substrate to restart growth.
- Cover the crop changes, saves and legacy harvests with 345 server checks.

## 0.3.0-beta.5

- Add normal vines, glow lichen, twisting vines, weeping vines and big dripleaf
  to both machines, reaching 56 crop/color entries. Seagrass and wither roses
  remain excluded. Each new crop yields one item every 600 ticks and retains
  the original plant/substrate and four-cycle bone-meal boost.
- Use reusable dirt for vines/dripleaf, stone for lichen, warped nylium for
  twisting vines and nether wart blocks for weeping vines.
- Add 20 growth models, six substrate shell variants, internal vine supports,
  a green tint for native vine textures and visual light emission for lichen.
  Preserve the existing enclosure, GUI, transfer logic and earlier crop rules.
- Extend packaged-JAR verification to 340 checks and add beta.4 disk migration
  plus a reproducible five-crop visual fixture.

## 0.3.0-beta.4

- Add sea pickles with reusable water, moss on stone or the existing dirt
  substrates, and chorus flowers on end stone. Both greenhouse variants now
  support 51 crop/color entries.
- Sea pickles and moss yield one matching item every 600 ticks; chorus yields
  one or two raw fruits every 900 ticks. Preserve the original plant/substrate
  and four accelerated bone-meal cycles; chorus cannot generate free flowers.
- Add twelve detailed growth models and two End pot variants. Reuse the kelp
  aquatic shell for sea pickles, with exactly one selected base per state.
- Extend packaged-JAR verification to 300 checks and add beta.3 disk migration
  and a reproducible three-crop visual fixture. Make model generation emit LF
  deterministically across checkouts.

## 0.3.0-beta.3

- Add priorities 1-3: glow berries, twelve common small flowers and four tall
  flowers, in both manual and automatic greenhouses (48 crop/color entries).
- Flowers yield one matching flower every 600 ticks (30 seconds); glow berries
  yield one or two berries every 900 ticks (45 seconds). Both retain dirt and
  plant, accept existing dirt variants, and use the four-cycle bone-meal bonus.
- Add 68 growth models using vanilla textures, a miniature internal berry
  support, and separate sunflower front/back sprites. Keep the approved shell,
  glass, roof, pot, selection/collision shape, GUI and all earlier crop rules.
- Extend packaged-JAR checks to 276, including all new crops, exact timing,
  manual/automatic harvest, menus, NBT and disk restart. Add a reproducible
  isolated visual fixture and a historical-QA migration option.

## 0.3.0-beta.2

- Add kelp to both greenhouses: one raw kelp every 30 seconds, with a reusable
  water bucket in the existing substrate slot and the usual bone-meal bonus.
- Show four growth stages with animated vanilla kelp textures and water in the
  pot. The aquatic base replaces the soil surface without overlapping faces;
  the frame, glass, pot, roof, automatic base and GUI layout are preserved.
- Extend the packaged-JAR crop suite to all 31 crop/color entries, including
  aquatic input rejection, menu slots, harvest/export, fertilizer and reload.

## 0.3.0-beta.1

- Promote the verified 0.2.9 implementation to the first installable beta;
  production classes, models, textures and recipes remain unchanged.
- Add Italian installation, update, dedicated-server and crop/automation guides.
- Build a deterministic install ZIP only after the exact packaged JAR passes
  all 151 live server checks; include checksums and a release manifest.
- Extend optional full-client/network and natural-tick QA to poplar.

## 0.2.9-alpha

- Make red, orange and yellow poplar variants reachable and preserve their
  selected color through growth, harvests and reloads.
- Reject foreign-namespace crop/substrate lookalikes.
- Verify all 30 crop/color entries, 300,000 seeded drop rolls and both loaded
  crafting recipes, including real server-menu ingredient consumption.

## 0.2.8-alpha

- Save growing progress between visual stage transitions.
- Avoid dirtying unchanged empty automatic greenhouses.
- Preserve items when the target has a smaller stack limit, respect upper-face
  insertion rules and use both halves of a double chest.
- Drop the pending mature harvest and stored items once on removal.
- Add isolated live server tests, restart and chunk unload checks, old-world
  migration checks and reproducible JAR packaging.
- Preserve the 0.2.7 model, textures, GUI and crop rules.

## 0.2.7-alpha

- Align the selection cuboid with the opaque posts and flat roof rim.
- Use four transparent double-sided glass planes and remove one hidden
  coplanar underside face, keeping the detailed one-block model and pot.
- Recover the actual 0.2.6 assets, retain its GUI and crop behavior, and promote
  root project sources for reproducible builds instead of legacy ZIP patching.

Validation details and explicit client-test limits are in `docs/`.
