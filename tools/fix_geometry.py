"""Reproducible migration from recovered 0.2.6 assets; no texture repainting."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / 'src/main/resources/assets/greenhouse/models/block'

for name in ('greenhouse', 'automatic_greenhouse'):
    path = MODELS / f'{name}.json'
    model = json.loads(path.read_text())
    elements = model['elements']
    # Retain the flat-roof model, pot, four posts and automatic machinery.
    # Complete the rim exactly where vanilla draws the single selection cuboid.
    for index, start, end in (
        (19, [1, 13.8, 1], [15, 14.85, 1.9]),
        (20, [1, 13.8, 14.1], [15, 14.85, 15]),
        (21, [1, 13.8, 1.9], [1.9, 14.85, 14.1]),
        (22, [14.1, 13.8, 1.9], [15, 14.85, 14.1]),
        (23, [1.9, 14.25, 1.9], [14.1, 14.85, 14.1]),
    ):
        elements[index]['from'], elements[index]['to'] = start, end
    # Two opposite normals on ONE plane: a camera on either side sees one face.
    # No thin cube edges, tiny depth separation, or glass intersecting the rim.
    for index, start, end in (
        (15, [1.9, 3.2, 1.45], [14.1, 13.8, 1.45]),
        (16, [1.9, 3.2, 14.55], [14.1, 13.8, 14.55]),
        (17, [1.45, 3.2, 1.9], [1.45, 13.8, 14.1]),
        (18, [14.55, 3.2, 1.9], [14.55, 13.8, 14.1]),
    ):
        pane = elements[index]
        pane['from'], pane['to'] = start, end
        pane['shade'] = False
        pane['neoforge_data'] = {'ambient_occlusion': False}
    # The small central underside plug in 0.2.6 is entirely inside the opaque
    # plinth. Its down face shared y=0 with the plinth and could z-fight below.
    if name == 'automatic_greenhouse':
        elements[34]['faces'].pop('down', None)
    path.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')

# Preview models are tooling-only snapshots, not referenced by blockstates or
# items. Keep them consistent with the fixed structural model too.
for path in (MODELS / 'previews').glob('*.json'):
    model = json.loads(path.read_text())
    name = 'automatic_greenhouse' if path.stem.startswith('automatic_') else 'greenhouse'
    base = json.loads((MODELS / f'{name}.json').read_text())
    crop = path.stem.removeprefix(name + '_')
    plant = json.loads((MODELS / 'plants' / f'{crop}.json').read_text())
    model['textures'] = base['textures'] | plant['textures']
    model['elements'] = base['elements'] + plant['elements']
    path.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
