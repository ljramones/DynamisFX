# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DynamisFX is a JavaFX 3D visualization and component library. It provides custom 3D shapes, geometry utilities, model importers, and a sampler application for demos.

## Build Commands

```bash
mvn compile                           # Build all modules
mvn test                              # Run all tests
mvn install                           # Install to local Maven repository
mvn clean install                     # Clean build and install
```

Run a single test class:
```bash
mvn test -pl FXyz-Core -Dtest=QuadMeshTest
```

Run tests for a specific module:
```bash
mvn test -pl FXyz-Core
```

## Module Structure

- **FXyz-Core** - Core 3D shapes, geometry, and utilities (`org.fxyz3d.shapes`, `org.fxyz3d.geometry`, `org.fxyz3d.utils`)
- **FXyz-Importers** - 3D model format importers
- **FXyz-Client** - FXSampler UI framework
- **FXyz-Samples** - Sample applications and demos (entry point: `Launcher` → `FXyzClient`)

## Architecture Notes

- Uses Java 25 with JPMS modules (each module has `module-info.java`)
- JavaFX for 3D graphics with `javafx.controls`, `javafx.graphics`, `javafx.fxml`
- Geometry libraries: jcsg (CSG operations), vvecmath (vector math), poly2tri (triangulation)
- UI libraries: ControlsFX, ReactFX, EasyBind, JFXTras
- Test framework: JUnit 5 with Hamcrest matchers

## Coding Conventions

- Package root: `org.fxyz3d`
- 4-space indentation
- Standard Java naming: `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields
- New shapes go in `FXyz-Core/src/main/java/org/fxyz3d/shapes/`
- Tests mirror source package structure in `src/test/java/`
- BSD 3-Clause license headers required on all source files
