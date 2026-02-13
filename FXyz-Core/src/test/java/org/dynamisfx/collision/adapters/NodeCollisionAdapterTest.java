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
