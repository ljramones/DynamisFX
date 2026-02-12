package org.fxyz3d.collision;

import javafx.scene.Node;

/**
 * JavaFX scene-node adapter hooks for collision bounds and filters.
 */
public final class NodeCollisionAdapter {

    public static final String FILTER_PROPERTY_KEY = "org.fxyz3d.collision.filter";
    public static final String LAYER_BITS_PROPERTY_KEY = "org.fxyz3d.collision.layerBits";
    public static final String MASK_BITS_PROPERTY_KEY = "org.fxyz3d.collision.maskBits";
    public static final String KIND_PROPERTY_KEY = "org.fxyz3d.collision.kind";

    private NodeCollisionAdapter() {
    }

    public static Aabb boundsInParent(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        return Aabb.fromBounds(node.getBoundsInParent());
    }

    public static void setFilter(Node node, CollisionFilter filter) {
        if (node == null || filter == null) {
            throw new IllegalArgumentException("node and filter must not be null");
        }
        node.getProperties().put(FILTER_PROPERTY_KEY, filter);
    }

    public static CollisionFilter getFilter(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        Object direct = node.getProperties().get(FILTER_PROPERTY_KEY);
        if (direct instanceof CollisionFilter f) {
            return f;
        }

        int layerBits = intProp(node, LAYER_BITS_PROPERTY_KEY, CollisionFilter.DEFAULT.layerBits());
        int maskBits = intProp(node, MASK_BITS_PROPERTY_KEY, CollisionFilter.DEFAULT.maskBits());
        CollisionKind kind = kindProp(node, KIND_PROPERTY_KEY, CollisionFilter.DEFAULT.kind());
        return new CollisionFilter(layerBits, maskBits, kind);
    }

    private static int intProp(Node node, String key, int fallback) {
        Object value = node.getProperties().get(key);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    private static CollisionKind kindProp(Node node, String key, CollisionKind fallback) {
        Object value = node.getProperties().get(key);
        return value instanceof CollisionKind k ? k : fallback;
    }
}
