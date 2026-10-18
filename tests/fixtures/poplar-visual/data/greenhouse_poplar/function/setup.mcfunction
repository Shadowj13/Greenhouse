forceload add -16 -16 16 16
gamemode creative @s
fill -8 119 -8 12 119 12 minecraft:smooth_stone
fill -8 120 -8 12 125 12 minecraft:air
clear @s
setblock 0 120 0 greenhouse:greenhouse[crop=poplar_red,stage=3]
data merge block 0 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_red", "mature": 1b, "newCycle": 0b}
setblock 2 120 0 greenhouse:greenhouse[crop=poplar_orange,stage=3]
data merge block 2 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_orange", "mature": 1b, "newCycle": 0b}
setblock 4 120 0 greenhouse:greenhouse[crop=poplar_yellow,stage=3]
data merge block 4 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_yellow", "mature": 1b, "newCycle": 0b}
setblock 0 120 -2 minecraft:chest
setblock 0 121 -2 greenhouse:automatic_greenhouse[crop=poplar_red,stage=3]
data merge block 0 121 -2 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}, {"Slot": 9, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 10, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 11, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 12, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 13, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 14, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 15, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 16, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 17, "id": "minecraft:cobblestone", "count": 64}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_red", "mature": 1b, "newCycle": 0b}
setblock 2 120 -2 minecraft:chest
setblock 2 121 -2 greenhouse:automatic_greenhouse[crop=poplar_orange,stage=3]
data merge block 2 121 -2 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}, {"Slot": 9, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 10, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 11, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 12, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 13, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 14, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 15, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 16, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 17, "id": "minecraft:cobblestone", "count": 64}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_orange", "mature": 1b, "newCycle": 0b}
setblock 4 120 -2 minecraft:chest
setblock 4 121 -2 greenhouse:automatic_greenhouse[crop=poplar_yellow,stage=3]
data merge block 4 121 -2 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:poplar_sapling", "count": 1}, {"Slot": 9, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 10, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 11, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 12, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 13, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 14, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 15, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 16, "id": "minecraft:cobblestone", "count": 64}, {"Slot": 17, "id": "minecraft:cobblestone", "count": 64}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:poplar_log", "count": 2}]}, "elapsed": 900, "duration": 900, "chargedCycles": 0, "crop": "poplar_yellow", "mature": 1b, "newCycle": 0b}
time set noon
weather clear
tp @s 2.5 120 5.5 180 15
item replace entity @s hotbar.0 with minecraft:stick
item replace entity @s hotbar.1 with minecraft:feather
item replace entity @s hotbar.2 with minecraft:blaze_rod
item replace entity @s hotbar.3 with minecraft:bone
item replace entity @s hotbar.4 with minecraft:paper
tag @s add poplar_qa_init
tellraw @s {"text":"GREENHOUSE 0.2.9 POPLAR FIXTURE READY","color":"green"}
