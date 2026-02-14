# DynamisFX Full Technical Rename Plan (Phase 2)

## Objective

Complete the transition from FXyz branding to DynamisFX across technical identifiers, while minimizing breakage:

- Maven coordinates
- Java module names
- Java package names
- Public class names with FXyz prefix
- Repository/module directory names

This plan assumes the Phase 1 branding pass is already complete.

## Scope

Rename targets:

- `groupId`: `org.fxyz3d` -> `org.dynamisfx`
- Parent `artifactId`: `fxyz3d-parent` -> `dynamisfx-parent`
- Module `artifactId`s:
  - `fxyz3d` -> `dynamisfx-core`
  - `fxyz3d-client` -> `dynamisfx-client`
  - `fxyz3d-importers` -> `dynamisfx-importers`
  - `fxyz3d-physics-ode4j` -> `dynamisfx-physics-ode4j`
  - `fxyz3d-physics-orekit` -> `dynamisfx-physics-orekit`
  - `fxyz3d-samples` -> `dynamisfx-demo`
- JPMS module names (where applicable): `org.fxyz3d.*` -> `org.dynamisfx.*`
- Root packages: `org.fxyz3d` -> `org.dynamisfx`
- Class names containing product prefix (example: `FXyzClient` -> `DynamisFXClient`)
- Repository folders (optional, last): `FXyz-*` -> `DynamisFX-*`

Out of scope for this pass:

- historical license header text in legacy files (can be a separate legal cleanup)
- runtime feature behavior changes unrelated to rename

## Risk Profile

High-risk break areas:

- external consumers importing `org.fxyz3d.*`
- JPMS module-path users (`module-info.java` requires/exports)
- reflection/resource lookups with package strings
- ServiceLoader registrations
- sample launcher entry points

## Strategy

Use a staged migration with a compatibility window.

## Phase A - Coordinate Split (Non-code break reduction)

Goal: publish new Maven coordinates while preserving old API package names briefly.

1. Introduce new coordinates first (`org.dynamisfx:*`) using current code.
2. Keep old artifacts available for one transition release.
3. In old artifacts, add relocation metadata (where feasible) and README migration notes.
4. Keep package names unchanged in this phase to reduce immediate code breakage.

Exit criteria:

- New coordinates publish successfully.
- Existing users can still consume previous FXyz coordinates.

## Phase B - Package + JPMS Rename (Breaking)

Goal: perform the actual source-level rename.

1. Bulk rename Java sources:
   - directories `org/fxyz3d` -> `org/dynamisfx`
   - package declarations/imports across all modules
2. Update `module-info.java`:
   - module names
   - `exports`/`requires` clauses
3. Update string-based references:
   - FXML controller paths
   - CSS/resource classpath lookups
   - reflection / property keys
   - ServiceLoader files under `META-INF/services`
4. Update test sources and test resources accordingly.

Exit criteria:

- `mvn clean test` passes in all modules.
- sampler and at least one importer/exporter workflow runs.

## Phase C - Compatibility Shims (Recommended)

Goal: provide a transition layer for downstream projects.

Options:

1. `fxyz-compat` bridge module:
   - package `org.fxyz3d.*` forwarding wrappers to `org.dynamisfx.*`
   - deprecate all wrappers for removal in next major
2. Legacy artifact wrappers:
   - publish minimal FXyz artifacts depending on DynamisFX artifacts
   - include migration warning in build logs/docs

Recommendation:

- implement both for one major cycle if maintenance budget allows.

## Phase D - Repository Layout Rename (Optional)

Goal: align folder names with new identity.

1. Rename module directories:
   - `DynamisFX-Core` -> `DynamisFX-Core` etc.
2. Update root `pom.xml` module paths.
3. Update CI scripts and release pipelines.

Do this only after Phase B is stable to avoid mixing path and package churn.

## Detailed Execution Checklist

1. Freeze feature work on `main` during rename branch.
2. Create long-lived branch: `refactor/dynamisfx-namespace`.
3. Perform automated refactor in this order:
   - Maven coordinates
   - package declarations/imports
   - module-info
   - resources/FXML/services
4. Compile per module incrementally:
   - `mvn -pl <module> -DskipTests compile`
5. Run full tests:
   - `mvn clean test`
6. Run smoke tests:
   - sampler launch
   - at least OBJ/glTF/USD importer path
   - at least STL/OBJ export path
7. Update docs and migration guide:
   - old -> new import examples
   - old -> new dependencies
8. Tag release as a major version bump.

## Versioning and Release Plan

- Current line: `0.6.x` (FXyz coordinates)
- Transition line: `0.7.0` (DynamisFX coordinates, optional compat)
- Breaking package rename: `1.0.0` (or `0.8.0` if staying pre-1.0 policy)

Recommendation:

- Use `1.0.0` for package/module rename to signal explicit break.

## CI/CD Requirements

Add/ensure:

- matrix build for JDK 25+
- module-path compile checks
- sample launcher smoke test
- importer/exporter integration tests with fixture assets
- japicmp/revapi check against prior release (for compatibility reporting)

## Documentation Deliverables

Must ship with rename:

- `docs/Physics_Migration_Guide.md` updates for package/module names
- new `docs/Rename_Migration_Guide.md`:
  - dependency mapping table
  - package import mapping table
  - class rename mapping table
  - common failure troubleshooting
- README dependency snippets updated to new coordinates

## Mapping Tables (Initial)

### Maven

- `org.fxyz3d:fxyz3d-parent` -> `org.dynamisfx:dynamisfx-parent`
- `org.fxyz3d:fxyz3d` -> `org.dynamisfx:dynamisfx-core`
- `org.fxyz3d:fxyz3d-client` -> `org.dynamisfx:dynamisfx-client`
- `org.fxyz3d:fxyz3d-importers` -> `org.dynamisfx:dynamisfx-importers`
- `org.fxyz3d:fxyz3d-physics-ode4j` -> `org.dynamisfx:dynamisfx-physics-ode4j`
- `org.fxyz3d:fxyz3d-physics-orekit` -> `org.dynamisfx:dynamisfx-physics-orekit`
- `org.fxyz3d:fxyz3d-samples` -> `org.dynamisfx:dynamisfx-demo`

### Packages

- `org.fxyz3d.*` -> `org.dynamisfx.*`

### JPMS (example pattern)

- `org.fxyz3d.core` -> `org.dynamisfx.core`
- `org.fxyz3d.importers` -> `org.dynamisfx.importers`
- `org.fxyz3d.client` -> `org.dynamisfx.client`

## Rollback Plan

If Phase B destabilizes too many modules:

1. Revert branch to Phase A tag.
2. Ship coordinates-only transition release.
3. Reattempt package rename module-by-module (Core -> Importers -> Client -> Samples).

## Immediate Next Step

Create a dry-run branch and execute Phase A only (coordinates), then publish a migration preview document for early adopters before Phase B.
