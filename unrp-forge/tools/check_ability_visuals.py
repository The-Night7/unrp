#!/usr/bin/env python3
"""Check shipped animation and texture references for non-Origin ability definitions."""
import argparse
import csv
import json
import math
from pathlib import Path
import re
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets'
JAVA = ROOT / 'src/main/java/com/myriam/unrp'
CONTROL_MOVES = {'stance_next', 'mode_next'}


def png_size(path):
    data = path.read_bytes()
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError(f'{path}: invalid PNG signature')
    offset, size, compressed, ended = 8, None, bytearray(), False
    while offset < len(data):
        if len(data) - offset < 12:
            raise ValueError(f'{path}: truncated PNG chunk')
        length = struct.unpack('>I', data[offset:offset + 4])[0]
        kind = data[offset + 4:offset + 8]
        payload = data[offset + 8:offset + 8 + length]
        crc = data[offset + 8 + length:offset + 12 + length]
        if len(payload) != length or len(crc) != 4 or zlib.crc32(kind + payload) != struct.unpack('>I', crc)[0]:
            raise ValueError(f'{path}: damaged {kind!r} chunk')
        if kind == b'IHDR':
            size = struct.unpack('>II', payload[:8])
        if kind == b'IDAT':
            compressed.extend(payload)
        offset += length + 12
        if kind == b'IEND':
            ended = True
            break
    if not ended or size is None or min(size) <= 0 or not compressed:
        raise ValueError(f'{path}: incomplete PNG')
    decoder = zlib.decompressobj()
    decoder.decompress(compressed)
    if not decoder.eof:
        raise ValueError(f'{path}: truncated image data')
    return size


def validate_pose(name, clip):
    if clip.get('len', 12) <= 0 or not clip.get('keys'):
        raise ValueError(f'{name}: empty animation')
    times = [key['t'] for key in clip['keys']]
    if times != sorted(times) or times[0] < 0 or times[-1] > clip.get('len', 12):
        raise ValueError(f'{name}: invalid keyframe times')
    animated = False
    for key in clip['keys']:
        for part in ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg'):
            if part in key:
                values = key[part]
                if not values or not all(isinstance(v, (int, float)) and math.isfinite(v) for v in values):
                    raise ValueError(f'{name}: invalid {part} rotation')
                animated |= any(v != 0 for v in values)
    if not animated:
        raise ValueError(f'{name}: animation has no pose')


