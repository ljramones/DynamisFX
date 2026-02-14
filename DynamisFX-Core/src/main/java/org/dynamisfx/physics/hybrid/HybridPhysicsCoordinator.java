/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dynamisfx.physics.hybrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyState;

/**
 * Coordinates two physics worlds under one deterministic timeline.
 */
public final class HybridPhysicsCoordinator {

    private final PhysicsWorld generalWorld;
    private final PhysicsWorld orbitalWorld;
    private final HybridCapabilityReport capabilityReport;
    private final AtomicLong nextLinkId = new AtomicLong(1L);
    private final Map<Long, LinkRuntime> links = new LinkedHashMap<>();
    private final AtomicReference<HybridSnapshot> latestSnapshot = new AtomicReference<>();
    private final AtomicReference<HybridStepTelemetry> latestTelemetry = new AtomicReference<>();
    private double simulationTimeSeconds;
    private double lastStepSeconds;
    private int lastRejectedHandoffs;

    public HybridPhysicsCoordinator(PhysicsWorld generalWorld, PhysicsWorld orbitalWorld) {
        this(generalWorld, orbitalWorld, HybridCapabilityPolicy.LENIENT);
    }

    public HybridPhysicsCoordinator(
            PhysicsWorld generalWorld,
            PhysicsWorld orbitalWorld,
            HybridCapabilityPolicy capabilityPolicy) {
        this.generalWorld = Objects.requireNonNull(generalWorld, "generalWorld must not be null");
        this.orbitalWorld = Objects.requireNonNull(orbitalWorld, "orbitalWorld must not be null");
        Objects.requireNonNull(capabilityPolicy, "capabilityPolicy must not be null");
        this.capabilityReport = validateCapabilities(capabilityPolicy, generalWorld, orbitalWorld);
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
        links.put(id, new LinkRuntime(link));
        return id;
    }

    public boolean removeLink(long linkId) {
        return links.remove(linkId) != null;
    }

    public boolean setLinkEnabled(long linkId, boolean enabled) {
        LinkRuntime runtime = links.get(linkId);
        if (runtime == null) {
            return false;
        }
        runtime.enabled = enabled;
        return true;
    }

    public boolean isLinkEnabled(long linkId) {
        LinkRuntime runtime = links.get(linkId);
        return runtime != null && runtime.enabled;
    }

    public boolean updateLink(long linkId, HybridBodyLink replacement) {
        Objects.requireNonNull(replacement, "replacement must not be null");
        LinkRuntime runtime = links.get(linkId);
        if (runtime == null) {
            return false;
        }
        runtime.link = replacement;
        return true;
    }

    public int removeLinksForBody(PhysicsBodyHandle handle) {
        int removed = 0;
        java.util.Iterator<Map.Entry<Long, LinkRuntime>> iterator = links.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LinkRuntime> pair = iterator.next();
            HybridBodyLink link = pair.getValue().link;
            if (link.generalBody().equals(handle) || link.orbitalBody().equals(handle)) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public void clearLinks() {
        links.clear();
    }

    public Collection<HybridBodyLink> links() {
        ArrayList<HybridBodyLink> result = new ArrayList<>(links.size());
        for (LinkRuntime runtime : links.values()) {
            result.add(runtime.link);
        }
        return result;
    }

    public Collection<HybridLinkDiagnostics> linkDiagnostics() {
        ArrayList<HybridLinkDiagnostics> result = new ArrayList<>(links.size());
        for (Map.Entry<Long, LinkRuntime> pair : links.entrySet()) {
            LinkRuntime runtime = pair.getValue();
            result.add(new HybridLinkDiagnostics(
                    pair.getKey(),
                    runtime.link,
                    runtime.enabled,
                    runtime.rejectedCount,
                    runtime.lastPositionErrorMeters,
                    runtime.lastLinearVelocityErrorMetersPerSecond,
                    runtime.lastAngularVelocityErrorRadiansPerSecond,
                    runtime.lastHandoffTimeSeconds));
        }
        return result;
    }

    public HybridSnapshot latestSnapshot() {
        return latestSnapshot.get();
    }

    public HybridStepTelemetry latestTelemetry() {
        return latestTelemetry.get();
    }

    public HybridCapabilityReport capabilityReport() {
        return capabilityReport;
    }

    public double simulationTimeSeconds() {
        return simulationTimeSeconds;
    }

    public HybridSnapshot step(double dtSeconds) {
        return step(dtSeconds, 0.0, dtSeconds);
    }

    public HybridSnapshot step(double dtSeconds, double interpolationAlpha, double extrapolationSeconds) {
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }
        validateRenderMetadata(interpolationAlpha, extrapolationSeconds);

        long orbitalStart = System.nanoTime();
        orbitalWorld.step(dtSeconds);
        long afterOrbital = System.nanoTime();
        generalWorld.step(dtSeconds);
        long afterGeneral = System.nanoTime();

        lastRejectedHandoffs = 0;
        int handoffCount = 0;
        long handoffStart = System.nanoTime();
        for (LinkRuntime runtime : links.values()) {
            if (!runtime.enabled) {
                continue;
            }
            applyHandoff(runtime);
            handoffCount++;
        }
        long afterHandoff = System.nanoTime();

        simulationTimeSeconds += dtSeconds;
        lastStepSeconds = dtSeconds;
        HybridSnapshot snapshot = captureSnapshot(interpolationAlpha, extrapolationSeconds);
        latestSnapshot.set(snapshot);
        latestTelemetry.set(new HybridStepTelemetry(
                simulationTimeSeconds,
                dtSeconds,
                afterOrbital - orbitalStart,
                afterGeneral - afterOrbital,
                afterHandoff - handoffStart,
                links.size(),
                handoffCount,
                lastRejectedHandoffs));
        return snapshot;
    }

