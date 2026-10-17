# Client acceptance: Greenhouse 0.2.8-alpha

These checks supplement the server-only consolidation report. They use the
unchanged delivered JAR, SHA-256
`35f378718cf4daba1d0024a2cd1c25321d3008b42f4435b65f7c8c8e3e7d1efc`.
All worlds, accounts and configurations used here are isolated test fixtures.
The user's Prism instance is read only; its assets and installed mod files are
copied or referenced without changing its worlds or configuration.

## Graphics and GUI — 17 October 2026

Actual Minecraft windows were controlled and inspected on the local Intel UHD
620 / OpenGL 3.3 graphics device. Screenshots were saved by Minecraft's F2
function, without retouching.

| Configuration | Checks passed |
| --- | --- |
| Minecraft 26.3, NeoForge 26.3.0.76, Greenhouse only, English | Manual greenhouse front and oblique targeting; transparent panels, pot and flat roof; vanilla chest reference; night lighting; real chest receiving automatic products; ready, growing and empty GUI states; real client harvest button. |
| Minecraft 26.3, NeoForge 26.3.0.85, Jade 26.3.5, JEI 31.13.0.71, Mezz Config 0.6.8, Italian | Automatic greenhouse targeting with Jade tooltip; normal greenhouse oblique view; both greenhouse GUIs with JEI visible; readable Italian labels. |

The floating rectangle above the flat lid is absent in the inspected targeted
views. Minecraft still draws its normal selection edges along the opaque frame.
The glass remains transparent and the internal pot and distinct automatic base
remain visible. Jade displays its information panel without reintroducing the
floating cage. This establishes the observed result for these configurations;
it is not a guarantee for every camera angle or third-party highlight renderer.

Clicking Harvest in the real client delivered exactly two oak logs, changed the
state from Ready to Growing, updated progress and remaining boost, and disabled
the button until the next mature crop. The empty greenhouse displayed 0%,
Insert plant and a disabled button. The automatic GUI displayed its nine output
slots while JEI remained visible beside it.

Sixteen native screenshots were delivered with the JAR. Their filenames and
SHA-256 checksums are recorded in `evidence/client-screenshots.json`.

The .85 compatibility launch used the exact installed Prism patched client code
and libraries. A workspace copy of that runtime received the `Minecraft-Dists:
client` manifest marker required by NeoForge's development launcher. Game class
and asset bytes were unchanged. The first attempt without this metadata failed
before game startup and is not counted as a mod compatibility failure.

## Additional network and load checks — 18 October 2026

The separate QA mod provides `networksetup`, `networkassert`, `soaksetup`,
`soakstats` and `soakassert` console phases. It is built only as
`build/qa/greenhouse-qa.jar`, never included in the installable Greenhouse JAR.

Network checks use a dedicated NeoForge .76 server bound to 127.0.0.1 and two
independent full client processes with distinct development identities. They
exercise actual game packets. This test server uses offline development
profiles and does not authenticate a real Minecraft account or test Internet
latency.

Both clients displayed the same mature greenhouse at 100%. Clicking Harvest
in GreenhouseNetA delivered two logs there. GreenhouseNetB received 0% and the
disabled button while its inventory remained empty. Four native screenshots
record the two menus before and after the real client packet. All four QA
checks (setup plus three final server assertions) passed. World ticks were
explicitly frozen during this short packet test so a new crop could not mature
while screenshots were being taken; players, menus and network packets still
ran. The normal growth cycle resumed after the assertions.

The load fixture force-loads 256 active automatic greenhouses and lets the real
world tick normally. Its final assertion requires at least 6,000 natural world
ticks and checks every machine's exact oak-log yield plus preserved inputs.
Unlike the earlier microbenchmark, it includes the full local server tick.
Timing depends on this machine and its simultaneous client workload.
The server tick-time field is Minecraft's trailing average at the measurement,
not an average over the entire acceptance run.

The final run passed: 6,401 world ticks, at least 6,365 active ticks per machine,
exact yield and preserved inputs on all 256 machines. Maximum initial chunk
activation delay was 36 ticks. The ending trailing tick time was 0.239 ms;
the server saved and stopped normally. Structured results are in
`evidence/client-acceptance.json`.

An initial assertion incorrectly assumed newly force-loaded chunks ticked
immediately. The first and last fixture machines showed 17 and 18 ticks of
initial activation delay. The corrected assertion derives completed cycles
from consumed fertilizer and remaining charges independently of output, checks
the exact log yield, requires at least 6,000 active machine ticks for every
machine and bounds initial activation delay to 40 world ticks. Product code
was not changed for this fixture correction.

To reproduce the visual scene, copy `tests/fixtures/client-visual` into an
isolated world's datapacks folder and reload. It clears the test inventory,
force-loads its platform and provides hotbar-selected camera views.
For real network checks, install the separate QA JAR only on the isolated
server, connect development clients named GreenhouseNetA and GreenhouseNetB,
then issue `tick freeze` and `greenhouseqa networksetup` from the console.
After the real client click and screenshots, `greenhouseqa networkassert`
checks the authoritative state. The load sequence is `greenhouseqa soaksetup`,
`tick unfreeze`, wait for at least 6,100 world ticks, `tick freeze`, and
`greenhouseqa soakassert`. `greenhouseqa soakstats` reports progress.

The portable launcher `python tests/run_acceptance_server.py --soak-only`
prepares a fresh fixture world, runs the natural-tick load test for 320 seconds,
records JSON and logs under `build/qa-results`, then saves and stops the server.
Run `gradlew prepareQaRuntime` first and use Java 25 (`--java` accepts its path).
Without `--soak-only`, it waits for full clients and accepts JSONL console
commands in the printed workspace control file. The bind address is always
127.0.0.1 and the port defaults to 25576.

## Remaining scope limits

Shaders, alternate resource packs, other inventory mods, Internet latency and
large public multiplayer servers are outside this run. This is a bounded
acceptance check; it does not certify a crash-free session of arbitrary length.
The previously documented 23 server integration checks and .7-to-.8 save
migration remain the basis for persistence, stack limits and removal behavior.
