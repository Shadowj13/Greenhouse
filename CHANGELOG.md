# Changelog

## 0.3.0-beta.2

- Add kelp to both greenhouses: one raw kelp every 30 seconds, with a reusable
  water bucket in the existing substrate slot and the usual bone-meal bonus.
- Show four growth stages with animated vanilla kelp textures and water in the
  pot. The aquatic base replaces the soil surface without overlapping faces;
  the frame, glass, pot, roof, automatic base and GUI layout are preserved.
- Extend the packaged-JAR crop suite to all 31 crop/color entries, including
  aquatic input rejection, menu slots, harvest/export, fertilizer and reload.

## 0.3.0-beta.1

- Promote the verified 0.2.9 implementation to the first installable beta;
  production classes, models, textures and recipes remain unchanged.
- Add Italian installation, update, dedicated-server and crop/automation guides.
- Build a deterministic install ZIP only after the exact packaged JAR passes
  all 151 live server checks; include checksums and a release manifest.
- Extend optional full-client/network and natural-tick QA to poplar.

## 0.2.9-alpha

- Make red, orange and yellow poplar variants reachable and preserve their
  selected color through growth, harvests and reloads.
- Reject foreign-namespace crop/substrate lookalikes.
- Verify all 30 crop/color entries, 300,000 seeded drop rolls and both loaded
  crafting recipes, including real server-menu ingredient consumption.

## 0.2.8-alpha

- Save growing progress between visual stage transitions.
- Avoid dirtying unchanged empty automatic greenhouses.
- Preserve items when the target has a smaller stack limit, respect upper-face
  insertion rules and use both halves of a double chest.
- Drop the pending mature harvest and stored items once on removal.
- Add isolated live server tests, restart and chunk unload checks, old-world
  migration checks and reproducible JAR packaging.
- Preserve the 0.2.7 model, textures, GUI and crop rules.

## 0.2.7-alpha

- Align the selection cuboid with the opaque posts and flat roof rim.
- Use four transparent double-sided glass planes and remove one hidden
  coplanar underside face, keeping the detailed one-block model and pot.
- Recover the actual 0.2.6 assets, retain its GUI and crop behavior, and promote
  root project sources for reproducible builds instead of legacy ZIP patching.

Validation details and explicit client-test limits are in `docs/`.
