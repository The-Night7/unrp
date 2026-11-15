#!/usr/bin/env python3
"""Regenerate the two original HUD texture layers and their geometry preview."""
from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).with_name('design_hud_textures.py')),run_name='__main__')
