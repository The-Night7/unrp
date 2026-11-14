"""Regression checks for namespace remapping and preserving authored assets."""
import json
from pathlib import Path
import tempfile
import unittest

from import_model_assets import import_assets


class ImportAssetsTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.source = Path(self.tmp.name) / 'reference'
        self.target = Path(self.tmp.name) / 'unrp'
        self.source.mkdir()
        self.target.mkdir()

    def put(self, root, name, value):
        path = root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(value if isinstance(value, bytes) else json.dumps(value).encode())
        return path

    def test_models_and_sounds_are_remapped(self):
        self.put(self.source, 'models/item/tool.json', {
            'parent': 'reference:item/base', 'textures': {'layer0': 'reference:item/tool'}})
        self.put(self.source, 'sounds.json', {'reference.click': {'sounds': ['reference:ui/click']}})
        import_assets(self.source, self.target)
        model = json.loads((self.target / 'models/item/tool.json').read_text())
        self.assertEqual(model['parent'], 'unrp:item/base')
        self.assertEqual(model['textures']['layer0'], 'unrp:item/tool')
        self.assertNotIn('reference', (self.target / 'sounds.json').read_text())

    def test_mote_names_do_not_replace_custom_particles(self):
        original = self.put(self.target, 'textures/particle/mote_0.png', b'custom')
        self.put(self.source, 'textures/particle/mote_0.png', b'imported')
        self.put(self.source, 'particles/mote.json', {'textures': ['reference:mote_0']})
        self.put(self.source, 'palladium/particle.json', {'particle': 'reference:mote'})
        import_assets(self.source, self.target)
        self.assertEqual(original.read_bytes(), b'custom')
        self.assertEqual((self.target / 'textures/particle/aura_mote_0.png').read_bytes(), b'imported')
        self.assertEqual(json.loads((self.target / 'particles/aura_mote.json').read_text()),
                         {'textures': ['unrp:aura_mote_0']})
        self.assertEqual(json.loads((self.target / 'palladium/particle.json').read_text()),
                         {'particle': 'unrp:aura_mote'})

    def test_local_assets_and_translations_take_precedence(self):
        local = self.put(self.target, 'models/item/tool.json', {'parent': 'minecraft:item/generated'})
        self.put(self.source, 'models/item/tool.json', {'parent': 'reference:item/base'})
        self.put(self.source, 'lang/en_us.json', {'item.reference.tool': 'Imported', 'item.reference.new': 'New'})
        self.put(self.target, 'lang/en_us.json', {'item.unrp.tool': 'Custom'})
        import_assets(self.source, self.target)
        self.assertEqual(json.loads(local.read_text())['parent'], 'minecraft:item/generated')
        self.assertEqual(json.loads((self.target / 'lang/en_us.json').read_text()),
                         {'item.unrp.tool': 'Custom', 'item.unrp.new': 'New'})
        import_assets(self.source, self.target, overwrite=True)
        self.assertEqual(json.loads(local.read_text())['parent'], 'unrp:item/base')

    def test_overlapping_directories_are_rejected(self):
        with self.assertRaises(ValueError):
            import_assets(self.source, self.source / 'nested')


if __name__ == '__main__':
    unittest.main()
