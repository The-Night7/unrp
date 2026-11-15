#!/usr/bin/env python3
"""Draw native 16x16 Minecraft sprites. No generated illustrations or image resizing.
Run from any directory; requires Pillow. Each drawing uses integer pixel coordinates.
"""
from pathlib import Path
import importlib.util
import sys,json,html,base64
from PIL import Image, ImageDraw
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/unrp'
spec=importlib.util.spec_from_file_location('catalog',ROOT/'tools/gen_items.py')
catalog=importlib.util.module_from_spec(spec);spec.loader.exec_module(catalog)
META={i:(kind,a,b) for i,kind,a,b,*_ in catalog.ITEMS}
OUTFITS={i:(piece,setname) for i,piece,setname,*_ in catalog.OUTFITS}
GOLD='#b98b45';PAPER='#d9cfac';INK='#242832'

def colour(c,f=1):
    if isinstance(c,str):c=tuple(bytes.fromhex(c.lstrip('#')))
    return tuple(min(255,max(0,round(v*f))) for v in c[:3])+(255,)

def blank():
    im=Image.new('RGBA',(16,16));return im,ImageDraw.Draw(im)

def lore_outfit(name):
    """Known school uniforms and Rei's X-Static suit, adapted to a 16px icon."""
    im,d=blank()
    agwin=name.startswith('agwin')
    school=name.startswith(('wellstone','agwin','school_uniform'))
    chest=any(t in name for t in ('blazer','shirt','chestplate','top'))
    legs=any(t in name for t in ('trousers','leggings','pants','skirt'))
    if school:
        jacket='#9c8770' if agwin else '#44666a'
        tie='#862d39' if agwin else '#29384d'
        trousers='#51443b' if agwin else '#25262b'
        if chest:
            base='#e1e2df' if 'shirt' in name else jacket
            d.polygon([(4,3),(11,3),(14,5),(13,10),(11,9),(11,14),(4,14),(4,9),(2,10),(1,5)],fill=colour(base))
            d.line((2,5,2,8),fill=colour(base,1.18));d.line((11,5,11,13),fill=colour(base,.72))
            d.polygon([(6,3),(9,3),(9,5),(8,6),(7,6),(6,5)],fill=colour('#eeeae1'))
            if not agwin and 'shirt' not in name:
                d.rectangle((6,6,9,12),fill=colour('#8c8e89'));d.line((6,12,9,12),fill=colour('#656b68'))
            d.line((7,5,7,8),fill=colour(tie));d.point((8,6),fill=colour(tie))
            if 'girl' in name:
                d.line((6,5,9,5),fill=colour(tie));d.point((6,6),fill=colour(tie));d.point((9,6),fill=colour(tie))
            if agwin and 'shirt' not in name:d.line((5,7,5,13),fill=colour('#282523'));d.line((10,7,10,13),fill=colour('#282523'))
            if 'one_shoulder' in name:
                d.rectangle((1,4,3,8),fill=colour('#e1e2df'));d.line((4,3,4,8),fill=colour(jacket,.8))
            if 'seraphina' in name:
                d.line((2,4,4,5),fill=colour('#e1e2df'));d.line((11,4,13,5),fill=colour('#e1e2df'))
        elif legs:
            if 'skirt' in name:
                base='#51443b' if agwin else '#29384d'
                d.polygon([(5,3),(10,3),(13,13),(2,13)],fill=colour(base))
                for x in (5,8,10):d.line((x,5,x,12),fill=colour(base,.7))
                d.line((5,3,10,3),fill=colour(base,1.25))
            else:
                d.rectangle((4,2,11,5),fill=colour(trousers));d.line((4,2,11,2),fill=colour(trousers,1.4))
                for left in (4,9):
                    d.rectangle((left,6,left+2,13),fill=colour(trousers));d.line((left,6,left,12),fill=colour(trousers,1.4))
        else:
            sock='#5c4b3b' if agwin else '#202127'
            for left in (2,9):
                if 'girl' in name:d.rectangle((left+1,4,left+3,9),fill=colour(sock))
                d.rectangle((left+1,9,left+3,12),fill=colour('#312b2b'));d.rectangle((left,12,left+4,13),fill=colour('#242326'));d.line((left+1,10,left+3,10),fill=colour('#59504a'))
    elif name.startswith('xstatic'):
        if chest:
            d.polygon([(4,3),(11,3),(14,5),(13,10),(11,9),(11,14),(4,14),(4,9),(2,10),(1,5)],fill=colour('#80878e'))
            d.rectangle((2,5,3,6),fill=colour('#795a43'));d.rectangle((12,5,13,6),fill=colour('#795a43'))
            d.rectangle((4,12,11,13),fill=colour('#795a43'));d.rectangle((5,12,6,13),fill=colour('#a07b51'));d.rectangle((9,12,10,13),fill=colour('#a07b51'));d.line((5,4,5,10),fill=colour('#a1a5a7'))
        elif legs:
            d.rectangle((4,2,11,5),fill=colour('#27272d'))
            for left in (4,9):d.rectangle((left,6,left+2,13),fill=colour('#27272d'));d.line((left,6,left,12),fill=colour('#45434a'))
        else:
            for left in (2,9):d.rectangle((left+1,7,left+3,12),fill=colour('#795a43'));d.rectangle((left,12,left+4,13),fill=colour('#513d31'));d.line((left+1,8,left+2,8),fill=colour('#a07b51'))
    return finish(catalog.outline(im))

