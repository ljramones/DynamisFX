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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Formatting and sink helpers for handoff diagnostics snapshots.
 */
public final class StateHandoffDiagnostics {

    private StateHandoffDiagnostics() {
    }

    public static String format(StateHandoffSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        return String.format(
                "handoff[%s] t=%.3f object=%s zone=%s anchor=(%.3f, %.3f, %.3f) globalPos=(%.3f, %.3f, %.3f) "
                        + "globalVel=(%.3f, %.3f, %.3f) globalAngVel=(%.3f, %.3f, %.3f) "
                        + "globalOri=(%.3f, %.3f, %.3f, %.3f) "
                        + "localPos=(%.3f, %.3f, %.3f) localVel=(%.3f, %.3f, %.3f) "
                        + "localAngVel=(%.3f, %.3f, %.3f) localOri=(%.3f, %.3f, %.3f, %.3f)",
                snapshot.direction(),
                snapshot.simulationTimeSeconds(),
                snapshot.objectId(),
                snapshot.zoneId().value(),
                snapshot.zoneAnchorPosition().x(),
                snapshot.zoneAnchorPosition().y(),
                snapshot.zoneAnchorPosition().z(),
                snapshot.globalPosition().x(),
                snapshot.globalPosition().y(),
                snapshot.globalPosition().z(),
                snapshot.globalVelocity().x(),
                snapshot.globalVelocity().y(),
                snapshot.globalVelocity().z(),
                snapshot.globalAngularVelocity().x(),
                snapshot.globalAngularVelocity().y(),
                snapshot.globalAngularVelocity().z(),
                snapshot.globalOrientation().x(),
                snapshot.globalOrientation().y(),
                snapshot.globalOrientation().z(),
                snapshot.globalOrientation().w(),
                snapshot.localPosition().x(),
                snapshot.localPosition().y(),
                snapshot.localPosition().z(),
                snapshot.localVelocity().x(),
                snapshot.localVelocity().y(),
                snapshot.localVelocity().z(),
                snapshot.localAngularVelocity().x(),
                snapshot.localAngularVelocity().y(),
                snapshot.localAngularVelocity().z(),
                snapshot.localOrientation().x(),
                snapshot.localOrientation().y(),
                snapshot.localOrientation().z(),
                snapshot.localOrientation().w());
    }

    public static String toJson(StateHandoffSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        return "{"
                + "\"direction\":\"" + escapeJson(snapshot.direction().name()) + "\","
                + "\"simulationTimeSeconds\":" + snapshot.simulationTimeSeconds() + ","
                + "\"objectId\":\"" + escapeJson(snapshot.objectId()) + "\","
                + "\"zoneId\":\"" + escapeJson(snapshot.zoneId().value()) + "\","
                + "\"zoneAnchorPosition\":" + vectorToJson(snapshot.zoneAnchorPosition()) + ","
                + "\"globalPosition\":" + vectorToJson(snapshot.globalPosition()) + ","
                + "\"globalVelocity\":" + vectorToJson(snapshot.globalVelocity()) + ","
                + "\"globalAngularVelocity\":" + vectorToJson(snapshot.globalAngularVelocity()) + ","
                + "\"globalOrientation\":" + quaternionToJson(snapshot.globalOrientation()) + ","
                + "\"localPosition\":" + vectorToJson(snapshot.localPosition()) + ","
                + "\"localVelocity\":" + vectorToJson(snapshot.localVelocity()) + ","
                + "\"localAngularVelocity\":" + vectorToJson(snapshot.localAngularVelocity()) + ","
                + "\"localOrientation\":" + quaternionToJson(snapshot.localOrientation())
                + "}";
    }

    public static Consumer<StateHandoffSnapshot> loggingSink(Logger logger) {
        Objects.requireNonNull(logger, "logger must not be null");
        return snapshot -> logger.info(format(snapshot));
    }

    private static String vectorToJson(org.dynamisfx.physics.model.PhysicsVector3 v) {
        return "{"
                + "\"x\":" + v.x() + ","
                + "\"y\":" + v.y() + ","
                + "\"z\":" + v.z()
                + "}";
    }

    private static String quaternionToJson(org.dynamisfx.physics.model.PhysicsQuaternion q) {
        return "{"
                + "\"x\":" + q.x() + ","
                + "\"y\":" + q.y() + ","
                + "\"z\":" + q.z() + ","
                + "\"w\":" + q.w()
                + "}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
