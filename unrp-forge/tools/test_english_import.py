"""Guard the English input format and persisted ability IDs during catalog imports."""
import importlib.util
from pathlib import Path
import sys
import unittest

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('catalog_import', ROOT / 'tools/build_unodex.py')
catalog = importlib.util.module_from_spec(spec)
spec.loader.exec_module(catalog)


class EnglishImportTest(unittest.TestCase):
    def test_source_uses_the_documented_english_columns(self):
        rows = catalog.read_source_rows(ROOT / 'tools/input/unodex-export.txt')
        self.assertTrue(rows)
        self.assertTrue({'Name', 'Ability', 'School', 'Height', 'Rate', 'Source Sheet'} <= rows[0].keys())

    def test_translated_ability_names_keep_their_saved_ids(self):
        self.assertEqual('alchimie', catalog.stable_ability_id('Alchemy+'))
        self.assertEqual('covenant', catalog.stable_ability_id('Covenant'))

    def test_import_preserves_numeric_data_and_school_membership(self):
        warnings = []
        characters, _ = catalog.build(catalog.read_source_rows(ROOT / 'tools/input/unodex-export.txt'), warnings)
        arlo = next(person for person in characters if person['id'] == 'arlo')
        self.assertEqual('barrier', arlo['ability_id'])
        self.assertTrue(arlo['level'] >= 6)
        self.assertTrue(any(member['school'] == 'wellston' for member in arlo['schools']))
        self.assertTrue(any(snapshot['rate'] is not None for snapshot in arlo['snapshots']))


if __name__ == '__main__':
    unittest.main()
