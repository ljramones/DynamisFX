# FXyz 3D Import/Export Roadmap

## Purpose
Define a practical capability roadmap for FXyz 3D import/export, focused on real user workflows and incremental delivery.

## Current Baseline (Today)
- Strong static mesh import coverage: OBJ, STL, glTF, PLY, OFF, 3DS, DAE, X3D, VRML, DXF, Maya, FXML, 3MF.
- Static mesh export coverage: STL, OBJ, glTF/GLB, PLY, OFF, 3MF.
- USD import is MVP-only (`.usda` mesh subset).
- No FBX import/export.
- No full scene export pipeline (export API is mesh-centric).
- No animation/skeleton/morph export pipeline.

## Guiding Principles
- Prefer stable intermediate contracts (TUF) for complex formats.
- Ship user-visible value early (conversion-assisted paths are acceptable).
- Keep importer behavior deterministic with corpus-based regression checks.
- Defer high-complexity format fidelity (animation, full material graphs) until core static workflows are stable.

## V1: Production Static Mesh Workflow
Goal: “Users can reliably import/export static meshes for common pipelines.”

### Import
- Finalize USD MVP behavior and docs:
  - `.usda` static mesh only.
  - Clear errors for unsupported USD features.
- Add FBX static import via converter path:
  - `FBX -> glTF -> import` (or `FBX -> glTF -> TUF -> import`).
- Normalize coordinates/units consistently (single internal convention).
- Enforce triangulated output for import pipeline.

### Export
- Keep mesh-level exporters stable (STL/OBJ/glTF/PLY/OFF/3MF).
- Add explicit feature matrix docs per exporter:
  - what is preserved/lost (UVs, normals, materials, smoothing groups).

### Quality
- Build corpus tests for:
  - transform baking
  - normals/UV fidelity
  - count/AABB regression checks
- Fix advertised-vs-implemented mismatches (e.g., ASE if still listed).

## V2: Scene-Aware Interchange
Goal: “Move beyond single meshes to scene-level workflows.”

### Import
- USD expansion:
  - basic scene hierarchy
  - transforms
  - optional materials/textures
  - targeted `.usdz` handling (if feasible in selected backend)
- FBX robustness improvements:
  - multi-node hierarchy preservation option
  - better materials mapping

### Export
- Introduce scene export API (new abstraction above `TriangleMesh`):
  - multiple nodes
  - per-node transform
  - material assignment
- Add scene-level glTF export path first (best ecosystem fit).

### Platform
- Introduce canonical conversion pipeline:
  - normalize imported scene -> internal model -> format-specific writer.

## V3: Advanced Fidelity (Selective)
Goal: “Support advanced DCC/engine handoff where it matters.”

### Import
- USD advanced features:
  - composition/variants (limited target subset)
  - broader primvar/material support
- FBX advanced support:
  - animation curves
  - skinning/bones
  - blendshapes (if demand exists)

### Export
- Animation-aware export path (start with glTF animation).
- Skinning/morph export for formats that support it.
- Optional tangent generation/validation pipeline for PBR workflows.

### Architecture
- Optional native extraction bridges (Panama + C ABI) for high-fidelity USD/FBX where converter routes are insufficient.

## Cross-Cutting Backlog
1. Create a single capability matrix document per format:
   - Geometry, UV, normals, materials, hierarchy, animation, skinning, morphs.
2. Add importer diagnostics model:
   - warnings for dropped features instead of silent loss.
3. Add “keep intermediate files” debug option for converter-backed importers.
4. Add structured telemetry/logging for failed imports (feature unsupported vs parse error).
5. Add end-user docs with “best format for X” recommendations:
   - runtime assets: glTF/GLB
   - printing: STL/3MF
   - legacy/simple interchange: OBJ/PLY/OFF

## Suggested Delivery Order
1. V1 FBX static import (converter-backed).
2. USD MVP hardening + regression corpus.
3. Capability matrix + diagnostics.
4. Scene export API design and glTF scene exporter (V2 kickoff).
5. Selective advanced fidelity work by demand (V3).
