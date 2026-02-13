package org.dynamisfx.physics.jolt;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Jolt backend backed by the published jolt-jni bindings.
 */
final class JoltJniBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private final JoltJniNativeLoader.LoadResult loadResult;

    JoltJniBackend() {
        this.loadResult = JoltJniNativeLoader.ensureLoaded();
    }

    boolean isAvailable() {
        return loadResult.available();
    }

    @Override
    public String id() {
        return "jolt";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        if (!loadResult.available()) {
            throw unavailable("initialize");
        }
        return new JoltJniWorld(configuration, loadResult.description());
    }

    private IllegalStateException unavailable(String operation) {
        return new IllegalStateException(
                "jolt-jni backend unavailable for operation "
                        + operation
                        + "; load-status="
                        + loadResult.description()
                        + ", path property="
                        + JoltJniNativeLoader.NATIVE_PATH_PROPERTY
                        + ", env="
                        + JoltJniNativeLoader.NATIVE_PATH_ENV);
    }
}
