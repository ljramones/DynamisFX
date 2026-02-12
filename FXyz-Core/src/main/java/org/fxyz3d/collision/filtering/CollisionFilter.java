package org.fxyz3d.collision;

/**
 * Layer/mask filter and trigger/solid classification.
 */
public record CollisionFilter(int layerBits, int maskBits, CollisionKind kind) {

    public static final CollisionFilter DEFAULT = new CollisionFilter(1, 0xFFFFFFFF, CollisionKind.SOLID);

    public CollisionFilter {
        if (layerBits == 0) {
            throw new IllegalArgumentException("layerBits must not be 0");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
    }

    public boolean canInteract(CollisionFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return (this.layerBits & other.maskBits) != 0
                && (other.layerBits & this.maskBits) != 0;
    }

    public boolean responseEnabledWith(CollisionFilter other) {
        return canInteract(other)
                && this.kind == CollisionKind.SOLID
                && other.kind == CollisionKind.SOLID;
    }
}
