"""Run the packaged mod with a separate QA mod in an isolated local server.

Prepare with `gradlew prepareQaRuntime`; no client, launcher, user world or
account is accessed. The fixture server is confined to build/integration.
"""
import argparse
import hashlib
import json
import os
import re
from pathlib import Path
import shutil
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--java', default=str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java')
parser.add_argument('--jar', type=Path)
parser.add_argument('--report-name', default='consolidation')
scenarios = parser.add_mutually_exclusive_group()
scenarios.add_argument('--core-only', action='store_true')
scenarios.add_argument('--crops-only', action='store_true')
scenarios.add_argument('--checkpoint-only', action='store_true')
scenarios.add_argument('--reload-only', action='store_true')
args = parser.parse_args()
props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in line)
jar = (args.jar or ROOT / f'build/libs/greenhouse-{props["mod_version"]}.jar').resolve()
if not jar.is_file():
    parser.error(f'Mod JAR not found: {jar}')
run = ROOT / 'build/integration'
run.mkdir(parents=True, exist_ok=True)
# Each new suite starts with a clean fixture world. A reload-only migration
# deliberately reuses the checkpoint written by the previous server process.
world = (run / 'qa-world').resolve()
assert world.parent == run.resolve() and world.name == 'qa-world'
if not args.reload_only and world.exists():
    shutil.rmtree(world)
mods = run / 'mods'
mods.mkdir(exist_ok=True)
for p in mods.glob('greenhouse*.jar'):
    p.unlink()
shutil.copy2(jar, mods / 'greenhouse.jar')
shutil.copy2(ROOT / 'build/qa/greenhouse-qa.jar', mods / 'greenhouse-qa.jar')
(run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
(run / 'server.properties').write_text('\n'.join((
    'server-ip=127.0.0.1', 'server-port=0', 'level-name=qa-world',
    'level-type=minecraft:flat', 'pause-when-empty-seconds=0',
    'view-distance=3', 'simulation-distance=3', 'max-tick-time=120000',
    'spawn-protection=0', 'sync-chunk-writes=true',
))+'\n', encoding='utf-8')
classpath = (ROOT / 'build/qa/runtime-classpath.txt').read_text(encoding='utf-8')
jvm = ['-Xmx2G', '-Dfile.encoding=UTF-8', '--sun-misc-unsafe-memory-access=allow',
       '--enable-native-access=ALL-UNNAMED', '--add-opens', 'java.base/java.lang.invoke=ALL-UNNAMED',
       '--add-exports', 'jdk.naming.dns/com.sun.jndi.dns=java.naming', '-cp', classpath,
       'net.neoforged.fml.startup.Server', '--fml.neoForgeVersion', props['neo_version'],
       '--fml.mcVersion', props['minecraft_version'], '--fml.neoFormVersion', '1', '--nogui']
argfile = run / 'server.args'
argfile.write_text('\n'.join('"'+s.replace('\\', '/')+'"' for s in jvm), encoding='utf-8')
reports = ROOT / 'build/qa-results'
reports.mkdir(exist_ok=True)
all_results = {}
server_exit_codes = []


def launch(phases, index):
    log = reports / f'{args.report_name}-{index}.log'
    with log.open('w', encoding='utf-8') as output:
        proc = subprocess.Popen([args.java, '@'+str(argfile)], cwd=run, stdin=subprocess.PIPE,
                                stdout=output, stderr=subprocess.STDOUT, text=True, encoding='utf-8')
        def send(command):
            proc.stdin.write(command+'\n')
            proc.stdin.flush()
        def wait_for(find, timeout=180):
            deadline = time.monotonic()+timeout
            while time.monotonic() < deadline:
                text = log.read_text(encoding='utf-8', errors='replace')
                value = find(text)
                if value is not None:
                    return value
                if proc.poll() is not None:
                    raise RuntimeError(f'Server exited {proc.returncode}:\n'+text[-10000:])
                time.sleep(0.25)
            raise TimeoutError('Server test timeout:\n'+log.read_text(encoding='utf-8', errors='replace')[-10000:])
        try:
            wait_for(lambda t: True if 'Done (' in t else None)
            send('tick freeze')
            for phase in phases:
                if phase == 'unload':
                    send('tick rate 1000')
                    send('tick unfreeze')
                    time.sleep(4)
                    send('tick freeze')
                send('greenhouseqa '+phase)
                def result(text):
                    for line in text.splitlines():
                        if 'GREENHOUSE_QA_RESULT ' in line:
                            data = json.loads(line.split('GREENHOUSE_QA_RESULT ', 1)[1])
                            if data['phase'] == phase:
                                return data['results']
                    return None
                data = wait_for(result, 180)
                all_results[phase] = data
                print(phase, json.dumps(data, ensure_ascii=True), flush=True)
            send('save-all flush')
            wait_for(lambda t: True if 'Saved the game' in t else None)
        finally:
            if proc.poll() is None:
                send('stop')
                try:
                    proc.wait(timeout=45)
                except subprocess.TimeoutExpired:
                    proc.terminate()
                    proc.wait(timeout=10)
    if proc.returncode != 0:
        raise RuntimeError(f'Server shutdown failed: {proc.returncode}')
    server_exit_codes.append(proc.returncode)


if args.crops_only:
    launch(['crops', 'recipes'], 1)
elif args.checkpoint_only:
    launch(['checkpoint'], 1)
elif args.reload_only:
    launch(['reload'], 1)
else:
    launch(['core', 'benchmark'] if args.core_only else ['core', 'crops', 'recipes', 'checkpoint', 'benchmark', 'unload'], 1)
if not (args.core_only or args.crops_only or args.checkpoint_only or args.reload_only):
    launch(['reload'], 2)
from zipfile import ZipFile
with ZipFile(jar) as archive:
    artifact_version = re.search(r'^version="([^"]+)"', archive.read('META-INF/neoforge.mods.toml').decode('utf-8'), re.M).group(1)
target = reports / (args.report_name+'.json')
target.write_text(json.dumps({'minecraft': props['minecraft_version'], 'neoforge': props['neo_version'],
                             'mod_version': artifact_version, 'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
                             'server_exit_codes': server_exit_codes, 'results': all_results}, indent=2)+'\n', encoding='utf-8')
failed = {phase: {k: v for k, v in values.items() if isinstance(v,str) and v.startswith('FAIL')}
          for phase, values in all_results.items()}
failed = {k: v for k, v in failed.items() if v}
if failed:
    print('FAILED', json.dumps(failed), flush=True)
    sys.exit(1)
print('All live integration checks passed; server saved and stopped normally.', flush=True)
