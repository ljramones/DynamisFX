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

package org.dynamisfx.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.Group;
import org.junit.jupiter.api.Test;

class NodeCollisionAdapterTest {

    @Test
    void storesAndRetrievesDirectFilterProperty() {
        Group node = new Group();
        CollisionFilter filter = new CollisionFilter(0b0010, 0b0101, CollisionKind.TRIGGER);
        NodeCollisionAdapter.setFilter(node, filter);

        assertEquals(filter, NodeCollisionAdapter.getFilter(node));
    }

    @Test
    void derivesFilterFromLayerMaskAndKindProperties() {
        Group node = new Group();
        node.getProperties().put(NodeCollisionAdapter.LAYER_BITS_PROPERTY_KEY, 0b0100);
        node.getProperties().put(NodeCollisionAdapter.MASK_BITS_PROPERTY_KEY, 0b0011);
        node.getProperties().put(NodeCollisionAdapter.KIND_PROPERTY_KEY, CollisionKind.TRIGGER);

        CollisionFilter filter = NodeCollisionAdapter.getFilter(node);
        assertEquals(0b0100, filter.layerBits());
        assertEquals(0b0011, filter.maskBits());
        assertEquals(CollisionKind.TRIGGER, filter.kind());
    }

    @Test
    void defaultsFilterWhenNoPropertiesPresent() {
        Group node = new Group();
        CollisionFilter filter = NodeCollisionAdapter.getFilter(node);
        assertEquals(CollisionFilter.DEFAULT, filter);
    }
}
