package org.dynamisfx.physics.ode4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Adapter shell that exposes the ODE4j world through the {@link RigidBodyWorld} contract.
 */
public final class Ode4jRigidBodyWorldAdapter implements RigidBodyWorld {

    private final PhysicsBackend backend;
    private final PhysicsWorld delegate;

    public Ode4jRigidBodyWorldAdapter(PhysicsWorldConfiguration configuration) {
        backend = new Ode4jBackend();
        delegate = backend.createWorld(configuration);
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return delegate.capabilities();
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        return delegate.createBody(definition);
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        return delegate.removeBody(handle);
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        return delegate.bodies();
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        return delegate.getBodyState(handle);
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        delegate.setBodyState(handle, state);
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        return delegate.createConstraint(definition);
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        return delegate.removeConstraint(handle);
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        return delegate.constraints();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        return delegate.runtimeTuning();
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        delegate.setRuntimeTuning(tuning);
    }

    @Override
    public void step(double dtSeconds) {
        delegate.step(dtSeconds);
    }

    /**
     * Bulk-read convenience API for transform snapshot extraction.
     */
    public Map<PhysicsBodyHandle, TransformSnapshot> readTransforms(Collection<PhysicsBodyHandle> handles) {
        Objects.requireNonNull(handles, "handles must not be null");
        Map<PhysicsBodyHandle, TransformSnapshot> result = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : handles) {
            PhysicsBodyState state = delegate.getBodyState(handle);
            result.put(handle, new TransformSnapshot(
                    state.position().x(),
                    state.position().y(),
                    state.position().z(),
                    state.orientation().x(),
                    state.orientation().y(),
                    state.orientation().z(),
                    state.orientation().w(),
                    state.timestampSeconds()));
        }
        return result;
    }

    @Override
    public void close() {
        delegate.close();
        backend.close();
    }

    public record TransformSnapshot(
            double posX,
            double posY,
            double posZ,
            double quatX,
            double quatY,
            double quatZ,
            double quatW,
            double timestampSeconds) {
    }
}
