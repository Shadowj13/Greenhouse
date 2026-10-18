"""Check an installable JAR, optionally proving preserved baseline bytecode."""
import argparse
import hashlib
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('jar', type=Path)
parser.add_argument('baseline', type=Path, nargs='?')
parser.add_argument('--all-classes', action='store_true', help='Require all production classes to match the baseline')
args = parser.parse_args()
jar_path = args.jar
props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines() if '=' in line)
with ZipFile(jar_path) as jar:
    assert jar.testzip() is None, 'Corrupt ZIP entry'
    names = set(jar.namelist())
    assert not any(n.startswith(('tests/', 'tools/', 'run/')) for n in names)
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if not path.is_file():
            continue
        name = path.relative_to(ROOT / 'src/main/resources').as_posix()
        expected = path.read_bytes()
        actual = jar.read(name)
        if path.suffix == '.toml':
            text = expected.decode()
            for key, value in props.items():
                text = text.replace('${' + key + '}', value)
            expected = text.encode()
        # Git checkouts may use CRLF; JSON/TOML line endings are immaterial.
        if path.suffix in ('.json', '.toml'):
            actual, expected = actual.replace(b'\r\n', b'\n'), expected.replace(b'\r\n', b'\n')
        assert actual == expected, f'Stale or missing asset: {name}'
    classes = [n for n in names if n.endswith('.class')]
    assert len(classes) == 12, classes
    for name in classes:
        code = jar.read(name)
        assert code[:4] == b'\xca\xfe\xba\xbe'
        assert int.from_bytes(code[6:8], 'big') == 69, 'Target must be Java 25'
    toml = jar.read('META-INF/neoforge.mods.toml').decode()
    assert '${' not in toml
    assert f'version="{props["mod_version"]}"' in toml
    assert f'versionRange="{props["minecraft_version_range"]}"' in toml
    if args.baseline:
        with ZipFile(args.baseline) as baseline:
            unchanged = classes if args.all_classes else [n for n in classes if '/GreenhouseBlock' not in n and not n.endswith('/Crop.class')]
            for name in unchanged:
                assert jar.read(name) == baseline.read(name), f'Unexpected production bytecode change: {name}'
            print(f'{len(unchanged)} production classes identical to baseline')
print(f'JAR integrity, metadata, {len(classes)} Java 25 classes and all packaged resources verified')
print('SHA256', hashlib.sha256(jar_path.read_bytes()).hexdigest())
