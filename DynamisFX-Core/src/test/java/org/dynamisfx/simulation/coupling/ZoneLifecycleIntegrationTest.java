package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class ZoneLifecycleIntegrationTest {

    @Test
    void roundTripsOrbitalToPhysicsAndBackThroughTransitionStack() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        MutableCouplingObservationProvider observations = new MutableCouplingObservationProvider();
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(observations, 100.0, 200.0, 0.0);
        DefaultCouplingManager manager = new DefaultCouplingManager(policy, observations);
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        FakeRigidWorld world = new FakeRigidWorld();
        PhysicsZone zone = new StubZone("zone-a", world, new PhysicsVector3(100.0, 0.0, 0.0));
        manager.registerZone(zone);
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);
        CouplingTransitionApplier applier = new CouplingTransitionApplier(
                buffers,
                registry,
                CouplingBodyDefinitionProvider.dynamicSphere(1.0, 1.0));
        CouplingStateReconciler reconciler = new CouplingStateReconciler(
                buffers.orbital()::get,
                buffers.rigid()::get,
                buffers.rigid()::put,
                buffers.orbital()::put,
                buffers.rigid()::remove,
                buffers.orbital()::remove,
                (objectId, zones) -> DeterministicZoneSelector.select(zones, null, null));
        manager.addTransitionListener(applier);
        manager.addTransitionListener(reconciler);

        buffers.orbital().put("lander-1", new OrbitalState(
                new PhysicsVector3(130.0, 0.0, 0.0),
                new PhysicsVector3(3.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0));
        observations.setDistanceMeters("lander-1", 50.0);
        observations.setActiveContact("lander-1", false);
        manager.update(1.0);

        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
        ZoneBodyRegistry.ZoneBodyBinding binding = registry.bindingForObject("lander-1").orElseThrow();
        PhysicsBodyState promotedState = world.getBodyState(binding.bodyHandle());
        assertEquals(30.0, promotedState.position().x(), 1e-9);

        world.setBodyState(binding.bodyHandle(), new PhysicsBodyState(
                new PhysicsVector3(40.0, 1.0, -2.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(2.0, 0.0, 0.0),
                new PhysicsVector3(0.1, 0.0, 0.0),
                ReferenceFrame.WORLD,
                2.0));
        observations.setDistanceMeters("lander-1", 250.0);
        observations.setActiveContact("lander-1", false);
        manager.update(2.0);

        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());
        assertTrue(registry.bindingForObject("lander-1").isEmpty());
        assertTrue(world.bodies().isEmpty());
        OrbitalState demoted = buffers.orbital().get("lander-1").orElseThrow();
        assertEquals(140.0, demoted.position().x(), 1e-9);
        assertEquals(1.0, demoted.position().y(), 1e-9);
        assertEquals(-2.0, demoted.position().z(), 1e-9);
        assertEquals(2.0, demoted.linearVelocity().x(), 1e-9);
        assertEquals(0.1, demoted.angularVelocity().x(), 1e-9);
        assertTrue(buffers.rigid().get("lander-1").isEmpty());
    }

    @Test
    void blocksDemotionWhileContactActiveThenDemotesWhenCleared() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        MutableCouplingObservationProvider observations = new MutableCouplingObservationProvider();
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(observations, 100.0, 200.0, 0.0);
        DefaultCouplingManager manager = new DefaultCouplingManager(policy, observations);
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        FakeRigidWorld world = new FakeRigidWorld();
        manager.registerZone(new StubZone("zone-a", world, new PhysicsVector3(100.0, 0.0, 0.0)));
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);
        manager.addTransitionListener(new CouplingTransitionApplier(
                buffers, registry, CouplingBodyDefinitionProvider.dynamicSphere(1.0, 1.0)));
        manager.addTransitionListener(new CouplingStateReconciler(
                buffers.orbital()::get,
                buffers.rigid()::get,
                buffers.rigid()::put,
                buffers.orbital()::put,
                buffers.rigid()::remove,
                buffers.orbital()::remove,
                (objectId, zones) -> DeterministicZoneSelector.select(zones, null, null)));
        List<CouplingTelemetryEvent> telemetry = new ArrayList<>();
        manager.addTelemetryListener(telemetry::add);

        buffers.orbital().put("lander-1", new OrbitalState(
                new PhysicsVector3(120.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0));
        observations.setDistanceMeters("lander-1", 20.0);
        observations.setActiveContact("lander-1", false);
        manager.update(1.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        observations.setDistanceMeters("lander-1", 300.0);
        observations.setActiveContact("lander-1", true);
        manager.update(2.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
        assertTrue(registry.bindingForObject("lander-1").isPresent());
        assertEquals(CouplingDecisionReason.BLOCKED_BY_CONTACT, telemetry.get(1).reason());

        observations.setActiveContact("lander-1", false);
        manager.update(3.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());
        assertTrue(registry.bindingForObject("lander-1").isEmpty());
        assertEquals(CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD, telemetry.get(2).reason());
    }

    @Test
    void appliesDistanceHysteresisForReentryWithoutThrashing() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        MutableCouplingObservationProvider observations = new MutableCouplingObservationProvider();
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(observations, 100.0, 200.0, 0.0);
        DefaultCouplingManager manager = new DefaultCouplingManager(policy, observations);
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        FakeRigidWorld world = new FakeRigidWorld();
        manager.registerZone(new StubZone("zone-a", world, new PhysicsVector3(100.0, 0.0, 0.0)));
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);
        manager.addTransitionListener(new CouplingTransitionApplier(
                buffers, registry, CouplingBodyDefinitionProvider.dynamicSphere(1.0, 1.0)));
        manager.addTransitionListener(new CouplingStateReconciler(
                buffers.orbital()::get,
                buffers.rigid()::get,
                buffers.rigid()::put,
                buffers.orbital()::put,
                buffers.rigid()::remove,
                buffers.orbital()::remove,
                (objectId, zones) -> DeterministicZoneSelector.select(zones, null, null)));
        List<CouplingModeTransitionEvent> transitions = new ArrayList<>();
        manager.addTransitionListener(transitions::add);

        buffers.orbital().put("lander-1", new OrbitalState(
                new PhysicsVector3(150.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0));

        observations.setDistanceMeters("lander-1", 90.0);
        observations.setActiveContact("lander-1", false);
        manager.update(1.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        observations.setDistanceMeters("lander-1", 150.0);
        manager.update(2.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        observations.setDistanceMeters("lander-1", 210.0);
        manager.update(3.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());

        observations.setDistanceMeters("lander-1", 150.0);
        manager.update(4.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());

        observations.setDistanceMeters("lander-1", 95.0);
        manager.update(5.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        assertEquals(3, transitions.size());
        assertEquals(CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD, transitions.get(0).reason());
        assertEquals(CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD, transitions.get(1).reason());
        assertEquals(CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD, transitions.get(2).reason());
    }

    private static final class FakeRigidWorld implements RigidBodyWorld {
        private final Map<PhysicsBodyHandle, PhysicsBodyState> states = new LinkedHashMap<>();
        private long nextHandle = 1L;
        private PhysicsRuntimeTuning tuning = new PhysicsRuntimeTuning(10, 1.0, 0.0, 1e-5, 0.1);

        @Override
        public PhysicsCapabilities capabilities() {
            return new PhysicsCapabilities(true, false, false, false, false);
        }

        @Override
        public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
            PhysicsBodyHandle handle = new PhysicsBodyHandle(nextHandle++);
            states.put(handle, definition.initialState());
            return handle;
        }

        @Override
        public boolean removeBody(PhysicsBodyHandle handle) {
            return states.remove(handle) != null;
        }

        @Override
        public Collection<PhysicsBodyHandle> bodies() {
            return new ArrayList<>(states.keySet());
        }

        @Override
        public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
            return states.get(handle);
        }

        @Override
        public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
            states.put(handle, state);
        }

        @Override
        public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean removeConstraint(PhysicsConstraintHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsConstraintHandle> constraints() {
            return List.of();
        }

        @Override
        public PhysicsRuntimeTuning runtimeTuning() {
            return tuning;
        }

        @Override
        public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
            this.tuning = tuning;
        }

        @Override
        public void step(double dtSeconds) {
        }
    }

    private record StubZone(String id, RigidBodyWorld world, PhysicsVector3 anchorPosition) implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId(id);
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public double radiusMeters() {
            return 1_000.0;
        }
    }
}
