forceload add -16 -16 32 32
gamemode creative @s
fill -6 119 -6 25 119 18 minecraft:smooth_stone
fill -6 120 -6 25 127 18 minecraft:air
clear @s
setblock 0 120 0 greenhouse:greenhouse[crop=sea_pickle,stage=0]
data merge block 0 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "sea_pickle", "mature": 0b, "newCycle": 0b}
setblock 2 120 0 greenhouse:greenhouse[crop=sea_pickle,stage=1]
data merge block 2 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "sea_pickle", "mature": 0b, "newCycle": 0b}
setblock 4 120 0 greenhouse:greenhouse[crop=sea_pickle,stage=2]
data merge block 4 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "sea_pickle", "mature": 0b, "newCycle": 0b}
setblock 6 120 0 greenhouse:greenhouse[crop=sea_pickle,stage=3]
data merge block 6 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:sea_pickle", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "sea_pickle", "mature": 1b, "newCycle": 0b}
setblock 12 120 0 greenhouse:greenhouse[crop=sea_pickle,stage=3]
data merge block 12 120 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:sea_pickle", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "sea_pickle", "mature": 1b, "newCycle": 0b}
setblock 16 120 0 minecraft:chest
setblock 16 121 0 greenhouse:automatic_greenhouse[crop=sea_pickle,stage=0]
data merge block 16 121 0 {"Items": [{"Slot": 0, "id": "minecraft:water_bucket", "count": 1}, {"Slot": 1, "id": "minecraft:sea_pickle", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "sea_pickle", "mature": 0b, "newCycle": 0b}
setblock 0 120 4 greenhouse:greenhouse[crop=moss,stage=0]
data merge block 0 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "moss", "mature": 0b, "newCycle": 0b}
setblock 2 120 4 greenhouse:greenhouse[crop=moss,stage=1]
data merge block 2 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "moss", "mature": 0b, "newCycle": 0b}
setblock 4 120 4 greenhouse:greenhouse[crop=moss,stage=2]
data merge block 4 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "moss", "mature": 0b, "newCycle": 0b}
setblock 6 120 4 greenhouse:greenhouse[crop=moss,stage=3]
data merge block 6 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:moss_block", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "moss", "mature": 1b, "newCycle": 0b}
setblock 12 120 4 greenhouse:greenhouse[crop=moss,stage=3]
data merge block 12 120 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:moss_block", "count": 1}]}, "elapsed": 600, "duration": 600, "chargedCycles": 0, "crop": "moss", "mature": 1b, "newCycle": 0b}
setblock 16 120 4 minecraft:chest
setblock 16 121 4 greenhouse:automatic_greenhouse[crop=moss,stage=0]
data merge block 16 121 4 {"Items": [{"Slot": 0, "id": "minecraft:stone", "count": 1}, {"Slot": 1, "id": "minecraft:moss_block", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600, "chargedCycles": 0, "crop": "moss", "mature": 0b, "newCycle": 0b}
setblock 0 120 8 greenhouse:greenhouse[crop=chorus,stage=0]
data merge block 0 120 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 600000, "chargedCycles": 0, "crop": "chorus", "mature": 0b, "newCycle": 0b}
setblock 2 120 8 greenhouse:greenhouse[crop=chorus,stage=1]
data merge block 2 120 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": []}, "elapsed": 150000, "duration": 600000, "chargedCycles": 0, "crop": "chorus", "mature": 0b, "newCycle": 0b}
setblock 4 120 8 greenhouse:greenhouse[crop=chorus,stage=2]
data merge block 4 120 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": []}, "elapsed": 300000, "duration": 600000, "chargedCycles": 0, "crop": "chorus", "mature": 0b, "newCycle": 0b}
setblock 6 120 8 greenhouse:greenhouse[crop=chorus,stage=3]
data merge block 6 120 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:chorus_fruit", "count": 1}]}, "elapsed": 600000, "duration": 600000, "chargedCycles": 0, "crop": "chorus", "mature": 1b, "newCycle": 0b}
setblock 12 120 8 greenhouse:greenhouse[crop=chorus,stage=3]
data merge block 12 120 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:chorus_fruit", "count": 1}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "chorus", "mature": 1b, "newCycle": 0b}
setblock 16 120 8 minecraft:chest
setblock 16 121 8 greenhouse:automatic_greenhouse[crop=chorus,stage=0]
data merge block 16 121 8 {"Items": [{"Slot": 0, "id": "minecraft:end_stone", "count": 1}, {"Slot": 1, "id": "minecraft:chorus_flower", "count": 1}], "pending": {"Items": []}, "elapsed": 1, "duration": 900, "chargedCycles": 0, "crop": "chorus", "mature": 0b, "newCycle": 0b}
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
tag @s add crops_beta4_init
time set noon
weather clear
tp @s 3.5 122 14.5 180 32
tellraw @s {"text":"GREENHOUSE BETA.4 CROP FIXTURE READY","color":"green"}
