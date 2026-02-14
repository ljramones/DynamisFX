# DynamisFX Rename Migration Guide

## Purpose

This guide covers migration from the legacy FXyz namespace to DynamisFX.

It includes:

- Maven coordinate changes
- Java package/module changes
- Public type rename mapping
- Compatibility notes

## Coordinate Migration

### Parent

- Old: `org.fxyz3d:fxyz3d-parent`
- New: `org.dynamisfx:dynamisfx-parent`

### Modules

- Old: `org.fxyz3d:fxyz3d`
- New: `org.dynamisfx:dynamisfx-core`

- Old: `org.fxyz3d:fxyz3d-client`
- New: `org.dynamisfx:dynamisfx-client`

- Old: `org.fxyz3d:fxyz3d-importers`
- New: `org.dynamisfx:dynamisfx-importers`

- Old: `org.fxyz3d:fxyz3d-physics-ode4j`
- New: `org.dynamisfx:dynamisfx-physics-ode4j`

- Old: `org.fxyz3d:fxyz3d-physics-orekit`
- New: `org.dynamisfx:dynamisfx-physics-orekit`

- Old: `org.fxyz3d:fxyz3d-samples`
- New: `org.dynamisfx:dynamisfx-demo`

## Package Migration

- Old root: `org.fxyz3d.*`
- New root: `org.dynamisfx.*`

Examples:

- `org.fxyz3d.shapes.primitives.SpringMesh` -> `org.dynamisfx.shapes.primitives.SpringMesh`
- `org.fxyz3d.importers.Importer3D` -> `org.dynamisfx.importers.Importer3D`
- `org.fxyz3d.physics.api.PhysicsWorld` -> `org.dynamisfx.physics.api.PhysicsWorld`

## JPMS Module Migration

- `org.fxyz3d.core` -> `org.dynamisfx.core`
- `org.fxyz3d.client` -> `org.dynamisfx.client`
- `org.fxyz3d.importers` -> `org.dynamisfx.importers`
- `org.fxyz3d.samples` -> `org.dynamisfx.samples`

Update `module-info.java`:

- `requires org.fxyz3d.*` -> `requires org.dynamisfx.*`
- `uses org.fxyz3d.*` -> `uses org.dynamisfx.*`
- `provides org.fxyz3d.* with ...` -> `provides org.dynamisfx.* with ...`

## Public Type Rename Mapping

Primary API renames:

- `org.dynamisfx.client.FXyzClient` -> `org.dynamisfx.client.DynamisFXClient`
- `org.dynamisfx.FXyzSample` -> `org.dynamisfx.DynamisFXSample`
- `org.dynamisfx.FXyzSampleBase` -> `org.dynamisfx.DynamisFXSampleBase`
- `org.dynamisfx.FXyzSamplerProject` -> `org.dynamisfx.DynamisFXSamplerProject`
- `org.dynamisfx.samples.FXyzSample` -> `org.dynamisfx.samples.DynamisFXSample`
- `org.dynamisfx.samples.FXyzProject` -> `org.dynamisfx.samples.DynamisFXProject`

## Compatibility Layer

Legacy `FXyz*` types still exist as deprecated shims and forward to `DynamisFX*`:

- `org.dynamisfx.client.FXyzClient`
- `org.dynamisfx.FXyzSample`
- `org.dynamisfx.FXyzSampleBase`
- `org.dynamisfx.FXyzSamplerProject`
- `org.dynamisfx.samples.FXyzSample`
- `org.dynamisfx.samples.FXyzProject`

These are transitional and should be removed in a later major release.

For consumers that still compile against `org.fxyz3d.*`, use the dedicated compatibility artifact:

- `org.dynamisfx:dynamisfx-compat`

## ServiceLoader Migration

Old service descriptor:

- `META-INF/services/org.fxyz3d.FXyzSamplerProject`

Current descriptor:

- `META-INF/services/org.dynamisfx.DynamisFXSamplerProject`

Provider class:

- `org.dynamisfx.samples.DynamisFXProject`

## Example POM Migration

Before:

```xml
<dependency>
  <groupId>org.fxyz3d</groupId>
  <artifactId>fxyz3d</artifactId>
  <version>${fxyz.version}</version>
</dependency>
```

After:

```xml
<dependency>
  <groupId>org.dynamisfx</groupId>
  <artifactId>dynamisfx-core</artifactId>
  <version>${dynamisfx.version}</version>
</dependency>
```

Optional compatibility dependency (temporary):

```xml
<dependency>
  <groupId>org.dynamisfx</groupId>
  <artifactId>dynamisfx-compat</artifactId>
  <version>${dynamisfx.version}</version>
</dependency>
```

## Example Import Migration

Before:

```java
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.shapes.primitives.CubeMesh;
```

After:

```java
import org.dynamisfx.importers.Importer3D;
import org.dynamisfx.shapes.primitives.CubeMesh;
```

## Common Issues and Fixes

1. `ClassNotFoundException` for `org.fxyz3d.*`
- Cause: old imports or reflection string literals.
- Fix: update to `org.dynamisfx.*`.

2. JPMS `module not found`
- Cause: old `requires org.fxyz3d.*`.
- Fix: rename to `org.dynamisfx.*`.

3. ServiceLoader returns no providers
- Cause: old service file/interface name.
- Fix: use `org.dynamisfx.DynamisFXSamplerProject` descriptor and provider.

4. Deprecated shim warnings
- Cause: references to `FXyz*` transitional types.
- Fix: migrate to `DynamisFX*` APIs directly.

## Recommended Migration Order for Consumers

1. Update Maven coordinates to `org.dynamisfx`.
2. Update package imports to `org.dynamisfx.*`.
3. Update JPMS `module-info.java`.
4. Replace `FXyz*` type usages with `DynamisFX*`.
5. Update ServiceLoader descriptors if applicable.
