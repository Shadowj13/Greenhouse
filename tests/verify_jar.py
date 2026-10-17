"""Check an installable JAR, optionally proving preserved UI/crop bytecode."""
import hashlib
import sys
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
jar_path = Path(sys.argv[1])
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
    if len(sys.argv) > 2:
        with ZipFile(sys.argv[2]) as baseline:
            unchanged = [n for n in classes if '/GreenhouseBlock' not in n]
            for name in unchanged:
                assert jar.read(name) == baseline.read(name), f'Unexpected mechanics/UI bytecode change: {name}'
            print(f'{len(unchanged)} GUI, menu, crop and registry classes identical to baseline')
print(f'JAR integrity, metadata, {len(classes)} Java 25 classes and all packaged resources verified')
print('SHA256', hashlib.sha256(jar_path.read_bytes()).hexdigest())
