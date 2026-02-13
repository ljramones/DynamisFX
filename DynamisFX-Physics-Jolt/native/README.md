# Jolt C Shim

This folder defines the C ABI boundary for Jolt integration.

Goals:
- stable C ABI for Panama/jextract
- POD data structures for vectors/quaternions/transforms
- bulk-friendly functions for body state reads/writes

Planned artifact name:
- `dynamisfx_jolt_cshim` (shared library)

Header contract:
- `jolt_c_api.h`

Build locally with CMake:
```bash
cd DynamisFX-Physics-Jolt/native
cmake -S . -B build
cmake --build build --config Release
```

Default build (`DYNAMISFX_JOLT_BACKEND=stub`) is a deterministic in-memory shim used to validate ABI and Java integration plumbing.

To validate local Jolt SDK wiring, configure `real` mode:
```bash
cd DynamisFX-Physics-Jolt/native
cmake -S . -B build-real \
  -DDYNAMISFX_JOLT_BACKEND=real \
  -DJOLT_SDK_DIR=/path/to/jolt/include-root \
  -DJOLT_LIBRARY=/path/to/libJolt.(a|so|dylib)
cmake --build build-real --config Release
```

`real` mode now wires world/body creation, stepping, state reads/writes, and raycast through Jolt while preserving the existing C ABI/JNI contract.

The shared library also exports JNI symbols consumed by `JoltNativeBridge`.
