"""Generate the shelf mushroom and its internal poplar support."""
import json
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/greenhouse'
DIRECTIONS = ('north', 'south', 'east', 'west', 'up', 'down')
CAP_UVS = (
    ((5, 2, 10, 3), (5, 3, 10, 4), (5, 0, 8.5, 1), (5, 1, 8.5, 2), (5, 3.5, 0, 0), (5, 3.5, 0, 7)),
    ((7, 3, 14, 4.5), (7, 4.5, 14, 6), (7, 0, 12, 1.5), (7, 1.5, 12, 3), (7, 5, 0, 0), (7, 5, 0, 10)),
)
STEM_UVS = (
    ((5, 5, 8, 5.5), (5, 5.5, 8, 6), (5, 4, 7, 4.5), (5, 4.5, 7, 5), (3, 9, 0, 7), (3, 9, 0, 11)),
    ((4, 12, 8, 13), (4, 13, 8, 14), (4, 10, 7, 11), (4, 11, 7, 12), (4, 13, 0, 10), (4, 13, 0, 16)),
)


def cuboid(start, end, faces):
    return {'from': start, 'to': end, 'shade_direction_override': 'up',
            'neoforge_data': {'ambient_occlusion': False}, 'faces': faces}


def mushroom_faces(uvs, covered):
    return {direction: {'texture': '#fungus', 'uv': uv}
            for direction, uv in zip(DIRECTIONS, uvs) if direction not in covered}


def write(path, data):
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8', newline='\n')


for stage, (width, depth, thickness) in enumerate(((2.6, 1.45, .55), (3.5, 1.95, .75),
                                                   (4.4, 2.5, .95), (5.6, 3.15, 1.15))):
    sprite = int(stage >= 2)
    base = 9.3
    log = cuboid([6.7, 5, 6.0], [9.3, 12.0, 8.1], {
        direction: {'texture': '#log_top' if direction in ('up', 'down') else '#log'}
        for direction in DIRECTIONS})
    cap = cuboid([round(8-width/2, 3), base, 8.1],
                 [round(8+width/2, 3), round(base+thickness, 3), round(8.1+depth, 3)],
                 mushroom_faces(CAP_UVS[sprite], ()))
    underside = cuboid([round(8-width*.31, 3), round(base-thickness*.56, 3), 8.1],
                       [round(8+width*.31, 3), base, round(8.1+depth*.6, 3)],
                       mushroom_faces(STEM_UVS[sprite], ('up', 'north')))
    write(ASSETS / f'models/block/plants/shelf_mushroom_stage_{stage}.json', {
        'ambientocclusion': False,
        'textures': {'particle': 'minecraft:block/shelf_mushroom_particle',
                     'fungus': f'minecraft:block/shelf_mushroom_stage{sprite}',
                     'log': 'minecraft:block/poplar_log', 'log_top': 'minecraft:block/poplar_log_top'},
        'elements': [log, cap, underside]})

for name in ('greenhouse', 'automatic_greenhouse'):
    path = ASSETS / f'blockstates/{name}.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    data['multipart'] = [part for part in data['multipart'] if part.get('when', {}).get('crop') != 'shelf_mushroom']
    for stage in range(4):
        data['multipart'].append({'when': {'crop': 'shelf_mushroom', 'stage': str(stage)},
                                  'apply': {'model': f'greenhouse:block/plants/shelf_mushroom_stage_{stage}'}})
    write(path, data)
