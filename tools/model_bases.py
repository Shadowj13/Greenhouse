"""Shared multipart substrate selection; keeps exactly one shell per crop."""


def configure_bases(kind, states):
    models = {f'greenhouse:block/{kind}{suffix}' for suffix in ('', '_aquatic', '_end')}
    plants = [part for part in states['multipart']
              if part.get('apply', {}).get('model') not in models]
    states['multipart'] = [
        {'when': {'AND': [{'crop': '!kelp'}, {'crop': '!sea_pickle'}, {'crop': '!chorus'}]},
         'apply': {'model': f'greenhouse:block/{kind}'}},
        {'when': {'OR': [{'crop': 'kelp'}, {'crop': 'sea_pickle'}]},
         'apply': {'model': f'greenhouse:block/{kind}_aquatic'}},
        {'when': {'crop': 'chorus'}, 'apply': {'model': f'greenhouse:block/{kind}_end'}}
    ] + plants
