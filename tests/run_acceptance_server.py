"""Isolated loopback server for full-client checks or a natural-tick load run.

Prepare with gradlew prepareQaRuntime. No launcher account or user world is used.
The local test server accepts the Minecraft EULA and uses offline test profiles.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time
import uuid

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java', default=str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java')
parser.add_argument('--port', type=int, default=25576)
parser.add_argument('--soak-only', action='store_true')
args = parser.parse_args()
props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines() if '=' in line)
jar = ROOT / f'build/libs/greenhouse-{props["mod_version"]}.jar'
run = ROOT / 'build/acceptance'
run.mkdir(parents=True, exist_ok=True)
(run / 'mods').mkdir(exist_ok=True)
shutil.copy2(jar, run / 'mods/greenhouse.jar')
shutil.copy2(ROOT / 'build/qa/greenhouse-qa.jar', run / 'mods/greenhouse-qa.jar')
(run / 'eula.txt').write_text('eula=true\n')
(run / 'server.properties').write_text('\n'.join([
    'server-ip=127.0.0.1', f'server-port={args.port}', 'level-name=acceptance-'+uuid.uuid4().hex[:8],
    'level-type=minecraft:flat', 'pause-when-empty-seconds=0', 'online-mode=false',
    'enforce-secure-profile=false', 'white-list=false', 'enforce-whitelist=false',
    'view-distance=3', 'simulation-distance=3', 'max-tick-time=120000',
    'spawn-protection=0', 'allow-flight=true', 'sync-chunk-writes=true'])+'\n')
classpath = (ROOT / 'build/qa/runtime-classpath.txt').read_text()
jvm = ['-Xmx1536M', '-Dfile.encoding=UTF-8', '-Dgreenhouse.qa.skipLookalikes=true', '--sun-misc-unsafe-memory-access=allow',
       '--enable-native-access=ALL-UNNAMED', '--add-opens', 'java.base/java.lang.invoke=ALL-UNNAMED',
       '--add-exports', 'jdk.naming.dns/com.sun.jndi.dns=java.naming', '-cp', classpath,
       'net.neoforged.fml.startup.Server', '--fml.neoForgeVersion', props['neo_version'],
       '--fml.mcVersion', props['minecraft_version'], '--fml.neoFormVersion', '1', '--nogui']
argfile = run / 'server.args'
argfile.write_text('\n'.join('"'+s.replace('\\', '/')+'"' for s in jvm))
control = run / 'control.jsonl'
control.write_text('')
reports = ROOT / 'build/qa-results'
reports.mkdir(exist_ok=True)
log = reports / 'acceptance-server.log'
report = reports / 'acceptance.json'
snapshots, actions = {}, []
metadata = {'minecraft': props['minecraft_version'], 'neoforge': props['neo_version'],
            'mod_version': props['mod_version'], 'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest()}
with log.open('w', encoding='utf-8') as output:
    proc = subprocess.Popen([args.java, '@'+str(argfile)], cwd=run, stdin=subprocess.PIPE,
                            stdout=output, stderr=subprocess.STDOUT, text=True, encoding='utf-8')
    start = time.monotonic()
    seen, ready, ready_at, final_sent, stop_sent = 0, False, None, False, False
    def send(command):
        proc.stdin.write(command+'\n')
        proc.stdin.flush()
    try:
        while proc.poll() is None and time.monotonic()-start < 1800:
            text = log.read_text(encoding='utf-8', errors='replace')
            if not ready and 'Done (' in text:
                ready, ready_at = True, time.monotonic()
                send('tick freeze')
                send('time set noon')
                if args.soak_only:
                    send('greenhouseqa soaksetup')
                    send('tick unfreeze')
                print('Ready. Console commands as JSONL {"command":"..."}: '+str(control), flush=True)
            lines = control.read_text().splitlines()
            for line in lines[seen:]:
                command = json.loads(line)
                send(command['command'])
                actions.append(command)
            seen = len(lines)
            for line in text.splitlines():
                if 'GREENHOUSE_QA_RESULT ' in line:
                    data = json.loads(line.split('GREENHOUSE_QA_RESULT ', 1)[1])
                    snapshots[data['phase']] = data['results']
            report.write_text(json.dumps(dict(metadata, results=snapshots, actions=actions), indent=2)+'\n')
            if args.soak_only and ready_at is not None and not final_sent and time.monotonic()-ready_at >= 320:
                send('tick freeze')
                send('greenhouseqa soakassert')
                final_sent = True
            if args.soak_only and 'soakassert' in snapshots and not stop_sent:
                send('save-all flush')
                send('stop')
                stop_sent = True
            time.sleep(0.5)
    finally:
        if proc.poll() is None:
            send('stop')
            try:
                proc.wait(timeout=45)
            except subprocess.TimeoutExpired:
                proc.terminate()
                proc.wait(timeout=10)
report.write_text(json.dumps(dict(metadata, results=snapshots, actions=actions, server_exit_code=proc.returncode), indent=2)+'\n')
failed = any(isinstance(value, str) and value.startswith('FAIL') for phase in snapshots.values() for value in phase.values())
if not ready or proc.returncode != 0 or failed or (args.soak_only and 'soakassert' not in snapshots):
    sys.exit('Acceptance checks failed or did not complete; see '+str(report))
print('Server saved and stopped normally. Results: '+str(report))
