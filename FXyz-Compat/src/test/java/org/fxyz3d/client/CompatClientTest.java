package org.fxyz3d.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompatClientTest {

    @Test
    void legacyClientExtendsNewClient() {
        assertTrue(org.dynamisfx.client.DynamisFXClient.class.isAssignableFrom(FXyzClient.class));
    }
}
