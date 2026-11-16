#!/usr/bin/env python3
"""Build the real two-block bed and its complete item model from existing machine textures."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/unrp'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n')
def box(lo,hi,texture):
    return {'from':lo,'to':hi,'faces':{face:{'texture':'#'+texture,'uv':[0,0,16,16]} for face in ('north','south','east','west','up','down')}}
textures={'shell':'unrp:block/recovery_machine_shell','screen':'unrp:block/recovery_machine_screen','vent':'unrp:block/recovery_machine_vent','mattress':'minecraft:block/light_gray_wool','blanket':'minecraft:block/cyan_wool','pillow':'minecraft:block/white_wool','particle':'unrp:block/recovery_machine_shell'}
parts={}
for part in ('head','foot'):
    elements=[box([1,3,0],[15,5,16],'shell'),box([2,5,0],[14,8,16],'mattress'),box([1,0,2],[3,3,4],'shell'),box([13,0,12],[15,3,14],'shell'),box([0,5,1],[1,9,15],'shell'),box([15,5,1],[16,9,15],'shell')]
    if part=='head':
        elements += [box([3,8,2],[13,9,7],'pillow'),box([1,5,0],[15,10,1],'shell'),box([14,8,1],[16,15,3],'shell'),box([11,13,0],[16,20,3],'shell'),box([11.25,13.5,3.01],[15.75,19.5,3.03],'screen')]
    else:
        elements += [box([2,8,1],[14,9,16],'blanket'),box([2,7,14],[14,8,16],'shell'),box([1,5,15],[15,8,16],'shell'),box([4,3,12],[12,5,15],'vent')]
    parts[part]=elements
    write(A/f'models/block/recovery_bed_{part}.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,'elements':elements})
variants={}
for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
    for part in parts:
        variants[f'facing={facing},part={part}']={'model':f'unrp:block/recovery_bed_{part}','y':angle,'uvlock':True}
write(A/'blockstates/recovery_bed.json',{'variants':variants})
# Inventory/held representation contains the same geometry as BOTH placed halves.
import copy
whole=copy.deepcopy(parts['head'])
for element in copy.deepcopy(parts['foot']):
    element['from'][2]+=16;element['to'][2]+=16;whole.append(element)
write(A/'models/item/recovery_bed.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,'elements':whole,'display':{'gui':{'rotation':[25,135,0],'translation':[0,0,0],'scale':[.42,.42,.42]},'ground':{'translation':[0,2,0],'scale':[.25,.25,.25]},'fixed':{'scale':[.4,.4,.4]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2,0],'scale':[.3,.3,.3]},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,0,0],'scale':[.35,.35,.35]}}})
print('Generated both recovery-bed halves and the matching complete item model.')
