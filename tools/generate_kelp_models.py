"""Generate only the aquatic base variants and four kelp stages; reuse vanilla textures."""
import json
from pathlib import Path
from model_bases import configure_bases

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/greenhouse'


def write(path, value):
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8', newline='\n')


def plane(cx, z, bottom, top, width, texture, axis):
    low = [cx-width/2, bottom, z] if axis == 'z' else [cx, bottom, z-width/2]
    high = [cx+width/2, top, z] if axis == 'z' else [cx, top, z+width/2]
    return {'from': low, 'to': high, 'shade': False,
            'neoforge_data': {'ambient_occlusion': False},
            'faces': {face: {'texture': texture, 'uv': [0, 0, 16, 16]}
                      for face in (('north', 'south') if axis == 'z' else ('west', 'east'))}}


for kind in ('greenhouse', 'automatic_greenhouse'):
    model = json.loads((ASSETS / f'models/block/{kind}.json').read_text())
    # Replace the existing soil surface, never overlay a coplanar water face.
    # The walls, glass, pot rim, flat roof and automatic base are identical.
    model['textures']['dirt'] = 'minecraft:block/water_still'
    model['elements'][6] = {
        'from': [4.65, 4.96, 4.65], 'to': [11.35, 4.96, 11.35],
        'shade': False,
        'neoforge_data': {'color': 'ff3f76e4', 'ambient_occlusion': False},
        'faces': {'up': {'texture': '#dirt', 'uv': [0, 0, 16, 16]}}
    }
    write(ASSETS / f'models/block/{kind}_aquatic.json', model)
    path = ASSETS / f'blockstates/{kind}.json'
    states = json.loads(path.read_text())
    configure_bases(kind, states)
    parts = states['multipart']
    # Update existing stage entries in place so regenerating kelp does not
    # reorder unrelated crops or duplicate the newer substrate selectors.
    for stage in range(4):
        expected = {'when': {'crop': 'kelp', 'stage': str(stage)},
                    'apply': {'model': f'greenhouse:block/plants/kelp_stage_{stage}'}}
        matching = [i for i, part in enumerate(parts)
                    if part.get('when', {}) == expected['when']]
        if matching:
            parts[matching[0]] = expected
            for index in reversed(matching[1:]):
                del parts[index]
        else:
            parts.append(expected)
    states['multipart'] = parts
    write(path, states)

for stage, height in enumerate((1.5, 3.5, 5.8, 8.6)):
    elements = []
    for cx, z, scale in ((6.8, 7.1, 1.0), (9.1, 9.0, 0.8)):
        top = round(4.96 + height * scale, 3)
        sections = [(4.96, top, '#kelp')]
        if height > 4:
            middle = round(4.96 + height * scale / 2, 3)
            sections = [(4.96, middle, '#stem'), (middle, top, '#kelp')]
        for bottom, upper, texture in sections:
            for axis in ('x', 'z'):
                elements.append(plane(cx, z, bottom, upper, 3.2, texture, axis))
    write(ASSETS / f'models/block/plants/kelp_stage_{stage}.json', {
        'ambientocclusion': False,
        'textures': {'particle': 'minecraft:block/kelp', 'kelp': 'minecraft:block/kelp',
                     'stem': 'minecraft:block/kelp_plant'},
        'elements': elements
    })
print('Generated aquatic base variants and four kelp stages using animated vanilla textures.')
