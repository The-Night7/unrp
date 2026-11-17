"""Check maintained prose for untranslated French and keep client locales aligned."""
import csv
import io
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
SKIP = {'.git', '.gradle', 'build', 'run', 'logs', 'exports', '__pycache__', 'node_modules'}
EXTENSIONS = {'.java', '.md', '.py', '.json', '.toml', '.gradle', '.properties', '.txt', '.svg'}
# These are untranslated UI/documentation words, not names or persisted registry IDs.
FRENCH = re.compile(
    r'\b(?:aucun|aucune|joueur|pouvoir|niveau|puissance|vitesse|potentiel|'
    r'maintenir|retirer|refuser|appliquer|dissoudre|membres|enregistrer|'
    r'fichier|fichiers|données|génère|généré|capacité|capacités|dégâts|'
    r'utilitaires|personnage|personnages|rendu|couleur|boucle|inconnu|inconnue|quantique|ancien|'
    r'serveur|invalide|requis|envoyer|suivante|valider|groupe|territoire|' 
    r'récupération|étourdi|revendiquer|choisir)\b', re.IGNORECASE)
JAVA_TOKENS = re.compile(r'"(?:\\.|[^"\\])*"|//[^\n]*|/\*.*?\*/', re.DOTALL)


def check():
    errors = []
    for path in ROOT.rglob('*'):
        if not path.is_file() or path.suffix not in EXTENSIONS or any(part in SKIP for part in path.parts):
            continue
        if path == Path(__file__).resolve():
            continue
        text = path.read_text(errors='replace')
        if path.suffix == '.java':
            fragments = ((text.count('\n', 0, token.start()) + 1, token.group())
                         for token in JAVA_TOKENS.finditer(text))
        elif path.name == 'unodex-export.txt':
            fragments = []
            parts = re.split(r'^## Sheet name: (.+)$', text, flags=re.MULTILINE)
            for index in range(1, len(parts), 2):
                for row in csv.DictReader(io.StringIO(parts[index + 1].lstrip('\n'))):
                    # Personal names and source URLs are not translated.
                    fragments.extend((0, value or '') for key, value in row.items()
                                     if key not in {'Name', 'Reference', 'Graph Link', 'Image', None})
        else:
            fragments = enumerate(text.splitlines(), 1)
        for line, fragment in fragments:
            if FRENCH.search(fragment):
                errors.append(f'{path.relative_to(ROOT)}:{line}: {fragment.strip()[:140]}')
    for english in ROOT.rglob('en_us.json'):
        if any(part in SKIP for part in english.parts):
            continue
        fallback = english.with_name('fr_fr.json')
        if fallback.exists() and json.loads(english.read_text()) != json.loads(fallback.read_text()):
            errors.append(f'{fallback.relative_to(ROOT)} differs from the English interface')
    if errors:
        print('\n'.join(dict.fromkeys(errors)), file=sys.stderr)
        return 1
    print('English prose and locale checks passed.')
    return 0


if __name__ == '__main__':
    sys.exit(check())
