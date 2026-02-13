package org.dynamisfx.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TransformStoreTest {

    @Test
    void publishedReadBufferStaysStableUntilNextPublish() {
        TransformStore store = new TransformStore(2);
        store.setTransform(0, 1.0, 2.0, 3.0, 0.0, 0.0, 0.0, 1.0);
        store.publish(2.5);

        TransformStore.TransformSample published = store.sample(0);
        assertEquals(1.0, published.posX(), 1e-9);
        assertEquals(2.5, published.simulationTimeSeconds(), 1e-9);

        store.setTransform(0, 9.0, 8.0, 7.0, 0.1, 0.2, 0.3, 0.9);
        TransformStore.TransformSample stillRead = store.sample(0);
        assertEquals(1.0, stillRead.posX(), 1e-9);

        store.publish(3.0);
        TransformStore.TransformSample updated = store.sample(0);
        assertEquals(9.0, updated.posX(), 1e-9);
        assertEquals(3.0, updated.simulationTimeSeconds(), 1e-9);
    }

    @Test
    void validatesCapacityIndicesAndValues() {
        assertThrows(IllegalArgumentException.class, () -> new TransformStore(0));

        TransformStore store = new TransformStore(1);
        assertThrows(IllegalArgumentException.class, () ->
                store.setTransform(1, 0, 0, 0, 0, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () ->
                store.setTransform(0, Double.NaN, 0, 0, 0, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> store.sample(-1));
        assertThrows(IllegalArgumentException.class, () -> store.publish(Double.NaN));
    }
}
