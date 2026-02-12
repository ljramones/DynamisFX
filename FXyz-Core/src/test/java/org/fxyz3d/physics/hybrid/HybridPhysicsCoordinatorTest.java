package org.fxyz3d.physics.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.api.PhysicsConstraintDefinition;
import org.fxyz3d.physics.api.PhysicsConstraintHandle;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.BoxShape;
import org.fxyz3d.physics.model.PhysicsBodyDefinition;
import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsBodyType;
import org.fxyz3d.physics.model.PhysicsQuaternion;
import org.fxyz3d.physics.model.PhysicsRuntimeTuning;
import org.fxyz3d.physics.model.PhysicsVector3;
import org.fxyz3d.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class HybridPhysicsCoordinatorTest {

    @Test
    void validatesLinkBodiesExistInTheirWorlds() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));

        assertThrows(IllegalArgumentException.class, () -> coordinator.registerLink(new HybridBodyLink(
                generalBody,
                new PhysicsBodyHandle(999),
                HybridOwnership.ORBITAL,
                StateHandoffMode.FULL_STATE)));
    }

    @Test
    void appliesOrbitalOwnershipWithFullStateHandoff() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        new PhysicsVector3(0, 0, 0),
                        new PhysicsQuaternion(0, 0, 0, 1),
                        new PhysicsVector3(0, 0, 0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        new PhysicsVector3(10, 0, 0),
                        new PhysicsQuaternion(0, 0.5, 0, 0.866),
                        new PhysicsVector3(2, 0, 0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));

        coordinator.registerLink(new HybridBodyLink(
                generalBody, orbitalBody, HybridOwnership.ORBITAL, StateHandoffMode.FULL_STATE));

        coordinator.step(1.0);

        PhysicsBodyState generalState = general.getBodyState(generalBody);
        assertEquals(12.0, generalState.position().x(), 1e-9);
        assertEquals(2.0, generalState.linearVelocity().x(), 1e-9);
        assertEquals(0.5, generalState.orientation().y(), 1e-9);
        assertEquals(1.0, generalState.timestampSeconds(), 1e-9);
    }

    @Test
    void appliesPositionVelocityOnlyWithoutOverwritingFollowerOrientation() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        new PhysicsVector3(1, 0, 0),
                        new PhysicsQuaternion(0.1, 0.2, 0.3, 0.9),
                        new PhysicsVector3(3, 0, 0),
                        new PhysicsVector3(1, 2, 3),
                        ReferenceFrame.WORLD,
                        0.0)));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        new PhysicsVector3(0, 0, 0),
                        new PhysicsQuaternion(0, 0, 0, 1),
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));

        coordinator.registerLink(new HybridBodyLink(
                generalBody, orbitalBody, HybridOwnership.GENERAL, StateHandoffMode.POSITION_VELOCITY_ONLY));
        coordinator.step(1.0);

        PhysicsBodyState orbitalState = orbital.getBodyState(orbitalBody);
        assertEquals(4.0, orbitalState.position().x(), 1e-9);
        assertEquals(3.0, orbitalState.linearVelocity().x(), 1e-9);
        assertEquals(0.0, orbitalState.orientation().x(), 1e-9);
        assertEquals(1.0, orbitalState.orientation().w(), 1e-9);
    }

    @Test
    void publishesImmutableSnapshotPerStep() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        coordinator.registerLink(new HybridBodyLink(
                generalBody, orbitalBody, HybridOwnership.ORBITAL, StateHandoffMode.FULL_STATE));

        HybridSnapshot snapshot = coordinator.step(0.5);
        assertNotNull(snapshot);
        assertEquals(0.5, snapshot.simulationTimeSeconds(), 1e-9);
        assertEquals(0.0, snapshot.interpolationAlpha(), 1e-9);
        assertEquals(0.5, snapshot.extrapolationSeconds(), 1e-9);
        assertNotNull(coordinator.latestSnapshot());
        assertThrows(UnsupportedOperationException.class, () ->
                snapshot.generalStates().put(generalBody, PhysicsBodyState.IDENTITY));
    }

    @Test
    void rejectsDivergentHandoffWhenPolicyRequiresIt() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        new PhysicsVector3(1000.0, 0.0, 0.0),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));

        coordinator.registerLink(new HybridBodyLink(
                generalBody,
                orbitalBody,
                HybridOwnership.ORBITAL,
                StateHandoffMode.FULL_STATE,
                ConflictPolicy.REJECT_ON_DIVERGENCE,
                10.0));
        coordinator.step(0.1);

        PhysicsBodyState currentGeneral = general.getBodyState(generalBody);
        assertEquals(0.0, currentGeneral.position().x(), 1e-9);
        assertEquals(1, coordinator.lastRejectedHandoffs());
    }

    @Test
    void rejectsOnVelocityAndAngularDivergenceWhenConfigured() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(50.0, 0.0, 0.0),
                        new PhysicsVector3(0.0, 10.0, 0.0),
                        ReferenceFrame.WORLD,
                        0.0)));

        coordinator.registerLink(new HybridBodyLink(
                generalBody,
                orbitalBody,
                HybridOwnership.ORBITAL,
                StateHandoffMode.FULL_STATE,
                ConflictPolicy.REJECT_ON_DIVERGENCE,
                Double.POSITIVE_INFINITY,
                1.0,
                1.0));
        coordinator.step(0.1);

        PhysicsBodyState currentGeneral = general.getBodyState(generalBody);
        assertEquals(0.0, currentGeneral.linearVelocity().x(), 1e-9);
        assertEquals(1, coordinator.lastRejectedHandoffs());
    }

    @Test
    void supportsLinkLifecycleAndDiagnostics() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));

        long linkId = coordinator.registerLink(new HybridBodyLink(
                generalBody, orbitalBody, HybridOwnership.ORBITAL, StateHandoffMode.FULL_STATE));

        assertTrue(coordinator.isLinkEnabled(linkId));
        assertTrue(coordinator.setLinkEnabled(linkId, false));
        assertFalse(coordinator.isLinkEnabled(linkId));
        coordinator.step(0.1);
        assertEquals(0, coordinator.lastRejectedHandoffs());

        assertTrue(coordinator.setLinkEnabled(linkId, true));
        assertTrue(coordinator.updateLink(linkId, new HybridBodyLink(
                generalBody,
                orbitalBody,
                HybridOwnership.ORBITAL,
                StateHandoffMode.FULL_STATE,
                ConflictPolicy.REJECT_ON_DIVERGENCE,
                0.0)));
        general.setBodyState(generalBody, new PhysicsBodyState(
                new PhysicsVector3(5.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                0.0));
        coordinator.step(0.1);
        assertEquals(1, coordinator.lastRejectedHandoffs());

        HybridLinkDiagnostics diag = coordinator.linkDiagnostics().iterator().next();
        assertEquals(linkId, diag.linkId());
        assertTrue(diag.enabled());
        assertTrue(diag.rejectedCount() >= 1);

        assertEquals(1, coordinator.removeLinksForBody(generalBody));
        assertEquals(0, coordinator.links().size());
    }

    @Test
    void updatesRenderMetadataFromAccumulatorOutput() {
        FakeWorld general = new FakeWorld();
        FakeWorld orbital = new FakeWorld();
        HybridPhysicsCoordinator coordinator = new HybridPhysicsCoordinator(general, orbital);

        PhysicsBodyHandle generalBody = general.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        PhysicsBodyHandle orbitalBody = orbital.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 1.0, new BoxShape(1, 1, 1), PhysicsBodyState.IDENTITY));
        coordinator.registerLink(new HybridBodyLink(
                generalBody, orbitalBody, HybridOwnership.ORBITAL, StateHandoffMode.FULL_STATE));

        coordinator.step(0.2);
        HybridSnapshot updated = coordinator.updateRenderMetadata(0.4, 0.03);
        assertNotNull(updated);
        assertEquals(0.4, updated.interpolationAlpha(), 1e-9);
        assertEquals(0.03, updated.extrapolationSeconds(), 1e-9);
    }

    private static final class FakeWorld implements PhysicsWorld {

        private final Map<PhysicsBodyHandle, PhysicsBodyState> states = new LinkedHashMap<>();
        private long nextHandle = 1L;
        private PhysicsRuntimeTuning tuning = new PhysicsRuntimeTuning(20, 1.0, 0.0, 1e-5, 0.1);

        @Override
        public org.fxyz3d.physics.api.PhysicsCapabilities capabilities() {
            return org.fxyz3d.physics.api.PhysicsCapabilities.EMPTY;
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
            throw new UnsupportedOperationException("not used in test");
        }

        @Override
        public boolean removeConstraint(PhysicsConstraintHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsConstraintHandle> constraints() {
            return java.util.List.of();
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
            for (Map.Entry<PhysicsBodyHandle, PhysicsBodyState> entry : states.entrySet()) {
                PhysicsBodyState state = entry.getValue();
                PhysicsVector3 p = state.position();
                PhysicsVector3 v = state.linearVelocity();
                PhysicsBodyState next = new PhysicsBodyState(
                        new PhysicsVector3(
                                p.x() + v.x() * dtSeconds,
                                p.y() + v.y() * dtSeconds,
                                p.z() + v.z() * dtSeconds),
                        state.orientation(),
                        v,
                        state.angularVelocity(),
                        state.referenceFrame(),
                        state.timestampSeconds() + dtSeconds);
                entry.setValue(next);
            }
        }
    }
}
