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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Binary codec for snapshot serialization and replay pipelines.
 */
public final class HybridSnapshotIO {

    private static final int MAGIC = 0x48595331; // HYS1
    private static final int VERSION = 1;

    private HybridSnapshotIO() {
    }

    public static void write(OutputStream output, List<HybridSnapshot> snapshots) throws IOException {
        Objects.requireNonNull(output, "output must not be null");
        Objects.requireNonNull(snapshots, "snapshots must not be null");
        DataOutputStream out = new DataOutputStream(output);
        out.writeInt(MAGIC);
        out.writeInt(VERSION);
        out.writeInt(snapshots.size());
        for (HybridSnapshot snapshot : snapshots) {
            writeSnapshot(out, snapshot);
        }
        out.flush();
    }

    public static List<HybridSnapshot> read(InputStream input) throws IOException {
        Objects.requireNonNull(input, "input must not be null");
        DataInputStream in = new DataInputStream(input);
        int magic = in.readInt();
        if (magic != MAGIC) {
            throw new IOException("bad snapshot stream magic");
        }
        int version = in.readInt();
        if (version != VERSION) {
            throw new IOException("unsupported snapshot stream version: " + version);
        }
        int count = in.readInt();
        if (count < 0) {
            throw new IOException("negative snapshot count");
        }
        ArrayList<HybridSnapshot> snapshots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            snapshots.add(readSnapshot(in));
        }
        return snapshots;
    }

    private static void writeSnapshot(DataOutputStream out, HybridSnapshot snapshot) throws IOException {
        out.writeDouble(snapshot.simulationTimeSeconds());
        out.writeDouble(snapshot.interpolationAlpha());
        out.writeDouble(snapshot.extrapolationSeconds());
        writeStateMap(out, snapshot.generalStates());
        writeStateMap(out, snapshot.orbitalStates());
    }

    private static HybridSnapshot readSnapshot(DataInputStream in) throws IOException {
        double t = in.readDouble();
        double alpha = in.readDouble();
        double extrapolation = in.readDouble();
        Map<PhysicsBodyHandle, PhysicsBodyState> general = readStateMap(in);
        Map<PhysicsBodyHandle, PhysicsBodyState> orbital = readStateMap(in);
        return new HybridSnapshot(t, alpha, extrapolation, general, orbital);
    }

    private static void writeStateMap(DataOutputStream out, Map<PhysicsBodyHandle, PhysicsBodyState> map)
            throws IOException {
        out.writeInt(map.size());
        for (Map.Entry<PhysicsBodyHandle, PhysicsBodyState> entry : map.entrySet()) {
            out.writeLong(entry.getKey().value());
            writeState(out, entry.getValue());
        }
    }

    private static Map<PhysicsBodyHandle, PhysicsBodyState> readStateMap(DataInputStream in) throws IOException {
        int size = in.readInt();
        if (size < 0) {
            throw new IOException("negative state map size");
        }
        Map<PhysicsBodyHandle, PhysicsBodyState> map = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            PhysicsBodyHandle handle = new PhysicsBodyHandle(in.readLong());
            PhysicsBodyState state = readState(in);
            map.put(handle, state);
        }
        return map;
    }

    private static void writeState(DataOutputStream out, PhysicsBodyState state) throws IOException {
        writeVector(out, state.position());
        writeQuaternion(out, state.orientation());
        writeVector(out, state.linearVelocity());
        writeVector(out, state.angularVelocity());
        out.writeInt(state.referenceFrame().ordinal());
        out.writeDouble(state.timestampSeconds());
    }

    private static PhysicsBodyState readState(DataInputStream in) throws IOException {
        PhysicsVector3 position = readVector(in);
        PhysicsQuaternion orientation = readQuaternion(in);
        PhysicsVector3 linearVelocity = readVector(in);
        PhysicsVector3 angularVelocity = readVector(in);
        int frameOrdinal = in.readInt();
        ReferenceFrame[] frames = ReferenceFrame.values();
        if (frameOrdinal < 0 || frameOrdinal >= frames.length) {
            throw new IOException("invalid frame ordinal: " + frameOrdinal);
        }
        double timestamp = in.readDouble();
        return new PhysicsBodyState(
                position,
                orientation,
                linearVelocity,
                angularVelocity,
                frames[frameOrdinal],
                timestamp);
    }

    private static void writeVector(DataOutputStream out, PhysicsVector3 vector) throws IOException {
        out.writeDouble(vector.x());
        out.writeDouble(vector.y());
        out.writeDouble(vector.z());
    }

    private static PhysicsVector3 readVector(DataInputStream in) throws IOException {
        return new PhysicsVector3(in.readDouble(), in.readDouble(), in.readDouble());
    }

    private static void writeQuaternion(DataOutputStream out, PhysicsQuaternion q) throws IOException {
        out.writeDouble(q.x());
        out.writeDouble(q.y());
        out.writeDouble(q.z());
        out.writeDouble(q.w());
    }

    private static PhysicsQuaternion readQuaternion(DataInputStream in) throws IOException {
        return new PhysicsQuaternion(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
    }
}
