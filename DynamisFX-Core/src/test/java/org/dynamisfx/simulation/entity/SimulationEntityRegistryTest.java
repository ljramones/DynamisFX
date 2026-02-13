package org.dynamisfx.simulation.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulationEntityRegistryTest {

    @Test
    void registersRetrievesAndRemovesEntities() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        registry.register("lander-1", "entity-a");

        assertEquals(1, registry.size());
        assertEquals("entity-a", registry.get("lander-1").orElseThrow());
        assertTrue(registry.remove("lander-1"));
        assertEquals(0, registry.size());
    }

    @Test
    void validatesInputs() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        assertThrows(IllegalArgumentException.class, () -> registry.register("", "x"));
        assertThrows(NullPointerException.class, () -> registry.register("id", null));
        assertThrows(IllegalArgumentException.class, () -> registry.get(" "));
        assertThrows(IllegalArgumentException.class, () -> registry.remove(""));
    }
}
