"""Failure checks for the non-Origin visual audit; no game or graphics context required."""
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import check_ability_visuals as visuals


class AbilityVisualAuditTest(unittest.TestCase):
    def test_missing_pose_is_reported_instead_of_silently_ignored(self):
        read = Path.read_text

        def without_point(path, *args, **kwargs):
            text = read(path, *args, **kwargs)
            if path == visuals.ASSETS / 'unrp/poses.json':
                data = json.loads(text)
                del data['og_point']
                return json.dumps(data)
            return text

        with patch.object(Path, 'read_text', without_point):
            with self.assertRaisesRegex(ValueError, 'unresolved pose og_point'):
                visuals.audit()

    def test_missing_icon_is_reported(self):
        is_file = Path.is_file

        def missing_icon(path):
            if path.name == 'energy_discharge_long_beam.png':
                return False
            return is_file(path)

        with patch.object(Path, 'is_file', missing_icon):
            with self.assertRaisesRegex(ValueError, 'Missing texture'):
                visuals.audit()

    def test_damaged_png_is_rejected(self):
        source = visuals.ASSETS / 'unrp/textures/icons/healing_activation.png'
        content = bytearray(source.read_bytes())
        content[29] ^= 1  # IHDR CRC, leaving the header and dimensions readable.
        with tempfile.TemporaryDirectory() as directory:
            damaged = Path(directory) / 'damaged.png'
            damaged.write_bytes(content)
            with self.assertRaisesRegex(ValueError, 'damaged'):
                visuals.png_size(damaged)

    def test_empty_and_nonfinite_animations_are_rejected(self):
        for clip in ({'len': 10, 'keys': []},
                     {'len': 10, 'keys': [{'t': 0, 'right_arm': [float('nan'), 0, 0]}]},
                     {'len': 10, 'keys': [{'t': 12, 'right_arm': [90, 0, 0]}]}):
            with self.assertRaises(ValueError):
                visuals.validate_pose('broken', clip)

    def test_origin_definitions_stay_outside_the_report(self):
        rows, _ = visuals.audit()
        powers = {row['ability'] for row in rows}
        self.assertNotIn('covenant', powers)
        self.assertNotIn('counterweight', powers)
        self.assertIn('barrier', powers)


if __name__ == '__main__':
    unittest.main()
