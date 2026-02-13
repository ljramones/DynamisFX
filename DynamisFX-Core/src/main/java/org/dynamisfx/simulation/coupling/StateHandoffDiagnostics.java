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
                        + "globalVel=(%.3f, %.3f, %.3f) localPos=(%.3f, %.3f, %.3f) localVel=(%.3f, %.3f, %.3f)",
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
                snapshot.localPosition().x(),
                snapshot.localPosition().y(),
                snapshot.localPosition().z(),
                snapshot.localVelocity().x(),
                snapshot.localVelocity().y(),
                snapshot.localVelocity().z());
    }

    public static Consumer<StateHandoffSnapshot> loggingSink(Logger logger) {
        Objects.requireNonNull(logger, "logger must not be null");
        return snapshot -> logger.info(format(snapshot));
    }
}
