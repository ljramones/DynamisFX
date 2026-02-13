package org.dynamisfx.simulation.coupling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Reconciles object state when authority moves between orbital and local rigid simulation.
 */
public final class CouplingStateReconciler implements CouplingTransitionListener {

    private final Function<String, Optional<OrbitalState>> orbitalStateSource;
    private final Function<String, Optional<PhysicsBodyState>> rigidStateSource;
    private final BiConsumer<String, PhysicsBodyState> rigidStateSink;
    private final BiConsumer<String, OrbitalState> orbitalStateSink;
    private final Consumer<String> rigidStateClearer;
    private final Consumer<String> orbitalStateClearer;
    private final BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver;
    private final Consumer<StateHandoffSnapshot> diagnosticsSink;

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink) {
        this(
                orbitalStateSource,
                rigidStateSource,
                rigidStateSink,
                orbitalStateSink,
                objectId -> {
                },
                objectId -> {
                },
                (objectId, zones) -> zones.isEmpty() ? Optional.empty() : Optional.of(zones.get(0)),
                snapshot -> {
                });
    }

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> rigidStateClearer,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver) {
        this(
                orbitalStateSource,
                rigidStateSource,
                rigidStateSink,
                orbitalStateSink,
                rigidStateClearer,
                orbitalStateClearer,
                zoneResolver,
                snapshot -> {
                });
    }

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> rigidStateClearer,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver,
            Consumer<StateHandoffSnapshot> diagnosticsSink) {
        this.orbitalStateSource = Objects.requireNonNull(orbitalStateSource, "orbitalStateSource must not be null");
        this.rigidStateSource = Objects.requireNonNull(rigidStateSource, "rigidStateSource must not be null");
        this.rigidStateSink = Objects.requireNonNull(rigidStateSink, "rigidStateSink must not be null");
        this.orbitalStateSink = Objects.requireNonNull(orbitalStateSink, "orbitalStateSink must not be null");
        this.rigidStateClearer = Objects.requireNonNull(rigidStateClearer, "rigidStateClearer must not be null");
        this.orbitalStateClearer = Objects.requireNonNull(orbitalStateClearer, "orbitalStateClearer must not be null");
        this.zoneResolver = Objects.requireNonNull(zoneResolver, "zoneResolver must not be null");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink must not be null");
    }

    @Override
    public void onTransition(CouplingModeTransitionEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String objectId = event.objectId();
        Optional<PhysicsZone> zoneOptional = zoneResolver.apply(objectId, event.zones());
        if (zoneOptional.isEmpty()) {
            return;
        }
        PhysicsZone zone = zoneOptional.get();
        ObjectSimulationMode fromMode = event.fromMode();
        ObjectSimulationMode toMode = event.toMode();
        if (fromMode != ObjectSimulationMode.PHYSICS_ACTIVE && toMode == ObjectSimulationMode.PHYSICS_ACTIVE) {
            promoteToPhysics(objectId, event.simulationTimeSeconds(), zone);
            return;
        }
        if (fromMode == ObjectSimulationMode.PHYSICS_ACTIVE && toMode != ObjectSimulationMode.PHYSICS_ACTIVE) {
            demoteToOrbital(objectId, event.simulationTimeSeconds(), zone);
        }
    }

    private void promoteToPhysics(String objectId, double simulationTimeSeconds, PhysicsZone zone) {
        Optional<OrbitalState> orbitalStateOptional = orbitalStateSource.apply(objectId);
        if (orbitalStateOptional.isEmpty()) {
            return;
        }
        OrbitalState orbitalState = orbitalStateOptional.get();
        PhysicsBodyState seeded = new PhysicsBodyState(
                subtract(orbitalState.position(), zone.anchorPosition()),
                orbitalState.orientation(),
                orbitalState.linearVelocity(),
                PhysicsVector3.ZERO,
                zone.anchorFrame(),
                simulationTimeSeconds);
        diagnosticsSink.accept(new StateHandoffSnapshot(
                StateHandoffDirection.PROMOTE_TO_PHYSICS,
                simulationTimeSeconds,
                objectId,
                zone.zoneId(),
                zone.anchorPosition(),
                orbitalState.position(),
                orbitalState.linearVelocity(),
                seeded.position(),
                seeded.linearVelocity()));
        rigidStateSink.accept(objectId, seeded);
        orbitalStateClearer.accept(objectId);
    }

    private void demoteToOrbital(String objectId, double simulationTimeSeconds, PhysicsZone zone) {
        Optional<PhysicsBodyState> rigidStateOptional = rigidStateSource.apply(objectId);
        if (rigidStateOptional.isEmpty()) {
            return;
        }
        PhysicsBodyState rigidState = rigidStateOptional.get();
        OrbitalState seeded = new OrbitalState(
                add(rigidState.position(), zone.anchorPosition()),
                rigidState.linearVelocity(),
                rigidState.orientation(),
                zone.anchorFrame(),
                simulationTimeSeconds);
        diagnosticsSink.accept(new StateHandoffSnapshot(
                StateHandoffDirection.DEMOTE_TO_ORBITAL,
                simulationTimeSeconds,
                objectId,
                zone.zoneId(),
                zone.anchorPosition(),
                seeded.position(),
                seeded.linearVelocity(),
                rigidState.position(),
                rigidState.linearVelocity()));
        orbitalStateSink.accept(objectId, seeded);
        rigidStateClearer.accept(objectId);
    }

    private static PhysicsVector3 add(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    private static PhysicsVector3 subtract(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }
}
