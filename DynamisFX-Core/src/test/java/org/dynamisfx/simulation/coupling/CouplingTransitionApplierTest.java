package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class CouplingTransitionApplierTest {

    @Test
    void createsBodyOnPromoteAndRemovesOnDemoteWithStateCapture() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        CouplingTransitionApplier applier = new CouplingTransitionApplier(
                buffers,
                registry,
                CouplingBodyDefinitionProvider.dynamicSphere(1.0, 1.0));
        FakeRigidWorld world = new FakeRigidWorld();
        PhysicsZone zone = new StubZone("zone-a", world, new PhysicsVector3(100.0, 0.0, 0.0));

        buffers.orbital().put("lander-1", new OrbitalState(
                new PhysicsVector3(130.0, 0.0, 0.0),
                new PhysicsVector3(3.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0));

        applier.onTransition(new CouplingModeTransitionEvent(
                1.0,
                "lander-1",
                ObjectSimulationMode.ORBITAL_ONLY,
                ObjectSimulationMode.PHYSICS_ACTIVE,
                CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));

        ZoneBodyRegistry.ZoneBodyBinding binding = registry.bindingForObject("lander-1").orElseThrow();
        PhysicsBodyState promoted = world.getBodyState(binding.bodyHandle());
        assertEquals(30.0, promoted.position().x(), 1e-9);
        assertEquals(3.0, promoted.linearVelocity().x(), 1e-9);

        world.setBodyState(binding.bodyHandle(), new PhysicsBodyState(
                new PhysicsVector3(42.0, 1.0, -2.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(7.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                2.0));

        applier.onTransition(new CouplingModeTransitionEvent(
                2.0,
                "lander-1",
                ObjectSimulationMode.PHYSICS_ACTIVE,
                ObjectSimulationMode.ORBITAL_ONLY,
                CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));

        assertTrue(registry.bindingForObject("lander-1").isEmpty());
        assertTrue(world.bodies().isEmpty());
        PhysicsBodyState captured = buffers.rigid().get("lander-1").orElseThrow();
        assertEquals(42.0, captured.position().x(), 1e-9);
        assertEquals(7.0, captured.linearVelocity().x(), 1e-9);
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