    public HybridSnapshot updateRenderMetadata(double interpolationAlpha, double extrapolationSeconds) {
        validateRenderMetadata(interpolationAlpha, extrapolationSeconds);
        HybridSnapshot current = latestSnapshot.get();
        if (current == null) {
            return null;
        }
        HybridSnapshot updated = new HybridSnapshot(
                current.simulationTimeSeconds(),
                interpolationAlpha,
                extrapolationSeconds,
                current.generalStates(),
                current.orbitalStates());
        latestSnapshot.set(updated);
        return updated;
    }

    public int lastRejectedHandoffs() {
        return lastRejectedHandoffs;
    }

    private void applyHandoff(LinkRuntime runtime) {
        HybridBodyLink link = runtime.link;
        PhysicsBodyState ownerState;
        PhysicsBodyState followerState;
        if (link.ownership() == HybridOwnership.GENERAL) {
            ownerState = generalWorld.getBodyState(link.generalBody());
            followerState = orbitalWorld.getBodyState(link.orbitalBody());
            updateDiagnostics(runtime, ownerState, followerState);
            if (shouldReject(runtime, link)) {
                lastRejectedHandoffs++;
                runtime.rejectedCount++;
                return;
            }
            orbitalWorld.setBodyState(link.orbitalBody(), mergeState(ownerState, followerState, link.handoffMode()));
        } else {
            ownerState = orbitalWorld.getBodyState(link.orbitalBody());
            followerState = generalWorld.getBodyState(link.generalBody());
            updateDiagnostics(runtime, ownerState, followerState);
            if (shouldReject(runtime, link)) {
                lastRejectedHandoffs++;
                runtime.rejectedCount++;
                return;
            }
            generalWorld.setBodyState(link.generalBody(), mergeState(ownerState, followerState, link.handoffMode()));
        }
        runtime.lastHandoffTimeSeconds = simulationTimeSeconds + lastStepSeconds;
    }

    private boolean shouldReject(LinkRuntime runtime, HybridBodyLink link) {
        if (link.conflictPolicy() != ConflictPolicy.REJECT_ON_DIVERGENCE) {
            return false;
        }
        return runtime.lastPositionErrorMeters > link.maxPositionDivergenceMeters()
                || runtime.lastLinearVelocityErrorMetersPerSecond > link.maxLinearVelocityDivergenceMetersPerSecond()
                || runtime.lastAngularVelocityErrorRadiansPerSecond
                > link.maxAngularVelocityDivergenceRadiansPerSecond();
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

    private HybridSnapshot captureSnapshot(double interpolationAlpha, double extrapolationSeconds) {
        Map<PhysicsBodyHandle, PhysicsBodyState> generalStates = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : generalWorld.bodies()) {
            generalStates.put(handle, generalWorld.getBodyState(handle));
        }
        Map<PhysicsBodyHandle, PhysicsBodyState> orbitalStates = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : orbitalWorld.bodies()) {
            orbitalStates.put(handle, orbitalWorld.getBodyState(handle));
        }
        return new HybridSnapshot(
                simulationTimeSeconds,
                interpolationAlpha,
                extrapolationSeconds,
                generalStates,
                orbitalStates);
    }

    private static void validateRenderMetadata(double interpolationAlpha, double extrapolationSeconds) {
        if (!Double.isFinite(interpolationAlpha) || interpolationAlpha < 0.0 || interpolationAlpha > 1.0) {
            throw new IllegalArgumentException("interpolationAlpha must be finite in [0,1]");
        }
        if (!Double.isFinite(extrapolationSeconds) || extrapolationSeconds < 0.0) {
            throw new IllegalArgumentException("extrapolationSeconds must be finite and >= 0");
        }
    }

    private static void updateDiagnostics(LinkRuntime runtime, PhysicsBodyState owner, PhysicsBodyState follower) {
        runtime.lastPositionErrorMeters = distance(owner.position(), follower.position());
        runtime.lastLinearVelocityErrorMetersPerSecond = distance(owner.linearVelocity(), follower.linearVelocity());
        runtime.lastAngularVelocityErrorRadiansPerSecond = distance(owner.angularVelocity(), follower.angularVelocity());
    }

    private static double distance(org.dynamisfx.physics.model.PhysicsVector3 a, org.dynamisfx.physics.model.PhysicsVector3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static final class LinkRuntime {
        private HybridBodyLink link;
        private boolean enabled = true;
        private long rejectedCount;
        private double lastPositionErrorMeters;
        private double lastLinearVelocityErrorMetersPerSecond;
        private double lastAngularVelocityErrorRadiansPerSecond;
        private double lastHandoffTimeSeconds;

        private LinkRuntime(HybridBodyLink link) {
            this.link = link;
        }
    }

    private static HybridCapabilityReport validateCapabilities(
            HybridCapabilityPolicy policy,
            PhysicsWorld generalWorld,
            PhysicsWorld orbitalWorld) {
        boolean generalRigid = generalWorld.capabilities().supportsRigidBodies();
        boolean orbitalNBody = orbitalWorld.capabilities().supportsNBody();
        boolean passed = generalRigid && orbitalNBody;
        String message = passed
                ? "capability gate passed"
                : "expected general world rigid-body support and orbital world n-body support";
        if (policy == HybridCapabilityPolicy.STRICT && !passed) {
            throw new IllegalStateException("hybrid strict capability gate failed: " + message);
        }
        return new HybridCapabilityReport(
                policy,
                generalRigid,
                orbitalNBody,
                passed,
                message);
    }
}
