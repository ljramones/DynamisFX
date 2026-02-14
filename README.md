# DynamisFX

A JavaFX 3D Visualization, Physics, and Component Library

[![Apache-2.0 license](https://img.shields.io/badge/license-Apache--2.0-%23D22128.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> **Note:** Maven Central artifacts are not yet published. Build from source for now (see [Quick Start](#quick-start)).

---

## About DynamisFX

DynamisFX began as a fork of [FXyz](https://github.com/FXyz/FXyz), a JavaFX 3D library. However, DynamisFX has undergone such extensive modifications that it is effectively a new project sharing only historical roots with FXyz.

### What's Different from FXyz

| Area | Changes |
|------|---------|
| **Testing** | Added comprehensive test suite (534+ unit tests) where FXyz had minimal coverage |
| **Architecture** | Refactored "god classes" (CubeWorld: 901→647 lines, CubeViewer: 868→700 lines) into focused, maintainable components |
| **Performance** | Eliminated O(n²) algorithms (e.g., `indexOf()` in parallel streams), improved overall efficiency |
| **Bug Fixes** | Fixed thread safety issues, swallowed exceptions, null pointer vulnerabilities, and other hidden defects |
| **Import/Export** | Doubled the number of supported 3D formats (17 formats, 11 importers, 6 exporters) |
| **Collision Detection** | Added complete collision detection system: broad-phase (spatial hash, sweep-and-prune), narrow-phase (SAT, GJK/EPA), contact manifolds, CCD |
| **Physics Integration** | Added physics backend SPI with ODE4j, Jolt, and Orekit implementations, hybrid coordinator for multi-backend scenarios |
| **Particle Systems** | Extended ScatterMesh architecture to support particle systems (in progress) |
| **Demos** | Expanded from ~20 demos to 70+ demos including 26 interactive ODE4j physics demos |
| **Code Quality** | Proper encapsulation (48+ public fields made private), documentation, input validation, consistent error handling |

### Why the Rename?

The name change to **DynamisFX** reflects the fundamental transformation:
- *Dynamis* (Greek: δύναμις) means "power" or "force" — fitting for a library now featuring physics simulation
- The codebase shares little resemblance to the original FXyz beyond basic 3D shape primitives
- Maintaining the FXyz name would misrepresent the scope of changes

---

## Features

### 3D Shapes and Primitives
- 40+ parametric mesh types (springs, knots, surfaces, polyhedra, etc.)
- Texture mapping modes (vertices, faces, images, density maps)
- CSG (Constructive Solid Geometry) operations

### Collision Detection
- **Broad-phase**: SpatialHash3D, SweepAndPrune3D
- **Narrow-phase**: SAT (2D), GJK/EPA (3D convex)
- **Contact generation**: Manifold caching, warm-starting
- **CCD**: Continuous collision detection for fast-moving objects
- **Filtering**: Layer/mask system, trigger vs solid classification

### Physics Integration
- **Backend-agnostic SPI**: Swap physics engines without code changes
- **ODE4j Backend**: Rigid body dynamics, constraints (ball, hinge, slider, fixed), motors
- **Jolt Backend**: High-performance physics via JNI/Panama bindings (with automatic ODE4j fallback)
- **Orekit Backend**: Orbital mechanics, multi-body gravitational simulation
- **Hybrid Coordinator**: Combine backends (e.g., orbital + local rigid body)

### Import/Export (17 Formats)

| Format | Import | Export | Notes |
|--------|:------:|:------:|-------|
| OBJ | ✅ | ✅ | With MTL materials |
| STL | ✅ | ✅ | ASCII & binary |
| glTF 2.0 | ✅ | ✅ | .gltf and .glb |
| PLY | ✅ | ✅ | ASCII & binary |
| OFF | ✅ | ✅ | Object File Format |
| 3MF | ✅ | ✅ | 3D Manufacturing |
| 3DS | ✅ | - | 3D Studio |
| COLLADA | ✅ | - | .dae XML format |
| X3D | ✅ | - | XML-based |
| VRML | ✅ | - | .wrl files |
| DXF | ⚠️ | - | 3DFACE/POLYLINE only |
| Maya | ✅ | - | .ma ASCII |
| FXML | ✅ | - | JavaFX format |
| USD | ⚠️ | - | ASCII .usda only |

### Selection System
- Click-based 3D picking
- Single and multi-select modes
- Pseudo-class styling (`:selected`)

---

## Quick Start

### Build

```bash
mvn clean install
```

Requires JDK 25.

### Run the Demo Application

```bash
mvn -pl DynamisFX-Demo -DskipTests javafx:run
```

Browse 70+ demos organized by category:
- **Shapes**: Parametric meshes, surfaces, primitives
- **Physics > ODE4J Demos**: 26 interactive physics simulations
- **Collision Detection**: Broad-phase, narrow-phase, ray casting
- **Importers**: Load various 3D formats

### Maven Dependency

```xml
<dependency>
    <groupId>org.dynamisfx</groupId>
    <artifactId>dynamisfx-core</artifactId>
    <version>0.6.0</version>
</dependency>
```

### Basic Example

```java
package org.dynamisfx;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.dynamisfx.shapes.primitives.SpringMesh;

public class Sample extends Application {
    @Override
    public void start(Stage stage) {
        SpringMesh spring = new SpringMesh(10, 2, 2, 8 * 2 * Math.PI, 200, 100, 0, 0);
        spring.setTextureModeVertices3D(1530, p -> p.f);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-100);

        Scene scene = new Scene(new Group(spring), 600, 400, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BISQUE);
        scene.setCamera(camera);

        stage.setScene(scene);
        stage.setTitle("DynamisFX");
        stage.show();
    }
}
```

---

## Module Structure

| Module | Description |
|--------|-------------|
| **DynamisFX-Core** | Shapes, geometry, collision, physics API, utilities |
| **DynamisFX-Importers** | 3D file format importers and exporters |
| **DynamisFX-Client** | Sampler application framework |
| **DynamisFX-Demo** | 70+ demo applications |
| **DynamisFX-Physics-ODE4j** | ODE4j physics backend |
| **DynamisFX-Physics-Jolt** | Jolt Physics backend (JNI + Panama bindings) |
| **DynamisFX-Physics-Orekit** | Orekit orbital mechanics backend |
| **DynamisFX-Compat** | Legacy `org.fxyz3d.*` compatibility shim |

---

## Physics Demos (ODE4j)

26 interactive physics demonstrations:

| Demo | Description |
|------|-------------|
| Boxstack | Drop and stack boxes, spheres, capsules |
| Chain | Swinging pendulum chain (ball joints) |
| Hinge | Rotating door with angle limits |
| Slider | Sliding piston with position limits |
| Friction | Compare friction coefficients |
| Kinematic | Moving platforms |
| Cards | Card house collapse |
| Gyroscopic | Spinning body precession |
| Domino | Domino chain reaction |
| Newton's Cradle | Conservation of momentum |
| Wrecking Ball | Destructible tower |
| Bridge | Suspension bridge under load |
| Pendulum Wave | Synchronized pendulums |
| Stack Challenge | Interactive stacking game |
| Motor | Motor-driven joints demo |
| Crane | Motorized crane with extending arm |
| Buggy | 4-wheeled vehicle (WASD controls) |
| Ragdoll | Articulated ragdoll physics |
| Seesaw | Balance and center of mass |
| Pinball | Pinball with motor flippers |
| Catapult | Trebuchet launching projectiles |
| Windmill | Spinning windmill |
| Elevator | Multi-floor platform (slider motor) |
| Marble Run | Marble rolling through ramps |
| Balancing | Stack on unstable pivots |
| Conveyor | Conveyor belt system |

---

## Collision Detection System

### API Overview

```java
// Broad-phase candidate generation
SpatialHash3D<MyObject> hash = new SpatialHash3D<>(cellSize);
hash.insert(obj, aabb);
List<CollisionPair<MyObject>> candidates = hash.queryPairs();

// Narrow-phase validation (GJK)
boolean collides = Gjk3D.intersects(supportA, supportB);

// Full manifold extraction (GJK + EPA)
Optional<CollisionManifold3D> manifold = Gjk3D.intersectsWithManifold(supportA, supportB);

// Complete collision world with events
CollisionWorld3D<Node> world = new CollisionWorld3D<>(adapter);
world.step(); // Fires ENTER/STAY/EXIT events
```

### Supported Checks
- AABB vs AABB
- Sphere vs Sphere, Sphere vs AABB
- Ray vs AABB (with hit distance)
- Convex vs Convex (GJK/EPA)
- 2D polygon vs polygon (SAT)
- Continuous collision detection (swept volumes)

---

## Runtime Configuration

```bash
# Select physics backend
-Ddynamisfx.samples.physics.backend=ode4j|jolt

# Force backend fallback (testing)
-Ddynamisfx.samples.physics.forceJoltFailure=true

# Enable script engine probing
-Ddynamisfx.samples.enableScriptEngine=true
```

---

## Documentation

| Document | Description |
|----------|-------------|
| `docs/Core_Collision.md` | Collision system details |
| `docs/Physics_Integration_Plan.md` | Physics backend roadmap |
| `docs/Physics_Migration_Guide.md` | Migration from direct ODE4j usage |
| `docs/Runtime_Diagnostics_Runbook.md` | Troubleshooting guide |

---

## Legacy Compatibility

For projects migrating from FXyz, a compatibility module provides the old `org.fxyz3d.*` package paths:

```xml
<dependency>
    <groupId>org.dynamisfx</groupId>
    <artifactId>dynamisfx-compat</artifactId>
    <version>0.6.1-SNAPSHOT</version>
</dependency>
```

---

## Acknowledgments

- **FXyz** — The original JavaFX 3D library that DynamisFX evolved from
- **ControlsFX** — The sampler framework (heavily modified for 3D)
- **ODE4j** — Open Dynamics Engine for Java (physics backend)
- **Jolt Physics** — High-performance C++ physics engine (via JNI/Panama)
- **Orekit** — Space dynamics library (orbital mechanics backend)

---

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.

This project includes code derived from FXyz (BSD-3) and ControlsFX (BSD-3).
See [NOTICE](NOTICE) for full attribution.
