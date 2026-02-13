package org.dynamisfx.simulation.coupling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Telemetry event emitted per object update by the coupling manager.
 */
public record CouplingTelemetryEvent(
        double simulationTimeSeconds,
        String objectId,
        ObjectSimulationMode fromMode,
        ObjectSimulationMode toMode,
        boolean transitioned,
        CouplingDecisionReason reason,
        double lastTransitionTimeSeconds,
        OptionalDouble observedDistanceMeters,
        OptionalDouble predictedInterceptSeconds,
        Optional<ZoneId> selectedZoneId,
        Optional<ReferenceFrame> selectedZoneFrame,
        List<ZoneId> zoneIds,
        List<ReferenceFrame> zoneFrames) {

    public CouplingTelemetryEvent(
            double simulationTimeSeconds,
            String objectId,
            ObjectSimulationMode fromMode,
            ObjectSimulationMode toMode,
            boolean transitioned,
            CouplingDecisionReason reason) {
        this(
                simulationTimeSeconds,
                objectId,
                fromMode,
                toMode,
                transitioned,
                reason,
                -1.0,
                OptionalDouble.empty(),
                OptionalDouble.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of());
    }

    public CouplingTelemetryEvent {
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(fromMode, "fromMode must not be null");
        Objects.requireNonNull(toMode, "toMode must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (!Double.isFinite(lastTransitionTimeSeconds)) {
            throw new IllegalArgumentException("lastTransitionTimeSeconds must be finite");
        }
        Objects.requireNonNull(observedDistanceMeters, "observedDistanceMeters must not be null");
        Objects.requireNonNull(predictedInterceptSeconds, "predictedInterceptSeconds must not be null");
        Objects.requireNonNull(selectedZoneId, "selectedZoneId must not be null");
        Objects.requireNonNull(selectedZoneFrame, "selectedZoneFrame must not be null");
        Objects.requireNonNull(zoneIds, "zoneIds must not be null");
        Objects.requireNonNull(zoneFrames, "zoneFrames must not be null");
        zoneIds = List.copyOf(zoneIds);
        zoneFrames = List.copyOf(zoneFrames);
    }
}
