#!/usr/bin/env python3
"""Build actual placed models, shared item models and wearable UV sheets as one asset set."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,importlib.util,sys
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/unrp'
spec=importlib.util.spec_from_file_location('catalog',ROOT/'tools/gen_items.py');catalog=importlib.util.module_from_spec(spec);spec.loader.exec_module(catalog)
def savejson(path,data):path.write_text(json.dumps(data,indent=2)+'\n')
def box(lo,hi,tex,uv=None):
    return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex,'uv':uv or [0,0,16,16]} for f in ('north','south','east','west','up','down')}}
def model(parts,textures):return {'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,'elements':parts}
def texture(name,base,accent,kind='metal'):
    im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im)
    d.line((0,0,15,0),fill='#b5b9b5');d.line((0,1,0,15),fill='#77838a');d.line((1,15,15,15),fill='#252e37');d.line((15,1,15,14),fill='#252e37')
    if kind=='screen':
        d.rectangle((2,2,13,13),fill='#17282b');d.line((3,9,5,9,7,5,9,11,11,7,12,7),fill=accent,width=1);d.rectangle((3,3,5,3),fill=accent)
    elif kind=='vent':
        for y in (4,7,10):d.rectangle((3,y,12,y+1),fill='#222b34');d.line((3,y+2,12,y+2),fill='#737e83')
        d.rectangle((3,13,5,13),fill=accent)
    elif kind=='cloth':pass
    elif kind=='glass':
        d.rectangle((1,1,14,14),fill=base);d.rectangle((3,3,12,12),fill=tuple(round(int(base[i:i+2],16)*.7) for i in (1,3,5)));d.line((3,9,8,4),fill='#a3c5c7');d.line((8,12,12,8),fill=accent)
    elif kind=='crystal':
        for x,y in ((3,4),(9,3),(6,10),(12,9),(2,12)):d.rectangle((x,y,x+2,y+2),fill=accent)
    else:
        for x,y in ((2,2),(13,2),(2,13),(13,13)):d.point((x,y),fill='#d1c8ab')
        d.rectangle((3,5,12,6),fill=accent);d.line((4,10,11,10),fill='#78878c')
    im.save(A/f'textures/block/{name}.png')

def machines():
    for name,accent in [('conversion_machine','#c9a257'),('recovery_machine','#83aaa4'),('suppression_field','#8d949f'),('suppression_field_on','#c48259'),('phylactery','#74a4ae')]:
        for face,kind in [('shell','metal'),('screen','screen'),('vent','vent')]:texture(name+'_'+face,'#56636d',accent,kind)
        texture(name+'_glass','#48737d',accent,'glass')
    for name in ('conversion_machine','recovery_machine','suppression_field','suppression_field_on','phylactery'):
        tx={k:f'unrp:block/{name}_{k}' for k in ('shell','screen','vent','glass')};tx['particle']=tx['shell']
        if name=='conversion_machine':
            parts=[box([1,0,1],[15,2,15],'shell'),box([2,2,4],[12,13,14],'vent'),box([3,5,2],[11,12,4],'screen'),box([3,12,3],[11,14,6],'shell'),box([12,3,5],[15,11,13],'glass'),box([3,3,1],[11,5,4],'shell')]
        elif name=='recovery_machine':
            parts=[box([1,1,1],[12,3,15],'shell'),box([2,3,2],[11,6,14],'glass'),box([2,6,11],[11,10,14],'shell'),box([3,6,10],[10,7,12],'shell'),box([1,0,2],[3,2,4],'shell'),box([10,0,12],[12,2,14],'shell'),box([13,0,9],[15,11,11],'shell'),box([12,10,7],[16,15,10],'screen')]
        elif name.startswith('suppression'):
            parts=[box([1,0,1],[15,3,15],'vent'),box([5,3,5],[11,11,11],'shell'),box([3,11,3],[13,13,13],'screen'),box([5,13,5],[11,15,11],'glass')]
            for x,z in ((3,3),(11,3),(3,11),(11,11)):parts.append(box([x,3,z],[x+2,10,z+2],'vent'))
        else:
            parts=[box([3,0,3],[13,2,13],'shell'),box([4,2,4],[12,12,12],'glass'),box([3,12,3],[13,14,13],'shell'),box([6,14,6],[10,16,10],'shell')]
            for x,z in ((3,3),(12,3),(3,12),(12,12)):parts.append(box([x,2,z],[x+1,12,z+1],'shell'))
        savejson(A/f'models/block/{name}.json',model(parts,tx))
    for name,base,accent in [('salt_block','#bfc8c6','#e4e7db'),('barrier_block','#4b8998','#abd7da'),('barrier_block_am','#a18c50','#e1d4a2'),('particle_block','#485e7e','#b1c1d6')]:
        texture(name+'_surface',base,accent,'crystal' if name=='salt_block' else 'glass')
        savejson(A/f'models/block/{name}.json',model([box([0,0,0],[16,16,16],'surface')],{'surface':f'unrp:block/{name}_surface','particle':f'unrp:block/{name}_surface'}))
    for name,*_ in catalog.BLOCKS:savejson(A/f'models/item/{name}.json',{'parent':f'unrp:block/{name}'})

def garment(setname,layer):
    im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    agwin=setname.startswith('agwin');wellston=setname.startswith('wellstone');xstatic=setname=='xstatic';spectre=setname=='spectre'
    base='#9c8770' if agwin else '#44666a' if wellston else '#7c858c' if xstatic else '#30383e'
    trim='#862d39' if agwin else '#29384d' if wellston else '#795a43' if xstatic else '#596468'
    if 'shirt' in setname:base='#e0e2df'
    # Standard humanoid box unwrap. Left arm/leg have separate UVs to support shoulder variants.
    def wrapped(u,v,w,h,depth,c):
        d.rectangle((u,v,u+2*(w+depth)-1,v+h+depth-1),fill=c)
        d.line((u+depth,v+depth,u+depth+w-1,v+depth),fill='#c0c5c2' if 'shirt' in setname else '#7e8e8e')
        d.line((u+depth+w-1,v+depth+1,u+depth+w-1,v+depth+h-1),fill='#30383e')
    if layer==1:
        wrapped(16,16,8,12,4,base);wrapped(40,16,4,12,4,base);wrapped(32,48,4,12,4,base)
        if wellston or agwin:
            # White collar with the actual tie/bow and (Wellston) grey vest.
            d.rectangle((22,20,25,23),fill='#e9e8df')
            if wellston and 'shirt' not in setname:d.rectangle((22,24,25,30),fill='#898e8d')
            if 'girl' in setname:d.line((21,23,26,23),fill=trim);d.rectangle((22,24,23,24),fill=trim);d.rectangle((25,24,26,24),fill=trim)
            else:d.rectangle((23,22,24,26),fill=trim);d.point((23,27),fill=trim)
            # Open jacket lapels; shirt cuff pixels are not gold jewellery.
            if 'shirt' not in setname:d.line((21,23,21,30),fill='#28383c' if wellston else '#392e29');d.line((26,23,26,30),fill='#28383c' if wellston else '#392e29')
            for u,v in ((40,16),(32,48)):d.rectangle((u+4,v+13,u+7,v+15),fill='#dedfd9')
            if 'one_shoulder' in setname:d.rectangle((32,48,47,63),fill=(0,0,0,0))
            if 'seraphina' in setname:
                for u,v in ((40,16),(32,48)):d.rectangle((u,v,u+15,v+7),fill=(0,0,0,0))
        elif xstatic:
            d.rectangle((16,30,39,31),fill=trim)
            for u,v in ((40,16),(32,48)):d.rectangle((u,v+6,u+15,v+8),fill=trim)
            d.rectangle((20,30,21,31),fill='#ac8965');d.rectangle((26,30,27,31),fill='#ac8965')
        else:
            d.rectangle((23,21,24,31),fill=trim);d.line((20,29,22,29),fill='#748086');d.line((25,29,27,29),fill='#748086')
        # Bare hands; sleeve ends no longer turn the player's hands into painted mittens.
        for u,v in ((40,16),(32,48)):d.rectangle((u,v+15,u+15,v+15),fill=(0,0,0,0))
        # Boots occupy only the bottom of the outer leg UVs, not full trouser legs.
        shoe='#6e5140' if xstatic else '#302b2b'
        for u,v in ((0,16),(16,48)):
            wrapped(u,v,4,12,4,shoe)
            d.rectangle((u,v,u+15,v+11),fill=(0,0,0,0));d.line((u+4,v+13,u+7,v+13),fill='#8c7c6d')
        if setname=='ember_hat':
            d.rectangle((0,0,39,10),fill='#252a2f');d.rectangle((8,10,15,11),fill='#744a3c')
    else:
        pants='#51443b' if agwin else '#25262b'
        for u,v in ((0,16),(16,48)):
            wrapped(u,v,4,12,4,pants)
            d.line((u+4,v+5,u+4,v+14),fill='#75634f' if agwin else '#41434c')
        if 'girl_uniform' in setname:
            skirt='#51443b' if agwin else '#29384d'
            wrapped(16,16,8,7,4,skirt)
            for x in (21,24,27,33,36,39):d.line((x,21,x,26),fill='#392e29' if agwin else '#1d2638')
            # Knee/thigh socks, independent from the skirt shell.
            for u,v in ((0,16),(16,48)):
                d.rectangle((u,v,u+15,v+7),fill=(0,0,0,0));d.rectangle((u,v+8,u+15,v+15),fill='#51443b' if agwin else '#202128')
        else:d.rectangle((16,29,39,31),fill=pants)
    if layer==1 and setname=='spectre':
        for x1,x2 in ((0,7),(8,15),(16,23),(24,31)):d.rectangle((x1,12,x2,15),fill='#333c42')
    return im

def unwrap(lo,hi,u,v,w,h,depth):
    # Model UVs are in the 0..16 space even for a 64px sheet.
    rects={'north':[u+depth,v+depth,u+depth+w,v+depth+h],'south':[u+2*depth+w,v+depth,u+2*depth+2*w,v+depth+h],
           'west':[u,v+depth,u+depth,v+depth+h],'east':[u+depth+w,v+depth,u+2*depth+w,v+depth+h],
           'up':[u+depth,v,u+depth+w,v+depth],'down':[u+depth+w,v,u+depth+2*w,v+depth]}
    return {'from':lo,'to':hi,'faces':{face:{'texture':'#wear','uv':[x/4 for x in uv]} for face,uv in rects.items()}}

def wardrobe():
    sets=set(catalog.SETS)|{'spectre'}
    for name in sets:
        for layer in (1,2):garment(name,layer).save(A/f'textures/models/armor/{name}_layer_{layer}.png')
    outfits=list(catalog.OUTFITS)+[(n,p,s,'','') for n,p,s in [('spectre_top','chest','spectre'),('spectre_pants','legs','spectre'),('spectre_boots','feet','spectre'),('spectre_bandana','head','spectre'),('school_uniform_chestplate','chest','wellstone_uniform'),('school_uniform_leggings','legs','wellstone_uniform'),('school_uniform_boots','feet','wellstone_uniform')]]
    for name,piece,setname,*_ in outfits:
        layer=2 if piece=='legs' else 1
        if piece=='chest':parts=[unwrap([4,2,6],[12,14,10],16,16,8,12,4),unwrap([0,2,6],[4,14,10],40,16,4,12,4),unwrap([12,2,6],[16,14,10],32,48,4,12,4)]
        elif piece=='legs':
            parts=[unwrap([3,1,6],[7,13,10],0,16,4,12,4),unwrap([9,1,6],[13,13,10],16,48,4,12,4)]
            if 'girl_uniform' in setname:parts.append(unwrap([4,7,6],[12,14,10],16,16,8,7,4))
        elif piece=='feet':parts=[unwrap([2,2,6],[6,6,10],0,24,4,4,4),unwrap([10,2,6],[14,6,10],16,56,4,4,4)]
        else:parts=[unwrap([4,6,4],[12,14,12],0,0,8,8,8)]
        if setname=='ember_hat':parts.append(unwrap([3,8,3],[13,9,13],0,0,10,1,10))
        item=model(parts,{'wear':f'unrp:models/armor/{setname}_layer_{layer}','particle':f'unrp:models/armor/{setname}_layer_{layer}'})
        item['display']={'gui':{'rotation':[15,25,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[0,-25,0],'translation':[0,2,0],'scale':[.8,.8,.8]}}
        savejson(A/f'models/item/{name}.json',item)
    # The mask's item and worn head use the identical head UV sheet.
    mask=Image.new('RGBA',(64,64));d=ImageDraw.Draw(mask);d.rectangle((0,0,31,15),fill='#1d2027');d.rectangle((8,8,15,15),fill='#272b33');d.line((9,8,14,8),fill='#414650');d.rectangle((9,11,10,11),fill=(0,0,0,0));d.rectangle((13,11,14,11),fill=(0,0,0,0));mask.save(A/'textures/models/armor/joker_mask_layer_1.png')
    item=model([unwrap([4,4,4],[12,12,12],0,0,8,8,8)],{'wear':'unrp:models/armor/joker_mask_layer_1','particle':'unrp:models/armor/joker_mask_layer_1'})
    item['display']={'head':{'scale':[0,0,0]},'gui':{'rotation':[15,25,0],'scale':[1.2,1.2,1.2]}}
    savejson(A/'models/item/joker_mask.json',item)

def power_items():
    # No separate GUI illustration: the actual crystalline mesh is used in every perspective.
    for name,kind,c1,c2,*_ in catalog.ITEMS:
        if kind!='shard':continue
        im=Image.new('RGBA',(16,16),c1);d=ImageDraw.Draw(im)
        d.rectangle((0,0,5,15),fill=c2);d.line((7,0,7,15),fill=c2);d.rectangle((12,0,15,15),fill='#34333b')
        im.save(A/f'textures/item/{name}.png')
        parts=[box([5,3,5],[10,12,10],'crystal'),box([6,12,6],[9,15,9],'crystal'),box([7,15,7],[8,16,8],'crystal'),box([3,3,6],[5,8,9],'crystal'),box([10,4,6],[12,10,8],'crystal')]
        savejson(A/f'models/item/{name}.json',model(parts,{'crystal':f'unrp:item/{name}','particle':f'unrp:item/{name}'}))
    # A full textured blade model, rather than a separate drawn sword thumbnail.
    for name,c in [('ascalon','#6c9678'),('ascalon_mk2','#9fbd86')]:
        im=Image.new('RGBA',(16,16),c);d=ImageDraw.Draw(im);d.rectangle((0,0,3,15),fill='#d7dfba');d.rectangle((12,0,15,15),fill='#3d564c');d.line((7,0,7,15),fill='#b9c79b');im.save(A/f'textures/item/{name}.png')
        parts=[box([7,1,7],[9,5,9],'hilt'),box([4,5,6],[12,7,10],'hilt'),box([6,7,7],[10,13,9],'blade'),box([7,13,7],[9,16,9],'blade')]
        item=model(parts,{'blade':f'unrp:item/{name}','hilt':'unrp:block/phylactery_shell','particle':f'unrp:item/{name}'})
        item['parent']='minecraft:item/handheld'
        if name=='ascalon':item['overrides']=[{'predicate':{'unrp:mk2':1},'model':'unrp:item/ascalon_mk2'}]
        savejson(A/f'models/item/{name}.json',item)

def collision_source():
    target=ROOT/'src/main/java/com/myriam/unrp/item/MachineShapes.java'
    source=target.read_text();start=source.index('        return switch(id) {');end=source.index('            default -> Shapes.block();',start)
    cases='        return switch(id) {\n'
    for name in ('conversion_machine','recovery_machine','suppression_field','phylactery'):
        data=json.loads((A/f'models/block/{name}.json').read_text())
        boxes=['Block.box('+','.join(str(v) for v in e['from']+e['to'])+')' for e in data['elements']]
        cases+='            case "'+name+'" -> Shapes.or('+','.join(boxes)+');\n'
    target.write_text(source[:start]+cases+source[end:])

def main():
    machines();wardrobe();power_items();collision_source()
    print('Rebuilt actual placed models, wearable UVs and matching inventory/held meshes.')
if __name__=='__main__':main()
