package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable in-memory observation provider for phase-1 demos and scaffolding.
 */
public final class MutableCouplingObservationProvider implements CouplingObservationProvider {

    private final Map<String, Double> distanceByObjectId = new ConcurrentHashMap<>();
    private final Map<String, Boolean> contactByObjectId = new ConcurrentHashMap<>();

    @Override
    public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (zones == null) {
            throw new IllegalArgumentException("zones must not be null");
        }
        if (zones.isEmpty()) {
            return OptionalDouble.empty();
        }
        Double distance = distanceByObjectId.get(objectId);
        return distance == null ? OptionalDouble.empty() : OptionalDouble.of(distance);
    }

    @Override
    public boolean hasActiveContact(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        return Boolean.TRUE.equals(contactByObjectId.get(objectId));
    }

    public void setDistanceMeters(String objectId, double distanceMeters) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (!Double.isFinite(distanceMeters) || distanceMeters < 0.0) {
            throw new IllegalArgumentException("distanceMeters must be finite and >= 0");
        }
        distanceByObjectId.put(objectId, distanceMeters);
    }

    public void clearDistance(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        distanceByObjectId.remove(objectId);
    }

    public void setActiveContact(String objectId, boolean activeContact) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        contactByObjectId.put(objectId, activeContact);
    }
}
