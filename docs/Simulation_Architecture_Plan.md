# Simulation Architecture Plan

## Goal
Build a coupled simulation architecture in DynamisFX with:
- `OrbitalDynamicsEngine` for global astrodynamics truth (Orekit).
- `RigidBodyWorld` for local collision/constraint truth (ODE4J/Jolt/PhysX).
- `CouplingManager` + `PhysicsZone` for promotion/demotion between global and local simulation modes.

## Scope and Non-Goals
- Scope: engine contracts, zones, coupling lifecycle, threading/render sync, phased implementation.
- Non-goal: forcing Orekit and rigid engines into one interface.
- Non-goal: bit-identical behavior across backends.

## Core Concepts
- Two engine families:
  - Orbital: global frames/time propagation, events, ephemerides.
  - Rigid-body: local contact dynamics, constraints, scene interaction.
- Object modes:
  - `ORBITAL_ONLY`
  - `PHYSICS_ACTIVE`
  - `KINEMATIC_DRIVEN`
- Precision model:
  - Global inertial/planet-fixed frames for orbital state.
  - Zone-local frames for rigid-body simulation.
- Rendering model:
  - Double-buffered `TransformStore`, simulation writes and JavaFX reads.

## Proposed Contracts
- `OrbitalDynamicsEngine`
  - Propagate states for time interval.
  - Provide frame transforms and event queries.
  - Input/output in strict SI units.
- `RigidBodyWorld`
  - `step(dt, substeps)`
  - create/destroy bodies and shapes via descriptors
  - apply forces/impulses, set/get body motion state
  - query API: raycast/overlap (+ optional sweep by capability)
  - contact stream access
  - bulk transform read for efficient JavaFX sync
- `PhysicsZone`
  - `ZoneId`
  - global anchor transform
  - active radius
  - rigid-body backend world
  - object mapping tables (global object id <-> local body handle)
- `CouplingManager`
  - Own zones and object mode state machine.
  - Evaluate promote/demote rules.
  - Reconcile state at boundary transitions.

## Promotion/Demotion Rules
- Promote (`ORBITAL_ONLY` -> `PHYSICS_ACTIVE`) when:
  - object enters zone radius, or
  - predicted interaction/impact window opens.
- Demote (`PHYSICS_ACTIVE` -> `ORBITAL_ONLY`) when:
  - object exits zone + cooldown elapsed + no contact/constraints.
- `KINEMATIC_DRIVEN`:
  - orbital state injected into local rigid world as kinematic body.
- Stability controls:
  - hysteresis thresholds
  - cooldown timers
  - timestamped state reconciliation

## Data and Threading
- Simulation thread:
  - fixed timestep for rigid worlds
  - orbital propagation sampled at simulation time
  - writes next transform buffer atomically
- JavaFX thread:
  - reads last complete transform buffer
  - applies node transforms only
- No per-body callback updates on render path; bulk pull/push only.

## Capability Model
- Baseline rigid capabilities:
  - static/kinematic/dynamic bodies
  - primitive shapes
  - filtering
  - raycast/overlap
  - contact stream
- Optional capabilities:
  - constraints
  - CCD
  - mesh cooking
  - character controller
  - vehicles
  - soft bodies

## Phased Roadmap
### Phase 0: Architecture Freeze (1-2 days)
- Finalize interfaces, units, frame conventions, object mode semantics.
- Acceptance:
  - design review sign-off
  - interface package locations agreed

### Phase 1: Core Scaffolding (2-3 days)
- Add contracts and neutral model types in `DynamisFX-Core`.
- Add `SimulationClock` and double-buffer `TransformStore`.
- Acceptance:
  - compile-only scaffolding merged
  - basic clock and buffer tests pass

### Phase 2: Zone + Coupling State Machine (3-4 days)
- Implement `PhysicsZone` registry and `CouplingManager`.
- Implement promote/demote with hysteresis/cooldown.
- Acceptance:
  - transition unit tests pass
  - no thrash at boundary tests

### Phase 3: ODE4J Backend (4-6 days)
- Implement first `RigidBodyWorld` adapter.
- Add bulk transform read path and baseline queries.
- Acceptance:
  - backend conformance baseline passes
  - simple collision scenario stable

### Phase 4: Orekit Adapter (3-5 days)
- Implement `OrbitalDynamicsEngine` adapter + frame conversions.
- Acceptance:
  - propagation + frame conversion tests pass
  - reconciliation round-trip tests within tolerance

### Phase 5: Vertical Slice Demo (3-4 days)
- Lander scenario:
  - orbital propagation
  - promote below threshold
  - local physics collision
  - demote on escape
- Acceptance:
  - end-to-end demo reproducible
  - no visible transform jitter at transition

### Phase 6: Conformance + Validation (2-3 days)
- Add shared rigid conformance suite and coupling invariants.
- Acceptance:
  - all baseline tests green
  - tolerances documented

### Phase 7: Jolt Backend (1-2 weeks)
- Add Panama-backed module with C shim and capability discovery.
- Acceptance:
  - conformance parity for baseline features
  - packaging and loading verified on target platforms

### Phase 8: Enhancements (ongoing)
- Terrain tiles/heightfields, advanced gravity, docking constraints, CCD, mesh cooking, optional PhysX backend.

## Risks and Mitigations
- Risk: transition instability at mode boundaries.
  - Mitigation: hysteresis/cooldown + timestamped reconciliation + focused tests.
- Risk: backend semantic drift.
  - Mitigation: conformance suite with tolerance-based invariants.
- Risk: native backend packaging complexity.
  - Mitigation: isolate in optional modules and keep stable shim ABI.

## Immediate Next Actions
1. Create Java interface stubs in `DynamisFX-Core` for the four core contracts.
2. Add `TransformStore` and `SimulationClock` skeletons with unit tests.
3. Add a minimal `CouplingManager` transition test matrix.
