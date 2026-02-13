# Panama Notes

This folder contains Panama integration helpers for the Jolt C shim.

## JDK requirement
- JDK 22+ for `java.lang.foreign` API.

## Compile Panama probe sources
```bash
mvn -pl DynamisFX-Physics-Jolt -Dpanama=true -DskipTests compile
```

## Generate bindings with jextract
```bash
./panama/jextract.sh
```

Generated sources are written to:
- `src/panama/generated`

The generated package default is:
- `org.dynamisfx.physics.jolt.panama.generated`
