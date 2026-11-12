"""Shared exclusive shell selection. Every crop/stage selects exactly one shell."""

SPECIAL_BASES = {
    'aquatic': ('kelp', 'sea_pickle'),
    'end': ('chorus',),
    'stone': ('glow_lichen',),
    'warped': ('twisting_vines',),
    'crimson': ('weeping_vines',),
}


def configure_bases(kind, states):
    models = {f'greenhouse:block/{kind}{suffix}' for suffix in ('',) + tuple('_'+k for k in SPECIAL_BASES)}
    models.add(f'greenhouse:block/{kind}_wart')
    plants = [part for part in states['multipart']
              if part.get('apply', {}).get('model') not in models]
    excluded = [crop for crops in SPECIAL_BASES.values() for crop in crops]
    bases = [{'when': {'AND': [{'crop': '!'+crop} for crop in excluded]},
              'apply': {'model': f'greenhouse:block/{kind}'}}]
    for suffix, crops in SPECIAL_BASES.items():
        when = {'crop': crops[0]} if len(crops) == 1 else {'OR': [{'crop': crop} for crop in crops]}
        bases.append({'when': when, 'apply': {'model': f'greenhouse:block/{kind}_{suffix}'}})
    states['multipart'] = bases + plants
