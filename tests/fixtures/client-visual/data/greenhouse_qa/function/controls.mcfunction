execute if items entity @s weapon.mainhand minecraft:stick unless entity @s[tag=qa_manual] run function greenhouse_qa:manual
execute if items entity @s weapon.mainhand minecraft:feather unless entity @s[tag=qa_angle] run function greenhouse_qa:angle
execute if items entity @s weapon.mainhand minecraft:blaze_rod unless entity @s[tag=qa_auto] run function greenhouse_qa:auto
execute if items entity @s weapon.mainhand minecraft:bone unless entity @s[tag=qa_chest] run function greenhouse_qa:chest
execute if items entity @s weapon.mainhand minecraft:coal unless entity @s[tag=qa_night] run function greenhouse_qa:night
execute if items entity @s weapon.mainhand minecraft:torch unless entity @s[tag=qa_day] run function greenhouse_qa:day
execute if items entity @s weapon.mainhand minecraft:paper unless entity @s[tag=qa_empty] run function greenhouse_qa:empty
execute unless entity @s[tag=qa_night] run time set noon
