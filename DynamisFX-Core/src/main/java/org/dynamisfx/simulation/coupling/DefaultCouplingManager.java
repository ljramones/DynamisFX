package org.dynamisfx.simulation.coupling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Minimal in-memory coupling manager used for phase-1 orchestration scaffolding.
 */
public final class DefaultCouplingManager implements CouplingManager {

    private final Map<ZoneId, PhysicsZone> zones = new LinkedHashMap<>();
    private final Map<String, ObjectSimulationMode> modesByObjectId = new LinkedHashMap<>();
    private final Map<String, Double> lastTransitionTimeByObjectId = new LinkedHashMap<>();
    private final Set<CouplingTelemetryListener> telemetryListeners = new LinkedHashSet<>();
    private final CouplingTransitionPolicy transitionPolicy;
    private final CouplingObservationProvider observationProvider;

    public DefaultCouplingManager() {
        this(
                context -> CouplingTransitionDecision.noChange(CouplingDecisionReason.NO_CHANGE),
                new NoopObservationProvider());
    }

    public DefaultCouplingManager(CouplingTransitionPolicy transitionPolicy) {
        this(transitionPolicy, new NoopObservationProvider());
    }

    public DefaultCouplingManager(
            CouplingTransitionPolicy transitionPolicy,
            CouplingObservationProvider observationProvider) {
        this.transitionPolicy = Objects.requireNonNull(transitionPolicy, "transitionPolicy must not be null");
        this.observationProvider = Objects.requireNonNull(observationProvider, "observationProvider must not be null");
    }

    @Override
    public synchronized void registerZone(PhysicsZone zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        zones.put(zone.zoneId(), zone);
    }

    @Override
    public synchronized boolean removeZone(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return zones.remove(zoneId) != null;
    }

    @Override
    public synchronized Collection<PhysicsZone> zones() {
        return new ArrayList<>(zones.values());
    }

    @Override
    public synchronized Optional<ObjectSimulationMode> modeFor(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        return Optional.ofNullable(modesByObjectId.get(objectId));
    }

    @Override
    public synchronized void setMode(String objectId, ObjectSimulationMode mode) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(mode, "mode must not be null");
        modesByObjectId.put(objectId, mode);
    }

    /**
     * Returns the last simulation timestamp when the object changed mode.
     */
    public synchronized Optional<Double> lastTransitionTimeSeconds(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        return Optional.ofNullable(lastTransitionTimeByObjectId.get(objectId));
    }

    public synchronized void addTelemetryListener(CouplingTelemetryListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        telemetryListeners.add(listener);
    }

    public synchronized boolean removeTelemetryListener(CouplingTelemetryListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        return telemetryListeners.remove(listener);
    }

    @Override
    public synchronized void update(double simulationTimeSeconds) {
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        Collection<PhysicsZone> zoneSnapshot = new ArrayList<>(zones.values());
        for (Map.Entry<String, ObjectSimulationMode> entry : modesByObjectId.entrySet()) {
            String objectId = entry.getKey();
            ObjectSimulationMode currentMode = entry.getValue();
            double lastTransitionTime = lastTransitionTimeByObjectId.getOrDefault(objectId, -1.0);
            CouplingTransitionContext context = new CouplingTransitionContext(
                    objectId,
                    currentMode,
                    simulationTimeSeconds,
                    lastTransitionTime,
                    observationProvider.predictedInterceptSeconds(objectId, zoneSnapshot),
                    zoneSnapshot);
            CouplingTransitionDecision decision = transitionPolicy.evaluate(context);
            ObjectSimulationMode resolvedMode = decision.nextMode().orElse(currentMode);
            boolean transitioned = resolvedMode != currentMode;
            if (transitioned) {
                entry.setValue(resolvedMode);
                lastTransitionTimeByObjectId.put(objectId, simulationTimeSeconds);
            }
            emitTelemetry(new CouplingTelemetryEvent(
                    simulationTimeSeconds,
                    objectId,
                    currentMode,
                    resolvedMode,
                    transitioned,
                    decision.reason()));
        }
    }

    private void emitTelemetry(CouplingTelemetryEvent event) {
        if (telemetryListeners.isEmpty()) {
            return;
        }
        for (CouplingTelemetryListener listener : telemetryListeners) {
            listener.onTelemetry(event);
        }
    }

    private static final class NoopObservationProvider implements CouplingObservationProvider {
        @Override
        public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.empty();
        }

        @Override
        public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.empty();
        }

        @Override
        public boolean hasActiveContact(String objectId) {
            return false;
        }
    }
}
