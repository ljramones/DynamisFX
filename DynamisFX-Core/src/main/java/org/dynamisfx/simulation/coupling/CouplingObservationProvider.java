package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.OptionalDouble;

/**
 * Supplies transition-relevant observations for coupling policies.
 */
public interface CouplingObservationProvider {

    /**
     * Returns distance (meters) from object to nearest relevant zone boundary/anchor metric.
     * Implementations define how distance is measured.
     */
    OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones);

    /**
     * Indicates whether the object currently has active physical contact/constraints.
     */
    boolean hasActiveContact(String objectId);
}
