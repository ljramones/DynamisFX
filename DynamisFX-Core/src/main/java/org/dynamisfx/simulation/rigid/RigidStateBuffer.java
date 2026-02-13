package org.dynamisfx.simulation.rigid;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Thread-safe in-memory buffer for rigid-body states keyed by object id.
 */
public final class RigidStateBuffer {

    private final Map<String, PhysicsBodyState> statesByObjectId = new ConcurrentHashMap<>();

    public void put(String objectId, PhysicsBodyState state) {
        validateObjectId(objectId);
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        statesByObjectId.put(objectId, state);
    }

    public Optional<PhysicsBodyState> get(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(statesByObjectId.get(objectId));
    }

    public boolean remove(String objectId) {
        validateObjectId(objectId);
        return statesByObjectId.remove(objectId) != null;
    }

    public Map<String, PhysicsBodyState> snapshot() {
        return Map.copyOf(statesByObjectId);
    }

    public boolean advanceLinear(String objectId, double dtSeconds, double timestampSeconds) {
        validateObjectId(objectId);
        if (!Double.isFinite(dtSeconds) || dtSeconds < 0.0) {
            throw new IllegalArgumentException("dtSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
        PhysicsBodyState current = statesByObjectId.get(objectId);
        if (current == null) {
            return false;
        }
        PhysicsVector3 p = current.position();
        PhysicsVector3 v = current.linearVelocity();
        PhysicsVector3 nextPosition = new PhysicsVector3(
                p.x() + (v.x() * dtSeconds),
                p.y() + (v.y() * dtSeconds),
                p.z() + (v.z() * dtSeconds));
        statesByObjectId.put(objectId, new PhysicsBodyState(
                nextPosition,
                current.orientation(),
                v,
                current.angularVelocity(),
                current.referenceFrame(),
                timestampSeconds));
        return true;
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
