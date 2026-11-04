"""Generate an isolated, reproducible beta.4 visual fixture, not a user world."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIXTURE = ROOT / 'tests/fixtures/crops4-visual'
FUNCTIONS = FIXTURE / 'data/greenhouse_crops4/function'


def generate():
    FUNCTIONS.mkdir(parents=True, exist_ok=True)
    tags = FIXTURE / 'data/minecraft/tags/function'
    tags.mkdir(parents=True, exist_ok=True)
    (FIXTURE / 'pack.mcmeta').write_text(json.dumps({'pack': {
        'description': 'Greenhouse beta.4 sea pickles, moss and chorus visual fixture',
        'min_format': 121, 'max_format': 121}})+'\n', encoding='utf-8')
    (tags / 'tick.json').write_text('{"values":["greenhouse_crops4:tick"]}\n', encoding='utf-8')
    (FUNCTIONS / 'tick.mcfunction').write_text(
        'execute as @a[tag=!crops_beta4_init] at @s run function greenhouse_crops4:setup\n'
        'execute as @a[tag=crops_beta4_init] at @s run function greenhouse_crops4:controls\n',
        encoding='utf-8')
    crops = (('sea_pickle', 'sea_pickle', 'water_bucket', 'sea_pickle', 600),
             ('moss', 'moss_block', 'stone', 'moss_block', 600),
             ('chorus', 'chorus_flower', 'end_stone', 'chorus_fruit', 900))
    commands = ['forceload add -16 -16 32 32', 'gamemode creative @s',
                'fill -6 119 -6 25 119 18 minecraft:smooth_stone',
                'fill -6 120 -6 25 127 18 minecraft:air', 'clear @s']

    def machine(spec, x, y, z, stage=3, automatic=False, real=False):
        crop, plant, soil, product, ticks = spec
        kind = 'automatic_greenhouse' if automatic else 'greenhouse'
        if automatic:
            commands.append(f'setblock {x} {y-1} {z} minecraft:chest')
        commands.append(f'setblock {x} {y} {z} greenhouse:{kind}[crop={crop},stage={stage}]')
        duration = ticks if real else 600000
        nbt = {'Items': [{'Slot': 0, 'id': 'minecraft:'+soil, 'count': 1},
                         {'Slot': 1, 'id': 'minecraft:'+plant, 'count': 1}],
               'pending': {'Items': [{'Slot': 0, 'id': 'minecraft:'+product, 'count': 1}] if stage == 3 else []},
               'elapsed': duration if stage == 3 else max(1, stage*duration//4),
               'duration': duration, 'chargedCycles': 0, 'crop': crop,
               'mature': stage == 3, 'newCycle': False}
        commands.append(f'data merge block {x} {y} {z} '+
                        json.dumps(nbt).replace('true', '1b').replace('false', '0b'))

    for row, spec in enumerate(crops):
        for stage in range(4):
            machine(spec, stage*2, 120, row*4, stage)
        machine(spec, 12, 120, row*4, real=True)
        machine(spec, 16, 121, row*4, 0, automatic=True, real=True)
    machine(('blue_orchid', 'blue_orchid', 'dirt', 'blue_orchid', 600), 21, 120, 0, real=True)
    machine(('kelp', 'kelp', 'water_bucket', 'kelp', 600), 21, 120, 4, real=True)
    commands.append('setblock 21 120 8 greenhouse:greenhouse[crop=empty,stage=0]')
    views = (('stick', '3.5 122 14.5 180 32'),
             ('feather', '3.5 120 3.0 180 18'),
             ('blaze_rod', '3.5 120 7.0 180 18'),
             ('bone', '3.5 120 11.0 180 18'),
             ('paper', '12.5 120 3.0 180 23'),
             ('flint', '12.5 120 7.0 180 23'),
             ('coal', '12.5 120 11.0 180 23'),
             ('brick', '16.5 121 11.0 180 23'),
             ('quartz', '16.5 120 2.5 180 40'))
    for slot, (item, _) in enumerate(views):
        commands.append(f'item replace entity @s hotbar.{slot} with minecraft:{item}')
    commands += ['tag @s add crops_beta4_init', 'time set noon', 'weather clear',
                 'tp @s 3.5 122 14.5 180 32',
                 'tellraw @s {"text":"GREENHOUSE BETA.4 CROP FIXTURE READY","color":"green"}']
    (FUNCTIONS / 'setup.mcfunction').write_text('\n'.join(commands)+'\n', encoding='utf-8')
    (FUNCTIONS / 'controls.mcfunction').write_text(
        '\n'.join(f'execute if items entity @s weapon.mainhand minecraft:{item} run tp @s {view}'
                  for item, view in views)+'\ntime set noon\nweather clear\n', encoding='utf-8')


if __name__ == '__main__':
    generate()
    print('Generated beta.4 visual fixture with three crops, four stages, real harvests and chest export.')
