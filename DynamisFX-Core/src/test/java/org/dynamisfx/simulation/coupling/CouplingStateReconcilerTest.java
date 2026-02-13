package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class CouplingStateReconcilerTest {

    @Test
    void seedsRigidStateWhenPromotingToPhysics() {
        Map<String, OrbitalState> orbitalStates = new LinkedHashMap<>();
        Map<String, PhysicsBodyState> rigidStates = new LinkedHashMap<>();
        CouplingStateReconciler reconciler = new CouplingStateReconciler(
                objectId -> Optional.ofNullable(orbitalStates.get(objectId)),
                objectId -> Optional.ofNullable(rigidStates.get(objectId)),
                rigidStates::put,
                (objectId, state) -> orbitalStates.put(objectId, state),
                rigidStates::remove,
                orbitalStates::remove,
                (objectId, zones) -> Optional.of(zones.get(0)));
        orbitalStates.put("lander-1", new OrbitalState(
                new PhysicsVector3(110.0, 2.0, 3.0),
                new PhysicsVector3(4.0, 5.0, 6.0),
                new PhysicsVector3(0.5, 0.6, 0.7),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                1.0));

        reconciler.onTransition(new CouplingModeTransitionEvent(
                2.0,
                "lander-1",
                ObjectSimulationMode.ORBITAL_ONLY,
                ObjectSimulationMode.PHYSICS_ACTIVE,
                CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD,
                List.of(new StubZone(new PhysicsVector3(100.0, 0.0, 0.0)))));

        PhysicsBodyState seeded = rigidStates.get("lander-1");
        assertEquals(10.0, seeded.position().x(), 1e-9);
        assertEquals(2.0, seeded.position().y(), 1e-9);
        assertEquals(3.0, seeded.position().z(), 1e-9);
        assertEquals(4.0, seeded.linearVelocity().x(), 1e-9);
        assertEquals(0.5, seeded.angularVelocity().x(), 1e-9);
        assertTrue(orbitalStates.isEmpty());
    }

    @Test
    void seedsOrbitalStateWhenDemotingFromPhysics() {
        Map<String, OrbitalState> orbitalStates = new LinkedHashMap<>();
        Map<String, PhysicsBodyState> rigidStates = new LinkedHashMap<>();
        CouplingStateReconciler reconciler = new CouplingStateReconciler(
                objectId -> Optional.ofNullable(orbitalStates.get(objectId)),
                objectId -> Optional.ofNullable(rigidStates.get(objectId)),
                rigidStates::put,
                orbitalStates::put,
                rigidStates::remove,
                orbitalStates::remove,
                (objectId, zones) -> Optional.of(zones.get(0)));
        rigidStates.put("lander-1", new PhysicsBodyState(
                new PhysicsVector3(15.0, 1.0, -2.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(7.0, 8.0, 9.0),
                new PhysicsVector3(0.1, 0.2, 0.3),
                ReferenceFrame.WORLD,
                3.0));

        reconciler.onTransition(new CouplingModeTransitionEvent(
                4.0,
                "lander-1",
                ObjectSimulationMode.PHYSICS_ACTIVE,
                ObjectSimulationMode.ORBITAL_ONLY,
                CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD,
                List.of(new StubZone(new PhysicsVector3(100.0, 0.0, 0.0)))));

        OrbitalState seeded = orbitalStates.get("lander-1");
        assertEquals(115.0, seeded.position().x(), 1e-9);
        assertEquals(1.0, seeded.position().y(), 1e-9);
        assertEquals(-2.0, seeded.position().z(), 1e-9);
        assertEquals(7.0, seeded.linearVelocity().x(), 1e-9);
        assertEquals(0.1, seeded.angularVelocity().x(), 1e-9);
        assertTrue(rigidStates.isEmpty());
    }

    @Test
    void emitsDiagnosticsSnapshotsForPromoteAndDemote() {
        Map<String, OrbitalState> orbitalStates = new LinkedHashMap<>();
        Map<String, PhysicsBodyState> rigidStates = new LinkedHashMap<>();
        List<StateHandoffSnapshot> snapshots = new CopyOnWriteArrayList<>();
        CouplingStateReconciler reconciler = new CouplingStateReconciler(
                objectId -> Optional.ofNullable(orbitalStates.get(objectId)),
                objectId -> Optional.ofNullable(rigidStates.get(objectId)),
                rigidStates::put,
                orbitalStates::put,
                rigidStates::remove,
                orbitalStates::remove,
                (objectId, zones) -> Optional.of(zones.get(0)),
                snapshots::add);
        PhysicsZone zone = new StubZone(new PhysicsVector3(100.0, 0.0, 0.0));
        orbitalStates.put("lander-1", new OrbitalState(
                new PhysicsVector3(110.0, 0.0, 0.0),
                new PhysicsVector3(1.0, 0.0, 0.0),
                new PhysicsVector3(0.5, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0));

        reconciler.onTransition(new CouplingModeTransitionEvent(
                1.0,
                "lander-1",
                ObjectSimulationMode.ORBITAL_ONLY,
                ObjectSimulationMode.PHYSICS_ACTIVE,
                CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));
        rigidStates.put("lander-1", new PhysicsBodyState(
                new PhysicsVector3(5.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                new PhysicsVector3(0.25, 0.0, 0.0),
                ReferenceFrame.WORLD,
                1.0));
        reconciler.onTransition(new CouplingModeTransitionEvent(
                2.0,
                "lander-1",
                ObjectSimulationMode.PHYSICS_ACTIVE,
                ObjectSimulationMode.ORBITAL_ONLY,
                CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));

        assertEquals(2, snapshots.size());
        assertEquals(StateHandoffDirection.PROMOTE_TO_PHYSICS, snapshots.get(0).direction());
        assertEquals(StateHandoffDirection.DEMOTE_TO_ORBITAL, snapshots.get(1).direction());
        assertEquals(10.0, snapshots.get(0).localPosition().x(), 1e-9);
        assertEquals(0.5, snapshots.get(0).globalAngularVelocity().x(), 1e-9);
        assertEquals(105.0, snapshots.get(1).globalPosition().x(), 1e-9);
        assertEquals(0.25, snapshots.get(1).localAngularVelocity().x(), 1e-9);
    }

    private record StubZone(PhysicsVector3 anchorPosition) implements PhysicsZone {

        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public double radiusMeters() {
            return 1_000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
