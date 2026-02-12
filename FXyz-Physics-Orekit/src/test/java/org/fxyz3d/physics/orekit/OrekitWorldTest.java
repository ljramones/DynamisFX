package org.fxyz3d.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.api.PhysicsConstraintDefinition;
import org.fxyz3d.physics.api.PhysicsConstraintType;
import org.fxyz3d.physics.model.PhysicsBodyDefinition;
import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsBodyType;
import org.fxyz3d.physics.model.PhysicsRuntimeTuning;
import org.fxyz3d.physics.model.PhysicsVector3;
import org.fxyz3d.physics.model.PhysicsWorldConfiguration;
import org.fxyz3d.physics.model.ReferenceFrame;
import org.fxyz3d.physics.model.SphereShape;
import org.junit.jupiter.api.Test;

class OrekitWorldTest {

    @Test
    void advancesDynamicBodyUsingNBodyAttraction() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0));

        world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                5.972e24,
                new SphereShape(6_371_000.0),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        org.fxyz3d.physics.model.PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.ICRF,
                        0.0)));

        PhysicsBodyHandle orbiter = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1200.0,
                new SphereShape(2.0),
                new PhysicsBodyState(
                        new PhysicsVector3(7_000_000.0, 0.0, 0.0),
                        org.fxyz3d.physics.model.PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(0.0, 7_500.0, 0.0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.ICRF,
                        0.0)));

        world.step(10.0);
        PhysicsBodyState state = world.getBodyState(orbiter);

        assertTrue(state.position().y() > 0.0);
        assertNotEquals(7_000_000.0, state.position().x(), 1e-6);
        assertEquals(10.0, state.timestampSeconds(), 1e-9);
    }

    @Test
    void constraintOperationsAreExplicitlyUnsupported() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0));
        PhysicsBodyHandle a = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new SphereShape(1.0), PhysicsBodyState.IDENTITY));
        PhysicsBodyHandle b = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new SphereShape(1.0), PhysicsBodyState.IDENTITY));

        assertThrows(UnsupportedOperationException.class, () -> world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.BALL,
                a,
                b,
                new PhysicsVector3(0, 0, 0))));
        assertFalse(world.removeConstraint(new org.fxyz3d.physics.api.PhysicsConstraintHandle(99)));
    }

    @Test
    void supportsRuntimeTuningAndLifecycleValidation() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.EME2000,
                PhysicsVector3.ZERO,
                1.0));
        world.setRuntimeTuning(new PhysicsRuntimeTuning(8, 0.7, 0.2, 1e-4, 0.05));
        assertEquals(8, world.runtimeTuning().solverIterations());

        world.close();
        assertThrows(IllegalStateException.class, () -> world.bodies());
        assertThrows(IllegalStateException.class, () -> world.step(1.0));
    }
}
