#!/usr/bin/env python3
"""Regression checks against the production assets; prevents icon/world/worn divergence."""
import unittest,json,hashlib,importlib.util,sys
sys.dont_write_bytecode=True
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/unrp'
spec=importlib.util.spec_from_file_location('preview',Path(__file__).with_name('preview_equipment.py'));preview=importlib.util.module_from_spec(spec);spec.loader.exec_module(preview)
spec=importlib.util.spec_from_file_location('catalog',Path(__file__).with_name('gen_items.py'));catalog=importlib.util.module_from_spec(spec);spec.loader.exec_module(catalog)
class EquipmentAssets(unittest.TestCase):
    def test_every_block_item_inherits_the_actual_placed_model(self):
        for name,*_ in catalog.BLOCKS:
            item=json.loads((A/f'models/item/{name}.json').read_text())
            self.assertEqual({'parent':f'unrp:block/{name}'},item)
            self.assertTrue(preview.resolve('unrp:item/'+name)['elements'])
    def test_clothing_inventory_uses_the_actual_worn_uv_sheet(self):
        for name,piece,setname,*_ in catalog.OUTFITS:
            model=preview.resolve('unrp:item/'+name)
            self.assertEqual(f'unrp:models/armor/{setname}_layer_{2 if piece=="legs" else 1}',model['textures']['wear'])
            with Image.open(preview.texture(model,'#wear')) as image:self.assertEqual((64,64),image.size)
    def test_no_separate_gui_fake_model_for_shards(self):
        for name,kind,*_ in catalog.ITEMS:
            if kind=='shard':
                model=preview.resolve('unrp:item/'+name)
                self.assertNotIn('loader',model);self.assertNotIn('perspectives',model);self.assertTrue(model['elements'])
    def test_all_production_face_uvs_resolve_to_real_pngs(self):
        for path in (A/'models/item').glob('*.json'):
            if path.stem=='shard_3d':continue
            model=preview.resolve('unrp:item/'+path.stem)
            for element in model.get('elements',[]):
                for face in element['faces'].values():
                    self.assertTrue(preview.texture(model,face['texture']).exists())
                    for uv in face['uv']:self.assertTrue(0<=uv<=16,(path,uv))
    def test_preview_manifest_matches_the_packaged_source_textures(self):
        manifest=json.loads((ROOT/'art/equipment-preview-manifest.json').read_text())
        for item,data in manifest.items():
            for path,digest in data['textures'].items():self.assertEqual(digest,hashlib.sha256((A/path).read_bytes()).hexdigest(),(item,path))
    def test_machine_collision_cuboids_match_rendered_models(self):
        source=(ROOT/'src/main/java/com/myriam/unrp/item/MachineShapes.java').read_text()
        for name in ('conversion_machine','recovery_machine','suppression_field','phylactery'):
            row=source.split('case "'+name+'" -> ',1)[1].split(';',1)[0]
            for element in preview.resolve('unrp:block/'+name)['elements']:
                self.assertIn('Block.box('+','.join(str(v) for v in element['from']+element['to'])+')',row)
    def test_mask_has_one_shared_texture_and_no_flat_head_item(self):
        m=preview.resolve('unrp:item/joker_mask')
        self.assertEqual('unrp:models/armor/joker_mask_layer_1',m['textures']['wear'])
        self.assertEqual([0,0,0],m['display']['head']['scale'])
        source=(ROOT/'src/main/java/com/myriam/unrp/client/CosmeticLayer.java').read_text()
        self.assertIn('textures/models/armor/joker_mask_layer_1.png',source)
if __name__=='__main__':unittest.main()
