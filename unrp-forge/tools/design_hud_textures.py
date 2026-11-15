#!/usr/bin/env python3
"""Redesign the original two 200x80 HUD PNG layers without changing their UV layout.
All coordinates are source texture pixels; requires Pillow. No AI artwork or resizing.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import base64
ROOT=Path(__file__).resolve().parents[1]
TARGET=ROOT/'src/main/resources/assets/unrp/textures/gui/hud'

def main():
    back=Image.new('RGBA',(200,80));front=Image.new('RGBA',(200,80))
    b=ImageDraw.Draw(back);f=ImageDraw.Draw(front)
    # Same portrait and meter-panel footprints as the original textures.
    for x1,y1,x2,y2 in ((10,15,61,67),(66,15,191,67)):
        shape=[(x1+2,y1),(x2-2,y1),(x2,y1+2),(x2,y2-2),(x2-2,y2),(x1+2,y2),(x1,y2-2),(x1,y1+2)]
        b.polygon(shape,fill='#1a2028')
        b.line((x1+2,y1,x2-2,y1),fill='#b39354')
        b.line((x1,y1+2,x1,y2-2),fill='#80683f')
        b.line((x1+2,y2,x2-2,y2),fill='#413b32')
        b.line((x2,y1+2,x2,y2-2),fill='#413b32')
        b.line((x1+2,y1+2,x2-2,y1+2),fill='#3b4148')
        b.line((x1+2,y1+3,x1+2,y2-3),fill='#303943')
        # Short corner caps use gold sparingly; no ornamental background or repeated waves.
        for x,y,dx,dy in ((x1+1,y1+1,1,1),(x2-1,y1+1,-1,1),(x1+1,y2-1,1,-1),(x2-1,y2-1,-1,-1)):
            for i in range(5):f.point((x+dx*i,y),fill='#d4b570')
            for i in range(3):f.point((x,y+dy*i),fill='#b39354')
            f.point((x,y),fill='#f0d99b')
    # Portrait background remains behind the original rotating, clipped 3D player head.
    b.rectangle((13,18,58,63),fill='#121820')
    b.line((14,64,58,64),fill='#62563e')
    b.line((14,64,26,64),fill='#b39354')
    # Original fill rectangles: global render coordinates minus the PNG origin (8,8).
    windows=[(69,20,188,29),(69,34,187,43),(69,45,187,47),(69,49,124,55),(131,49,188,55)]
    for x1,y1,x2,y2 in windows:
        b.rectangle((x1-1,y1-1,x2+1,y2+1),fill='#080d14')
        b.rectangle((x1,y1,x2,y2),fill='#252d36')
        # Foreground highlights stay strictly OUTSIDE the live fill areas.
        f.line((x1,y1-1,x2,y1-1),fill='#50545a')
        f.line((x1,y2+1,x2,y2+1),fill='#11151b')
    back.save(TARGET/'rpg_hud_back.png');front.save(TARGET/'rpg_hud_front.png')
    # Preview uses the real layers. Coloured rectangles indicate live render slots only.
    example=back.copy();d=ImageDraw.Draw(example)
    for box,c in [((69,20,164,29),'#b82626'),((69,34,157,43),'#68a7ac'),((69,49,118,55),'#bd8b45'),((131,49,164,55),'#9ba4b0')]:d.rectangle(box,fill=c)
    example.alpha_composite(front)
    enlarged=example.resize((1000,400),Image.Resampling.NEAREST)
    board=Image.new('RGBA',(1040,440),'#303840');board.alpha_composite(enlarged,(20,20));board.save(ROOT/'docs/unrp-hud-preview.png')
    payload=base64.b64encode((ROOT/'docs/unrp-hud-preview.png').read_bytes()).decode()
    (ROOT/'docs/unrp-hud-preview.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="1040" height="440"><image width="1040" height="440" href="data:image/png;base64,{payload}"/></svg>')
    print('Updated original rpg_hud_back.png and rpg_hud_front.png, 200x80 each.')

if __name__=='__main__':main()
