#!/usr/bin/env python3
"""Export the English Covenant module, definition excerpts and integration map."""
from pathlib import Path
import hashlib
import json
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT / 'unrp-forge'
JAVA = PROJECT / 'src/main/java/com/myriam/unrp'
OUTPUT = ROOT / 'exports'
PREFIX = 'Covenant/'


def export():
    selected = set()
    def add(path):
        if path.is_file():
            selected.add(path)
    for path in JAVA.rglob('Covenant*.java'):
        add(path)
    for relative in ('ability/Specials17.java', 'home/HomeCommands.java', 'client/UiSkin.java'):
        add(JAVA / relative)
    for path in (PROJECT / 'src/test').rglob('*Covenant*.java'):
        add(path)
    assets = PROJECT / 'src/main/resources/assets/unrp'
    for pattern in ('textures/covenant/*.png', 'textures/icons/covenant_*.png'):
        for path in assets.glob(pattern):
            add(path)
    for relative in ('textures/misc/white.png', 'textures/gui/aura_back.png',
                     'textures/gui/aura_front.png', 'textures/gui/stars.png'):
        add(assets / relative)
    for path in (PROJECT / 'docs').glob('*COVENANT*'):
        add(path)
    for relative in ('docs/fidelity/covenant.md', 'docs/covenant-visuals.png',
                     'tools/design_covenant.py', 'tools/make_covenant_icons.py', 'tools/export_covenant.py'):
        add(PROJECT / relative)
    payload = {path.relative_to(ROOT).as_posix(): path.read_bytes() for path in sorted(selected)}
    guide = (PROJECT / 'docs/COVENANT_CODE_EXPLAINED.txt').read_bytes()
    payload['COVENANT_CODE_EXPLAINED.txt'] = guide
    data = PROJECT / 'src/main/resources/data/unrp/unodex'
    power = json.loads((data / 'ability_powers.json').read_text())
    abilities = json.loads((data / 'abilities.json').read_text())
    characters = []
    for path in sorted((data / 'characters').glob('*.json')):
        characters.extend(c for c in json.loads(path.read_text()).get('characters', []) if c.get('ability_id') == 'covenant')
    definitions = {
        'note': 'Covenant-only reference excerpts. Merge into the full project definitions; do not replace shared files with these subsets.',
        'power': power['powers']['covenant'],
        'ability': next(a for a in abilities['abilities'] if a['id'] == 'covenant'),
        'characters': characters,
        'stat_ranges': json.loads((data / 'stat_ranges.json').read_text())['ranges']['covenant'],
        'pool_weight': json.loads((data / 'ability_pool.json').read_text())['pool'].get('covenant', 0),
    }
    def encoded(value):
        return (json.dumps(value, ensure_ascii=False, indent=2) + '\n').encode()
    payload['DEFINITIONS_COVENANT.json'] = encoded(definitions)
    excerpts = {
        'ability_powers.covenant.json': {'powers': {'covenant': definitions['power']}},
        'abilities.covenant.json': {'abilities': [definitions['ability']]},
        'characters.covenant.json': {'characters': characters},
        'stat_ranges.covenant.json': {'ranges': {'covenant': definitions['stat_ranges']}},
        'ability_pool.covenant.json': {'pool': {'covenant': definitions['pool_weight']}},
    }
    for name, excerpt in excerpts.items():
        payload['definitions/' + name] = encoded({'_export_note': definitions['note'], **excerpt})
    references = {
        'UnrpMod.java': 'Registers common events and server/client components.',
        'ability/AbilityEngine.java': 'Input, aura, effective stats, movement permissions and borrowed-power integration.',
        'ability/AbilityEvents.java': 'Forge events, server startup and shutdown.',
        'ability/Specials.java': 'Special-move routing and server tick.',
        'ability/Superpowers.java': 'Power ownership and held-ability integration.',
        'command/UnrpCommands.java': 'Registers covenantWritingCommands and routes validated writing actions.',
        'net/Net.java': 'Registers CovenantPacket on the mod channel.',
        'client/ClientEvents.java': 'Adds CovenantSignatureLayer to player rendering.',
        'look/AbilityBar.java': 'Common ability bar and Covenant stance display.',
        'player/PlayerStateService.java': 'Shared effective stats and player state synchronization.',
        'data/UnodexData.java': 'Ability definitions, aliases and assignment-only exclusions.',
        'deal/ContractItem.java': 'Physical Pact documents, NBT signatures and copy detection.',
        'deal/PactLedger.java': 'Authoritative signed Pact records.',
        'item/ModItems.java': 'Physical contract item registration.',
        'item/UnItems.java': 'Persistent player NBT access used by the home commands.',
        'social/SocialData.java': 'Registered territory data used for Jurisdiction.',
        'social/SocialService.java': 'Shared player and ability-tree data delivery.',
    }
    lines = [
        'COVENANT — SHARED INTEGRATION POINTS',
        '===================================', '',
        'These files belong to the full UNRP project. They are dependencies, not',
        'standalone Covenant source files. Use their listed call sites when integrating',
        'the module. The archive contains the complete Covenant-specific Java files.', '',
    ]
    for relative, description in references.items():
        path = JAVA / relative
        lines.extend([relative, '  ' + description])
        if path.exists():
            for number, line in enumerate(path.read_text().splitlines(), 1):
                code = line.split('//', 1)[0].strip()
                if (('Specials17.' in code or 'CovenantPacket' in code or 'CovenantSignatureLayer' in code)
                        and not code.startswith(('*', '//')) and 'bar(' not in code
                        and not any('\u00c0' <= char <= '\u017f' for char in code)):
                    lines.append(f'  Line {number}: {code}')
        lines.append('')
    payload['INTEGRATION_POINTS.txt'] = ('\n'.join(lines) + '\n').encode()
    commit = subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=ROOT, capture_output=True, text=True, check=True).stdout.strip()
    manifest = {
        'scope': 'English Covenant-specific sources, native assets, tests, definition excerpts and a shared-system integration map; not a standalone mod',
        'git_head_for_reference': commit,
        'source': 'Current workspace sources. Definition excerpts and integration notes are explicitly generated reference material.',
        'files': [{'path': name, 'bytes': len(content), 'sha256': hashlib.sha256(content).hexdigest()}
                  for name, content in sorted(payload.items())],
    }
    payload['MANIFEST.json'] = encoded(manifest)
    OUTPUT.mkdir(exist_ok=True)
    archive = OUTPUT / 'covenant-code.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zip_file:
        for name, content in sorted(payload.items()):
            zip_file.writestr(PREFIX + name, content)
    (OUTPUT / 'COVENANT_CODE_EXPLAINED.txt').write_bytes(guide)
    digest = hashlib.sha256(archive.read_bytes()).hexdigest()
    (OUTPUT / 'covenant-code.zip.sha256').write_text(digest + '  covenant-code.zip\n')
    with zipfile.ZipFile(archive) as zip_file:
        assert zip_file.testzip() is None
        assert len(zip_file.namelist()) == len(set(zip_file.namelist()))
        for name, content in payload.items():
            assert not name.startswith('/') and '..' not in Path(name).parts
            assert zip_file.read(PREFIX + name) == content
        for item in manifest['files']:
            assert hashlib.sha256(zip_file.read(PREFIX + item['path'])).hexdigest() == item['sha256']
    print(json.dumps({'zip': str(archive), 'txt': str(OUTPUT / 'COVENANT_CODE_EXPLAINED.txt'),
                      'files': len(payload), 'zip_bytes': archive.stat().st_size, 'verified': True}))


if __name__ == '__main__':
    export()
