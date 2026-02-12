package org.fxyz3d.physics.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * In-memory recorder for snapshot replay.
 */
public final class HybridSnapshotRecorder {

    private final List<HybridSnapshot> snapshots = new ArrayList<>();

    public void record(HybridSnapshot snapshot) {
        snapshots.add(Objects.requireNonNull(snapshot, "snapshot must not be null"));
    }

    public int size() {
        return snapshots.size();
    }

    public void clear() {
        snapshots.clear();
    }

    public List<HybridSnapshot> snapshots() {
        return List.copyOf(snapshots);
    }

    public void replay(Consumer<HybridSnapshot> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        for (HybridSnapshot snapshot : snapshots) {
            consumer.accept(snapshot);
        }
    }
}
