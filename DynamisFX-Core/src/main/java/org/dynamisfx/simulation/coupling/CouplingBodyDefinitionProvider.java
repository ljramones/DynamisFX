package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.SphereShape;

/**
 * Supplies rigid-body definitions for objects promoted into physics-active mode.
 */
@FunctionalInterface
public interface CouplingBodyDefinitionProvider {

    PhysicsBodyDefinition createBodyDefinition(
            String objectId,
            CouplingModeTransitionEvent event,
            PhysicsZone zone,
            PhysicsBodyState seedState);

    static CouplingBodyDefinitionProvider dynamicSphere(double radiusMeters, double massKg) {
        if (!(radiusMeters > 0.0)) {
            throw new IllegalArgumentException("radiusMeters must be > 0");
        }
        if (!(massKg > 0.0) || !Double.isFinite(massKg)) {
            throw new IllegalArgumentException("massKg must be finite and > 0");
        }
        return (objectId, event, zone, seedState) -> {
            if (objectId == null || objectId.isBlank()) {
                throw new IllegalArgumentException("objectId must not be blank");
            }
            Objects.requireNonNull(event, "event must not be null");
            Objects.requireNonNull(zone, "zone must not be null");
            Objects.requireNonNull(seedState, "seedState must not be null");
            return new PhysicsBodyDefinition(
                    PhysicsBodyType.DYNAMIC,
                    massKg,
                    new SphereShape(radiusMeters),
                    seedState);
        };
    }
}
