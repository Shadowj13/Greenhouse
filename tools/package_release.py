"""Create a deterministic install bundle only from a passing packaged-JAR run."""
import argparse
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output-dir', type=Path, default=ROOT / 'build/distributions')
parser.add_argument('--report', type=Path, default=ROOT / 'build/qa-results/consolidation.json')
args = parser.parse_args()
props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines() if '=' in line)
version = props['mod_version']
jar = ROOT / f'build/libs/greenhouse-{version}.jar'
binary = jar.read_bytes()
digest = hashlib.sha256(binary).hexdigest()
receipt = json.loads(args.report.read_text())
assert receipt['mod_version'] == version and receipt['jar_sha256'] == digest, 'Stale server test receipt'
assert receipt['server_exit_codes'] == [0, 0], 'The two test servers must stop normally'
values = [v for phase in receipt['results'].values() for v in phase.values() if isinstance(v, str) and (v == 'PASS' or v.startswith('FAIL'))]
assert len(values) == 276 and all(v == 'PASS' for v in values), 'All 276 server checks must pass'
with ZipFile(jar) as archive:
    assert archive.testzip() is None
    assert not any('/qa/' in n or n.startswith(('tests/', 'tools/')) for n in archive.namelist())
    assert f'version="{version}"' in archive.read('META-INF/neoforge.mods.toml').decode()
manifest = {'mod':'Greenhouse', 'version':version, 'minecraft':props['minecraft_version'],
            'neoforge_minimum':props['neo_version'], 'java':25, 'jar':jar.name, 'jar_sha256':digest,
            'stage':'beta' if 'beta' in version else 'alpha', 'server_checks_passed':len(values),
            'source_repository':'https://github.com/Shadowj13/Greenhouse',
            'scope':'Packaged-JAR server checks. Other client/load evidence and limits are documented separately.'}
entries = {jar.name:binary, 'SHA256SUMS.txt':f'{digest}  {jar.name}\n'.encode(),
           'release-manifest.json':(json.dumps(manifest, indent=2)+'\n').encode()}
for name, source in [('INSTALLAZIONE.md', 'docs/INSTALLAZIONE.md'), ('USO.md','docs/USO.md'), ('CHANGELOG.md','CHANGELOG.md')]:
    content=(ROOT/source).read_text(encoding='utf-8').replace('\r\n','\n')
    if name=='INSTALLAZIONE.md': assert version in content, 'Stale installation guide'
    entries[name]=content.encode('utf-8')
args.output_dir.mkdir(parents=True, exist_ok=True)
target=args.output_dir/f'Greenhouse-{version}-Minecraft-{props["minecraft_version"]}-NeoForge.zip'
with ZipFile(target,'w') as archive:
    for name, data in sorted(entries.items()):
        info=ZipInfo(name, date_time=(1980,1,1,0,0,0)); info.compress_type=ZIP_DEFLATED
        info.create_system=3; info.external_attr=0o100644 << 16
        archive.writestr(info,data)
with ZipFile(target) as archive:
    assert archive.testzip() is None and set(archive.namelist())==set(entries)
    assert archive.read(jar.name)==binary
manifest_path=target.with_suffix('.json'); manifest_path.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
target.with_suffix(target.suffix+'.sha256').write_text(hashlib.sha256(target.read_bytes()).hexdigest()+'  '+target.name+'\n')
print(f'Validated install bundle: {target.name}; {len(values)} checks; JAR SHA256 {digest}')
