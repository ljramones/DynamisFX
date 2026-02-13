package org.dynamisfx.physics.ode4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.coupling.CouplingBodyDefinitionProvider;
import org.dynamisfx.simulation.coupling.CouplingDecisionReason;
import org.dynamisfx.simulation.coupling.CouplingStateReconciler;
import org.dynamisfx.simulation.coupling.CouplingTransitionApplier;
import org.dynamisfx.simulation.coupling.DefaultCouplingManager;
import org.dynamisfx.simulation.coupling.MutableCouplingObservationProvider;
import org.dynamisfx.simulation.coupling.PhysicsZone;
import org.dynamisfx.simulation.coupling.ThresholdTransitionPolicy;
import org.dynamisfx.simulation.coupling.ZoneBodyRegistry;
import org.dynamisfx.simulation.coupling.ZoneId;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class Ode4jCouplingLifecycleIntegrationTest {

    @Test
    void runsPromoteAndDemoteAgainstLiveOde4jWorld() {
        MutableCouplingObservationProvider observation = new MutableCouplingObservationProvider();
        DefaultCouplingManager manager = new DefaultCouplingManager(new ThresholdTransitionPolicy(
                observation,
                50.0,
                100.0,
                0.0));
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
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
                (objectId, zones) -> zones.stream().findFirst());

        Ode4jRigidBodyWorldAdapter world = new Ode4jRigidBodyWorldAdapter(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0.0, -9.81, 0.0),
                1.0 / 60.0));
        try {
            PhysicsZone zone = new StubZone("zone-a", new PhysicsVector3(100.0, 0.0, 0.0), world);
            manager.registerZone(zone);
            manager.addTransitionListener(applier);
            manager.addTransitionListener(reconciler);
            manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);
            buffers.orbital().put("lander-1", new OrbitalState(
                    new PhysicsVector3(130.0, 5.0, 0.0),
                    new PhysicsVector3(2.0, 0.0, 0.0),
                    PhysicsQuaternion.IDENTITY,
                    ReferenceFrame.WORLD,
                    0.0));

            observation.setDistanceMeters("lander-1", 20.0);
            manager.update(1.0);

            assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
            ZoneBodyRegistry.ZoneBodyBinding binding = registry.bindingForObject("lander-1").orElseThrow();
            PhysicsBodyState localState = world.getBodyState(binding.bodyHandle());
            assertEquals(30.0, localState.position().x(), 1e-6);

            world.setBodyState(binding.bodyHandle(), new PhysicsBodyState(
                    new PhysicsVector3(15.0, 1.0, 0.0),
                    PhysicsQuaternion.IDENTITY,
                    new PhysicsVector3(5.0, 0.0, 0.0),
                    PhysicsVector3.ZERO,
                    ReferenceFrame.WORLD,
                    2.0));

            observation.setDistanceMeters("lander-1", 150.0);
            manager.update(2.0);

            assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());
            assertTrue(registry.bindingForObject("lander-1").isEmpty());
            Optional<OrbitalState> demoted = buffers.orbital().get("lander-1");
            assertTrue(demoted.isPresent());
            assertEquals(115.0, demoted.orElseThrow().position().x(), 1e-6);
            assertEquals(5.0, demoted.orElseThrow().linearVelocity().x(), 1e-6);
        } finally {
            world.close();
        }
    }

    private record StubZone(String id, PhysicsVector3 anchorPosition, RigidBodyWorld world) implements PhysicsZone {
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
            return 1000.0;
        }
    }
}
