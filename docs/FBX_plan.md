# FBX Import Plan (FXyz Nouveau)

## Goal
Deliver a practical FBX importer with minimal risk by reusing the same strategy as USD:
1. Fast prototype path using conversion tools.
2. Stable internal contract (prefer TUF).
3. Optional native path later only if fidelity/performance requires it.

## Reality Constraints
- FBX is proprietary and exporter behavior varies.
- Official high-fidelity route is Autodesk FBX SDK (C++).
- Full in-Java robust parsing is high-risk and high-maintenance.

## FBX v1 Scope (Static Meshes)
Support:
- Static mesh geometry
- Multi-mesh files
- Optional hierarchy preservation (default recommended: bake/flatten)
- Baked world transforms
- Triangle indices
- Normals and UVs when available
- Material name capture (string only)
- Units and axis normalization into TRIPS convention

Out of scope for v1:
- Animation
- Skinning/rigging
- Blendshapes
- Cameras/lights
- Full material graph fidelity

## Recommended Architecture
Primary path:
- FBX -> converter -> glTF -> FXyz importer

Preferred canonical path after initial prototype:
- FBX -> converter -> glTF -> TUF -> FXyz importer

Reason:
- Fast time-to-value now.
- Single internal representation later (TUF) shared with USD and other importers.

## Phased Plan

### Phase 1: "Hello FBX" Prototype
Deliverable:
- An experimental FBX import action that runs a converter and loads result.

Implementation:
- Add external tool runner utility:
  - OS/arch binary selection
  - stdout/stderr capture
  - robust timeout/error surface
- Start with `fbx2gltf` conversion.
- Feed result into existing glTF import path.

Acceptance:
- Known FBX test file imports and renders correctly.

### Phase 2: Contract + Regression Stabilization
Deliverable:
- Deterministic FBX corpus tests and expected outputs.

Corpus should include:
- Single cube
- Multi-mesh with parent transforms
- Non-uniform scale
- Negative scale/mirror
- UV + normals case
- Blender-exported FBX
- Maya/Max-exported FBX (if available)
- Known-bad file for graceful failure

Checks per file:
- Mesh count
- Vertex/index counts
- AABB tolerance checks
- Optional buffer hash checks

### Phase 3: Canonical Pipeline via TUF
Deliverable:
- `glTF -> TUF` packer and importer route.

Benefits:
- Unified importer debugging and validation.
- Easier parity across FBX and USD paths.
- Cleaner evolution for future native extractors.

Target defaults:
- `TRIANGULATED = true`
- `BAKE_XFORMS = true`

### Phase 4: Packaging + UX
Decisions:
- Bundle converter binaries, or
- Plugin/"bring-your-own-converter" mode.

UX requirements:
- Clear failure message + actionable stderr tail
- Full log location
- Cache intermediate files
- Optional "keep intermediate files" setting

### Phase 5: Hardening
Focus:
- Coordinate system normalization consistency
- Unit conversion consistency
- Transform baking correctness
- Triangulation guarantees
- Cross-exporter behavior differences

## Optional Native Path (Plan B)
Only if converter path is insufficient:
- Implement C++ shim over FBX SDK.
- Expose a small C ABI (e.g. `extract_to_file(...)`).
- Call from Java via Panama.
- Keep output contract identical to TUF to avoid downstream changes.

## Milestones
### M1: FBX importer exists
- Converter runner integrated
- glTF import from converted FBX works
- Mesh renders in JavaFX

### M2: FBX importer is stable
- Corpus in place
- Regression tests in CI
- Error handling/logging hardened

### M3: Pipeline unified
- glTF -> TUF packer implemented
- FXyz importer reads TUF path for FBX

### M4: User-ready
- Packaging strategy finalized
- Cache/intermediate handling
- Friendly diagnostics

## Open Decisions
1. Should v1 preserve hierarchy or default to baked/flattened meshes?
2. What is the canonical internal coordinate convention (Y-up vs Z-up)?
3. Should converter binaries be bundled or externally installed?
4. Is TUF mandatory for FBX in v1, or can direct glTF import ship first?

## Recommended Defaults
- Start with conversion (`fbx2gltf`) and ship quickly.
- Bake/flatten transforms for v1.
- Normalize into one internal coordinate/unit convention.
- Add TUF canonicalization immediately after prototype stabilization.
