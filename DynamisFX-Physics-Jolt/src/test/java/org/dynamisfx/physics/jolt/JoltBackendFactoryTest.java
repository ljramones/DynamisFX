package org.dynamisfx.physics.jolt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class JoltBackendFactoryTest {

    @Test
    void createsBackendWithExpectedIdentityAndCapabilities() {
        JoltBackendFactory factory = new JoltBackendFactory();
        assertEquals("jolt", factory.backendId());

        PhysicsBackend backend = factory.createBackend();
        assertNotNull(backend);
        assertEquals("jolt", backend.id());
        assertTrue(backend.capabilities().supportsRigidBodies());
        assertTrue(backend.capabilities().supportsJoints());
        assertTrue(backend.capabilities().supportsContinuousCollisionDetection());
        assertTrue(backend.capabilities().supportsQueries());
    }
}
