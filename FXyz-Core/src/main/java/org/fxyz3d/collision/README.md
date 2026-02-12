# Collision Package Layout

Collision code is organized by responsibility while keeping package names stable (`org.fxyz3d.collision`).

## Main Source Folders

- `bounds/`: AABB and sphere bounds primitives.
- `broadphase/`: broad-phase interfaces and implementations.
- `narrowphase/`: SAT/GJK/CCD/intersection primitives.
- `contact/`: contact generation, manifolds, caching, solving.
- `constraints/`: point/distance and generic constraint contracts.
- `pipeline/`: pairing/filtering pipeline orchestration.
- `world/`: world stepping, responders, body adapters.
- `filtering/`: collision kinds, filters, mask/layer logic.
- `events/`: collision event payloads and event types.
- `debug/`: debug snapshot models.
- `adapters/`: JavaFX/node collision adapters.
- `geometry/`: ray and related helpers.

## Test Source Mirrors

Tests under `FXyz-Core/src/test/java/org/fxyz3d/collision/` mirror the same folder structure for easier navigation.
