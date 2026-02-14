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
    private final Map<String, Double> interceptSecondsByObjectId = new ConcurrentHashMap<>();
    private final Map<String, Boolean> contactByObjectId = new ConcurrentHashMap<>();
    private final Map<String, Double> altitudeByObjectId = new ConcurrentHashMap<>();

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
    public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (zones == null) {
            throw new IllegalArgumentException("zones must not be null");
        }
        if (zones.isEmpty()) {
            return OptionalDouble.empty();
        }
        Double intercept = interceptSecondsByObjectId.get(objectId);
        return intercept == null ? OptionalDouble.empty() : OptionalDouble.of(intercept);
    }

    @Override
    public boolean hasActiveContact(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        return Boolean.TRUE.equals(contactByObjectId.get(objectId));
    }

    @Override
    public OptionalDouble altitudeMetersAboveSurface(String objectId, Collection<PhysicsZone> zones) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (zones == null) {
            throw new IllegalArgumentException("zones must not be null");
        }
        if (zones.isEmpty()) {
            return OptionalDouble.empty();
        }
        Double altitude = altitudeByObjectId.get(objectId);
        return altitude == null ? OptionalDouble.empty() : OptionalDouble.of(altitude);
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

    public void setPredictedInterceptSeconds(String objectId, double predictedInterceptSeconds) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (!Double.isFinite(predictedInterceptSeconds) || predictedInterceptSeconds < 0.0) {
            throw new IllegalArgumentException("predictedInterceptSeconds must be finite and >= 0");
        }
        interceptSecondsByObjectId.put(objectId, predictedInterceptSeconds);
    }

    public void clearPredictedIntercept(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        interceptSecondsByObjectId.remove(objectId);
    }

    public void setAltitudeMetersAboveSurface(String objectId, double altitudeMeters) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        if (!Double.isFinite(altitudeMeters) || altitudeMeters < 0.0) {
            throw new IllegalArgumentException("altitudeMeters must be finite and >= 0");
        }
        altitudeByObjectId.put(objectId, altitudeMeters);
    }

    public void clearAltitudeMetersAboveSurface(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        altitudeByObjectId.remove(objectId);
    }
}
