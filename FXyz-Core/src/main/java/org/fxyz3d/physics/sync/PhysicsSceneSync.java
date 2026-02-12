package org.fxyz3d.physics.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.model.PhysicsBodyState;

/**
 * Binds arbitrary node objects to physics handles and applies per-frame states.
 */
public final class PhysicsSceneSync<N> {

    private final NodeStateApplier<N> applier;
    private final Map<PhysicsBodyHandle, N> bindings = new LinkedHashMap<>();

    public PhysicsSceneSync(NodeStateApplier<N> applier) {
        this.applier = Objects.requireNonNull(applier, "applier must not be null");
    }

    public void bind(PhysicsBodyHandle handle, N node) {
        if (handle == null || node == null) {
            throw new IllegalArgumentException("handle and node must not be null");
        }
        bindings.put(handle, node);
    }

    public boolean unbindHandle(PhysicsBodyHandle handle) {
        if (handle == null) {
            return false;
        }
        return bindings.remove(handle) != null;
    }

    public boolean unbindNode(N node) {
        if (node == null) {
            return false;
        }
        PhysicsBodyHandle found = null;
        for (Map.Entry<PhysicsBodyHandle, N> entry : bindings.entrySet()) {
            if (entry.getValue() == node) {
                found = entry.getKey();
                break;
            }
        }
        return found != null && bindings.remove(found) != null;
    }

    public void clear() {
        bindings.clear();
    }

    public int bindingCount() {
        return bindings.size();
    }

    public void applyFrame(Function<PhysicsBodyHandle, PhysicsBodyState> stateProvider) {
        if (stateProvider == null) {
            throw new IllegalArgumentException("stateProvider must not be null");
        }
        for (Map.Entry<PhysicsBodyHandle, N> entry : bindings.entrySet()) {
            PhysicsBodyState state = stateProvider.apply(entry.getKey());
            if (state != null) {
                applier.apply(entry.getValue(), state);
            }
        }
    }
}
