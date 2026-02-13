package org.dynamisfx.physics.jolt;

import com.github.stephengold.joltjni.Jolt;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loads jolt-jni native binaries from configured path or classpath resources.
 */
final class JoltJniNativeLoader {

    static final String NATIVE_PATH_PROPERTY = "dynamisfx.joltjni.native.path";
    static final String NATIVE_PATH_ENV = "DYNAMISFX_JOLTJNI_NATIVE_PATH";

    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    private JoltJniNativeLoader() {
    }

    static LoadResult ensureLoaded() {
        if (LOADED.get()) {
            return new LoadResult(true, "already-loaded");
        }

        synchronized (JoltJniNativeLoader.class) {
            if (LOADED.get()) {
                return new LoadResult(true, "already-loaded");
            }

            Optional<Path> explicitPath = resolveExplicitPath();
            if (explicitPath.isPresent()) {
                Path path = explicitPath.orElseThrow();
                if (!Files.exists(path)) {
                    return new LoadResult(false, "explicit-path-missing:" + path);
                }
                try {
                    System.load(path.toString());
                    bootstrapJolt();
                    LOADED.set(true);
                    return new LoadResult(true, "loaded-explicit:" + path);
                } catch (Throwable throwable) {
                    return new LoadResult(false, "load-failed-explicit:" + path + " (" + throwable.getMessage() + ")");
                }
            }

            Optional<String> resourcePath = resolveBundledResourcePath();
            if (resourcePath.isEmpty()) {
                return new LoadResult(false, "unsupported-platform:" + platformKey());
            }

            String classpathResource = resourcePath.orElseThrow();
            try (InputStream stream = JoltJniNativeLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
                if (stream == null) {
                    return new LoadResult(false, "resource-missing:" + classpathResource);
                }
                String suffix = classpathResource.endsWith(".dll") ? ".dll"
                        : (classpathResource.endsWith(".dylib") ? ".dylib" : ".so");
                Path extracted = Files.createTempFile("dynamisfx-joltjni-", suffix);
                Files.copy(stream, extracted, StandardCopyOption.REPLACE_EXISTING);
                extracted.toFile().deleteOnExit();
                System.load(extracted.toAbsolutePath().toString());
                bootstrapJolt();
                LOADED.set(true);
                return new LoadResult(true, "loaded-resource:" + classpathResource);
            } catch (IOException ioException) {
                return new LoadResult(false, "resource-io-failed:" + ioException.getMessage());
            } catch (Throwable throwable) {
                return new LoadResult(false, "resource-load-failed:" + throwable.getMessage());
            }
        }
    }

    private static void bootstrapJolt() {
        Jolt.registerDefaultAllocator();
        if (!Jolt.newFactory()) {
            throw new IllegalStateException("jolt-jni failed to create native factory");
        }
        Jolt.registerTypes();
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

    private static Optional<String> resolveBundledResourcePath() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);

        if (os.contains("mac") && (arch.equals("aarch64") || arch.equals("arm64"))) {
            return Optional.of("osx/aarch64/com/github/stephengold/libjoltjni.dylib");
        }
        if (os.contains("mac") && (arch.equals("x86_64") || arch.equals("amd64"))) {
            return Optional.of("osx/x86-64/com/github/stephengold/libjoltjni.dylib");
        }
        if (os.contains("linux") && (arch.equals("x86_64") || arch.equals("amd64"))) {
            return Optional.of("linux/x86-64/com/github/stephengold/libjoltjni.so");
        }
        if (os.contains("win") && (arch.equals("x86_64") || arch.equals("amd64"))) {
            return Optional.of("windows/x86-64/com/github/stephengold/joltjni.dll");
        }
        return Optional.empty();
    }

    private static String platformKey() {
        return System.getProperty("os.name", "unknown") + "/" + System.getProperty("os.arch", "unknown");
    }

    record LoadResult(boolean available, String description) {
    }
}
