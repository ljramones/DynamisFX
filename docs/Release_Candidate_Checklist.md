# Release Candidate Checklist

Use this checklist for each release candidate.

## Release metadata

- Version: `____`
- Date: `____`
- Branch/Tag: `____`
- Release owner: `____`

## Build and test gates

1. Baseline build
- [ ] `mvn clean install` passes on release branch.

2. CI matrix
- [ ] Linux JDK 25 CI passes.
- [ ] macOS JDK 25 CI passes.

3. Sampler smoke
- [ ] Sampler launches:
  - [ ] `mvn -pl DynamisFX-Samples -DskipTests javafx:run`
- [ ] Sample tree is populated.
- [ ] Selecting multiple samples rapidly does not crash.

4. Physics backend smoke
- [ ] ODE4j path works:
  - [ ] `-Ddynamisfx.samples.physics.backend=ode4j`
- [ ] Jolt path works or falls back deterministically:
  - [ ] `-Ddynamisfx.samples.physics.backend=jolt`
  - [ ] Forced fallback test:
    - [ ] `mvn -pl DynamisFX-Samples test -Dtest=RigidBodyBackendSelectorTest#joltForcedFailureFallsBackToOde4j -Ddynamisfx.samples.physics.forceJoltFailure=true`

5. Jolt diagnostics
- [ ] `mvn -pl DynamisFX-Physics-Jolt test -Dtest=JoltRuntimeDiagnosticsTest` passes.
- [ ] Jolt diagnostics line is visible in sampler runtime diagnostics (when Jolt requested/resolved).

6. Packaging smoke
- [ ] `mvn -pl DynamisFX-Samples -DskipTests clean package javafx:jlink` passes.

## Dependency and logging hygiene

- [ ] SLF4J sanity check passes in CI.
- [ ] No unexpected runtime error stack traces during sampler smoke.
- [ ] Known warnings are documented in `docs/Runtime_Diagnostics_Runbook.md`.

## Documentation gates

- [ ] README runtime flags section is current.
- [ ] `docs/Runtime_Diagnostics_Runbook.md` is current.
- [ ] Migration docs updated if public behavior changed.

## Manual QA sign-off

- [ ] Core sample browsing
- [ ] Physics samples (ODE4j)
- [ ] Physics samples (Jolt / fallback)
- [ ] Import/export smoke samples
- [ ] Platform-specific notes captured

## Release artifacts

- [ ] Version bumped where required.
- [ ] Tag created: `____`
- [ ] Release notes drafted.
- [ ] Artifacts published to target repository.

## Post-release

- [ ] Verify artifact availability in repository index.
- [ ] Verify sample project dependency snippet works with released version.
- [ ] Create follow-up issues for deferred items.
