#!/usr/bin/env python3
"""Native Covenant pixel textures used by the actual world, hand and parchment renderers."""
from pathlib import Path
from PIL import Image,ImageDraw
import math,importlib.util,sys
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'src/main/resources/assets/unrp/textures/covenant';OUT.mkdir(parents=True,exist_ok=True)
GOLD='#d4aa53';LIGHT='#f7df97';DARK='#806035';INK='#392c23'
def crown(d,x,y,s=1):
    d.line([(x,y+4*s),(x,y),(x+3*s,y+2*s),(x+5*s,y-s),(x+7*s,y+2*s),(x+10*s,y),(x+10*s,y+4*s),(x,y+4*s)],fill=LIGHT,width=s)
    d.line((x+s,y+6*s,x+9*s,y+6*s),fill=GOLD,width=s)
# Court seal is a deliberately stepped 64px impression, not a smooth illustration.
im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
for r,c in [(30,GOLD),(27,DARK),(23,GOLD)]:
    pts=[]
    for i in range(96):
        a=i*math.tau/96;pts.append((round(31.5+math.cos(a)*r),round(31.5+math.sin(a)*r)))
    d.line(pts+[pts[0]],fill=c,width=1)
for i in range(12):
    a=i*math.tau/12;x=round(31.5+math.cos(a)*25);y=round(31.5+math.sin(a)*25)
    d.rectangle((x-1,y-1,x+1,y+1),fill=LIGHT)
    x2=round(31.5+math.cos(a)*20);y2=round(31.5+math.sin(a)*20)
    d.line((x2,y2,round(31.5+math.cos(a)*17),round(31.5+math.sin(a)*17)),fill=GOLD)
crown(d,22,24,2)
d.line((32,38,32,48),fill=GOLD,width=2);d.line((25,44,39,44),fill=LIGHT)
d.line((22,41,32,50,42,41),fill=DARK)
im.save(OUT/'court_seal.png')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);crown(d,3,3)
d.line((8,9,8,14),fill=GOLD);d.line((5,12,11,12),fill=LIGHT)
d.point((2,12),fill=DARK);d.point((13,12),fill=DARK);im.save(OUT/'signature.png')
im=Image.new('RGBA',(32,8));d=ImageDraw.Draw(im)
for x in range(2,30,6):
 d.line((x,2,x,6),fill=GOLD);d.line((x,2,x+3,4,x,5),fill=LIGHT)
d.line((1,7,30,7),fill=DARK);im.save(OUT/'constitution_ink.png')
im=Image.new('RGBA',(32,48));d=ImageDraw.Draw(im)
d.polygon([(3,1),(27,1),(30,4),(30,43),(27,46),(3,46),(1,43),(1,5)],fill='#b6a17b')
d.polygon([(4,2),(26,2),(29,5),(29,42),(26,45),(4,45),(2,42),(2,5)],fill='#e0cfaa')
d.rectangle((4,5,27,41),fill='#eadbb9');d.line((4,3,26,3),fill='#f6eaca');d.line((3,5,3,42),fill='#f1e2bd')
d.line((5,44,26,44),fill='#9d8058');d.line((28,6,28,41),fill='#c0a37c')
# Empty writing area. Real server law text is rendered here, never baked into an icon.
d.rectangle((25,39,28,42),fill='#8e4240');d.point((26,39),fill='#c47b60');d.point((26,41),fill='#e3bb76')
im.save(OUT/'parchment.png')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
d.polygon([(8,1),(13,3),(12,9),(8,14),(4,9),(3,3)],outline=GOLD)
d.line((6,7,8,9,11,5),fill=LIGHT,width=1);d.line((4,13,8,15,12,13),fill=DARK);im.save(OUT/'indemnity.png')
# Keep the existing native 16px icon vocabulary, but give each actual glyph selective depth.
spec=importlib.util.spec_from_file_location('glyphs',ROOT/'tools/make_covenant_icons.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
icons=[]
for name in m.NAMES:
 icon=m.make(name);alpha=icon.getchannel('A');shadow=Image.new('RGBA',icon.size);sd=ImageDraw.Draw(shadow)
 for y in range(15):
  for x in range(15):
   if alpha.getpixel((x,y)):sd.point((x+1,y+1),fill='#694d2d')
 shadow.alpha_composite(icon);shadow.save(ROOT/f'src/main/resources/assets/unrp/textures/icons/covenant_{name}.png');icons.append((name,shadow))
board=Image.new('RGBA',(800,480),'#202831');draw=ImageDraw.Draw(board)
draw.text((18,14),'COVENANT / native textures used by the game renderers',fill='#e5cf91')
for i,name in enumerate(('court_seal','signature','parchment','indemnity')):
 src=Image.open(OUT/f'{name}.png').convert('RGBA');src.thumbnail((128,176),Image.Resampling.NEAREST)
 if name!='parchment':src=src.resize((128,128),Image.Resampling.NEAREST)
 else:src=src.resize((96,144),Image.Resampling.NEAREST)
 board.alpha_composite(src,(20+i*190,46));draw.text((20+i*190,200),name,fill='#d3ccb9')
for i,(name,icon) in enumerate(icons):
 x=15+(i%13)*60;y=242+(i//13)*104;board.alpha_composite(icon.resize((48,48),Image.Resampling.NEAREST),(x,y));draw.text((x,y+53),name[:10],fill='#b9c3c8')
draw.text((18,450),'Texture sheet, not a Minecraft screenshot. Law text and effects are drawn in-game.',fill='#b9c3c8')
board.save(ROOT/'docs/covenant-visuals.png')
print('Generated Covenant world, hand, parchment, protection textures and 25 native ability glyphs.')
