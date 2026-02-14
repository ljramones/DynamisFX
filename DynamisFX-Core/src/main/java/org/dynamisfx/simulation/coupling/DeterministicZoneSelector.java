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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Deterministic zone selection helpers for overlapping-zone transitions.
 */
public final class DeterministicZoneSelector {

    private static final Comparator<PhysicsZone> ZONE_ORDER =
            Comparator.comparing(zone -> zone.zoneId().value());

    private DeterministicZoneSelector() {
    }

    public static Optional<PhysicsZone> select(
            List<PhysicsZone> zones,
            ZoneId preferredZoneId,
            PhysicsVector3 globalPositionHint) {
        Objects.requireNonNull(zones, "zones must not be null");
        if (zones.isEmpty()) {
            return Optional.empty();
        }
        if (preferredZoneId != null) {
            for (PhysicsZone zone : zones) {
                if (preferredZoneId.equals(zone.zoneId())) {
                    return Optional.of(zone);
                }
            }
        }
        if (globalPositionHint != null) {
            PhysicsZone best = null;
            double bestDistance2 = Double.POSITIVE_INFINITY;
            for (PhysicsZone zone : zones) {
                double distance2 = squaredDistance(globalPositionHint, zone.anchorPosition());
                if (distance2 < bestDistance2) {
                    best = zone;
                    bestDistance2 = distance2;
                    continue;
                }
                if (distance2 == bestDistance2 && best != null && ZONE_ORDER.compare(zone, best) < 0) {
                    best = zone;
                }
            }
            if (best != null) {
                return Optional.of(best);
            }
        }
        return zones.stream().min(ZONE_ORDER);
    }

    private static double squaredDistance(PhysicsVector3 a, PhysicsVector3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return (dx * dx) + (dy * dy) + (dz * dz);
    }
}
