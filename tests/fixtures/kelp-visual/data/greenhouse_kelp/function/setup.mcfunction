forceload add -16 -16 16 16
gamemode creative @s
fill -8 119 -8 15 119 12 minecraft:smooth_stone
fill -8 120 -8 15 126 12 minecraft:air
clear @s
setblock 0 120 0 greenhouse:greenhouse[crop=kelp,stage=0]
data merge block 0 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 2 120 0 greenhouse:greenhouse[crop=kelp,stage=1]
data merge block 2 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 150, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 4 120 0 greenhouse:greenhouse[crop=kelp,stage=2]
data merge block 4 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 300, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 6 120 0 greenhouse:greenhouse[crop=kelp,stage=3]
data merge block 6 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:kelp", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 1b, "newCycle": 0b}
setblock 0 120 -3 minecraft:chest
setblock 0 121 -3 greenhouse:automatic_greenhouse[crop=kelp,stage=0]
data merge block 0 121 -3 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 2 120 -3 minecraft:chest
setblock 2 121 -3 greenhouse:automatic_greenhouse[crop=kelp,stage=1]
data merge block 2 121 -3 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 150, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 4 120 -3 minecraft:chest
setblock 4 121 -3 greenhouse:automatic_greenhouse[crop=kelp,stage=2]
data merge block 4 121 -3 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": []}, "elapsed": 300, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 0b, "newCycle": 0b}
setblock 6 120 -3 minecraft:chest
setblock 6 121 -3 greenhouse:automatic_greenhouse[crop=kelp,stage=3]
data merge block 6 121 -3 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:kelp", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:kelp", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "kelp", "mature": 1b, "newCycle": 0b}
item replace entity @s hotbar.0 with minecraft:stick
item replace entity @s hotbar.1 with minecraft:feather
item replace entity @s hotbar.2 with minecraft:blaze_rod
item replace entity @s hotbar.3 with minecraft:bone
item replace entity @s hotbar.4 with minecraft:paper
tag @s add kelp_beta2_init
time set noon
weather clear
tp @s 3.5 120 7.0 180 22
tellraw @s {"text":"GREENHOUSE BETA.2 KELP FIXTURE READY","color":"green"}
