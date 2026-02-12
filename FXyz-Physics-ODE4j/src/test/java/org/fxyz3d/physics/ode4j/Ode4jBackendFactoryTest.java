package org.fxyz3d.physics.ode4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fxyz3d.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class Ode4jBackendFactoryTest {

    @Test
    void createsBackendWithExpectedIdentityAndCapabilities() {
        Ode4jBackendFactory factory = new Ode4jBackendFactory();
        assertEquals("ode4j", factory.backendId());

        PhysicsBackend backend = factory.createBackend();
        assertNotNull(backend);
        assertEquals("ode4j", backend.id());
        assertTrue(backend.capabilities().supportsRigidBodies());
    }
}
