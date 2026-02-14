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
mvn test -pl DynamisFX-Core -Dtest=QuadMeshTest
```

Run tests for a specific module:
```bash
mvn test -pl DynamisFX-Core
```

## Module Structure

- **DynamisFX-Core** - Core 3D shapes, geometry, and utilities (`org.dynamisfx.shapes`, `org.dynamisfx.geometry`, `org.dynamisfx.utils`)
- **DynamisFX-Importers** - 3D model format importers
- **DynamisFX-Client** - DynamisFX Sampler UI framework
- **DynamisFX-Demo** - Sample applications and demos (entry point: `Launcher` → `DynamisFXClient`)

## Architecture Notes

- Uses Java 25 with JPMS modules (each module has `module-info.java`)
- JavaFX for 3D graphics with `javafx.controls`, `javafx.graphics`, `javafx.fxml`
- Geometry libraries: jcsg (CSG operations), vvecmath (vector math), poly2tri (triangulation)
- UI libraries: ControlsFX, ReactFX, EasyBind, JFXTras
- Test framework: JUnit 5 with Hamcrest matchers

## Coding Conventions

- Package root: `org.dynamisfx`
- 4-space indentation
- Standard Java naming: `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields
- New shapes go in `DynamisFX-Core/src/main/java/org/dynamisfx/shapes/`
- Tests mirror source package structure in `src/test/java/`
- Apache 2.0 license headers required on all source files
