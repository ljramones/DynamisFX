FXyz3D
======

 - FXyz3D Core: 
 [ ![Download](http://img.shields.io/maven-central/v/org.fxyz3d/fxyz3d.svg?color=%234DC71F) ](https://search.maven.org/search?q=g:org.fxyz3d%20AND%20a:fxyz3d)

 - FXyz3D Client: 
[ ![Download](https://img.shields.io/maven-central/v/org.fxyz3d/fxyz3d-client.svg?color=%234DC71F) ](https://search.maven.org/search?q=g:org.fxyz3d%20AND%20a:fxyz3d-client)

 - FXyz3D Importers: 
[ ![Download](https://img.shields.io/maven-central/v/org.fxyz3d/fxyz3d-importers.svg?color=%234DC71F) ](https://search.maven.org/search?q=g:org.fxyz3d%20AND%20a:fxyz3d-importers)

A JavaFX 3D Visualization and Component Library

[![BSD-3 license](https://img.shields.io/badge/license-BSD--3-%230778B9.svg)](https://opensource.org/licenses/BSD-3-Clause)


## How to build

The project is managed by Maven. To build with JDK 17, type:

	mvn clean install

To deploy it to your local Maven repository, type:

	mvn clean install

## Use of FXyz3D Core

With FXyz3D there are many different 3D custom shapes. The following sample makes use of `SpringMesh` to create 
a 3D mesh of a spring.

### Sample

#### Maven project

If you have a Maven project, edit the `pom.xml` file and add:

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.fxyz3d</groupId>
    <artifactId>sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>17</maven.compiler.release>
        <javafx.maven.plugin.version>0.0.8</javafx.maven.plugin.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21</version>
        </dependency>
        <dependency>
            <groupId>org.fxyz3d</groupId>
            <artifactId>fxyz3d</artifactId>
            <version>0.6.0</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>${javafx.maven.plugin.version}</version>
                <configuration>
                    <mainClass>org.fxyz3d.Sample</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Application class

Then create a JavaFX Application class `Sample` under the `org.fxyz3d` package: 

```java
package org.fxyz3d;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.stage.Stage;
import org.fxyz3d.shapes.primitives.SpringMesh;
import org.fxyz3d.utils.CameraTransformer;

public class Sample extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateX(10);
        camera.setTranslateZ(-100);
        camera.setFieldOfView(20);

        CameraTransformer cameraTransform = new CameraTransformer();
        cameraTransform.getChildren().add(camera);
        cameraTransform.ry.setAngle(-30.0);
        cameraTransform.rx.setAngle(-15.0);

        SpringMesh spring = new SpringMesh(10, 2, 2, 8 * 2 * Math.PI, 200, 100, 0, 0);
        spring.setCullFace(CullFace.NONE);
        spring.setTextureModeVertices3D(1530, p -> p.f);

        Group group = new Group(cameraTransform, spring);

        Scene scene = new Scene(group, 600, 400, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BISQUE);
        scene.setCamera(camera);

        primaryStage.setScene(scene);
        primaryStage.setTitle("FXyz3D Sample");
        primaryStage.show();
    }
}
```

#### Run the sample

Run the sample:

```
mvn javafx:run
```

and you should see the result:

![](/resources/SpringMesh.png)

Note: For more information on JavaFX, check this [link](https://openjfx.io).

### FXSampler

To use the FXSampler and visualize all the samples and the different options available, run:

    mvn -pl FXyz-Samples -DskipTests javafx:run
    
There is a hidden side popup menu at the left, from where different samples can be selected. From the right panels different options can be applied dynamically to the 3D shape.

![](/resources/fxsampler.png)

 #### Custom image

You can create a custom image for your platform running:

    mvn -pl FXyz-Samples -DskipTests clean package javafx:jlink

And you can run it with Java 17 on your platform:

    FXyz-Samples/build/FXyz/bin/FXyzSamples

Special Thanks go to ControlsFX for providing the FXSampler framework.
http://fxexperience.com/controlsfx/
Our Sampler uses a heavily modified version, due to being 3D.

## Code Quality Improvements (2026)

A comprehensive code quality review was performed on FXyz-Core, addressing 20 issues across critical, high, medium, and low priorities. All 534 unit tests pass.

### Critical Fixes

| Issue | Files | Fix |
|-------|-------|-----|
| Thread Safety | `Constraint.java`, `ClothMesh.java` | Removed unsafe parallel streams on shared mutable state; changed to sequential execution |
| Swallowed Exceptions | `ClothMesh.java` | Replaced `printStackTrace()` with proper `java.util.logging` |

### High Priority Fixes

| Issue | Files | Fix |
|-------|-------|-----|
| God Class Refactoring | `CubeWorld.java` | Reduced from 901 to 647 lines; extracted inner classes (`Panels`, `GridLines`, `AxesGroups`, `ColorConfig`) |
| God Class Refactoring | `CubeViewer.java` | Reduced from 868 to 700 lines with same pattern |
| Dead Code Removal | `CubeWorld.java` | Removed unused `buildGridsOld()` method (165 lines) |
| Null Safety | `ClothMesh.java` | Added null guards for face picking operations |
| Exception Handling | `OBJWriter.java` | Now properly throws `IOException` instead of silent continuation |

### Medium Priority Fixes

| Issue | Files | Fix |
|-------|-------|-----|
| Algorithm Efficiency | `ClothMesh.java` | Removed O(n²) `indexOf()` in parallel stream |
| Input Validation | `Link.java` | Added null checks and range validation for constructor parameters |
| Magic Numbers | `ClothMesh.java` | Extracted physics constants: `PHYSICS_FIXED_DELTA_TIME`, `PHYSICS_TIME_SCALE`, `PHYSICS_MAX_TIMESTEPS`, `GRAVITY_FORCE`, `EDGE_FORCE` |
| Missing Documentation | `WeightedPoint.java`, `Link.java`, `ClothMesh.java`, `PolyLine3D.java` | Added comprehensive JavaDoc for physics simulation code |
| Deprecated API | `PolyLine3D.java` | Removed deprecated constructors |
| Encapsulation | `CubeWorld.java`, `CubeViewer.java`, `PolyLine3D.java` | Made 48+ public mutable fields private; added getters |
| Tight Coupling | `ClothMesh.java`, `WeightedPoint.java` | Removed circular dependency; `WeightedPoint` no longer references `ClothMesh` |

### Low Priority Fixes

| Issue | Files | Fix |
|-------|-------|-----|
| API Typo | `WeightedPoint.java`, `ClothMesh.java` | Renamed `attatchTo()` → `attachTo()` |
| Hardcoded Values | `CubeWorld.java` | Extracted color constants: `DARK_SLATE_GRAY`, `DARK_GRAY`, `LIGHT_GRAY` |
| Empty Methods | `WeightedPoint.java` | Implemented `removeConstraint()` method |
| Comment Cleanup | `CameraView.java`, `Skybox.java` | Fixed incomplete TODOs and typos |

### New Tests Added

- `CubeWorldTest.java` - 12 tests for CubeWorld functionality
- `CubeViewerTest.java` - 19 tests for CubeViewer functionality

### Breaking Changes

These changes include API modifications that may require updates to existing code:

- `WeightedPoint` constructors no longer accept `ClothMesh` parent parameter
- `attatchTo()` renamed to `attachTo()`
- Previously public fields in `CubeWorld`, `CubeViewer`, and `PolyLine3D` are now private (use getters instead)

## Import/Export Formats

FXyz3D supports multiple 3D file formats through the FXyz-Importers module.

## Generic Picking/Selection (2026)

FXyz-Core now includes a reusable picking/selection system for 3D scene nodes.

### Core API

- `org.fxyz3d.scene.selection.SelectionModel3D`
- `org.fxyz3d.scene.selection.PickSelectionHandler`
- `org.fxyz3d.scene.selection.SelectableGroup3D`

`SelectableGroup3D` installs click-based picking and selection by default and exposes:

- `getSelectionModel()` for current selection state
- `setSelectionEnabled(boolean)` to enable/disable selection handling
- `selectionEnabledProperty()` for binding/observation

### Where It Is Wired

The following core scene components now inherit from `SelectableGroup3D`:

- `CubeViewer`
- `CubeWorld`
- `CuboidViewer`
- `Axes`
- `Crosshair3D`
- `Skybox`

### Selection Behavior

- Primary-click selects picked nodes.
- `Shift`/platform shortcut key toggles selection in multi-select mode.
- Selection state is also reflected via:
  - node property key: `org.fxyz3d.scene.selection.selected`
  - pseudo-class: `:selected`

### Example

```java
CubeViewer viewer = new CubeViewer(true);
viewer.getSelectionModel().setSelectionMode(SelectionModel3D.SelectionMode.MULTIPLE);
viewer.setSelectionEnabled(true);

viewer.getSelectionModel().getSelectedNodes().addListener((change) -> {
    // react to selection updates
});
```

## Collision Foundations (2026)

FXyz-Core now includes a baseline collision package: `org.fxyz3d.collision`.

### Core API

- `BroadPhase3D<T>`
- `Aabb`
- `BoundingSphere`
- `Ray3D`
- `Intersection3D`
- `SpatialHash3D<T>`
- `SweepAndPrune3D<T>`
- `CollisionPair<T>`
- `ConvexPolygon2D`
- `ProjectionInterval`
- `Sat2D`
- `CollisionManifold2D`
- `ConvexSupport3D`
- `Gjk3D`
- `CollisionManifold3D`
- `Ccd3D`
- `CollisionPipeline`

### Supported Checks

- `Aabb` vs `Aabb`
- `BoundingSphere` vs `BoundingSphere`
- `BoundingSphere` vs `Aabb`
- `Ray3D` vs `Aabb` (including first hit distance via `OptionalDouble`)
- `ConvexPolygon2D` vs `ConvexPolygon2D` via SAT (with optional manifold)
- Convex 3D support-mapped shapes via `Gjk3D` intersection checks
- Convex 3D EPA manifold extraction via `Gjk3D.intersectsWithManifold(...)`
- Segment-vs-AABB and swept-AABB time-of-impact via `Ccd3D`
- Approximate swept convex-convex TOI via `Ccd3D.sweptConvexTimeOfImpact(...)`

### Broad-Phase

`SpatialHash3D` provides uniform-grid candidate generation for possible collisions.
`SweepAndPrune3D` provides axis-sweep candidate generation for dynamic scenes.
Both are broad-phase only and should be followed by narrow-phase validation.

`CollisionPipeline` can be used to filter broad-phase candidate pairs through a narrow-phase predicate (for example, SAT).

### Current Shortcomings

- No OBB (oriented bounding box) support
- No mesh-level narrow phase
- No rigid-body dynamics/response integration yet
- SAT is currently 2D convex-polygon only (concave shapes require decomposition)
- Convex-convex CCD currently uses sampled bracketing + bisection (not full conservative advancement yet)

### Broad-Phase Benchmark Harness

For local performance comparisons of broad-phase strategies, use:

- `FXyz-Core/src/test/java/org/fxyz3d/collision/BroadPhase3DBenchmark.java`

This compares `SpatialHash3D` and `SweepAndPrune3D` on generated AABB scenes.

See `docs/Core_Collision.md` for details.

### Supported Formats

| Format | Extension | Import | Export | Description |
|--------|-----------|--------|--------|-------------|
| **OBJ** | `.obj` | ✅ | ✅ | Wavefront OBJ with MTL materials |
| **STL** | `.stl` | ✅ | ✅ | Stereolithography (ASCII & binary) |
| **glTF 2.0** | `.gltf`, `.glb` | ✅ | ✅ | GL Transmission Format |
| **PLY** | `.ply` | ✅ | ✅ | Polygon File Format (ASCII & binary) |
| **OFF** | `.off` | ✅ | ✅ | Object File Format |
| **3MF** | `.3mf` | ✅ | ✅ | 3D Manufacturing Format (ZIP+XML) |
| **3DS** | `.3ds` | ✅ | - | 3D Studio binary format |
| **COLLADA** | `.dae` | ✅ | - | XML-based interchange format |
| **X3D** | `.x3d` | ✅ | - | Extensible 3D (XML-based) |
| **VRML** | `.wrl` | ✅ | - | Virtual Reality Modeling Language 2.0 |
| **DXF** | `.dxf` | ⚠️ | - | AutoCAD (3DFACE/POLYLINE only) |
| **Maya** | `.ma` | ✅ | - | Maya ASCII format |
| **FXML** | `.fxml` | ✅ | - | JavaFX FXML format |
| **USD** | `.usd`, `.usda`, `.usdc`, `.usdz` | ⚠️ | - | Partial support: ASCII `.usda` mesh import only |
| **STEP/IGES** | `.step`, `.stp`, `.iges`, `.igs` | ❌ | - | Stub only (requires CAD kernel) |

### Import Example

```java
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;

// Import any supported format - the loader is auto-detected from extension
Model3D model = Importer3D.load(new File("model.glb").toURI().toURL());
Group sceneRoot = new Group();
sceneRoot.getChildren().add(model.getRoot());

// Works with all supported formats:
// Importer3D.load(new File("model.obj").toURI().toURL());
// Importer3D.load(new File("model.stl").toURI().toURL());
// Importer3D.load(new File("model.3mf").toURI().toURL());
// Importer3D.load(new File("model.3ds").toURI().toURL());
// Importer3D.load(new File("model.dae").toURI().toURL());
// Importer3D.load(new File("model.x3d").toURI().toURL());
// Importer3D.load(new File("model.wrl").toURI().toURL());
// Importer3D.load(new File("model.dxf").toURI().toURL());
// Importer3D.load(new File("model.off").toURI().toURL());
// Importer3D.load(new File("model.ply").toURI().toURL());
// Importer3D.load(new File("model.usda").toURI().toURL()); // USD ASCII only
```

### Export Examples

```java
import org.fxyz3d.importers.Exporter3D;
import org.fxyz3d.importers.stl.StlExporter;
import org.fxyz3d.importers.gltf.GltfExporter;
import org.fxyz3d.importers.obj.ObjExporter;
import org.fxyz3d.importers.off.OffExporter;

// Unified export API - format auto-detected from file extension
Exporter3D.export(triangleMesh, new File("model.stl"));
Exporter3D.export(triangleMesh, new File("model.obj"));
Exporter3D.export(triangleMesh, new File("model.glb"));
Exporter3D.export(triangleMesh, new File("model.ply"));
Exporter3D.export(triangleMesh, new File("model.off"));
Exporter3D.export(triangleMesh, new File("model.3mf"));

// Or use specific exporters for more control:

// Export to STL (binary format, ideal for 3D printing)
StlExporter stlExporter = new StlExporter(StlExporter.Format.BINARY);
stlExporter.export(triangleMesh, new File("model.stl"), "MyModel");

// Export to glTF 2.0 (GLB binary container)
GltfExporter gltfExporter = new GltfExporter(GltfExporter.Format.GLB);
gltfExporter.export(triangleMesh, new File("model.glb"), "MyModel");

// Export to OBJ with materials
ObjExporter objExporter = new ObjExporter();
objExporter.setDiffuseColor(Color.CORNFLOWERBLUE);
objExporter.export(triangleMesh, new File("model.obj"), "MyModel");

// Export to OFF (simple ASCII format)
OffExporter offExporter = new OffExporter();
offExporter.export(triangleMesh, new File("model.off"), "MyModel");
```

### Format Details

#### OFF (Object File Format)
Simple ASCII format storing vertices and faces. Supports polygon triangulation for faces with more than 3 vertices. Lightweight and human-readable.

#### 3DS (3D Studio)
Binary chunk-based format from Autodesk 3D Studio. Supports multiple meshes per file with vertices, faces, and texture coordinates. Import only due to complex chunk hierarchy.

#### COLLADA (.dae)
XML-based interchange format widely supported by 3D modeling software. Parses `<library_geometries>` with support for both `<triangles>` and `<polylist>` primitives. Handles `<source>` elements with float arrays and accessor strides.

#### X3D
XML-based successor to VRML. Supports `<IndexedFaceSet>` and `<IndexedTriangleSet>` geometry nodes with automatic polygon triangulation. Coordinates parsed from `<Coordinate point="..."/>` attributes.

#### 3MF (3D Manufacturing Format)
Modern 3D printing format using a ZIP container with XML content. Stores mesh data in `3D/3dmodel.model` with vertices and triangles. Full import/export support with proper OPC (Open Packaging Conventions) structure.

#### VRML (.wrl)
Virtual Reality Modeling Language 2.0 text format. Parses `IndexedFaceSet` geometry nodes with `Coordinate` points and `coordIndex` face definitions. Supports automatic polygon triangulation for non-triangle faces.

#### DXF (AutoCAD)
AutoCAD Drawing Exchange Format with partial 3D support. Imports `3DFACE` entities (triangles and quads) and `POLYLINE` mesh entities. Note: `3DSOLID` entities use proprietary ACIS format and are not supported.

#### USD, STEP, IGES
These formats are recognized but not fully implemented due to external dependencies:
- **USD** (Universal Scene Description): MVP importer supports ASCII `.usda` meshes with `points`, `faceVertexCounts`, and `faceVertexIndices`.
  - **Supported now**: single-mesh geometry import, polygon triangulation to `TriangleMesh`.
  - **Not supported yet**: binary `.usdc`, packaged `.usdz`, and generic `.usd`; materials, textures, normals/UV primvars, transforms/scene hierarchy, animation, references/composition, variants, and non-mesh prim types.
  - **Workaround for unsupported USD features**: convert to glTF (Blender, usdview/OpenUSD tooling, Reality Converter).
- **STEP/IGES**: Require a CAD geometry kernel like OpenCASCADE. Convert to OBJ/STL using FreeCAD.

#### FBX (Not Yet Supported)
FBX is Autodesk's proprietary format with no official Java SDK. The FBX SDK is only available for C++ and Python, making pure Java implementation impractical without JNI bindings. **Workaround**: Convert FBX files to glTF or OBJ using Blender, Autodesk FBX Converter, or online tools before importing.
