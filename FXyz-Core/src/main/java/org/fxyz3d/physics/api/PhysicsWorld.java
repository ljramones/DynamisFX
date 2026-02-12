package org.fxyz3d.physics.api;

import java.util.Collection;
import org.fxyz3d.physics.model.PhysicsBodyDefinition;
import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsRuntimeTuning;

/**
 * Engine-neutral world surface used by FXyz runtime orchestration.
 */
public interface PhysicsWorld extends AutoCloseable {

    PhysicsCapabilities capabilities();

    PhysicsBodyHandle createBody(PhysicsBodyDefinition definition);

    boolean removeBody(PhysicsBodyHandle handle);

    Collection<PhysicsBodyHandle> bodies();

    PhysicsBodyState getBodyState(PhysicsBodyHandle handle);

    void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state);

    PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition);

    boolean removeConstraint(PhysicsConstraintHandle handle);

    Collection<PhysicsConstraintHandle> constraints();

    PhysicsRuntimeTuning runtimeTuning();

    void setRuntimeTuning(PhysicsRuntimeTuning tuning);

    void step(double dtSeconds);

    @Override
    default void close() {
        // no-op
    }
}
