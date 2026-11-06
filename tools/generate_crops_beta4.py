"""Generate sea-pickle, moss and chorus growth models; reference vanilla textures.

The detailed shell, GUI and existing plants are never edited. Run after the
kelp generator when creating all generated resources from scratch.
"""
import json
from pathlib import Path
from model_bases import configure_bases

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/greenhouse'
CROPS = ('sea_pickle', 'moss', 'chorus')
FACES = ('down', 'up', 'north', 'south', 'west', 'east')


def write(path, value):
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8', newline='\n')


def box(lo, hi, texture, omit=(), uv=None):
    return {'from': lo, 'to': hi, 'shade': False,
            'neoforge_data': {'ambient_occlusion': False},
            'faces': {face: {'texture': texture, 'uv': (uv or {}).get(face, [0, 0, 16, 16])}
                      for face in FACES if face not in omit}}


for stage in range(4):
    # A colony of 1, 2, 3, then 4 pickles, using the native sprite's separate
    # side/cap regions. Raised necks omit their hidden bottom face: no overlays.
    elements = []
    centers = ((8.0, 8.0, 2.8),) if stage == 0 else (
        (6.3, 6.3, 3.0), (9.3, 9.1, 2.4),
        (9.3, 6.3, 2.8), (6.3, 9.1, 2.1))[:stage+1]
    uv = {'down': [8, 1, 12, 5], 'up': [4, 1, 8, 5],
          'north': [4, 5, 8, 11], 'south': [0, 5, 4, 11],
          'west': [8, 5, 12, 11], 'east': [12, 5, 16, 11]}
    for x, z, height in centers:
        top = round(4.96+height, 3)
        elements.append(box([x-.8, 4.96, z-.8], [x+.8, top, z+.8], '#plant', uv=uv))
        elements.append(box([x-.28, top, z-.28], [x+.28, top+.3, z+.28],
                            '#plant', omit=('down',), uv={'up': [8, 1, 12, 5]}))
    write(ASSETS / f'models/block/plants/sea_pickle_stage_{stage}.json', {
        'ambientocclusion': False,
        'textures': {'plant': 'minecraft:block/sea_pickle', 'particle': 'minecraft:block/sea_pickle'},
        'elements': elements})

    if stage == 0:
        elements = [box([7, 5, 7], [9, 5.35, 9], '#plant')]
    elif stage == 1:
        elements = [box([6.2, 5, 6.2], [7.95, 5.75, 9.8], '#plant'),
                    box([8.05, 5, 6.2], [9.8, 5.55, 9.8], '#plant')]
    elif stage == 2:
        elements = [box([x, 5, z], [x+2.5, y, z+2.5], '#plant')
                    for x, z, y in ((5.4, 5.4, 6.1), (8.05, 5.4, 6.3),
                                    (5.4, 8.05, 5.95), (8.05, 8.05, 6.15))]
    else:
        elements = [box([5.3, 5, 5.3], [10.7, 6.0, 10.7], '#plant'),
                    box([6.0, 6.0, 6.0], [10.0, 6.7, 10.0], '#plant', omit=('down',)),
                    box([7.0, 6.7, 7.0], [9.0, 7.1, 9.0], '#plant', omit=('down',))]
    write(ASSETS / f'models/block/plants/moss_stage_{stage}.json', {
        'ambientocclusion': False,
        'textures': {'plant': 'minecraft:block/moss_block', 'particle': 'minecraft:block/moss_block'},
        'elements': elements})

    top = (6.3, 8.1, 10.2, 11.9)[stage]
    elements = [box([7.55, 5, 7.55], [8.45, top, 8.45], '#stem', omit=('up',)),
                box([7.1, top, 7.1], [8.9, top+1.5, 8.9], '#flower')]
    if stage >= 1:
        base = round(top-1.65, 3)
        elements += [box([5.8, base, 7.55], [7.55, base+.8, 8.45], '#stem', omit=('east',)),
                     box([5.8, base+.8, 7.55], [6.65, top-.3, 8.45], '#stem', omit=('down', 'up')),
                     box([5.45, top-.3, 7.2], [7.0, top+.9, 8.8], '#flower')]
    if stage >= 2:
        base = round(top-2.7, 3)
        elements += [box([8.45, base, 7.55], [10.2, base+.8, 8.45], '#stem', omit=('west',)),
                     box([9.35, base+.8, 7.55], [10.2, top-.8, 8.45], '#stem', omit=('down', 'up')),
                     box([9.0, top-.8, 7.2], [10.55, top+.4, 8.8], '#flower')]
    if stage == 3:
        elements += [box([7.55, 7.0, 8.45], [8.45, 7.8, 10.1], '#stem', omit=('north',)),
                     box([7.55, 7.8, 9.25], [8.45, 10.5, 10.1], '#stem', omit=('down', 'up')),
                     box([7.2, 10.5, 8.9], [8.8, 11.8, 10.55], '#flower')]
    write(ASSETS / f'models/block/plants/chorus_stage_{stage}.json', {
        'ambientocclusion': False,
        'textures': {'stem': 'minecraft:block/chorus_plant',
                     'flower': 'minecraft:block/chorus_flower_dead' if stage == 3 else 'minecraft:block/chorus_flower',
                     'particle': 'minecraft:block/chorus_plant'}, 'elements': elements})

for kind in ('greenhouse', 'automatic_greenhouse'):
    # Only the soil texture changes. Every element of the approved shell is
    # identical; the aquatic model is the already validated kelp model.
    model = json.loads((ASSETS / f'models/block/{kind}.json').read_text(encoding='utf-8'))
    model['textures']['dirt'] = 'minecraft:block/end_stone'
    write(ASSETS / f'models/block/{kind}_end.json', model)
    path = ASSETS / f'blockstates/{kind}.json'
    states = json.loads(path.read_text(encoding='utf-8'))
    states['multipart'] = [part for part in states['multipart']
                           if part.get('when', {}).get('crop') not in CROPS]
    configure_bases(kind, states)
    for crop in CROPS:
        for stage in range(4):
            states['multipart'].append({'when': {'crop': crop, 'stage': str(stage)},
                                       'apply': {'model': f'greenhouse:block/plants/{crop}_stage_{stage}'}})
    write(path, states)
print('Generated 12 crop models, 2 End pot variants and exclusive substrate selectors.')
