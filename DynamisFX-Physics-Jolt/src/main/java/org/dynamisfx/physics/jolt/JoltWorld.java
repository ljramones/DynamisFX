package org.dynamisfx.physics.jolt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Initial Jolt world shell that fails fast until the native c-shim is present.
 */
public final class JoltWorld implements PhysicsWorld {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private final PhysicsWorldConfiguration configuration;
    private final JoltNativeBridge bridge;

    JoltWorld(PhysicsWorldConfiguration configuration, JoltNativeBridge bridge) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.bridge = Objects.requireNonNull(bridge, "bridge must not be null");
        requireNative();
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        throw unavailable("createBody");
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        throw unavailable("removeBody");
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        throw unavailable("bodies");
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        throw unavailable("getBodyState");
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        throw unavailable("setBodyState");
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        throw unavailable("createConstraint");
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        throw unavailable("removeConstraint");
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        return List.of();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        return configuration.runtimeTuning();
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        throw unavailable("setRuntimeTuning");
    }

    @Override
    public PhysicsVector3 gravity() {
        return configuration.gravity();
    }

    @Override
    public void setGravity(PhysicsVector3 gravity) {
        throw unavailable("setGravity");
    }

    @Override
    public void step(double dtSeconds) {
        throw unavailable("step");
    }

    private void requireNative() {
        if (!bridge.isAvailable()) {
            throw unavailable("initialize");
        }
    }

    private IllegalStateException unavailable(String operation) {
        return new IllegalStateException(
                "Jolt native backend unavailable for operation "
                        + operation
                        + "; expected native library "
                        + JoltNativeBridge.LIBRARY_NAME
                        + " ("
                        + JoltNativeBridge.expectedLibraryFileName()
                        + "), load-status="
                        + bridge.loadDescription()
                        + ", configured path property="
                        + JoltNativeBridge.NATIVE_PATH_PROPERTY
                        + ", env="
                        + JoltNativeBridge.NATIVE_PATH_ENV);
    }
}
