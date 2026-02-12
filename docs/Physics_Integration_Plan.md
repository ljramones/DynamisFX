# Physics Integration Plan (Orekit + ODE4j)

## Goal

Integrate two physics domains without coupling FXyz core to a specific engine:

- `ODE4j` for local/general rigid-body simulation.
- `Orekit` for astrodynamics/orbital propagation.

## Principles

- Keep `FXyz-Core` engine-agnostic.
- Define stable SPI/API first, then adapters.
- Use fixed-step simulation with immutable state snapshots for render sync.
- Keep unit/frame/time conventions explicit.

## Phased Roadmap

### Phase 0 - Architecture Freeze

Status: `COMPLETE`

Deliverables:

- Canonical conventions:
  - units: meters, kilograms, seconds, radians
  - explicit reference frames per state
- Engine-agnostic SPI boundaries in `FXyz-Core`
- Architecture document and non-goals

Exit criteria:

- No Orekit/ODE4j-specific classes in `FXyz-Core` public API.

### Phase 1 - Core SPI + Sync Skeleton

Status: `COMPLETE`

Deliverables:

- `org.fxyz3d.physics.api` interfaces for backends/worlds.
- `org.fxyz3d.physics.model` immutable state/config records.
- `org.fxyz3d.physics.sync` node/body binding and state-application helpers.
- Fixed-step accumulator utility.
- Unit tests for determinism, binding/sync behavior, and validation.

Exit criteria:

- A mock backend can drive scene-state synchronization through the SPI.

### Phase 2 - ODE4j Adapter MVP

Status: `IN PROGRESS`

Deliverables:

- New module: `FXyz-Physics-ODE4j`
- Rigid bodies (box/sphere/capsule), gravity, core constraints
- Adapter implementation through SPI only
- Samples + regression tests

Exit criteria:

- Stable rigid-body sample with no direct ODE4j leakage into `FXyz-Core`.

Current kickoff progress:

- Module created with Maven wiring and parent aggregation.
- Backend shell implemented:
  - `Ode4jBackendFactory`
  - `Ode4jBackend`
  - `Ode4jWorld`
- Initial lifecycle tests added.
- `Ode4jWorld` now uses ODE4j world/body/geom/mass integration for:
  - body creation by shape
  - state read/write mapping
  - gravity-driven step evolution
- ODE4j collision/contact path added:
  - space collision callback
  - contact joint generation
- Minimal constraint mapping added:
  - SPI: `PhysicsConstraintDefinition` (`FIXED`, `BALL`)
  - ODE4j world create/remove/list support
- FXyz sample integration added:
  - `org.fxyz3d.samples.utilities.Ode4jPhysicsSyncSample`
- World-level tuning parameters added in core model config and wired to ODE4j:
  - solver iterations
  - friction
  - bounce
  - soft CFM
  - bounce velocity

Next sub-phase:

- tune contact parameters and expose configuration in world config
- add additional joint types and backend capability reporting by joint family

### Phase 3 - Orekit Adapter MVP

Status: `IN PROGRESS`

Deliverables:

- New module: `FXyz-Physics-Orekit`
- Propagator-backed body states and frame-aware transforms
- Time-scale controls and orbital sample

Exit criteria:

- Orbital entities updated through the same SPI contract.

Current kickoff progress:

- Module created with Maven wiring and parent aggregation.
- Backend shell implemented:
  - `OrekitBackendFactory`
  - `OrekitBackend`
  - `OrekitWorld`
- Orbital stepping scaffold added for inertial-frame N-body style integration:
  - static central bodies can influence dynamic body acceleration
  - fixed-step state updates remain backend-neutral through `PhysicsWorld`
- Runtime tuning plumbing preserved through the shared SPI for consistency.
- Sample integration added:
  - `org.fxyz3d.samples.utilities.OrekitOrbitSyncSample`
- Initial regression tests added for:
  - backend factory + capabilities
  - basic orbital stepping behavior
  - unsupported constraint path + lifecycle guards

### Phase 4 - Hybrid Coordinator

Status: `PENDING`

Deliverables:

- Coordinator layer to run both worlds in one timeline
- State handoff policy and ownership rules
- Thread-safe snapshot exchange

Exit criteria:

- Deterministic mixed simulation tick with clear ownership.

### Phase 5 - Hardening

Status: `PENDING`

Deliverables:

- Capability flags and feature gating
- Profiling/debug tooling
- Snapshot serialization/replay
- Migration guide and docs

Exit criteria:

- Production-ready extension model.

## Immediate Execution Slice (Current)

Implemented in this slice:

- Commit-level consolidation of Phase 0/1/2 foundations.
- Phase 3 kickoff module and orbital scaffold:
  - `FXyz-Physics-Orekit`
  - backend/world shell + tests + sample wiring.

Next slice:

- Replace orbital scaffold internals with Orekit propagator primitives.
- Add explicit time-scale controls and frame transformation hooks.
- Define Phase 4 hybrid coordinator ownership contracts.
