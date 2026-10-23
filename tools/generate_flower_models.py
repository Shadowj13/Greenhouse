"""Generate priority 1-3 crops without changing the approved greenhouse shell.

Run from any directory. All plant textures reference Minecraft assets; no vanilla
texture is copied into the mod. Regeneration is deterministic.
"""
import json
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/greenhouse'
SMALL = ('dandelion', 'poppy', 'blue_orchid', 'allium', 'azure_bluet',
         'red_tulip', 'orange_tulip', 'white_tulip', 'pink_tulip',
         'oxeye_daisy', 'cornflower', 'lily_of_the_valley')
TALL = ('sunflower', 'lilac', 'peony', 'rose_bush')
CROPS = ('glow_berries',) + SMALL + TALL


def write(path, value):
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def plane(bottom, top, width, texture, axis, uv=None):
    lo = [8-width/2, bottom, 8] if axis == 'z' else [8, bottom, 8-width/2]
    hi = [8+width/2, top, 8] if axis == 'z' else [8, top, 8+width/2]
    return {'from': lo, 'to': hi, 'shade': False,
            'neoforge_data': {'ambient_occlusion': False},
            'faces': {face: {'texture': texture, 'uv': uv or [0, 0, 16, 16]}
                      for face in (('north', 'south') if axis == 'z' else ('west', 'east'))}}


def cross(bottom, top, width, texture, uv=None):
    return [plane(bottom, top, width, texture, axis, uv) for axis in ('x', 'z')]


def beam(lo, hi):
    return {'from': lo, 'to': hi,
            'faces': {face: {'texture': '#support', 'uv': [0, 0, 16, 16]}
                      for face in ('down', 'up', 'north', 'south', 'west', 'east')}}


for crop in CROPS:
    for stage in range(4):
        if crop in SMALL:
            height = (2.0, 3.5, 5.2, 7.0)[stage]
            textures = {'plant': f'minecraft:block/{crop}'}
            elements = cross(5.0, 5.0+height, height, '#plant')
        elif crop in TALL:
            height = (2.4, 4.6, 6.8, 9.0)[stage]
            middle, top = 5.0+height/2, 5.0+height
            textures = {'bottom': f'minecraft:block/{crop}_bottom',
                        'top': f'minecraft:block/{crop}_top'}
            elements = cross(5.0, middle, height/2, '#bottom')
            if crop == 'sunflower':
                # Vanilla sunflower_top contains leaves/stem only. The flower
                # uses the separate front/back sprites, within the enclosure.
                textures.update(front='minecraft:block/sunflower_front',
                                back='minecraft:block/sunflower_back')
                elements += cross(middle, middle+height/4, height/2, '#top', [0, 8, 16, 16])
                head = plane(middle+height/8, top, height*0.7, '#front', 'z')
                head['faces']['south']['texture'] = '#back'
                elements.append(head)
            else:
                elements += cross(middle, top, height/2, '#top')
        else:
            textures = {'support': 'greenhouse:block/planks',
                        'tip': 'minecraft:block/cave_vines_lit' if stage == 3 else 'minecraft:block/cave_vines',
                        'stem': 'minecraft:block/cave_vines_plant_lit' if stage == 3 else 'minecraft:block/cave_vines_plant'}
            elements = [beam([5.5, 5.0, 7.65], [6.0, 13.3, 8.35]),
                        beam([10.0, 5.0, 7.65], [10.5, 13.3, 8.35]),
                        beam([6.0, 12.95, 7.65], [10.0, 13.3, 8.35])]
            bottom = (11.2, 9.4, 7.3, 5.25)[stage]
            split = min(13.0, bottom+3.2)
            elements += cross(bottom, split, 3.6, '#tip')
            if split < 13.0:
                elements += cross(split, 13.0, 3.6, '#stem')
        textures['particle'] = next(value for key, value in textures.items() if key != 'support')
        write(ASSETS / f'models/block/plants/{crop}_stage_{stage}.json', {
            'ambientocclusion': False, 'textures': textures, 'elements': elements})

for kind in ('greenhouse', 'automatic_greenhouse'):
    path = ASSETS / f'blockstates/{kind}.json'
    states = json.loads(path.read_text(encoding='utf-8'))
    states['multipart'] = [part for part in states['multipart']
                           if part.get('when', {}).get('crop') not in CROPS]
    for crop in CROPS:
        for stage in range(4):
            states['multipart'].append({'when': {'crop': crop, 'stage': str(stage)},
                                       'apply': {'model': f'greenhouse:block/plants/{crop}_stage_{stage}'}})
    write(path, states)
print('Generated 68 plant models and both blockstates for the 17 priority 1-3 crops.')
