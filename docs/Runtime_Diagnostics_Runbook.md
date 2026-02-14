# Runtime Diagnostics Runbook

This runbook captures the runtime knobs and verification commands for DynamisFX sampler stability on Java 25 / JavaFX 21.

## Baseline commands

- Build all modules:

  ```bash
  mvn clean install
  ```

- Run sampler (default backend path):

  ```bash
  mvn -pl DynamisFX-Samples -DskipTests javafx:run
  ```

- Run sampler with explicit backend:

  ```bash
  mvn -pl DynamisFX-Samples -DskipTests javafx:run -Ddynamisfx.samples.physics.backend=jolt
  ```

## Runtime switches

- `-Ddynamisfx.samples.physics.backend=ode4j|jolt`
  - Requests rigid backend for physics samples.

- `-Ddynamisfx.samples.physics.forceJoltFailure=true`
  - Forces Jolt selection to fall back to ODE4j (test path).

- `-Ddynamisfx.samples.enableScriptEngine=true`
  - Enables script engine probing for script controls.
  - Default is disabled to avoid noisy provider warnings when GraalJS is unavailable.

- `-Ddynamisfx.client.transparentWindow=true`
  - Enables legacy transparent sampler frame (off by default).

- `-Ddynamisfx.client.hiddenSides=true`
  - Enables legacy hidden-sides menu behavior (off by default).

## What to check in sampler UI

Open the welcome page and inspect **Runtime Diagnostics**:

- Java / JavaFX / OS values should match expected environment.
- Backend resolution line should show requested and resolved backend.
- Fallback should indicate `true` only when fallback path was used.
- Script engine status should show `enabled` and `available` values.
- Jolt line appears when requested/resolved backend is Jolt.

## Focused verification commands

- Sampler service discovery + backend fallback tests:

  ```bash
  mvn -pl DynamisFX-Samples test -Dtest=org.dynamisfx.samples.utilities.ServiceDiscoverySmokeTest,org.dynamisfx.samples.utilities.RigidBodyBackendSelectorTest
  ```

- Forced-fallback path only:

  ```bash
  mvn -pl DynamisFX-Samples test -Dtest=RigidBodyBackendSelectorTest#joltForcedFailureFallsBackToOde4j -Ddynamisfx.samples.physics.forceJoltFailure=true
  ```

- Jolt runtime diagnostics unit test:

  ```bash
  mvn -pl DynamisFX-Physics-Jolt test -Dtest=JoltRuntimeDiagnosticsTest
  ```

- jlink smoke package:

  ```bash
  mvn -pl DynamisFX-Samples -DskipTests clean package javafx:jlink
  ```

## Known non-fatal warnings

- JavaFX internal/native warnings from upstream modules may still appear on Java 25.
- `sun.misc.Unsafe` deprecation warnings from third-party dependencies are upstream and non-fatal.

## Release gate

Before release, confirm:

1. CI matrix passes on Linux and macOS with JDK 25.
2. Sampler launches and sample tree is populated.
3. Selecting physics samples does not crash while switching rapidly.
4. Backend fallback path is deterministic and tested.
5. `javafx:jlink` smoke packaging succeeds.
