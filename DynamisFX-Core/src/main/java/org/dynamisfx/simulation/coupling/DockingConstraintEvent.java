package org.dynamisfx.simulation.coupling;

import org.dynamisfx.physics.api.PhysicsConstraintHandle;

/**
 * Telemetry event emitted by docking constraint controller updates.
 */
public record DockingConstraintEvent(
        DockingConstraintEventType type,
        String objectId,
        ZoneId zoneId,
        String reason,
        double distanceMeters,
        PhysicsConstraintHandle constraintHandle) {
}
