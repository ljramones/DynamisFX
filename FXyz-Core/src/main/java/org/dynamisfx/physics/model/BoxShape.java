package org.dynamisfx.physics.model;

/**
 * Box shape (full extents in meters).
 */
public record BoxShape(double width, double height, double depth) implements PhysicsShape {

    public BoxShape {
        if (!(width > 0.0) || !(height > 0.0) || !(depth > 0.0)) {
            throw new IllegalArgumentException("box extents must be > 0");
        }
    }
}
