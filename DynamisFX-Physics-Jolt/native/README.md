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

The current implementation is a deterministic in-memory shim (no Jolt dependency yet) used to validate ABI and Java integration plumbing.
