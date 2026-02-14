# Physics Migration Guide (2026)

This guide summarizes migration to the current physics architecture in DynamisFX:

- engine-neutral SPI in `org.dynamisfx.physics.api`
- ODE4j backend module (`dynamisfx-physics-ode4j`)
- Orekit backend module (`dynamisfx-physics-orekit`)
- hybrid coordination in `org.dynamisfx.physics.hybrid`

## 1) Use `PhysicsWorld` Instead of Engine-Specific World Types

Before:

- direct engine/world coupling in app code

After:

- create backend via `PhysicsBackendFactory`
- create world via `PhysicsBackend.createWorld(...)`
- work through `PhysicsWorld` (`createBody`, `step`, `getBodyState`, etc.)

## 2) Move Scene Sync to `PhysicsSceneSync`

Use `PhysicsSceneSync<N>` to bind node objects to body handles, then apply frame updates through a state provider.

## 3) Adopt Fixed-Step Simulation

Use `FixedStepAccumulator` to avoid variable-step instability:

- call `advance(frameDt, world::step)`
- consume `FixedStepResult` interpolation/extrapolation metadata for render smoothing

## 4) Hybrid Orchestration (ODE4j + Orekit)

Use `HybridPhysicsCoordinator` when mixing general and orbital simulation:

- register cross-world links via `HybridBodyLink`
- set ownership (`GENERAL` vs `ORBITAL`)
- set handoff mode (`FULL_STATE` vs `POSITION_VELOCITY_ONLY`)
- optionally enforce divergence checks via `ConflictPolicy.REJECT_ON_DIVERGENCE`

## 5) Capability Gate

Choose policy in coordinator constructor:

- `LENIENT` for development/staged rollout
- `STRICT` for production validation

Inspect gate results with `capabilityReport()`.

## 6) Diagnostics and Profiling

Use:

- `latestTelemetry()` for per-step timing/counter data
- `linkDiagnostics()` for per-link divergence/rejection state

## 7) Snapshot Replay Pipeline

- `HybridSnapshotRecorder` for in-memory record/replay
- `HybridSnapshotIO` for binary snapshot stream round-trip

## 8) Breaking/Behavioral Changes to Note

- `HybridBodyLink` now supports optional divergence thresholds for:
  - position
  - linear velocity
  - angular velocity
- `HybridSnapshot` includes render metadata:
  - `interpolationAlpha`
  - `extrapolationSeconds`
- `HybridPhysicsCoordinator.step(...)` has an overload accepting render metadata.
