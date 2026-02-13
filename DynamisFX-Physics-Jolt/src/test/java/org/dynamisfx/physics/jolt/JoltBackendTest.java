package org.dynamisfx.physics.jolt;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class JoltBackendTest {

    @AfterEach
    void clearProviderProperty() {
        System.clearProperty(JoltBackend.PROVIDER_PROPERTY);
    }

    @Test
    void failsFastWhenNativeBridgeIsUnavailable() {
        JoltBackend backend = new JoltBackend(new JoltNativeBridge(false));
        PhysicsWorldConfiguration configuration = new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 120.0);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> backend.createWorld(configuration));
        assertTrue(error.getMessage().contains("dynamisfx_jolt_cshim"));
    }

    @Test
    void rejectsUnknownProviderProperty() {
        System.setProperty(JoltBackend.PROVIDER_PROPERTY, "nope");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, JoltBackend::new);
        assertTrue(error.getMessage().contains("Unknown jolt provider"));
    }
}
