#!/usr/bin/env python3
"""Exterior geometry views of the actual Java-generated campuses (not game screenshots)."""
from pathlib import Path
from PIL import Image,ImageDraw
import struct,sys,math
ROOT=Path(__file__).resolve().parents[1]
SOURCE=Path(sys.argv[1]);OUTPUT=ROOT/'docs'

def render(name):
    data=(SOURCE/f'{name}.bin').read_bytes();size,height=struct.unpack('>ii',data[:8]);cells=data[8:];layer=size*size
    palette=[line.split() for line in (SOURCE/f'{name}.palette').read_text().splitlines()]
    names=[m for m,c in palette];colors=[tuple(bytes.fromhex(c[1:])) for m,c in palette];air=names.index('AIR')
    def at(x,y,z):return cells[x+z*size+y*layer] if 0<=x<size and 0<=z<size and 0<=y<height else air
    scale=3.2
    def project(x,y,z):return (50+(x+z)*scale*.866,60+height*scale+(x-z+size)*scale*.5-y*scale)
    width=int(size*2*scale*.866+100);h=int(size*scale+height*scale+140)
    im=Image.new('RGB',(width,h),'#182329');d=ImageDraw.Draw(im)
    title=name.replace('_',' ').title()
    d.text((25,18),title+' / production layout 6',fill='#edd6a1')
    d.text((25,34),'Actual campus coordinates; simplified material colours and block shapes. Not an in-game screenshot.',fill='#a7b7bd')
    blocks=[]
    for y in range(height):
        for z in range(size):
            for x in range(size):
                m=at(x,y,z)
                if m==air:continue
                if at(x,y+1,z)==air or at(x,y,z-1)==air or at(x+1,y,z)==air:blocks.append((x-z+y*.2,x,y,z,m))
    def cube(x0,y0,z0,x1,y1,z1,color):
        for pts,factor in [([(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],1),
                           ([(x0,y0,z0),(x1,y0,z0),(x1,y1,z0),(x0,y1,z0)],.84),
                           ([(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)],.67)]:
            d.polygon([project(*p) for p in pts],fill=tuple(round(c*factor) for c in color))
    for _,x,y,z,m in sorted(blocks):
        kind=names[m];c=colors[m]
        if kind in ('FENCE','GATE_EW','GATE_NS','WINDOW_MULLION'):
            cube(x+.44,y,z+.44,x+.56,y+1,z+.56,c)
            for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
                if at(x+dx,y,z+dz)!=air:
                    for low in (.2,.75):cube(x+min(.5,.5+dx*.5),y+low,z+min(.5,.5+dz*.5),x+max(.5,.5+dx*.5)+.04,y+low+.1,z+max(.5,.5+dz*.5)+.04,c)
        elif kind in ('DESK','CAFETERIA_TABLE','WINDOW_SILL'):cube(x,y+.5,z,x+1,y+1,z+1,c)
        elif kind in ('CORNICE','ROOF_VENT'):cube(x,y,z,x+1,y+.5,z+1,c)
        elif kind in ('STAIRS','STAIRS_NORTH','CHAIR','CAFETERIA_CHAIR'):
            cube(x,y,z,x+1,y+.5,z+1,c);north=kind!='STAIRS';cube(x,y+.5,z if north else z+.5,x+1,y+1,z+.5 if north else z+1,c)
        elif kind in ('PENDANT','STREET_LANTERN'):cube(x+.3,y+.2,z+.3,x+.7,y+.7,z+.7,c)
        elif kind=='STREET_POST':cube(x+.35,y,z+.35,x+.65,y+1,z+.65,c)
        elif kind in ('SIGN','WALL_SIGN'):cube(x+.1,y+.3,z+.45,x+.9,y+.85,z+.55,c)
        elif kind in ('DOOR_LOWER','DOOR_UPPER','SIDE_DOOR_LOWER','SIDE_DOOR_UPPER'):cube(x,y,z,x+1,y+1,z+.16,c)
        elif kind=='FLOWER':cube(x+.4,y,z+.4,x+.6,y+.6,z+.6,c)
        else:cube(x,y,z,x+1,y+1,z+1,c)
    d.text((25,h-36),f'{size} x {size} blocks | terraces adapt to terrain during world generation',fill='#d2c69e')
    im.save(OUTPUT/f'{name.replace("_","-")}-exterior.png')
    return im
images=[render(p.stem) for p in sorted(SOURCE.glob('*.bin'))]
thumbs=[]
for im in images:
    im.thumbnail((800,550),Image.Resampling.NEAREST);thumbs.append(im)
board=Image.new('RGB',(1600,3*550),'#182329')
for i,im in enumerate(thumbs):board.paste(im,((i%2)*800,(i//2)*550))
board.save(OUTPUT/'schools-exterior-preview.png')
print('Rendered',len(images),'exterior geometry previews.')
