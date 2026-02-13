package org.dynamisfx.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class OrekitBackendFactoryTest {

    @Test
    void createsBackendWithExpectedCapabilities() {
        OrekitBackendFactory factory = new OrekitBackendFactory();
        assertEquals("orekit", factory.backendId());

        PhysicsBackend backend = factory.createBackend();
        assertEquals("orekit", backend.id());
        assertFalse(backend.capabilities().supportsRigidBodies());
        assertTrue(backend.capabilities().supportsNBody());
        assertFalse(backend.capabilities().supportsJoints());
    }
}
