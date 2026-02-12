# FXyz Core Collision Foundations

## Scope

`FXyz-Core` now includes a baseline collision package: `org.fxyz3d.collision`.

Implemented primitives and utilities:

- `BroadPhase3D<T>`: broad-phase strategy interface
- `Aabb`: immutable axis-aligned bounding box
- `BoundingSphere`: immutable sphere bounds
- `Ray3D`: immutable ray (origin + direction)
- `Intersection3D`: primitive intersection checks
- `SpatialHash3D<T>`: broad-phase candidate pair generation
- `SweepAndPrune3D<T>`: broad-phase candidate generation via axis sweep
- `CollisionPair<T>`: unordered candidate pair container
- `ConvexPolygon2D`: immutable convex polygon (SAT input)
- `ProjectionInterval`: 1D projected interval for SAT
- `Sat2D`: SAT narrow-phase checks (`intersects`, manifold output)
- `CollisionManifold2D`: normal + penetration depth
- `ConvexSupport3D`: support mapping interface for convex 3D shapes
- `Gjk3D`: GJK narrow-phase for convex 3D shapes, with EPA manifold extraction
- `CollisionManifold3D`: normal + penetration depth
- `Ccd3D`: continuous collision detection helpers (time-of-impact)
- `CollisionPipeline`: applies narrow-phase tests to broad-phase candidates

## Implemented Checks

- `Aabb` vs `Aabb`
- `BoundingSphere` vs `BoundingSphere`
- `BoundingSphere` vs `Aabb`
- `Ray3D` vs `Aabb` with hit distance (`OptionalDouble`)
- `ConvexPolygon2D` vs `ConvexPolygon2D` via SAT (2D)

## Broad-Phase Behavior

`SpatialHash3D` uses a uniform grid (`cellSize`) and returns potential pairs that share at least one occupied cell.

`SweepAndPrune3D` sorts by X-axis interval start and checks active overlaps in Y/Z for candidate generation.

Important: broad-phase results are candidate pairs only. Final collision validation should use narrow-phase checks from `Intersection3D`.

Use `CollisionPipeline` to filter broad-phase candidate pairs with narrow-phase checks.

## Narrow-Phase Behavior

- SAT (`Sat2D`) supports 2D convex polygons and can return MTV-like manifold data.
- GJK (`Gjk3D`) supports convex 3D shapes through support mappings (`ConvexSupport3D`).
- EPA (`Gjk3D.intersectsWithManifold`) provides manifold normal and penetration depth for intersecting convex pairs.

## CCD Behavior

- `Ccd3D.segmentAabbTimeOfImpact(start, end, box)` returns first `t` in `[0,1]` for segment-vs-AABB.
- `Ccd3D.sweptAabbTimeOfImpact(moving, delta, target)` returns first `t` in `[0,1]` for moving-AABB-vs-static-AABB.
- `Ccd3D.sweptConvexTimeOfImpact(shapeA, deltaA, shapeB, deltaB)` returns approximate first `t` in `[0,1]` for moving convex shapes using sampled bracketing + binary refinement over GJK.

## Benchmark Harness

`FXyz-Core/src/test/java/org/fxyz3d/collision/BroadPhase3DBenchmark.java` provides a simple runtime comparison for:

- `SpatialHash3D`
- `SweepAndPrune3D`

It is intended for local tuning/regression checks (not as a strict microbenchmark framework).

## Current Shortcomings (Intentional v1)

- No oriented bounding boxes (OBB)
- No triangle-mesh narrow-phase
- No continuous collision detection
- No rigid-body integration/response
- No scene graph integration helpers yet (automatic Node-to-volume adapters are not included)
- SAT support is currently 2D and convex-only (concave requires decomposition)
- Convex-convex CCD is currently approximate (sampled + bisection), not full conservative advancement.

## Testing

Coverage added in `FXyz-Core`:

- `AabbAndSphereValidationTest`
- `Intersection3DTest`
- `SpatialHash3DTest`
- `Sat2DTest`
- `SweepAndPrune3DTest`
- `Gjk3DTest`
- `Ccd3DTest`
- `CollisionPipelineTest`
- `BroadPhase3DBenchmark` (manual benchmark harness)
