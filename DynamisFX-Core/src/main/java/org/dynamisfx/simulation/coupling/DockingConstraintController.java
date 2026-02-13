package org.dynamisfx.simulation.coupling;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsConstraintType;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Creates/releases docking constraints with latch/release hysteresis.
 */
public final class DockingConstraintController {

    private final ZoneBodyRegistry bodyRegistry;
    private final Map<String, LatchState> latchesByObjectId = new ConcurrentHashMap<>();
    private final Consumer<DockingConstraintEvent> diagnosticsSink;

    public DockingConstraintController(ZoneBodyRegistry bodyRegistry) {
        this(bodyRegistry, event -> {
        });
    }

    public DockingConstraintController(
            ZoneBodyRegistry bodyRegistry,
            Consumer<DockingConstraintEvent> diagnosticsSink) {
        this.bodyRegistry = Objects.requireNonNull(bodyRegistry, "bodyRegistry must not be null");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink must not be null");
    }

    public boolean updateLatch(
            PhysicsZone zone,
            String objectId,
            PhysicsBodyHandle targetBodyHandle,
            double latchDistanceMeters,
            double releaseDistanceMeters,
            boolean forceRelease) {
        Objects.requireNonNull(zone, "zone must not be null");
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(targetBodyHandle, "targetBodyHandle must not be null");
        if (!Double.isFinite(latchDistanceMeters) || latchDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("latchDistanceMeters must be finite and > 0");
        }
        if (!Double.isFinite(releaseDistanceMeters) || releaseDistanceMeters < latchDistanceMeters) {
            throw new IllegalArgumentException("releaseDistanceMeters must be finite and >= latchDistanceMeters");
        }

        RigidBodyWorld world = zone.world();
        if (world == null || !world.capabilities().supportsJoints()) {
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.SKIPPED,
                    objectId,
                    zone.zoneId(),
                    "joints-unsupported",
                    Double.NaN,
                    null));
            clearLatch(objectId, world);
            return false;
        }

        PhysicsBodyHandle objectHandle = bodyRegistry.bindingForObject(objectId)
                .filter(binding -> zone.zoneId().equals(binding.zoneId()))
                .map(ZoneBodyRegistry.ZoneBodyBinding::bodyHandle)
                .orElse(null);
        if (objectHandle == null) {
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.SKIPPED,
                    objectId,
                    zone.zoneId(),
                    "object-unbound",
                    Double.NaN,
                    null));
            clearLatch(objectId, world);
            return false;
        }

        PhysicsBodyState objectState = safeGetBodyState(world, objectHandle);
        PhysicsBodyState targetState = safeGetBodyState(world, targetBodyHandle);
        if (objectState == null || targetState == null) {
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.SKIPPED,
                    objectId,
                    zone.zoneId(),
                    "missing-state",
                    Double.NaN,
                    null));
            clearLatch(objectId, world);
            return false;
        }

        LatchState latch = latchesByObjectId.get(objectId);
        double distance = distance(objectState.position(), targetState.position());
        if (latch == null) {
            if (forceRelease || distance > latchDistanceMeters) {
                diagnosticsSink.accept(new DockingConstraintEvent(
                        DockingConstraintEventType.SKIPPED,
                        objectId,
                        zone.zoneId(),
                        forceRelease ? "force-release" : "distance-too-large",
                        distance,
                        null));
                return false;
            }
            PhysicsConstraintHandle handle = world.createConstraint(new PhysicsConstraintDefinition(
                    PhysicsConstraintType.FIXED,
                    objectHandle,
                    targetBodyHandle,
                    targetState.position()));
            latchesByObjectId.put(objectId, new LatchState(zone.zoneId(), handle, targetBodyHandle));
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.LATCHED,
                    objectId,
                    zone.zoneId(),
                    "distance-threshold",
                    distance,
                    handle));
            return true;
        }

        if (!zone.zoneId().equals(latch.zoneId) || !targetBodyHandle.equals(latch.targetBodyHandle)) {
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.RELEASED,
                    objectId,
                    zone.zoneId(),
                    "zone-or-target-changed",
                    distance,
                    latch.constraintHandle));
            clearLatch(objectId, world);
            return false;
        }
        if (forceRelease || distance >= releaseDistanceMeters) {
            world.removeConstraint(latch.constraintHandle);
            latchesByObjectId.remove(objectId);
            diagnosticsSink.accept(new DockingConstraintEvent(
                    DockingConstraintEventType.RELEASED,
                    objectId,
                    zone.zoneId(),
                    forceRelease ? "force-release" : "distance-release",
                    distance,
                    latch.constraintHandle));
            return false;
        }
        return true;
    }

    public boolean isLatched(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        return latchesByObjectId.containsKey(objectId);
    }

    public void release(String objectId, PhysicsZone zone) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(zone, "zone must not be null");
        clearLatch(objectId, zone.world());
    }

    private void clearLatch(String objectId, RigidBodyWorld world) {
        LatchState latch = latchesByObjectId.remove(objectId);
        if (latch != null && world != null) {
            world.removeConstraint(latch.constraintHandle);
        }
    }

    private static PhysicsBodyState safeGetBodyState(RigidBodyWorld world, PhysicsBodyHandle handle) {
        try {
            return world.getBodyState(handle);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static double distance(PhysicsVector3 a, PhysicsVector3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }

    private record LatchState(ZoneId zoneId, PhysicsConstraintHandle constraintHandle, PhysicsBodyHandle targetBodyHandle) {
    }
}
