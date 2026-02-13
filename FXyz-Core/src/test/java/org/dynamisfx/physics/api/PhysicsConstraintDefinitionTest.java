package org.dynamisfx.physics.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.junit.jupiter.api.Test;

class PhysicsConstraintDefinitionTest {

    @Test
    void validatesConstraintDefinition() {
        PhysicsConstraintDefinition definition = new PhysicsConstraintDefinition(
                PhysicsConstraintType.BALL,
                new PhysicsBodyHandle(1),
                new PhysicsBodyHandle(2),
                new PhysicsVector3(0, 1, 2));
        assertEquals(PhysicsConstraintType.BALL, definition.type());

        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(null, new PhysicsBodyHandle(1), new PhysicsBodyHandle(2), null));
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(PhysicsConstraintType.FIXED, null, new PhysicsBodyHandle(2), null));
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(
                        PhysicsConstraintType.HINGE,
                        new PhysicsBodyHandle(1),
                        new PhysicsBodyHandle(2),
                        null,
                        null,
                        null,
                        null));
        PhysicsConstraintDefinition slider = new PhysicsConstraintDefinition(
                PhysicsConstraintType.SLIDER,
                new PhysicsBodyHandle(1),
                new PhysicsBodyHandle(2),
                null,
                new PhysicsVector3(1, 0, 0),
                -1.0,
                1.0);
        assertEquals(PhysicsConstraintType.SLIDER, slider.type());
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(
                        PhysicsConstraintType.SLIDER,
                        new PhysicsBodyHandle(1),
                        new PhysicsBodyHandle(2),
                        null,
                        new PhysicsVector3(1, 0, 0),
                        2.0,
                        1.0));
    }
}
