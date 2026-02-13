# Jolt / PhysX Integration Plan

## Goal

Add a second high-performance general-physics backend with minimal risk and maximum reuse of the existing DynamisFX physics architecture.

Recommended order:

1. Build `Jolt` first.
2. Add `PhysX` only if a clear gap remains after Jolt validation.

## Why This Order

- Jolt gives modern rigid-body quality with a focused feature set and strong performance.
- It is a better first expansion target from ODE4j with lower integration burden than going all-in on PhysX immediately.
- PhysX is still valuable, but should be justified by concrete unmet requirements.

## Phase 0 - Scope and Acceptance Criteria

Status: `PLANNED`

Define up front:

- Target scenarios:
  - stacked rigid bodies
  - constrained mechanisms (hinge/slider chains)
  - fast-moving objects (CCD)
  - medium/large scene throughput
- Required API parity through `PhysicsWorld` SPI.
- Pass/fail thresholds against ODE4j baseline:
  - stability (jitter/explosions/tunneling)
  - throughput (step time)
  - determinism tolerance
  - constraint robustness

Exit criteria:

- Written benchmark and behavior criteria are agreed before backend coding starts.

## Phase 1 - Jolt Backend MVP (`FXyz-Physics-Jolt`)

Status: `PLANNED`

Deliverables:

- New module: `FXyz-Physics-Jolt`
- SPI implementation parity for current scope:
  - body create/remove/list
  - body state read/write
  - world stepping
  - shapes: box/sphere/capsule
  - constraints: `BALL`, `FIXED`, `HINGE`, `SLIDER`
- Capability reporting integrated with strict/lenient gate paths.
- One sample equivalent to ODE4j sample behavior.

Exit criteria:

- Jolt backend can run existing physics flow without changing `DynamisFX-Core` public SPI.

## Phase 2 - Jolt Hardening

Status: `PLANNED`

Deliverables:

- CCD stress tests and regression scenes.
- Constraint stress tests (chains/stacks/high-mass ratios).
- Benchmark harness comparison vs ODE4j.
- Coordinator telemetry parity (timing + rejection diagnostics).
- Documentation updates:
  - backend feature matrix
  - limitations and known gaps

Exit criteria:

- Jolt meets or exceeds agreed Phase 0 thresholds in required scenarios.

## Phase 3 - Decision Gate (Jolt-only vs PhysX)

Status: `PLANNED`

Decision questions:

- Are any required scenarios still failing with Jolt?
- Is missing functionality specifically available/stronger in PhysX?
- Is native packaging/licensing/ops complexity justified by value?

Outcomes:

- If no major gap: standardize on ODE4j + Jolt.
- If major gap remains: proceed to Phase 4 (PhysX backend).

## Phase 4 - PhysX Backend (Conditional)

Status: `PLANNED`

Deliverables:

- New module: `FXyz-Physics-PhysX`
- Same SPI contract and comparable feature slice as Jolt MVP.
- Reuse same regression/benchmark suites for apples-to-apples comparison.
- Capability matrix update with concrete strengths/limitations.

Exit criteria:

- PhysX backend provides measurable value over Jolt for targeted unmet needs.

## Phase 5 - Productionization

Status: `PLANNED`

Deliverables:

- Backend selection configuration:
  - `ode4j`
  - `jolt`
  - `physx` (if added)
- Packaging/distribution strategy per OS/arch for native backends.
- CI backend matrix and smoke tests.
- Final migration guidance:
  - recommended backend by scenario
  - known tradeoffs

Exit criteria:

- Stable multi-backend physics deployment with clear operational guidance.

## Immediate Next Slice

1. Create `FXyz-Physics-Jolt` module scaffold.
2. Wire Maven + backend factory + empty world implementation.
3. Add lifecycle/capability tests and one minimal sample.
