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

package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.junit.jupiter.api.Test;

class StateHandoffDiagnosticsTest {

    @Test
    void formatsSnapshotText() {
        StateHandoffSnapshot snapshot = new StateHandoffSnapshot(
                StateHandoffDirection.PROMOTE_TO_PHYSICS,
                1.5,
                "lander-1",
                new ZoneId("zone-a"),
                new PhysicsVector3(100.0, 0.0, 0.0),
                new PhysicsVector3(110.0, 1.0, 2.0),
                new PhysicsVector3(3.0, 4.0, 5.0),
                new PhysicsVector3(0.1, 0.2, 0.3),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(10.0, 1.0, 2.0),
                new PhysicsVector3(3.0, 4.0, 5.0),
                new PhysicsVector3(0.1, 0.2, 0.3),
                PhysicsQuaternion.IDENTITY);

        String formatted = StateHandoffDiagnostics.format(snapshot);

        assertTrue(formatted.contains("PROMOTE_TO_PHYSICS"));
        assertTrue(formatted.contains("object=lander-1"));
        assertTrue(formatted.contains("zone=zone-a"));
    }

    @Test
    void serializesSnapshotAsJson() {
        StateHandoffSnapshot snapshot = new StateHandoffSnapshot(
                StateHandoffDirection.DEMOTE_TO_ORBITAL,
                2.5,
                "lander-1",
                new ZoneId("zone-a"),
                new PhysicsVector3(100.0, 0.0, 0.0),
                new PhysicsVector3(105.0, 1.0, 2.0),
                new PhysicsVector3(3.0, 4.0, 5.0),
                new PhysicsVector3(0.3, 0.2, 0.1),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(5.0, 1.0, 2.0),
                new PhysicsVector3(3.0, 4.0, 5.0),
                new PhysicsVector3(0.3, 0.2, 0.1),
                PhysicsQuaternion.IDENTITY);

        String json = StateHandoffDiagnostics.toJson(snapshot);

        assertTrue(json.contains("\"direction\":\"DEMOTE_TO_ORBITAL\""));
        assertTrue(json.contains("\"objectId\":\"lander-1\""));
        assertTrue(json.contains("\"zoneId\":\"zone-a\""));
        assertTrue(json.contains("\"globalPosition\":{\"x\":105.0,\"y\":1.0,\"z\":2.0}"));
    }

    @Test
    void writesFormattedSnapshotToLogger() {
        Logger logger = Logger.getLogger("StateHandoffDiagnosticsTest");
        logger.setUseParentHandlers(false);
        List<String> messages = new ArrayList<>();
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        capture.setLevel(Level.INFO);
        logger.addHandler(capture);

        StateHandoffDiagnostics.loggingSink(logger).accept(new StateHandoffSnapshot(
                StateHandoffDirection.DEMOTE_TO_ORBITAL,
                2.0,
                "lander-1",
                new ZoneId("zone-a"),
                PhysicsVector3.ZERO,
                new PhysicsVector3(10.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(10.0, 0.0, 0.0),
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY));

        assertTrue(messages.stream().anyMatch(m -> m.contains("DEMOTE_TO_ORBITAL")));
        logger.removeHandler(capture);
    }

    @Test
    void validatesLoggerArgument() {
        assertThrows(NullPointerException.class, () -> StateHandoffDiagnostics.loggingSink(null));
    }
}
