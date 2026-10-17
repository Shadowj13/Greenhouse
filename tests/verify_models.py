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
