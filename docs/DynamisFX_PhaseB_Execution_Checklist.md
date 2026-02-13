# DynamisFX Phase B Execution Checklist

## Goal

Execute the breaking rename from `org.fxyz3d` to `org.dynamisfx` with controlled risk.

## Branch

- Active branch: `refactor-dynamisfx-namespace`

## Progress

- Completed: Step 1 (package/root migration to `org.dynamisfx`)
- Completed: Step 2 (JPMS module references updated to `org.dynamisfx.*`)
- In progress: Step 3 (public `FXyz*` type rename with compatibility shims)

## Pre-flight

1. Confirm clean baseline:
   - `mvn -DskipTests compile`
2. Snapshot rename surface:
   - `rg -n "org\\.fxyz3d|module org\\.fxyz3d|FXyzClient|FXyzSample|FXyzProject"`
3. Freeze feature merges into this branch until Phase B merge.

## Step 1 - Root package directory move

1. Move source roots per module:
   - `src/main/java/org/fxyz3d` -> `src/main/java/org/dynamisfx`
   - `src/test/java/org/fxyz3d` -> `src/test/java/org/dynamisfx`
2. Update package declarations and imports:
   - `package org.fxyz3d...` -> `package org.dynamisfx...`
   - `import org.fxyz3d...` -> `import org.dynamisfx...`
3. Update string-literal package paths where required (reflection/resource keys only).

Validation:

- `mvn -pl FXyz-Core -DskipTests compile`
- `mvn -pl FXyz-Importers -DskipTests compile`
- `mvn -pl FXyz-Client -DskipTests compile`
- `mvn -pl FXyz-Samples -DskipTests compile`

## Step 2 - JPMS module rename

1. Rename module declarations:
   - `module org.fxyz3d.core` -> `module org.dynamisfx.core`
   - importer/client/samples module names accordingly
2. Update all `requires` and `exports` references.

Validation:

- `mvn -DskipTests compile`
- run sampler module launch smoke test

## Step 3 - Public class prefix cleanup

1. Rename key public types:
   - `FXyzClient` -> `DynamisFXClient`
   - `FXyzSample` -> `DynamisFXSample`
   - `FXyzProject` -> `DynamisFXProject`
2. Keep temporary deprecated adapters (optional for one release):
   - legacy class extends/forwards to renamed class

Validation:

- `mvn -DskipTests compile`
- `mvn test`

## Step 4 - Resource and service wiring

1. Update FXML controller references.
2. Update `META-INF/services/*` entries.
3. Update CSS/resource paths if package-relative loading changed.

Validation:

- launch `FXyz-Samples` and verify major sample pages
- importer/exporter smoke checks

## Step 5 - Documentation and migration guide

1. Add `docs/Rename_Migration_Guide.md`:
   - old import -> new import mapping
   - old coordinates -> new coordinates mapping
   - compatibility notes
2. Update README code snippets to `org.dynamisfx` package names.

## Step 6 - Optional compatibility layer

1. Add small compatibility module for legacy imports:
   - `org.fxyz3d.*` wrappers forwarding to `org.dynamisfx.*`
2. Mark all wrappers deprecated with removal target version.

## Release Gate

All must pass before merge:

- `mvn clean test`
- sampler launch sanity
- import/export sanity (OBJ, STL, glTF minimum)
- no unresolved `org.fxyz3d` references except intentional compatibility wrappers/docs