def draw(name):
    if name.startswith(('wellstone','agwin','school_uniform','xstatic')):return lore_outfit(name)
    kind,a,b=META.get(name,('','#7ba9af','#cee6d5'))
    if name in ('disabler','syringe_disabler'):a,b='#83bddf','#d1e8ef'

    if name.startswith('armament'):kind='sword';a=catalog.ARM_COL.get(name.removeprefix('armament_'),'#c8cbd0');b='#eff0dd'
    if name.startswith('ascalon'):kind='sword';a='#68a06d' if name=='ascalon' else '#a4c679';b='#d8dfa5'
    if name.startswith('contract'):kind='contract'
    outfits=dict(OUTFITS)
    for n,p,s in [('spectre_top','chest','xstatic'),('spectre_pants','legs','xstatic'),('spectre_boots','feet','xstatic'),('spectre_bandana','head','xstatic'),('school_uniform_chestplate','chest','wellstone_uniform'),('school_uniform_leggings','legs','wellstone_uniform'),('school_uniform_boots','feet','wellstone_uniform')]:outfits[n]=(p,s)
    if name in outfits:
        piece,setname=outfits[name];im=catalog.draw_outfit(piece,setname);d=ImageDraw.Draw(im)
        if piece=='chest':
            d.line((5,6,6,9),fill=colour(catalog.SETS[setname][0],.7));d.line((10,7,10,11),fill=colour(catalog.SETS[setname][0],.7))
            d.line((3,4,3,7),fill=colour(catalog.SETS[setname][0],1.3));d.point((10,7),fill=colour(GOLD))
        if piece=='legs' and 'skirt' not in name:
            im,d=blank();base=catalog.SETS[setname][0]
            d.rectangle((4,2,11,5),fill=colour(base));d.line((4,2,11,2),fill=colour(catalog.SETS[setname][1]))
            for left in (4,9):
                d.rectangle((left,6,left+2,13),fill=colour(base));d.line((left,6,left,12),fill=colour(base,1.25));d.line((left+2,6,left+2,13),fill=colour(base,.65));d.line((left,13,left+2,13),fill=colour(base,.65))
            im=catalog.outline(im);d=ImageDraw.Draw(im)
        if piece=='feet':
            base=catalog.SETS[setname][0]
            for left in (2,9):
                d.line((left+1,9,left+3,9),fill=colour(base,1.4));d.line((left,12,left+4,12),fill=colour(base,.55));d.point((left+1,10),fill=colour('#8f8d89'))
        if 'skirt' in name:
            im,d=blank();d.polygon([(5,3),(10,3),(13,13),(2,13)],fill=colour(catalog.SETS[setname][0]));d.line((4,12,11,12),fill=colour('#823e49'))
            for x in (5,8,10):d.line((x,5,x,11),fill=colour(catalog.SETS[setname][0],.65))
        if 'one_shoulder' in name:d.rectangle((1,4,3,7),fill=colour(PAPER))
        return finish(im)
    im,d=blank()
    if kind=='vial':
        # Eight-pixel glass body, a three-pixel neck, and a restrained single gold clasp.
        d.rectangle((6,1,9,3),fill=colour('#777d86'));d.line((6,1,8,1),fill=colour('#bbc5c8'))
        d.polygon([(5,4),(10,4),(12,6),(12,13),(11,14),(4,14),(3,13),(3,6)],fill=colour('#506c79'))
        d.rectangle((4,7,11,12),fill=colour(a,.65));d.rectangle((5,7,10,11),fill=colour(a))
        d.line((5,7,10,7),fill=colour(b));d.line((4,6,4,11),fill=colour('#cfdfd5'));d.point((5,5),fill=colour('#e7eee3'))
        d.line((5,13,10,13),fill=colour('#91abb0'))
    elif kind in ('book','scroll','contract'):
        if kind=='scroll':
            d.polygon([(5,2),(12,2),(11,12),(4,12)],fill=colour(PAPER));d.line((6,3,11,3),fill=colour('#f3e4bc'))
            d.rectangle((3,2,5,4),fill=colour('#9d825b'));d.rectangle((2,11,11,13),fill=colour('#9d825b'));d.line((3,11,10,11),fill=colour(PAPER))
            d.line((7,5,10,5),fill=colour('#80735c'));d.line((6,7,10,7),fill=colour('#80735c'));d.line((5,9,9,9),fill=colour(a))
            d.rectangle((7,8,9,10),fill=colour(a,.65));d.point((8,9),fill=colour(b))
        elif kind=='contract':
            paper='#b7d2d2' if 'soul' in name else '#e1cdb4' if 'proposed' in name else PAPER
            d.polygon([(4,1),(10,1),(12,3),(12,13),(3,13),(3,3)],fill=colour(paper));d.line((4,2,9,2),fill=colour(paper,1.12));d.line((11,4,11,12),fill=colour(paper,.82))
            for y,w in ((4,6),(6,5),(8,6)):d.line((5,y,w+4,y),fill=colour('#897e69'))
            if 'stamped' in name:
                d.rectangle((8,10,10,12),fill=colour('#8c3543'));d.point((8,10),fill=colour('#d76d63'));d.point((9,13),fill=colour('#682d3c'))
            else:d.line((5,11,8,10),fill=colour('#77654b'))
        else:
            leather='#292a31' if name=='unordinary_book' else a
            d.polygon([(4,2),(11,1),(13,3),(13,13),(5,14),(3,12),(3,3)],fill=colour(leather,.6));d.rectangle((5,2,11,11),fill=colour(leather))
            d.line((6,2,10,2),fill=colour(leather,1.28));d.line((4,3,4,11),fill=colour(leather,1.28))
            d.line((6,12,12,12),fill=colour(PAPER));d.line((6,13,11,13),fill=colour('#998666'))
            if name=='unordinary_book':
                d.line((6,5,10,5),fill=colour('#ecece7'));d.line((6,7,10,7),fill=colour('#a4a5a8'));d.line((7,10,9,10),fill=colour('#a4a5a8'))
            else:
                for x,y in ((5,3),(11,3),(5,10),(11,10)):d.point((x,y),fill=colour(GOLD))
                symbol(d,name,7,5,b)
    elif kind=='shard':
        shapes={'edict':[(8,1),(11,5),(10,11),(6,14),(4,9)],'limit':[(9,1),(12,7),(8,13),(3,12),(6,6)],'dragon':[(6,2),(11,4),(12,9),(7,14),(4,10)],'singularity':[(9,1),(12,8),(7,14),(4,8)]}
        points=shapes.get(name.split('_')[0],[(8,1),(12,6),(10,12),(5,14),(3,8)])
        d.polygon(points,fill=colour(a,.58));d.polygon([points[0],(8,8),(5,12),(5,6)],fill=colour(a));d.line((8,3,6,8),fill=colour(b));d.line((9,6,8,11),fill=colour(a,1.2))
    elif name=='joker_mask':
        # John's black cloth balaclava, not a theatrical face plate. Eye openings are unlit.
        d.polygon([(5,1),(10,1),(12,3),(12,12),(10,14),(5,14),(3,12),(3,3)],fill=colour('#25272d'))
        d.line((5,2,9,2),fill=colour('#41434b'));d.line((4,4,4,10),fill=colour('#33363e'))
        d.line((5,6,6,6),fill=colour('#090b10'));d.line((9,6,10,6),fill=colour('#090b10'))
        d.line((6,11,9,11),fill=colour('#1b1d24'));d.line((11,8,11,12),fill=colour('#15181e'))
    elif name in {x[0] for x in catalog.BLOCKS}:
        if name=='recovery_machine':
            d.polygon([(2,8),(8,5),(13,8),(7,12),(2,10)],fill=colour('#d4dbdf'));d.polygon([(2,9),(7,12),(13,9),(13,11),(7,14),(2,12)],fill=colour('#717e88'))
            d.polygon([(3,8),(6,6),(8,7),(5,9)],fill=colour('#edf0e9'));d.line((3,12,3,14),fill=colour('#485362'));d.line((11,12,11,14),fill=colour('#485362'))
            d.rectangle((10,1,14,5),fill=colour('#525f6b'));d.rectangle((11,2,13,4),fill=colour('#23383d'));d.line((11,3,12,2),fill=colour('#94b8b2'));d.line((12,2,13,4),fill=colour('#94b8b2'));d.line((12,6,12,7),fill=colour('#81919a'))
        elif name in ('conversion_machine','suppression_field','phylactery'):
            c={'recovery_machine':'#79ab77','conversion_machine':'#d7a353','suppression_field':'#bc625c','phylactery':'#70acb5'}[name]
            d.polygon([(3,3),(10,1),(13,3),(13,12),(6,14),(2,12),(2,4)],fill=colour('#444c56'))
            d.polygon([(3,3),(10,2),(12,3),(6,5)],fill=colour('#89969a'));d.rectangle((4,6,9,10),fill=colour(c,.55));d.rectangle((5,6,8,9),fill=colour(c));d.line((5,6,7,6),fill=colour(c,1.4));d.line((11,5,11,10),fill=colour('#242832'));d.point((5,12),fill=colour(GOLD))
            if name=='recovery_machine':d.line((5,7,7,7),fill=colour('#e5e3c9'));d.point((6,6),fill=colour('#e5e3c9'));d.point((6,8),fill=colour('#e5e3c9'))
        else:
            c={'salt_block':'#d1d5ce','barrier_block':'#69b7c0','barrier_block_am':'#cdab57','particle_block':'#8e9ec2'}[name]
            d.polygon([(7,2),(13,5),(13,11),(7,14),(2,11),(2,5)],fill=colour(c,.6));d.polygon([(7,2),(13,5),(7,8),(2,5)],fill=colour(c,1.15));d.polygon([(2,6),(6,8),(6,12),(2,10)],fill=colour(c))
            if name=='salt_block':
                for pt in ((4,5),(8,4),(9,10),(3,8),(7,11)):d.point(pt,fill=colour(c,1.3))
            else:d.line((7,8,7,13),fill=colour(c,1.5));d.line((8,8,12,6),fill=colour(c,1.3))
    else:
        if not kind:raise ValueError('No pixel drawing for '+name)
        im=catalog.draw(kind,a,b,name)
        return finish(im)
    return finish(catalog.outline(im))

