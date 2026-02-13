package org.dynamisfx.simulation.coupling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Applies coupling transitions to live zone physics worlds by creating/removing bodies.
 */
public final class CouplingTransitionApplier implements CouplingTransitionListener {

    private final SimulationStateBuffers stateBuffers;
    private final ZoneBodyRegistry registry;
    private final CouplingBodyDefinitionProvider bodyDefinitionProvider;
    private final BiFunction<CouplingModeTransitionEvent, ZoneBodyRegistry, Optional<PhysicsZone>> zoneSelector;

    public CouplingTransitionApplier(
            SimulationStateBuffers stateBuffers,
            ZoneBodyRegistry registry,
            CouplingBodyDefinitionProvider bodyDefinitionProvider) {
        this(
                stateBuffers,
                registry,
                bodyDefinitionProvider,
                (event, bodyRegistry) -> selectBoundOrDeterministicZone(event, bodyRegistry, stateBuffers));
    }

    public CouplingTransitionApplier(
            SimulationStateBuffers stateBuffers,
            ZoneBodyRegistry registry,
            CouplingBodyDefinitionProvider bodyDefinitionProvider,
            BiFunction<CouplingModeTransitionEvent, ZoneBodyRegistry, Optional<PhysicsZone>> zoneSelector) {
        this.stateBuffers = Objects.requireNonNull(stateBuffers, "stateBuffers must not be null");
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.bodyDefinitionProvider = Objects.requireNonNull(bodyDefinitionProvider, "bodyDefinitionProvider must not be null");
        this.zoneSelector = Objects.requireNonNull(zoneSelector, "zoneSelector must not be null");
    }

    @Override
    public void onTransition(CouplingModeTransitionEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        if (event.toMode() == ObjectSimulationMode.PHYSICS_ACTIVE
                && event.fromMode() != ObjectSimulationMode.PHYSICS_ACTIVE) {
            applyPromote(event);
            return;
        }
        if (event.fromMode() == ObjectSimulationMode.PHYSICS_ACTIVE
                && event.toMode() != ObjectSimulationMode.PHYSICS_ACTIVE) {
            applyDemote(event);
        }
    }

    private void applyPromote(CouplingModeTransitionEvent event) {
        Optional<PhysicsZone> zoneOptional = zoneSelector.apply(event, registry);
        if (zoneOptional.isEmpty()) {
            return;
        }
        PhysicsZone zone = zoneOptional.get();
        if (zone.world() == null) {
            return;
        }
        PhysicsBodyState seedState = seededLocalState(event.objectId(), event.simulationTimeSeconds(), zone);
        if (seedState == null) {
            return;
        }
        stateBuffers.rigid().put(event.objectId(), seedState);

        ZoneBodyRegistry.ZoneBodyBinding existing = registry.bindingForObject(event.objectId()).orElse(null);
        if (existing != null && !existing.zoneId().equals(zone.zoneId())) {
            PhysicsZone boundZone = findZoneById(existing.zoneId(), event.zones()).orElse(null);
            if (boundZone != null && boundZone.world() != null) {
                boundZone.world().removeBody(existing.bodyHandle());
            }
            registry.unbind(event.objectId());
            existing = null;
        }

        PhysicsBodyHandle handle;
        if (existing != null) {
            handle = existing.bodyHandle();
            zone.world().setBodyState(handle, seedState);
        } else {
            PhysicsBodyDefinition definition = bodyDefinitionProvider.createBodyDefinition(
                    event.objectId(),
                    event,
                    zone,
                    seedState);
            handle = zone.world().createBody(definition);
        }
        registry.bind(event.objectId(), zone.zoneId(), handle);
    }

    private void applyDemote(CouplingModeTransitionEvent event) {
        ZoneBodyRegistry.ZoneBodyBinding binding = registry.bindingForObject(event.objectId()).orElse(null);
        if (binding == null) {
            return;
        }
        PhysicsZone zone = findZoneById(binding.zoneId(), event.zones()).orElse(null);
        if (zone == null || zone.world() == null) {
            registry.unbind(event.objectId());
            return;
        }
        PhysicsBodyState live = zone.world().getBodyState(binding.bodyHandle());
        stateBuffers.rigid().put(event.objectId(), live);
        zone.world().removeBody(binding.bodyHandle());
        registry.unbind(event.objectId());
    }

    private PhysicsBodyState seededLocalState(String objectId, double simulationTimeSeconds, PhysicsZone zone) {
        Optional<PhysicsBodyState> rigidState = stateBuffers.rigid().get(objectId);
        if (rigidState.isPresent()) {
            return rigidState.get();
        }
        Optional<OrbitalState> orbitalState = stateBuffers.orbital().get(objectId);
        if (orbitalState.isEmpty()) {
            return null;
        }
        OrbitalState state = orbitalState.get();
        return ZoneFrameTransform.orbitalToLocalRigid(state, simulationTimeSeconds, zone);
    }

    private static Optional<PhysicsZone> selectBoundOrDeterministicZone(
            CouplingModeTransitionEvent event,
            ZoneBodyRegistry registry,
            SimulationStateBuffers stateBuffers) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(stateBuffers, "stateBuffers must not be null");
        ZoneId boundZoneId = registry.bindingForObject(event.objectId())
                .map(ZoneBodyRegistry.ZoneBodyBinding::zoneId)
                .orElse(null);
        if (boundZoneId != null) {
            Optional<PhysicsZone> bound = findZoneById(boundZoneId, event.zones());
            if (bound.isPresent()) {
                return bound;
            }
        }
        PhysicsVector3 positionHint = stateBuffers.orbital().get(event.objectId())
                .map(OrbitalState::position)
                .orElse(null);
        return DeterministicZoneSelector.select(event.zones(), null, positionHint);
    }

    private static Optional<PhysicsZone> findZoneById(ZoneId zoneId, List<PhysicsZone> zones) {
        if (zoneId == null || zones == null) {
            return Optional.empty();
        }
        for (PhysicsZone zone : zones) {
            if (zoneId.equals(zone.zoneId())) {
                return Optional.of(zone);
            }
        }
        return Optional.empty();
    }
}
