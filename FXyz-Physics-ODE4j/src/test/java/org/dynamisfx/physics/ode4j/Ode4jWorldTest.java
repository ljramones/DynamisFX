package org.dynamisfx.physics.ode4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsConstraintType;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class Ode4jWorldTest {

    @Test
    void stepsDynamicBodyWithSimpleGravityIntegration() {
        Ode4jBackend backend = new Ode4jBackend();
        Ode4jWorld world = (Ode4jWorld) backend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0.0, -9.81, 0.0),
                1.0 / 60.0));

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));

        world.step(0.5);
        PhysicsBodyState state = world.getBodyState(handle);

        assertTrue(state.linearVelocity().y() < 0.0);
        assertTrue(state.position().y() < 0.0);
        assertEquals(0.5, state.timestampSeconds(), 1e-9);
        assertNotNull(state.orientation());
    }

    @Test
    void removeAndCloseLifecycleIsValidated() {
        Ode4jWorld world = new Ode4jWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0));
        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                0.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));

        assertTrue(world.removeBody(handle));
        assertFalse(world.removeBody(handle));

        world.close();
        assertThrows(IllegalStateException.class, () -> world.bodies());
        assertThrows(IllegalStateException.class, () -> world.step(0.016));
    }

    @Test
    void roundTripsBodyStateThroughEngineMapping() {
        Ode4jWorld world = new Ode4jWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0));
        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.KINEMATIC,
                0.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));

        PhysicsBodyState written = new PhysicsBodyState(
                new PhysicsVector3(4, 5, 6),
                new org.dynamisfx.physics.model.PhysicsQuaternion(0, 0, 0, 1),
                new PhysicsVector3(0.2, 0.3, 0.4),
                new PhysicsVector3(1.2, 1.3, 1.4),
                ReferenceFrame.WORLD,
                12.0);
        world.setBodyState(handle, written);

        PhysicsBodyState read = world.getBodyState(handle);
        assertEquals(4.0, read.position().x(), 1e-9);
        assertEquals(5.0, read.position().y(), 1e-9);
        assertEquals(6.0, read.position().z(), 1e-9);
        assertEquals(0.2, read.linearVelocity().x(), 1e-9);
        assertEquals(1.4, read.angularVelocity().z(), 1e-9);
    }

    @Test
    void createsAndRemovesConstraints() {
        Ode4jWorld world = new Ode4jWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0));
        PhysicsBodyHandle a = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        PhysicsBodyHandle b = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));

        PhysicsConstraintHandle handle = world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.BALL,
                a,
                b,
                new PhysicsVector3(0, 0, 0)));
        assertEquals(1, world.constraints().size());
        assertTrue(world.removeConstraint(handle));
        assertFalse(world.removeConstraint(handle));

        PhysicsConstraintHandle hinge = world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.HINGE,
                a,
                b,
                new PhysicsVector3(0, 0, 0),
                new PhysicsVector3(0, 1, 0),
                -0.5,
                0.5));
        PhysicsConstraintHandle slider = world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.SLIDER,
                a,
                b,
                null,
                new PhysicsVector3(1, 0, 0),
                -2.0,
                2.0));
        assertEquals(2, world.constraints().size());
        assertTrue(world.removeConstraint(hinge));
        assertTrue(world.removeConstraint(slider));
    }

    @Test
    void supportsExtendedWorldConfigurationParameters() {
        Ode4jWorld world = new Ode4jWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 120.0,
                24,
                0.8,
                0.0,
                1e-6,
                0.0));
        PhysicsBodyHandle body = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));
        world.step(1.0 / 120.0);
        PhysicsBodyState state = world.getBodyState(body);
        assertTrue(state.position().y() < 0.0);
    }

    @Test
    void supportsRuntimeTuningMutation() {
        Ode4jWorld world = new Ode4jWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 120.0));
        world.setRuntimeTuning(new PhysicsRuntimeTuning(12, 0.9, 0.2, 1e-4, 0.05));
        assertEquals(12, world.runtimeTuning().solverIterations());
        assertEquals(0.9, world.runtimeTuning().contactFriction(), 1e-9);
    }
}