def audit():
    data = json.loads((ROOT / 'src/main/resources/data/unrp/unodex/ability_powers.json').read_text())
    excluded_source = (JAVA / 'data/UnodexData.java').read_text().split('NOT_ROLLABLE =', 1)[1].split(';', 1)[0]
    excluded = set(re.findall(r'"([a-z_]+)"', excluded_source))
    # IDs and clips must agree: the network sends an index into both tables.
    ids_source = (JAVA / 'anim/MoveClipIds.java').read_text()
    ids = re.findall(r'"([^"]+)"', ids_source.split('KEYS =', 1)[1].split(';', 1)[0])
    clips = re.findall(r'new MoveClips\.Clip\("([^"]+)"', (JAVA / 'anim/MoveClips.java').read_text())
    if ids != clips:
        raise ValueError('Animation IDs and model clips disagree')
    for table in ('LENGTH', 'LOOPS', 'LOOP_END', 'RETRIGGER'):
        entries = ids_source.split(table + ' =', 1)[1].split('{', 1)[1].split('}', 1)[0].split(',')
        if len(entries) != len(ids):
            raise ValueError(f'Animation metadata {table} has the wrong length')
    legacy = json.loads((ASSETS / 'unrp/poses.json').read_text())
    shared = json.loads((ROOT / 'tools/overrides/shared_visual_icons.json').read_text())['moves']
    sources = '\n'.join(p.read_text() for p in (JAVA / 'ability').glob('*.java'))
    sizes = {}

    def texture(resource):
        namespace, path = resource.split(':', 1)
        file = ASSETS / namespace / path
        if not file.is_file():
            raise ValueError(f'Missing texture: {resource}')
        if resource not in sizes:
            sizes[resource] = png_size(file)
        return sizes[resource]

    rows = []
    for ability, power in data['powers'].items():
        if ability in excluded:
            continue
        texture(power['icon'])
        for index, move in enumerate(power.get('moves', [])):
            poses = []
            for field in ('pose', 'poseSneak'):
                name = move.get(field)
                if not name:
                    if field == 'pose' and move.get('special') not in CONTROL_MOVES:
                        raise ValueError(f'{ability}/{index}: missing animation')
                    continue
                if name in ids:
                    poses.append('keyframed:' + name)
                elif name in legacy:
                    validate_pose(name, legacy[name])
                    poses.append('pose:' + name)
                else:
                    raise ValueError(f'{ability}/{index}: unresolved {field} {name}')
            if texture(move['icon']) != (16, 16):
                raise ValueError(f'{ability}/{index}: icon must be 16 × 16')
            special = move.get('special')
            if special and f'"{special}"' not in sources:
                raise ValueError(f'{ability}/{index}: cannot locate handler {special}')
            rows.append({'ability': ability, 'move': index + 1, 'name': move['name'],
                         'animation': ', '.join(poses) or 'control only', 'icon': move['icon'],
                         'shared_icon': f'{ability}:{index}' in shared,
                         'effect_path': special or move.get('fx', {}).get('kind') or 'tags / attributes / passive',
                         'in_game_review': 'Not checked'})

    # Shared effects use atlas sprites; particle JSON paths are relative to textures/particle/.
    for file in (ASSETS / 'unrp/particles').glob('*.json'):
        for sprite in json.loads(file.read_text()).get('textures', []):
            namespace, name = sprite.split(':', 1)
            if namespace == 'unrp':
                texture(f'{namespace}:textures/particle/{name}.png')
    # Check literal asset references used by renderers, including entity models and animations.
    literals = set()
    for file in JAVA.rglob('*.java'):
        for resource in re.findall(r'"((?:textures|geo|animations)/[A-Za-z0-9_/.-]+\.(?:png|json))"', file.read_text()):
            literals.add(resource)
    for resource in literals:
        file = ASSETS / 'unrp' / resource
        if not file.is_file():
            raise ValueError(f'Missing renderer resource: {resource}')
        if resource.endswith('.png'):
            texture('unrp:' + resource)
        else:
            json.loads(file.read_text())
    mixins = json.loads((ROOT / 'src/main/resources/unrp.mixins.json').read_text())
    if 'PlayerModelMixin' not in mixins.get('client', []):
        raise ValueError('Player animation mixin is not registered')
    model = (JAVA / 'mixin/PlayerModelMixin.java').read_text()
    for renderer in ('PoseClient', 'RosterPoses', 'MoveAnimClient'):
        if renderer + '.apply' not in model:
            raise ValueError(f'{renderer} is not connected to the player model')
    return rows, len(sizes)


def write_report(rows, textures):
    report = ROOT / 'docs/generated/ABILITY_VISUALS.md'
    abilities = sorted({row['ability'] for row in rows})
    text = f'''# Ability visual coverage

This is a source and asset audit of **{len(abilities)} non-Origin power definitions** and **{len(rows)} moves**. It checks animation lookup, player-model hooks, icons, renderer resource paths and PNG integrity. Run `python tools/check_ability_visuals.py --write-report` from `unrp-forge/` to refresh it.

All moves resolve an animation or are explicit stance controls. All move icons resolve to 16 × 16 PNGs. The audit read {textures} referenced textures. Model clip indices agree with the network ID table. Special-handler names are located in the server sources; this does not establish that every handler produces a visible effect in every situation.

## Shared artwork

{sum(row['shared_icon'] for row in rows)} move icons reuse existing effect glyphs. The added legacy pose IDs also reuse existing gestures. These are shared visuals, not newly drawn, ability-specific artwork. Generic power definitions still use the common effect engine.

## In-game review

No ability is marked visually approved by this audit. Verify the first- and third-person pose, held/toggled/released states, target effects, visibility to another player, resource reload and interaction with clothing in Minecraft. A valid asset reference does not prove that a motion looks good or that an event fires at the right time.

The [per-move checklist](ability-visual-checklist.csv) records the exact animation, icon and effect path. Its review column starts as `Not checked`; do not treat it as a playtest result.

| Power ID | Moves | Shared move icons |
| --- | ---: | ---: |
'''
    for ability in abilities:
        group = [row for row in rows if row['ability'] == ability]
        text += f"| `{ability}` | {len(group)} | {sum(row['shared_icon'] for row in group)} |\n"
    report.write_text(text)
    with (report.parent / 'ability-visual-checklist.csv').open('w', newline='') as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write-report', action='store_true')
    args = parser.parse_args()
    result, textures = audit()
    if args.write_report:
        write_report(result, textures)
    print(f'Ability visuals: {len({row["ability"] for row in result})} powers, {len(result)} moves, {textures} textures; references passed. In-game appearance is not certified.')
