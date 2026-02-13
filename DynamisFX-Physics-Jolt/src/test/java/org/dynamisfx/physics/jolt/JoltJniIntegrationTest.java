package org.dynamisfx.physics.jolt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class JoltJniIntegrationTest {

    @AfterEach
    void clearProviderProperty() {
        System.clearProperty(JoltBackend.PROVIDER_PROPERTY);
    }

    @Test
    void stepsDynamicBodyWhenJoltJniIsAvailable() {
        Assumptions.assumeTrue(
                Boolean.getBoolean("dynamisfx.joltjni.integration"),
                "Set -Ddynamisfx.joltjni.integration=true to run native jolt-jni integration test");
        JoltJniNativeLoader.LoadResult loadResult = JoltJniNativeLoader.ensureLoaded();
        Assumptions.assumeTrue(loadResult.available(), "jolt-jni native runtime is unavailable: " + loadResult.description());

        System.setProperty(JoltBackend.PROVIDER_PROPERTY, JoltBackend.PROVIDER_JOLT_JNI);
        JoltBackend backend = new JoltBackend();

        PhysicsWorld world = backend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0.0, -9.81, 0.0),
                1.0 / 60.0));

        PhysicsBodyHandle body = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new SphereShape(0.5),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));

        assertNotNull(body);
        world.step(0.25);
        PhysicsBodyState state = world.getBodyState(body);
        assertTrue(state.position().y() < 0.0);
        world.close();
    }
}
