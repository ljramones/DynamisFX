/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
