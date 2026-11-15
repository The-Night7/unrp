#!/usr/bin/env python3
"""Geometry preview of the portrait HUD; sample face/text are illustrative, not a game capture."""
from PIL import Image,ImageDraw,ImageFont
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
im=Image.new('RGBA',(196,78),(0,0,0,0));d=ImageDraw.Draw(im)
GOLD='#d6b570';INK='#13171e';LIGHT='#f3e8d0'
def fill(x,y,x2,y2,c):
    if x2>x and y2>y:d.rectangle((x,y,x2-1,y2-1),fill=c)
def shade(c,f):return tuple(min(255,round(int(c[i:i+2],16)*f)) for i in (1,3,5))
def meter(x,y,w,h,r,c):
    fill(x+1,y-1,x+w-1,y+h+1,'#080b11');fill(x,y,x+w,y+h,'#080b11');fill(x+1,y,x+w-1,y+h,'#3a3d42');fill(x+2,y+1,x+w-2,y+h-1,'#22272f')
    f=round((w-4)*r);fill(x+2,y+1,x+2+f,y+h-1,c)
    if h>4:fill(x+2,y+1,x+2+f,y+2,shade(c,1.2));fill(x+2,y+h-3,x+2+f,y+h-1,shade(c,.65))
font=ImageFont.load_default(size=8)
def text(s,x,y,center=False):
    if center:x-=round(d.textlength(s,font=font)/2)
    d.text((x+1,y+1),s,font=font,fill='#080b11');d.text((x,y),s,font=font,fill=LIGHT)
fill(3,0,193,78,INK);fill(0,3,196,75,INK)
for x,y,dx,dy in [(2,2,1,1),(193,2,-1,1),(2,75,1,-1),(193,75,-1,-1)]:
    for i in range(8):fill(x+dx*i,y,x+dx*i+1,y+1,GOLD)
    for i in range(5):fill(x,y+dy*i,x+1,y+dy*i+1,GOLD)
fill(49,10,50,67,'#353843');fill(6,13,46,53,'#080c12');fill(7,14,45,52,'#76623f')
# Deliberately generic eight-pixel sample face. Actual HUD uses the player's current skin/hat.
face=['hhhhhhhh','hhhhhhhh','hsssss h','ssssssss','sesseess','ssssssss','ssmmmmss','sssmmsss']
pal={'h':'#48332b','s':'#b78465','e':'#c6c9bf','m':'#795043',' ':'#48332b'}
for y,row in enumerate(face):
    for x,c in enumerate(row):fill(8+x*36//8,15+y*36//8,8+(x+1)*36//8,15+(y+1)*36//8,pal[c])
fill(9,56,43,57,'#665438');fill(9,56,21,57,GOLD);fill(23,60,28,61,GOLD);fill(25,58,26,63,GOLD)
text('Sovereign',54,7)
# Same hard pixel icons and meter positions as UnrpHudOverlay.
x,y=54,24;c='#e0bb70'
for b in [(x,y,x+3,y+3),(x+4,y,x+7,y+3),(x+1,y+2,x+6,y+4),(x+2,y+4,x+5,y+5),(x+3,y+5,x+4,y+6)]:fill(*b,c)
meter(66,22,120,11,.8,'#d2ac65');text('80 / 100',126,23,True)
x,y=54,40;c='#6aaca8'
for b in [(x+3,y,x+4,y+2),(x+2,y+2,x+5,y+4),(x+1,y+4,x+6,y+6),(x+2,y+6,x+5,y+7)]:fill(*b,c)
meter(66,39,120,10,.75,c);text('120 / 160',126,40,True)
fill(55,58,59,62,'#c89b68');fill(54,59,60,61,'#c89b68');fill(58,61,60,63,'#e0d6bf');fill(59,63,61,64,'#e0d6bf')
meter(66,56,48,10,.9,'#b98553');text('18',90,57,True)
fill(123,57,130,62,'#98a7b1');fill(124,62,129,63,'#98a7b1');fill(125,63,128,64,'#98a7b1');fill(125,58,128,61,'#465666')
meter(135,56,51,10,.6,'#879aaa');text('12',160,57,True)
preview=Image.new('RGBA',(220,102),'#30383e');preview.alpha_composite(im,(12,12));preview.resize((880,408),Image.Resampling.NEAREST).save(ROOT/'docs/unrp-hud-preview.png')
import base64
payload=base64.b64encode((ROOT/'docs/unrp-hud-preview.png').read_bytes()).decode()
(ROOT/'docs/unrp-hud-preview.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="880" height="408"><image width="880" height="408" href="data:image/png;base64,{payload}"/></svg>')
