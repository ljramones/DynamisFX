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
