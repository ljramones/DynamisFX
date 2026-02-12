package org.fxyz3d.physics.hybrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.PhysicsBodyState;

/**
 * Coordinates two physics worlds under one deterministic timeline.
 */
public final class HybridPhysicsCoordinator {

    private final PhysicsWorld generalWorld;
    private final PhysicsWorld orbitalWorld;
    private final AtomicLong nextLinkId = new AtomicLong(1L);
    private final Map<Long, HybridBodyLink> links = new LinkedHashMap<>();
    private final AtomicReference<HybridSnapshot> latestSnapshot = new AtomicReference<>();
    private double simulationTimeSeconds;

    public HybridPhysicsCoordinator(PhysicsWorld generalWorld, PhysicsWorld orbitalWorld) {
        this.generalWorld = Objects.requireNonNull(generalWorld, "generalWorld must not be null");
        this.orbitalWorld = Objects.requireNonNull(orbitalWorld, "orbitalWorld must not be null");
    }

    public long registerLink(HybridBodyLink link) {
        Objects.requireNonNull(link, "link must not be null");
        if (!generalWorld.bodies().contains(link.generalBody())) {
            throw new IllegalArgumentException("generalBody is not present in generalWorld: " + link.generalBody());
        }
        if (!orbitalWorld.bodies().contains(link.orbitalBody())) {
            throw new IllegalArgumentException("orbitalBody is not present in orbitalWorld: " + link.orbitalBody());
        }
        long id = nextLinkId.getAndIncrement();
        links.put(id, link);
        return id;
    }

    public boolean removeLink(long linkId) {
        return links.remove(linkId) != null;
    }

    public Collection<HybridBodyLink> links() {
        return new ArrayList<>(links.values());
    }

    public HybridSnapshot latestSnapshot() {
        return latestSnapshot.get();
    }

    public double simulationTimeSeconds() {
        return simulationTimeSeconds;
    }

    public HybridSnapshot step(double dtSeconds) {
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }

        orbitalWorld.step(dtSeconds);
        generalWorld.step(dtSeconds);

        for (HybridBodyLink link : links.values()) {
            applyHandoff(link);
        }

        simulationTimeSeconds += dtSeconds;
        HybridSnapshot snapshot = captureSnapshot();
        latestSnapshot.set(snapshot);
        return snapshot;
    }

    private void applyHandoff(HybridBodyLink link) {
        PhysicsBodyState ownerState;
        PhysicsBodyState followerState;
        if (link.ownership() == HybridOwnership.GENERAL) {
            ownerState = generalWorld.getBodyState(link.generalBody());
            followerState = orbitalWorld.getBodyState(link.orbitalBody());
            orbitalWorld.setBodyState(link.orbitalBody(), mergeState(ownerState, followerState, link.handoffMode()));
        } else {
            ownerState = orbitalWorld.getBodyState(link.orbitalBody());
            followerState = generalWorld.getBodyState(link.generalBody());
            generalWorld.setBodyState(link.generalBody(), mergeState(ownerState, followerState, link.handoffMode()));
        }
    }

    private PhysicsBodyState mergeState(
            PhysicsBodyState ownerState,
            PhysicsBodyState followerState,
            StateHandoffMode handoffMode) {
        return switch (handoffMode) {
            case FULL_STATE -> new PhysicsBodyState(
                    ownerState.position(),
                    ownerState.orientation(),
                    ownerState.linearVelocity(),
                    ownerState.angularVelocity(),
                    followerState.referenceFrame(),
                    ownerState.timestampSeconds());
            case POSITION_VELOCITY_ONLY -> new PhysicsBodyState(
                    ownerState.position(),
                    followerState.orientation(),
                    ownerState.linearVelocity(),
                    followerState.angularVelocity(),
                    followerState.referenceFrame(),
                    ownerState.timestampSeconds());
        };
    }

    private HybridSnapshot captureSnapshot() {
        Map<PhysicsBodyHandle, PhysicsBodyState> generalStates = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : generalWorld.bodies()) {
            generalStates.put(handle, generalWorld.getBodyState(handle));
        }
        Map<PhysicsBodyHandle, PhysicsBodyState> orbitalStates = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : orbitalWorld.bodies()) {
            orbitalStates.put(handle, orbitalWorld.getBodyState(handle));
        }
        return new HybridSnapshot(simulationTimeSeconds, generalStates, orbitalStates);
    }
}
