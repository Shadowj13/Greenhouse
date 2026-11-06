"""Generate five beta.5 crops with native textures and unchanged detailed shells."""
import json
from pathlib import Path
from model_bases import configure_bases

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/greenhouse'
CROPS = ('vine', 'glow_lichen', 'twisting_vines', 'weeping_vines', 'big_dripleaf')
FACES = ('down', 'up', 'north', 'south', 'west', 'east')


def write(path, obj):
    path.write_text(json.dumps(obj, indent=2)+'\n', encoding='utf-8', newline='\n')


def element(lo, hi, textures, uv=None, emission=0, color=None):
    data = {'ambient_occlusion': False}
    if color is not None:
        data['color'] = color
    return {'from': lo, 'to': hi, 'shade': False, 'light_emission': emission,
            'neoforge_data': data,
            'faces': {face: {'texture': texture, 'uv': (uv or {}).get(face, [0, 0, 16, 16])}
                      for face, texture in textures.items()}}


def box(lo, hi, texture):
    return element(lo, hi, dict.fromkeys(FACES, texture))


def panel(lo, hi, texture, axis, **kwargs):
    faces = (('west', 'east'), ('down', 'up'), ('north', 'south'))[axis]
    return element(lo, hi, dict.fromkeys(faces, texture), **kwargs)


def cross(bottom, top, width, texture):
    return [panel([8-width/2, bottom, 8], [8+width/2, top, 8], texture, 2),
            panel([8, bottom, 8-width/2], [8, top, 8+width/2], texture, 0)]


def trellis(cap_texture='#support'):
    return [box([5.3, 5, 7.65], [5.7, 13.5, 8.35], '#support'),
            box([10.3, 5, 7.65], [10.7, 13.5, 8.35], '#support'),
            box([5.7, 13.1, 7.65], [10.3, 13.5, 8.35], cap_texture)]


for crop in CROPS:
    for stage in range(4):
        if crop == 'vine':
            textures = {'plant': 'minecraft:block/vine', 'support': 'greenhouse:block/planks'}
            bottom = (10.8, 9.0, 7.0, 5.2)[stage]
            elements = trellis() + [panel([5.8, bottom, 7.5], [10.2, 13.1, 7.5], '#plant', 2,
                                          color=0xFF64A432)]
        elif crop == 'glow_lichen':
            textures = {'plant': 'minecraft:block/glow_lichen', 'rock': 'minecraft:block/stone'}
            # A fixed rock with progressively larger patches. The 0.02-pixel
            # offset is outside the rock, never a coplanar overlay.
            elements = [box([6.0, 5, 6.3], [10.0, 10.0, 9.7], '#rock')]
            width = (1.4, 2.1, 3.0, 3.8)[stage]
            bottom, top = 7.5-width/2, 7.5+width/2
            elements.append(panel([8-width/2, bottom, 6.28], [8+width/2, top, 6.28], '#plant', 2, emission=7))
            if stage >= 1:
                elements.append(panel([5.98, bottom, 8-width/3], [5.98, top, 8+width/3], '#plant', 0, emission=7))
            if stage >= 2:
                elements.append(panel([8-width/2, bottom, 9.72], [8+width/2, top, 9.72], '#plant', 2, emission=7))
            if stage == 3:
                elements.append(panel([6.1, 10.02, 6.4], [9.9, 10.02, 9.6], '#plant', 1, emission=7))
        elif crop == 'twisting_vines':
            textures = {'tip': 'minecraft:block/twisting_vines', 'stem': 'minecraft:block/twisting_vines_plant'}
            top = (7.2, 9.3, 11.4, 13.5)[stage]
            split = max(5.0, top-3.0)
            elements = cross(split, top, 3.8, '#tip')
            if split > 5.0:
                elements += cross(5.0, split, 3.8, '#stem')
        elif crop == 'weeping_vines':
            textures = {'tip': 'minecraft:block/weeping_vines', 'stem': 'minecraft:block/weeping_vines_plant',
                        'support': 'greenhouse:block/planks', 'cap': 'minecraft:block/nether_wart_block'}
            bottom = (10.9, 9.0, 7.1, 5.2)[stage]
            split = min(13.1, bottom+3.0)
            elements = trellis('#cap') + cross(bottom, split, 3.8, '#tip')
            if split < 13.1:
                elements += cross(split, 13.1, 3.8, '#stem')
        else:
            textures = {k: f'minecraft:block/big_dripleaf_{k}' for k in ('top', 'stem', 'side', 'tip')}
            top = (7.2, 9.2, 11.2, 13.2)[stage]
            width = (2.4, 3.4, 4.5, 5.8)[stage]
            lo, hi = 8-width/2, 8+width/2
            elements = cross(5.0, top, 1.5, '#stem')
            elements += [panel([lo, top, lo], [hi, top, hi], '#top', 1),
                         panel([lo, top-width/4, lo], [hi, top, lo], '#tip', 2,
                               uv={f:[0, 0, 16, 4] for f in ('north', 'south')}),
                         panel([lo, top-width/4, lo], [lo, top, hi], '#side', 0,
                               uv={f:[0, 0, 16, 4] for f in ('west', 'east')}),
                         panel([hi, top-width/4, lo], [hi, top, hi], '#side', 0,
                               uv={f:[0, 0, 16, 4] for f in ('west', 'east')})]
        textures['particle'] = next(value for key, value in textures.items() if key not in ('support', 'rock'))
        write(ASSETS / f'models/block/plants/{crop}_stage_{stage}.json',
              {'ambientocclusion': False, 'textures': textures, 'elements': elements})

for kind in ('greenhouse', 'automatic_greenhouse'):
    for suffix, texture in (('stone', 'stone'), ('warped', 'warped_nylium'), ('wart', 'nether_wart_block')):
        model = json.loads((ASSETS / f'models/block/{kind}.json').read_text(encoding='utf-8'))
        model['textures']['dirt'] = 'minecraft:block/'+texture
        write(ASSETS / f'models/block/{kind}_{suffix}.json', model)
    path = ASSETS / f'blockstates/{kind}.json'
    states = json.loads(path.read_text(encoding='utf-8'))
    states['multipart'] = [p for p in states['multipart'] if p.get('when', {}).get('crop') not in CROPS]
    configure_bases(kind, states)
    for crop in CROPS:
        for stage in range(4):
            states['multipart'].append({'when': {'crop': crop, 'stage': str(stage)},
                                        'apply': {'model': f'greenhouse:block/plants/{crop}_stage_{stage}'}})
    write(path, states)
print('Generated 20 beta.5 plant models, 6 substrate variants and exclusive shell selection.')
