package org.dynamisfx.physics.jolt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.CapsuleShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsShape;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;

/**
 * Loads the Jolt C-shim native library.
 */
final class JoltNativeBridge {

    static final String LIBRARY_NAME = "dynamisfx_jolt_cshim";
    static final String NATIVE_PATH_PROPERTY = "dynamisfx.jolt.native.path";
    static final String NATIVE_PATH_ENV = "DYNAMISFX_JOLT_NATIVE_PATH";
    static final int SHAPE_SPHERE = 0;
    static final int SHAPE_BOX = 1;
    static final int SHAPE_CAPSULE = 2;
    static final int BODY_STATIC = 0;
    static final int BODY_KINEMATIC = 1;
    static final int BODY_DYNAMIC = 2;
    static final int API_VERSION = 1;

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

    int apiVersion() {
        if (!available) {
            return -1;
        }
        return nativeApiVersion();
    }

    long worldCreate(PhysicsWorldConfiguration configuration) {
        PhysicsRuntimeTuning tuning = configuration.runtimeTuning();
        PhysicsVector3 gravity = configuration.gravity();
        return nativeWorldCreate(
                gravity.x(),
                gravity.y(),
                gravity.z(),
                configuration.fixedStepSeconds(),
                tuning.solverIterations(),
                tuning.contactFriction(),
                tuning.contactBounce(),
                tuning.contactSoftCfm(),
                tuning.contactBounceVelocity());
    }

    void worldDestroy(long worldHandle) {
        nativeWorldDestroy(worldHandle);
    }

    long bodyCreate(long worldHandle, PhysicsBodyDefinition definition) {
        PhysicsBodyState state = definition.initialState();
        PhysicsVector3 shapeSize = mapShapeSize(definition.shape());
        return nativeBodyCreate(
                worldHandle,
                mapBodyType(definition.bodyType()),
                mapShapeType(definition.shape()),
                definition.massKg(),
                shapeSize.x(),
                shapeSize.y(),
                shapeSize.z(),
                state.position().x(),
                state.position().y(),
                state.position().z(),
                state.orientation().x(),
                state.orientation().y(),
                state.orientation().z(),
                state.orientation().w(),
                state.linearVelocity().x(),
                state.linearVelocity().y(),
                state.linearVelocity().z(),
                state.angularVelocity().x(),
                state.angularVelocity().y(),
                state.angularVelocity().z());
    }

    int bodyDestroy(long worldHandle, long bodyId) {
        return nativeBodyDestroy(worldHandle, bodyId);
    }

    PhysicsBodyState bodyGetState(long worldHandle, long bodyId, ReferenceFrame referenceFrame, double timestampSeconds) {
        double[] state = new double[13];
        int status = nativeBodyGetState(worldHandle, bodyId, state);
        if (status != 0) {
            return null;
        }
        return decodeState(state, referenceFrame, timestampSeconds);
    }

    int bodySetState(long worldHandle, long bodyId, PhysicsBodyState state) {
        return nativeBodySetState(worldHandle, bodyId, encodeState(state));
    }

    int worldStep(long worldHandle, double dtSeconds) {
        return nativeWorldStep(worldHandle, dtSeconds);
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

    private static int mapBodyType(PhysicsBodyType type) {
        return switch (type) {
            case STATIC -> BODY_STATIC;
            case KINEMATIC -> BODY_KINEMATIC;
            case DYNAMIC -> BODY_DYNAMIC;
        };
    }

    private static int mapShapeType(PhysicsShape shape) {
        if (shape instanceof SphereShape) {
            return SHAPE_SPHERE;
        }
        if (shape instanceof BoxShape) {
            return SHAPE_BOX;
        }
        if (shape instanceof CapsuleShape) {
            return SHAPE_CAPSULE;
        }
        throw new IllegalArgumentException("Unsupported shape: " + shape.getClass().getName());
    }

    private static PhysicsVector3 mapShapeSize(PhysicsShape shape) {
        if (shape instanceof SphereShape sphere) {
            return new PhysicsVector3(sphere.radius(), 0.0, 0.0);
        }
        if (shape instanceof BoxShape box) {
            return new PhysicsVector3(box.width(), box.height(), box.depth());
        }
        if (shape instanceof CapsuleShape capsule) {
            return new PhysicsVector3(capsule.radius(), capsule.length(), 0.0);
        }
        throw new IllegalArgumentException("Unsupported shape: " + shape.getClass().getName());
    }

    private static double[] encodeState(PhysicsBodyState state) {
        return new double[] {
                state.position().x(),
                state.position().y(),
                state.position().z(),
                state.orientation().x(),
                state.orientation().y(),
                state.orientation().z(),
                state.orientation().w(),
                state.linearVelocity().x(),
                state.linearVelocity().y(),
                state.linearVelocity().z(),
                state.angularVelocity().x(),
                state.angularVelocity().y(),
                state.angularVelocity().z()
        };
    }

    private static PhysicsBodyState decodeState(double[] values, ReferenceFrame referenceFrame, double timestampSeconds) {
        return new PhysicsBodyState(
                new PhysicsVector3(values[0], values[1], values[2]),
                new org.dynamisfx.physics.model.PhysicsQuaternion(values[3], values[4], values[5], values[6]),
                new PhysicsVector3(values[7], values[8], values[9]),
                new PhysicsVector3(values[10], values[11], values[12]),
                referenceFrame,
                timestampSeconds);
    }

    private static native int nativeApiVersion();

    private static native long nativeWorldCreate(
            double gx,
            double gy,
            double gz,
            double fixedStepSeconds,
            int solverIterations,
            double friction,
            double restitution,
            double cfm,
            double restitutionThreshold);

    private static native void nativeWorldDestroy(long worldHandle);

    private static native long nativeBodyCreate(
            long worldHandle,
            int bodyType,
            int shapeType,
            double massKg,
            double sx,
            double sy,
            double sz,
            double px,
            double py,
            double pz,
            double qx,
            double qy,
            double qz,
            double qw,
            double lvx,
            double lvy,
            double lvz,
            double avx,
            double avy,
            double avz);

    private static native int nativeBodyDestroy(long worldHandle, long bodyId);

    private static native int nativeBodyGetState(long worldHandle, long bodyId, double[] outState13);

    private static native int nativeBodySetState(long worldHandle, long bodyId, double[] state13);

    private static native int nativeWorldStep(long worldHandle, double dtSeconds);
}
