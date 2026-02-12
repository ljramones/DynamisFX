# Repository Guidelines

## Project Structure & Module Organization
This is a multi-module Maven build for the FXyz3D JavaFX library. Main modules live at the repo root:
- `FXyz-Core/` provides core 3D shapes, utilities, and geometry types under `src/main/java/org/fxyz3d`.
- `FXyz-Importers/` contains model importers and related tests.
- `FXyz-Client/` provides the sampler client UI.
- `FXyz-Samples/` contains sample applications, FXML, and resources.
Parent and module build configuration is defined in `pom.xml` files. Images and docs live under `resources/`.

## Build, Test, and Development Commands
Use JDK 17 with Maven:
- `mvn clean install`: build all modules.
- `mvn -pl FXyz-Samples -DskipTests javafx:run`: launch the FXyz sampler.
- `mvn -pl FXyz-Samples -DskipTests clean package javafx:jlink`: produce a custom runtime image.

## Coding Style & Naming Conventions
Code is Java, organized by package `org.fxyz3d`. Follow standard Java conventions: 4-space indentation, `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields, and `UPPER_SNAKE_CASE` for constants. Keep new classes in the module-appropriate package; e.g., shapes in `FXyz-Core/src/main/java/org/fxyz3d/shapes/`.

## Testing Guidelines
Unit tests live beside modules in `src/test/java`. Example: `FXyz-Core/src/test/java/org/fxyz3d/geometry/Point3DTest.java`. Run tests with `mvn test` (or module-scoped Maven test commands). Prefer naming tests `*Test.java` and mirroring the package of the class under test.

## Commit & Pull Request Guidelines
No Git history is available in this workspace, so commit message conventions cannot be inferred. Use clear, imperative subject lines (e.g., "Add Spheroid mesh defaults").
For PRs, include a short summary, note impacted modules (e.g., `FXyz-Core`), and attach screenshots when UI behavior changes (FXyz sampler or sample apps).

## Configuration Notes
This repo targets Java 17 and JavaFX. If you add new samples or resources, place them in the module-specific `src/main/resources` directories and register any new samples with the sampler when applicable.
