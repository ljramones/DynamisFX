# DynamisFX-Physics-Jolt

Optional Jolt backend module for DynamisFX rigid-body simulation.

Current state:
- Java backend/factory scaffolding is in place.
- Runtime expects a native C-shim library named `dynamisfx_jolt_cshim`.
- World creation fails fast when the native shim is not present.

Planned integration layers:
1. Java backend API (`org.dynamisfx.physics.jolt`)
2. Panama binding layer (generated from C headers)
3. C ABI shim (`native/jolt_c_api.h` + implementation)
4. Jolt C++ runtime

This module is intentionally optional. ODE4j remains the default pure-Java backend.
