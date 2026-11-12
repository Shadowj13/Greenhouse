forceload add -16 -16 32 32
gamemode creative @s
fill -6 119 -6 25 119 24 minecraft:smooth_stone
fill -6 120 -6 25 127 24 minecraft:air
clear @s
setblock 0 120 0 greenhouse:greenhouse[crop=vine,stage=0]
data merge block 0 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "vine", "mature": 0b, "newCycle": 0b}
setblock 2 120 0 greenhouse:greenhouse[crop=vine,stage=1]
data merge block 2 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "vine", "mature": 0b, "newCycle": 0b}
setblock 4 120 0 greenhouse:greenhouse[crop=vine,stage=2]
data merge block 4 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "vine", "mature": 0b, "newCycle": 0b}
setblock 6 120 0 greenhouse:greenhouse[crop=vine,stage=3]
data merge block 6 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:vine", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "vine", "mature": 1b, "newCycle": 0b}
setblock 12 120 0 greenhouse:greenhouse[crop=vine,stage=3]
data merge block 12 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:vine", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "vine", "mature": 1b, "newCycle": 0b}
setblock 16 120 0 minecraft:chest
setblock 16 121 0 greenhouse:automatic_greenhouse[crop=vine,stage=0]
data merge block 16 121 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:vine", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "vine", "mature": 0b, "newCycle": 0b}
setblock 0 120 4 greenhouse:greenhouse[crop=glow_lichen,stage=0]
data merge block 0 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "glow_lichen", "mature": 0b, "newCycle": 0b}
setblock 2 120 4 greenhouse:greenhouse[crop=glow_lichen,stage=1]
data merge block 2 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "glow_lichen", "mature": 0b, "newCycle": 0b}
setblock 4 120 4 greenhouse:greenhouse[crop=glow_lichen,stage=2]
data merge block 4 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "glow_lichen", "mature": 0b, "newCycle": 0b}
setblock 6 120 4 greenhouse:greenhouse[crop=glow_lichen,stage=3]
data merge block 6 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:glow_lichen", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "glow_lichen", "mature": 1b, "newCycle": 0b}
setblock 12 120 4 greenhouse:greenhouse[crop=glow_lichen,stage=3]
data merge block 12 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:glow_lichen", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "glow_lichen", "mature": 1b, "newCycle": 0b}
setblock 16 120 4 minecraft:chest
setblock 16 121 4 greenhouse:automatic_greenhouse[crop=glow_lichen,stage=0]
data merge block 16 121 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:glow_lichen", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "glow_lichen", "mature": 0b, "newCycle": 0b}
setblock 0 120 8 greenhouse:greenhouse[crop=twisting_vines,stage=0]
data merge block 0 120 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "twisting_vines", "mature": 0b, "newCycle": 0b}
setblock 2 120 8 greenhouse:greenhouse[crop=twisting_vines,stage=1]
data merge block 2 120 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "twisting_vines", "mature": 0b, "newCycle": 0b}
setblock 4 120 8 greenhouse:greenhouse[crop=twisting_vines,stage=2]
data merge block 4 120 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "twisting_vines", "mature": 0b, "newCycle": 0b}
setblock 6 120 8 greenhouse:greenhouse[crop=twisting_vines,stage=3]
data merge block 6 120 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:twisting_vines", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "twisting_vines", "mature": 1b, "newCycle": 0b}
setblock 12 120 8 greenhouse:greenhouse[crop=twisting_vines,stage=3]
data merge block 12 120 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:twisting_vines", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "twisting_vines", "mature": 1b, "newCycle": 0b}
setblock 16 120 8 minecraft:chest
setblock 16 121 8 greenhouse:automatic_greenhouse[crop=twisting_vines,stage=0]
data merge block 16 121 8 {"Items": [{"Slot": 0, "id": "minecraft:warped_nylium", "count": 1}, {"Slot": 1, "id": "minecraft:twisting_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "twisting_vines", "mature": 0b, "newCycle": 0b}
setblock 0 120 12 greenhouse:greenhouse[crop=weeping_vines,stage=0]
data merge block 0 120 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "weeping_vines", "mature": 0b, "newCycle": 0b}
setblock 2 120 12 greenhouse:greenhouse[crop=weeping_vines,stage=1]
data merge block 2 120 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "weeping_vines", "mature": 0b, "newCycle": 0b}
setblock 4 120 12 greenhouse:greenhouse[crop=weeping_vines,stage=2]
data merge block 4 120 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "weeping_vines", "mature": 0b, "newCycle": 0b}
setblock 6 120 12 greenhouse:greenhouse[crop=weeping_vines,stage=3]
data merge block 6 120 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:weeping_vines", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "weeping_vines", "mature": 1b, "newCycle": 0b}
setblock 12 120 12 greenhouse:greenhouse[crop=weeping_vines,stage=3]
data merge block 12 120 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:weeping_vines", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "weeping_vines", "mature": 1b, "newCycle": 0b}
setblock 16 120 12 minecraft:chest
setblock 16 121 12 greenhouse:automatic_greenhouse[crop=weeping_vines,stage=0]
data merge block 16 121 12 {"Items": [{"Slot": 0, "id": "minecraft:nether_wart_block", "count": 1}, {"Slot": 1, "id": "minecraft:weeping_vines", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "weeping_vines", "mature": 0b, "newCycle": 0b}
setblock 0 120 16 greenhouse:greenhouse[crop=big_dripleaf,stage=0]
data merge block 0 120 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 0b, "newCycle": 0b}
setblock 2 120 16 greenhouse:greenhouse[crop=big_dripleaf,stage=1]
data merge block 2 120 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 0b, "newCycle": 0b}
setblock 4 120 16 greenhouse:greenhouse[crop=big_dripleaf,stage=2]
data merge block 4 120 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 0b, "newCycle": 0b}
setblock 6 120 16 greenhouse:greenhouse[crop=big_dripleaf,stage=3]
data merge block 6 120 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:big_dripleaf", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 1b, "newCycle": 0b}
setblock 12 120 16 greenhouse:greenhouse[crop=big_dripleaf,stage=3]
data merge block 12 120 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:big_dripleaf", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 1b, "newCycle": 0b}
setblock 16 120 16 minecraft:chest
setblock 16 121 16 greenhouse:automatic_greenhouse[crop=big_dripleaf,stage=0]
data merge block 16 121 16 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:big_dripleaf", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "big_dripleaf", "mature": 0b, "newCycle": 0b}
setblock 21 120 0 greenhouse:greenhouse[crop=blue_orchid,stage=3]
data merge block 21 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:blue_orchid", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:blue_orchid", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "blue_orchid", "mature": 1b, "newCycle": 0b}
setblock 21 120 4 greenhouse:greenhouse[crop=kelp,stage=3]
data merge block 21 120 4 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:kelp", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 1b, "newCycle": 0b}
setblock 21 120 8 greenhouse:greenhouse[crop=empty,stage=0]
item replace entity @s hotbar.0 with minecraft:stick
item replace entity @s hotbar.1 with minecraft:feather
item replace entity @s hotbar.2 with minecraft:blaze_rod
item replace entity @s hotbar.3 with minecraft:bone
item replace entity @s hotbar.4 with minecraft:paper
item replace entity @s hotbar.5 with minecraft:flint
item replace entity @s hotbar.6 with minecraft:coal
item replace entity @s hotbar.7 with minecraft:brick
item replace entity @s hotbar.8 with minecraft:quartz
tag @s add crops_beta5_init
time set noon
weather clear
tp @s 3.5 123 22.5 180 35
tellraw @s {"text":"GREENHOUSE BETA.5 CROP FIXTURE READY","color":"green"}
