package org.dynamisfx.simulation.coupling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Transition event emitted when a simulation object changes coupling mode.
 */
public record CouplingModeTransitionEvent(
        double simulationTimeSeconds,
        String objectId,
        ObjectSimulationMode fromMode,
        ObjectSimulationMode toMode,
        CouplingDecisionReason reason,
        List<PhysicsZone> zones) {

    public CouplingModeTransitionEvent {
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(fromMode, "fromMode must not be null");
        Objects.requireNonNull(toMode, "toMode must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(zones, "zones must not be null");
        if (fromMode == toMode) {
            throw new IllegalArgumentException("fromMode and toMode must differ for transition events");
        }
        zones = List.copyOf(zones);
    }

    public List<ZoneId> zoneIds() {
        return zones.stream().map(PhysicsZone::zoneId).toList();
    }

    public List<ReferenceFrame> zoneFrames() {
        return zones.stream().map(PhysicsZone::anchorFrame).toList();
    }

    public Optional<ZoneId> selectedZoneId() {
        return DeterministicZoneSelector.select(zones, null, null).map(PhysicsZone::zoneId);
    }

    public Optional<ReferenceFrame> selectedZoneFrame() {
        return DeterministicZoneSelector.select(zones, null, null).map(PhysicsZone::anchorFrame);
    }
}
