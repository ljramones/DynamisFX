package org.fxyz3d;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompatTypesTest {

    @Test
    void sampleInterfaceMapsToDynamisType() {
        assertTrue(org.dynamisfx.DynamisFXSample.class.isAssignableFrom(FXyzSample.class));
    }

    @Test
    void projectInterfaceMapsToDynamisType() {
        assertTrue(org.dynamisfx.DynamisFXSamplerProject.class.isAssignableFrom(FXyzSamplerProject.class));
    }
}
