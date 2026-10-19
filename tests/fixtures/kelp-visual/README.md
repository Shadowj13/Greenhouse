# Kelp visual fixture

Isolated client test datapack for Minecraft 26.3, pack format 121. Install in a
new disposable creative world with cheats and the packaged Greenhouse beta.2.
It builds four normal and four automatic greenhouses, each initialized with
water and kelp at stages 0–3; normal game ticks continue after initialization.
Do not install in a player world: setup replaces the test platform and player
inventory. The native client acceptance uses a copy of an existing QA world.

Hotbar: stick = overview, feather = mature normal greenhouse, blaze rod = upper
angle showing water, bone = automatic greenhouse, paper = first normal stage.
Right-click opens the real menu; mature normal harvest uses the real button.
Capture evidence using Minecraft F2. The fixture does not implement growth,
harvest or rendering and is never included in the installable JAR.

The exact cycle timing, yield, fertilizer, export and persistence checks live
in the separate packaged-JAR server suite rather than this visual fixture.
