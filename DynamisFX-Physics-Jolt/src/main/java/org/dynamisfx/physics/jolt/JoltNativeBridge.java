package org.dynamisfx.physics.jolt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Loads the Jolt C-shim native library.
 */
final class JoltNativeBridge {

    static final String LIBRARY_NAME = "dynamisfx_jolt_cshim";
    static final String NATIVE_PATH_PROPERTY = "dynamisfx.jolt.native.path";
    static final String NATIVE_PATH_ENV = "DYNAMISFX_JOLT_NATIVE_PATH";

    private final boolean available;
    private final String loadDescription;

    JoltNativeBridge() {
        this(tryLoadAndDescribe());
    }

    JoltNativeBridge(boolean available) {
        this.available = available;
        this.loadDescription = available ? "injected-available" : "injected-unavailable";
    }

    private JoltNativeBridge(LoadResult result) {
        this.available = result.available();
        this.loadDescription = result.description();
    }

    boolean isAvailable() {
        return available;
    }

    String loadDescription() {
        return loadDescription;
    }

    private static LoadResult tryLoadAndDescribe() {
        Optional<Path> explicitPath = resolveExplicitPath();
        if (explicitPath.isPresent()) {
            Path path = explicitPath.orElseThrow();
            if (!Files.exists(path)) {
                return new LoadResult(false, "explicit-path-missing:" + path);
            }
            try {
                System.load(path.toString());
                return new LoadResult(true, "loaded-explicit:" + path);
            } catch (UnsatisfiedLinkError error) {
                return new LoadResult(false, "load-failed-explicit:" + path + " (" + error.getMessage() + ")");
            }
        }

        String mappedName = System.mapLibraryName(LIBRARY_NAME);
        try {
            System.loadLibrary(LIBRARY_NAME);
            return new LoadResult(true, "loaded-library:" + mappedName);
        } catch (UnsatisfiedLinkError error) {
            return new LoadResult(false, "load-failed-library:" + mappedName + " (" + error.getMessage() + ")");
        }
    }

    private static Optional<Path> resolveExplicitPath() {
        String propertyPath = System.getProperty(NATIVE_PATH_PROPERTY);
        if (propertyPath != null && !propertyPath.isBlank()) {
            return Optional.of(Path.of(propertyPath).toAbsolutePath().normalize());
        }
        String envPath = System.getenv(NATIVE_PATH_ENV);
        if (envPath != null && !envPath.isBlank()) {
            return Optional.of(Path.of(envPath).toAbsolutePath().normalize());
        }
        return Optional.empty();
    }

    static String expectedLibraryFileName() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            return "lib" + LIBRARY_NAME + ".dylib";
        }
        if (os.contains("win")) {
            return LIBRARY_NAME + ".dll";
        }
        return "lib" + LIBRARY_NAME + ".so";
    }

    private record LoadResult(boolean available, String description) {
    }
}
