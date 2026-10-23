"""Asset/outline regressions. Runs without Minecraft or third party packages."""
import json
import re
import struct
import unittest
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/greenhouse'
MODELS = ASSETS / 'models'


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def png_pixels(path):
    """Decode the bundled RGBA PNGs, including all five PNG row filters."""
    data = path.read_bytes()
    assert data[:8] == b'\x89PNG\r\n\x1a\n'
    pos, packed = 8, b''
    while pos < len(data):
        size = struct.unpack('>I', data[pos:pos+4])[0]
        kind, payload = data[pos+4:pos+8], data[pos+8:pos+8+size]
        if kind == b'IHDR':
            width, height, depth, color, _, _, interlace = struct.unpack('>IIBBBBB', payload)
            assert (depth, color, interlace) == (8, 6, 0)
        if kind == b'IDAT':
            packed += payload
        pos += 12 + size
    raw = zlib.decompress(packed)
    stride, previous, pixels, pos = width * 4, bytearray(width * 4), [], 0
    for _ in range(height):
        filt = raw[pos]
        row = bytearray(raw[pos+1:pos+1+stride])
        for i in range(stride):
            a, b, c = row[i-4] if i >= 4 else 0, previous[i], previous[i-4] if i >= 4 else 0
            p = a + b - c
            distances = (abs(p-a), abs(p-b), abs(p-c))
            paeth = (a, b, c)[distances.index(min(distances))]
            row[i] = (row[i] + (0, a, b, (a+b)//2, paeth)[filt]) & 255
        pixels.extend(tuple(row[i:i+4]) for i in range(0, stride, 4))
        previous, pos = row, pos+1+stride
    return pixels


class ModelTests(unittest.TestCase):
    def test_kelp_uses_four_stages_and_only_replaces_the_soil_surface(self):
        heights = []
        for kind in ('greenhouse', 'automatic_greenhouse'):
            base = read(MODELS / f'block/{kind}.json')
            aquatic = read(MODELS / f'block/{kind}_aquatic.json')
            self.assertEqual(base['elements'][:6], aquatic['elements'][:6])
            self.assertEqual(base['elements'][7:], aquatic['elements'][7:])
            water = aquatic['elements'][6]
            self.assertEqual(water['from'][1], water['to'][1])
            self.assertGreater(water['from'][1], base['elements'][6]['to'][1])
            self.assertEqual(set(water['faces']), {'up'})
            self.assertEqual(aquatic['textures']['dirt'], 'minecraft:block/water_still')
            parts = read(ASSETS / f'blockstates/{kind}.json')['multipart']
            bases = [p for p in parts if 'stage' not in p.get('when', {})]
            self.assertEqual([p['when']['crop'] for p in bases], ['!kelp', 'kelp'])
            stages = [p['when']['stage'] for p in parts
                      if p.get('when', {}).get('crop') == 'kelp' and 'stage' in p['when']]
            self.assertEqual(stages, ['0', '1', '2', '3'])
        for stage in range(4):
            model = read(MODELS / f'block/plants/kelp_stage_{stage}.json')
            heights.append(max(e['to'][1] for e in model['elements']))
            for e in model['elements']:
                self.assertEqual(sum(a == b for a, b in zip(e['from'], e['to'])), 1)
                self.assertEqual(len(e['faces']), 2)
                self.assertGreaterEqual(e['from'][1], 4.96)
        self.assertEqual(heights, sorted(set(heights)))

    def test_priority123_models_and_both_blockstates(self):
        crops = ('glow_berries', 'dandelion', 'poppy', 'blue_orchid', 'allium',
                 'azure_bluet', 'red_tulip', 'orange_tulip', 'white_tulip',
                 'pink_tulip', 'oxeye_daisy', 'cornflower', 'lily_of_the_valley',
                 'sunflower', 'lilac', 'peony', 'rose_bush')
        for crop in crops:
            versions = []
            for stage in range(4):
                model = read(MODELS / f'block/plants/{crop}_stage_{stage}.json')
                versions.append(model)
                self.assertFalse(model['ambientocclusion'])
                for e in model['elements']:
                    self.assertTrue(all(4.5 <= e['from'][a] <= e['to'][a] <= 11.5 for a in (0, 2)))
                    self.assertGreaterEqual(e['from'][1], 5.0)
                    self.assertLessEqual(e['to'][1], 14.25)
                    if any(a == b for a, b in zip(e['from'], e['to'])):
                        self.assertEqual(len(e['faces']), 2)
                        self.assertFalse(e['shade'])
                        self.assertFalse(e['neoforge_data']['ambient_occlusion'])
                for kind in ('greenhouse', 'automatic_greenhouse'):
                    parts = read(ASSETS / f'blockstates/{kind}.json')['multipart']
                    matched = [p for p in parts if p.get('when', {}) == {'crop': crop, 'stage': str(stage)}]
                    self.assertEqual(len(matched), 1)
                    self.assertEqual(matched[0]['apply']['model'], f'greenhouse:block/plants/{crop}_stage_{stage}')
            self.assertEqual(len({json.dumps(v, sort_keys=True) for v in versions}), 4)
            if crop != 'glow_berries':
                heights = [max(e['to'][1] for e in v['elements']) for v in versions]
                self.assertEqual(heights, sorted(set(heights)))

    def test_glow_berries_hang_from_a_support_and_ripen_only_in_final_stage(self):
        bottoms = []
        for stage in range(4):
            model = read(MODELS / f'block/plants/glow_berries_stage_{stage}.json')
            vines = [e for e in model['elements'] if len(e['faces']) == 2]
            bottoms.append(min(e['from'][1] for e in vines))
            self.assertEqual(max(e['to'][1] for e in vines), 13.0)
            self.assertEqual(len(model['elements']) - len(vines), 3)
            self.assertEqual(model['textures']['tip'].endswith('_lit'), stage == 3)
        self.assertEqual(bottoms, sorted(set(bottoms), reverse=True))

    def test_sunflower_has_separate_front_and_back_flower_sprites(self):
        for stage in range(4):
            model = read(MODELS / f'block/plants/sunflower_stage_{stage}.json')
            self.assertEqual(model['textures']['front'], 'minecraft:block/sunflower_front')
            self.assertEqual(model['textures']['back'], 'minecraft:block/sunflower_back')
            head = model['elements'][-1]
            self.assertEqual(head['faces']['north']['texture'], '#front')
            self.assertEqual(head['faces']['south']['texture'], '#back')

    def test_all_resource_references_resolve(self):
        for path in ASSETS.rglob('*.json'):
            model = read(path)
            def visit(node, key=''):
                if isinstance(node, dict):
                    for k, v in node.items():
                        visit(v, k)
                elif isinstance(node, list):
                    for v in node:
                        visit(v, key)
                elif isinstance(node, str) and node.startswith('greenhouse:'):
                    relative = node.split(':', 1)[1]
                    folder = 'models' if key in ('model', 'parent') else 'textures'
                    suffix = '.json' if folder == 'models' else '.png'
                    if key not in ('type', 'item', 'id'):
                        self.assertTrue((ASSETS / folder / (relative + suffix)).is_file(), (path, key, node))
            visit(model)

    def test_geometry_stays_in_one_block(self):
        for path in MODELS.rglob('*.json'):
            for e in read(path).get('elements', []):
                self.assertNotIn('rotation', e, path)
                for lo, hi in zip(e['from'], e['to']):
                    self.assertTrue(0 <= lo <= hi <= 16, (path, e))

    def test_selection_edges_are_continuously_covered_by_opaque_frame(self):
        java = (ROOT / 'src/main/java/it/bonsaigreenhouse/GreenhouseBlock.java').read_text()
        numbers = re.search(r'SHAPE=Block.box\(([^)]+)\)', java).group(1)
        box = [float(n) for n in numbers.split(',')]
        for name in ('greenhouse', 'automatic_greenhouse'):
            opaque = [e for e in read(MODELS / f'block/{name}.json')['elements']
                      if all(f['texture'] != '#glass' for f in e['faces'].values())]
            for axis in range(3):
                other = [a for a in range(3) if a != axis]
                for side_a in (0, 3):
                    for side_b in (0, 3):
                        fixed = {other[0]: box[other[0]+side_a], other[1]: box[other[1]+side_b]}
                        intervals = sorted((e['from'][axis], e['to'][axis]) for e in opaque
                            if all(e['from'][a] <= value <= e['to'][a] for a, value in fixed.items()))
                        reached = box[axis]
                        for lo, hi in intervals:
                            if lo <= reached + 1e-7:
                                reached = max(reached, hi)
                        self.assertGreaterEqual(reached, box[axis+3], (name, axis, fixed, intervals))

    def test_glass_is_four_single_double_sided_planes(self):
        for name in ('greenhouse', 'automatic_greenhouse'):
            model = read(MODELS / f'block/{name}.json')
            panes = [e for e in model['elements'] if any(f['texture'] == '#glass' for f in e['faces'].values())]
            self.assertEqual(len(panes), 4)
            planes = set()
            for e in panes:
                axes = [i for i in range(3) if e['from'][i] == e['to'][i]]
                self.assertEqual(len(axes), 1)
                axis = axes[0]
                self.assertEqual(set(e['faces']), ({'west', 'east'}, {'down', 'up'}, {'north', 'south'})[axis])
                self.assertFalse(e['shade'])
                self.assertFalse(e['neoforge_data']['ambient_occlusion'])
                self.assertTrue(all('cullface' not in f for f in e['faces'].values()))
                self.assertNotIn((axis, e['from'][axis]), planes)
                planes.add((axis, e['from'][axis]))
            pixels = png_pixels(ASSETS / 'textures/block/glass.png')
            self.assertTrue(all(0 < p[3] < 255 for p in pixels))
            self.assertTrue(all(min(p[:3]) > 100 for p in pixels), 'No black glass border')

    def test_flat_roof_pot_and_distinct_automatic_base_remain(self):
        normal = read(MODELS / 'block/greenhouse.json')
        automatic = read(MODELS / 'block/automatic_greenhouse.json')
        self.assertEqual(len(normal['elements']), 24)
        self.assertEqual(len(automatic['elements']), 37)
        self.assertEqual(normal['elements'][5:11], automatic['elements'][5:11])
        self.assertNotEqual(normal['textures']['planks'], automatic['textures']['planks'])
        self.assertEqual(normal['elements'][23]['to'][1], 14.85)
        for path in (MODELS / 'block/plants').glob('*.json'):
            self.assertLessEqual(max(e['to'][1] for e in read(path)['elements']), 14.25, path)


if __name__ == '__main__':
    unittest.main(verbosity=2)
