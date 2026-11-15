#!/usr/bin/env python3
"""Render the actual Minecraft item model JSON, UVs and PNGs. No substitute icon drawings.
Orthographic software renders show asset geometry; Minecraft lighting/hand poses differ.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageEnhance
import json,math,base64,hashlib,sys
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/unrp'

def resolve(name,seen=None):
    seen=set() if seen is None else seen
    if name in seen:raise ValueError('Model inheritance cycle: '+name)
    seen.add(name)
    p=A/'models'/(name.removeprefix('unrp:')+'.json');data=json.loads(p.read_text())
    parent=data.get('parent','')
    if parent.startswith('unrp:'):
        inherited=resolve(parent,seen);data={**inherited,**data,'textures':{**inherited.get('textures',{}),**data.get('textures',{})}}
    return data

def texture(model,key):
    seen=set();value=key
    while value.startswith('#'):
        if value in seen:raise ValueError('Texture cycle')
        seen.add(value);value=model['textures'][value[1:]]
    if not value.startswith('unrp:'):raise ValueError('Unsupported external texture '+value)
    path=A/'textures'/(value[5:]+'.png')
    return path

def project(p):
    x,y,z=p;return ((x+z)*2.55, (x-z)*1.45-y*2.85)

def rendered(name):
    model=resolve('unrp:item/'+name)
    if 'elements' not in model:
        key=model.get('textures',{}).get('layer0')
        if key is None:raise ValueError('No geometry or layer0: '+name)
        return Image.open(texture(model,key)).convert('RGBA').resize((80,80),Image.Resampling.NEAREST)
    faces=[]
    for e in model['elements']:
        x0,y0,z0=e['from'];x1,y1,z1=e['to']
        # Face vertices follow the model face UV orientation.
        points={'north':[(x1,y1,z0),(x0,y1,z0),(x1,y0,z0),(x0,y0,z0)],
                'east':[(x1,y1,z1),(x1,y1,z0),(x1,y0,z1),(x1,y0,z0)],
                'up':[(x0,y1,z0),(x1,y1,z0),(x0,y1,z1),(x1,y1,z1)]}
        for face,pts in points.items():
            if face not in e.get('faces',{}):continue
            # Optional Blockbench element rotation around its configured origin.
            if 'rotation' in e:
                rot=e['rotation'];o=rot['origin'];ang=math.radians(rot['angle']);c,s=math.cos(ang),math.sin(ang)
                def rotate(p):
                    q=[p[i]-o[i] for i in range(3)];a,b={'x':(1,2),'y':(2,0),'z':(0,1)}[rot['axis']];q[a],q[b]=q[a]*c-q[b]*s,q[a]*s+q[b]*c
                    return tuple(q[i]+o[i] for i in range(3))
                pts=[rotate(p) for p in pts]
            depth=sum(p[0]-p[2]+p[1]*.25 for p in pts)/4
            faces.append((depth,face,[project(p) for p in pts],e['faces'][face]))
    allpts=[p for _,_,ps,_ in faces for p in ps];xmin=min(p[0] for p in allpts);xmax=max(p[0] for p in allpts);ymin=min(p[1] for p in allpts);ymax=max(p[1] for p in allpts)
    scale=min(72/max(1,xmax-xmin),72/max(1,ymax-ymin))
    out=Image.new('RGBA',(80,80));
    for _,face,points,data in sorted(faces,key=lambda f:f[0]):
        pts=[((x-(xmin+xmax)/2)*scale+40,(y-(ymin+ymax)/2)*scale+40) for x,y in points]
        tex=Image.open(texture(model,data['texture'])).convert('RGBA')
        u0,v0,u1,v1=data.get('uv',[0,0,16,16]);u0*=tex.width/16;u1*=tex.width/16;v0*=tex.height/16;v1*=tex.height/16
        p0,p1,p2,p3=pts;ax=p1[0]-p0[0];ay=p1[1]-p0[1];bx=p2[0]-p0[0];by=p2[1]-p0[1];det=ax*by-ay*bx
        if abs(det)<1e-6:continue
        a=(u1-u0)*by/det;b=-(u1-u0)*bx/det;c=u0-a*p0[0]-b*p0[1]
        dd=-(v1-v0)*ay/det;ee=(v1-v0)*ax/det;ff=v0-dd*p0[0]-ee*p0[1]
        tile=tex.transform(out.size,Image.Transform.AFFINE,(a,b,c,dd,ee,ff),Image.Resampling.NEAREST)
        mask=Image.new('L',out.size);ImageDraw.Draw(mask).polygon([p0,p1,p3,p2],fill=255)
        import PIL.ImageChops
        tile.putalpha(PIL.ImageChops.multiply(tile.getchannel('A'),mask))
        tile=ImageEnhance.Brightness(tile).enhance({'up':1,'north':.9,'east':.72}[face]);out.alpha_composite(tile)
    return out

def main():
    names=sorted(p.stem for p in (A/'models/item').glob('*.json') if p.stem!='shard_3d')
    rows=(len(names)+7)//8;sheet=Image.new('RGBA',(112*8,112*rows+48),'#252b32');draw=ImageDraw.Draw(sheet)
    draw.text((12,10),'ACTUAL ITEM MODELS + UV TEXTURES / orthographic asset renders',fill='#e6d7b5')
    draw.text((12,24),'Same model files used by inventory and held items; not Minecraft screenshots.',fill='#a9b6c2')
    manifest={}
    for i,name in enumerate(names):
        icon=rendered(name);x=i%8*112+16;y=i//8*112+48;sheet.alpha_composite(icon,(x,y))
        draw.text((x-10,y+83),name[:19],fill='#c9c4b9')
        m=resolve('unrp:item/'+name);paths=set()
        for v in m.get('textures',{}).values():
            if v.startswith('unrp:'):paths.add(str(texture(m,v).relative_to(A)))
        manifest[name]={'model':'models/item/'+name+'.json','textures':{p:hashlib.sha256((A/p).read_bytes()).hexdigest() for p in sorted(paths)}}
    sheet.save(ROOT/'docs/unrp-items-preview.png')
    data=base64.b64encode((ROOT/'docs/unrp-items-preview.png').read_bytes()).decode()
    (ROOT/'docs/unrp-items-preview.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{sheet.width}" height="{sheet.height}"><image width="{sheet.width}" height="{sheet.height}" href="data:image/png;base64,{data}"/></svg>')
    (ROOT/'art/equipment-preview-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    print('Rendered',len(names),'actual item models from their production textures.')
if __name__=='__main__':main()
