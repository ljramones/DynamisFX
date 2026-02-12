# USD Import Plan (FXyz Nouveau)

## Goal
Add robust USD import to FXyz with a low-risk, staged approach:
1. Deliver value quickly with a Python-based extractor.
2. Lock a stable, versioned extraction contract.
3. Replace Python with a native C++ shim (OpenUSD-backed) through a C ABI.
4. Integrate with Java via Panama without changing downstream mesh handling.

## MVP Scope (Phase 0)
Target only what we need immediately:
- Static meshes only (`UsdGeom.Mesh`)
- World-space transforms baked
- Triangulated output
- Extract:
  - `positions` (`float3`)
  - `indices` (`uint32` triangles)
  - `normals` (optional)
  - `uvs` (optional)
  - `material assignment` (optional string per mesh)

Explicitly out of scope for MVP:
- Skeletal animation
- Subdivision surface fidelity
- USD variants/layers editing
- Point instancing
- Full composition semantics

## Architecture
USD files are not parsed directly by FXyz at first. Instead:
1. Extract USD -> `TUF v1` (TRIPSUSD binary format).
2. Java importer reads `TUF v1`.
3. Java adapter maps payload to FXyz/JavaFX mesh types.

Later, extraction backend swaps from Python to native C++ while preserving exact `TUF v1` bytes/semantics.

## Phased Delivery

### Phase 1: Python Extractor Prototype
Build CLI tool (`usd_extract.py`) that:
- Inputs: `.usd/.usda/.usdc/.usdz`, options like `--time`, `--triangulate`, `--bake-xforms`, `--include-normals`, `--include-uvs`
- Outputs: `TUF v1` binary file

Requirements:
- Treat extractor output as a stable spec, not throwaway output.
- Include validation/golden checks:
  - Index bounds
  - Triangulation consistency
  - Transform baking consistency
  - Optional AABB checks

Temporary Java integration:
- Run extractor CLI from importer process
- Read generated `TUF v1`
- Build FXyz meshes

### Phase 2: Contract Stabilization
Create a USD test corpus:
- Simple cube
- Quad mesh (triangulation required)
- Multi-mesh nested transforms
- Non-uniform scale transform case
- File with UVs/normals
- Point-instancing case (expected unsupported)

For each corpus file, maintain expected snapshots:
- Mesh count
- Vertex/index counts
- Approx AABB
- Optional hashes for positions/indices

Exit criteria:
- Contract and outputs stable across extractor revisions.

### Phase 3: Native C++ Extractor Shim
Implement OpenUSD-backed C++ shared library with a minimal C ABI:
- Preferred: `extract_to_file(...)` API to avoid cross-boundary ownership complexity
- Optional: in-memory result API with explicit `free` function

Rule:
- Native shim must emit the same contract/output layout as the Python extractor.

### Phase 4: Panama Integration
Replace Python process execution with direct Java->C calls via Panama.
Keep downstream importer pipeline unchanged (still consumes `TUF v1` semantics).

## TUF v1 Contract (TRIPSUSD Extract Binary Format)

### Global Rules
- Little-endian
- 8-byte alignment for sections/blobs
- UTF-8 string table with `u32` lengths
- `u64` offsets from file start
- Versioned and forward-compatible via chunk directory

### File Layout
- `[FileHeader]` (64 bytes)
- `[ChunkDirectory]` (`N * 48` bytes)
- `[ChunkData...]`

### Core Chunks (v1)
- `STRING_TABLE` (`0x0001`) - required
- `STAGE_META` (`0x0002`) - recommended
- `MESH_INDEX` (`0x0003`) - required

### Mesh Record (MESH_INDEX v1)
Per mesh record stores:
- name/path/material string IDs
- mesh flags
- vertex/index counts
- blob offsets/sizes for positions/normals/uvs/indices
- AABB min/max

Blob payloads:
- positions: `float32[vertexCount*3]`
- normals: `float32[vertexCount*3]` (optional)
- uvs: `float32[vertexCount*2]` (optional)
- indices: `uint32[indexCount]`

### Reader Validation Requirements
On Java load:
- Header magic/version/endian/file size checks
- Chunk bounds checks
- Required chunk presence
- Mesh record size/version checks
- Byte-length checks:
  - positions = `vertexCount*3*4`
  - indices = `indexCount*4`
  - normals/uvs if present
- Triangulated constraint (`indexCount % 3 == 0`) when flagged
- String ID and AABB sanity checks

## Reference Implementations to Keep

### Python Reference Packer
Maintain a stdlib-first packer outline that:
- Builds `STRING_TABLE`, `STAGE_META`, `MESH_INDEX`
- Inlines blob area in `MESH_INDEX` for v1 simplicity
- Writes header + directory + payloads
- Includes dummy end-to-end file generation

Notes:
- For large meshes, use `array('f')` / `array('I')` or NumPy for performance.
- If needed later, add `BLOB_ARENA` chunk without breaking v1 readers.

### Java Reader Skeleton
Maintain a defensive Java reader that:
- Uses `FileChannel` + `MappedByteBuffer`
- Parses and validates all core chunks
- Produces neutral `MeshPayload`
- Adapts payload to FXyz mesh creation in a dedicated adapter method

FXyz adapter expectations:
- `positions` -> `TriangleMesh` points
- `uvs` -> tex coords (or dummy `[0f,0f]`)
- `indices` -> JavaFX face format (`p0,t0,p1,t1,p2,t2`)

## Key Risks / Gotchas
- Triangulation consistency across Python and C++ implementations
- Transform baking correctness (`UsdGeomXformable` handling)
- `metersPerUnit` and `upAxis` normalization policy
- USDZ support parity in both prototype and native paths
- Contract drift between Python output and C++ output

## Definition of Done (MVP)
- Python extractor emits valid `TUF v1` for corpus MVP files
- Java reader consumes `TUF v1` and builds FXyz meshes
- Snapshot validations stable in CI
- Clear unsupported-feature behavior/messages

## Planned Next Steps
1. Add `tools/usd/usd_extract.py` with the agreed CLI/options.
2. Add `docs/formats/TUF_v1.md` as normative binary spec.
3. Add Java module/package for `TUF` reader + FXyz adapter.
4. Add corpus + expected snapshots under test resources.
5. Gate merge on corpus regression checks.
