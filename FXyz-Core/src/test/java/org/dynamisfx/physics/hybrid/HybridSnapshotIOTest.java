package org.dynamisfx.physics.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class HybridSnapshotIOTest {

    @Test
    void roundTripsSnapshotStreams() throws Exception {
        HybridSnapshot snapshot = new HybridSnapshot(
                1.25,
                0.5,
                0.01,
                java.util.Map.of(
                        new PhysicsBodyHandle(1),
                        new PhysicsBodyState(
                                new PhysicsVector3(1, 2, 3),
                                PhysicsQuaternion.IDENTITY,
                                new PhysicsVector3(4, 5, 6),
                                PhysicsVector3.ZERO,
                                ReferenceFrame.WORLD,
                                1.25)),
                java.util.Map.of(
                        new PhysicsBodyHandle(2),
                        new PhysicsBodyState(
                                new PhysicsVector3(7, 8, 9),
                                new PhysicsQuaternion(0, 0, 0, 1),
                                new PhysicsVector3(10, 11, 12),
                                PhysicsVector3.ZERO,
                                ReferenceFrame.ICRF,
                                1.25)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HybridSnapshotIO.write(out, List.of(snapshot));
        List<HybridSnapshot> readBack = HybridSnapshotIO.read(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(1, readBack.size());
        HybridSnapshot decoded = readBack.get(0);
        assertEquals(1.25, decoded.simulationTimeSeconds(), 1e-9);
        assertEquals(0.5, decoded.interpolationAlpha(), 1e-9);
        assertEquals(0.01, decoded.extrapolationSeconds(), 1e-9);
        assertEquals(1, decoded.generalStates().size());
        assertEquals(1, decoded.orbitalStates().size());
    }

    @Test
    void recorderReplaysInOrder() {
        HybridSnapshotRecorder recorder = new HybridSnapshotRecorder();
        HybridSnapshot a = new HybridSnapshot(0.1, 0.0, 0.1, java.util.Map.of(), java.util.Map.of());
        HybridSnapshot b = new HybridSnapshot(0.2, 0.0, 0.1, java.util.Map.of(), java.util.Map.of());
        recorder.record(a);
        recorder.record(b);

        StringBuilder sb = new StringBuilder();
        recorder.replay(snapshot -> sb.append(snapshot.simulationTimeSeconds()).append(","));
        assertEquals("0.1,0.2,", sb.toString());
        assertEquals(2, recorder.size());

        recorder.clear();
        assertEquals(0, recorder.size());
        assertThrows(NullPointerException.class, () -> recorder.replay(null));
    }
}
