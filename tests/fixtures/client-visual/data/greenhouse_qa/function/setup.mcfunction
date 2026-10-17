tag @s remove qa_manual
tag @s remove qa_angle
tag @s remove qa_auto
tag @s remove qa_chest
tag @s remove qa_night
tag @s remove qa_day
tag @s remove qa_empty
forceload add -16 -16 16 16
gamemode creative @s
tp @s 0.5 120 3.2 180 20
fill -8 119 -8 12 119 12 minecraft:smooth_stone
fill -8 120 -8 12 125 12 minecraft:air
setblock 0 120 0 greenhouse:greenhouse[crop=oak,stage=3]
data merge block 0 120 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:oak_sapling", "count": 1}, {"Slot": 2, "id": "minecraft:bone_meal", "count": 63}], "pending": {"Items": [{"Slot": 0, "id": "minecraft:oak_log", "count": 2}]}, "elapsed": 450, "duration": 450, "chargedCycles": 3, "crop": "oak", "mature": 1b, "newCycle": 0b}
setblock 3 120 0 minecraft:chest
setblock 3 121 0 greenhouse:automatic_greenhouse[crop=oak,stage=2]
data merge block 3 121 0 {"Items": [{"Slot": 0, "id": "minecraft:dirt", "count": 1}, {"Slot": 1, "id": "minecraft:oak_sapling", "count": 1}, {"Slot": 2, "id": "minecraft:bone_meal", "count": 63}, {"Slot": 9, "id": "minecraft:oak_log", "count": 16}], "pending": {"Items": []}, "elapsed": 225, "duration": 450, "chargedCycles": 3, "crop": "oak", "mature": 0b, "newCycle": 0b}
setblock 6 120 0 minecraft:chest
setblock 0 120 4 greenhouse:greenhouse
setblock 3 120 4 greenhouse:automatic_greenhouse
time set noon
weather clear
gamemode creative @s
clear @s
item replace entity @s hotbar.0 with minecraft:stick
item replace entity @s hotbar.1 with greenhouse:greenhouse
item replace entity @s hotbar.2 with greenhouse:automatic_greenhouse
tp @s 0.5 120 3.2 180 20
tellraw @s {"text":"GREENHOUSE 0.2.8 CLIENT FIXTURE READY","color":"green"}
tag @s add greenhouse_qa_init
item replace entity @s hotbar.1 with minecraft:feather
item replace entity @s hotbar.2 with minecraft:blaze_rod
item replace entity @s hotbar.3 with minecraft:bone
item replace entity @s hotbar.4 with minecraft:coal
item replace entity @s hotbar.5 with minecraft:torch
item replace entity @s hotbar.6 with minecraft:paper
item replace entity @s hotbar.7 with greenhouse:greenhouse
item replace entity @s hotbar.8 with greenhouse:automatic_greenhouse
tag @s add qa_manual