def symbol(d,name,x,y,c):
    c=colour(c);branch=name.rsplit('_',1)[-1]
    if branch=='med':d.line((x+1,y,x+1,y+3),fill=c);d.line((x,y+1,x+2,y+1),fill=c)
    elif branch=='proj':d.line((x,y+3,x+3,y),fill=c);d.line((x+1,y,x+3,y),fill=c)
    elif branch=='con':d.rectangle((x,y,x+3,y+3),outline=c)
    elif branch=='qtm':
        for pt in ((x+1,y),(x,y+1),(x+2,y+1),(x+1,y+2)):d.point(pt,fill=c)
    else:d.line((x,y,x,y+3),fill=c);d.line((x+3,y,x+3,y+3),fill=c);d.line((x+1,y+3,x+2,y+3),fill=c)

def finish(im):
    # Deliberate edge shade on existing flat colour clusters. No noise or resampling.
    src=im.copy();px=im.load()
    for y in range(1,15):
        for x in range(1,15):
            c=src.getpixel((x,y))
            if c[3] and max(c[:3])>55:
                if src.getpixel((x,y-1))[3]==0 or max(src.getpixel((x,y-1))[:3])<50:px[x,y]=colour(c,1.15)
                elif src.getpixel((x,y+1))[3]==0 or max(src.getpixel((x,y+1))[:3])<50:px[x,y]=colour(c,.75)
    return im

def main():
    target=ASSETS/'textures/item';names=sorted(p.stem for p in target.glob('*.png'))
    for name in names:
        item=json.loads((ASSETS/f'models/item/{name}.json').read_text())
        if 'elements' not in item and 'loader' not in item and item.get('parent') in ('minecraft:item/generated','minecraft:item/handheld','item/generated','item/handheld'):
            draw(name).save(target/f'{name}.png')
    import subprocess
    subprocess.run([sys.executable,str(ROOT/'tools/preview_equipment.py')],check=True)
    print(f'Drew {len(names)} native 16x16 icons.')

if __name__=='__main__':main()
