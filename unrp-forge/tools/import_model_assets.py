#!/usr/bin/env python3
"""Import an extracted reference assets directory into the UNRP namespace.

Usage: python3 tools/import_model_assets.py /path/to/assets/source_namespace
Existing UNRP assets take precedence. Use --overwrite to refresh imported files.
Translations are merged, with existing UNRP entries taking precedence.
"""
import argparse
import json
from pathlib import Path
import re

PROJECT = Path(__file__).resolve().parent.parent


def rewrite(text, namespace):
    # Keep the two mote particle implementations distinct.
    for old, new in [
        (f'{namespace}:textures/particle/mote_', 'unrp:textures/particle/aura_mote_'),
        (f'{namespace}:particle/mote_', 'unrp:particle/aura_mote_'),
    ]:
        text = text.replace(old, new)
    text = re.sub(r'"' + re.escape(namespace) + r':mote_([012])"', r'"unrp:aura_mote_\1"', text)
    text = text.replace(f'"{namespace}:mote"', '"unrp:aura_mote"')
    text = text.replace(namespace, 'unrp')
    brand = re.escape(namespace).replace('_', '_?')
    return re.sub(brand, 'UNRP', text, flags=re.IGNORECASE)


def import_assets(source, destination, overwrite=False):
    source, destination = source.resolve(), destination.resolve()
    if not source.is_dir():
        raise ValueError(f'Assets directory does not exist: {source}')
    if source == destination or source in destination.parents or destination in source.parents:
        raise ValueError('Source and destination must be separate directories')
    namespace = source.name
    copied = 0
    for file in sorted(source.rglob('*')):
        if not file.is_file() or 'kubejs_scripts' in file.relative_to(source).parts:
            continue
        relative = file.relative_to(source)
        name = relative.as_posix().replace(namespace, 'unrp')
        if name == 'particles/mote.json':
            name = 'particles/aura_mote.json'
        name = re.sub(r'(?<=textures/particle/)mote_([012])(?=\.png(?:\.mcmeta)?$)', r'aura_mote_\1', name)
        target = destination / name
        payload = file.read_bytes()
        if file.suffix in {'.json', '.mcmeta', '.vsh', '.fsh', '.glsl', '.txt'}:
            payload = rewrite(payload.decode('utf-8'), namespace).encode('utf-8')
        if relative.parts[0] == 'lang' and target.exists():
            incoming = json.loads(payload)
            current = json.loads(target.read_text())
            payload = (json.dumps(incoming | current, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
        elif target.exists() and not overwrite:
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        copied += 1
    return copied


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', type=Path, help='Extracted assets namespace directory')
    parser.add_argument('--overwrite', action='store_true', help='Replace existing assets, except local translations')
    args = parser.parse_args()
    print('UNRP assets imported:', import_assets(args.source, PROJECT / 'src/main/resources/assets/unrp', args.overwrite))
