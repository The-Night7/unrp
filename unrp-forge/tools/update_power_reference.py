from pathlib import Path
import json,re,shutil
root=Path(__file__).resolve().parents[1];docs=root/'docs';dest=docs/'fidelity'
dest.mkdir(exist_ok=True)
data=json.loads((root/'src/main/resources/data/unrp/unodex/ability_powers.json').read_text())['powers']
for p in dest.glob('*.md'):
 key=p.stem
 if key not in data or key=='covenant':continue
 definition=data[key];moves=definition.get('moves',[])
 names=[m.get('name') for m in moves if m.get('name') not in {'Change Stance','Change Mode'}]
 title=next((a['name'] for a in json.loads((root/'src/main/resources/data/unrp/unodex/abilities.json').read_text())['abilities'] if a['id']==key),key.replace('_',' ').title())
 lines=[f'# {title}', '', f'Runtime definition: `ability_powers.json` → `powers.{key}`. See [POWERS.md](../POWERS.md) for shared fields.', '', '| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |','| --- | --- | ---: | ---: | ---: | --- |']
 for m in moves:
  handler=m.get('special') or m.get('trigger_tag') or m.get('fx',{}).get('kind','passive')
  lines.append(f"| {m.get('name','Unnamed')} | {m.get('input','action')} | {m.get('cost',0)} | {m.get('min_aura',0)} | {m.get('cooldown',0)} | `{handler}` |")
 lines+=['','## Moves','']
 for m in moves:
  if m.get('desc'):lines.append(f"- **{m.get('name','Unnamed')}**: {m['desc']}")
 lines+=['','Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.','']
 p.write_text('\n'.join(lines))
index=['# Power reference','','These pages list the current English move definitions. Costs, minimum aura and cooldowns come from `ability_powers.json`; special handlers may add their own resource rules. Use the linked handlers for runtime behaviour and verify multiplayer effects in game.','','| Power | Reference |','| --- | --- |']
for p in sorted(dest.glob('*.md')):
 if p.name=='README.md':continue
 title=p.read_text().splitlines()[0].lstrip('# ')
 index.append(f'| {title} | [{p.name}]({p.name}) |')
index+=['','Use [ABILITY_DEVELOPMENT.md](../ABILITY_DEVELOPMENT.md) to change a power, and [POWERS.md](../POWERS.md) for field meanings.','']
(dest/'README.md').write_text('\n'.join(index))
