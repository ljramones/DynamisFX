package org.dynamisfx.simulation.orbital;

import java.util.Collection;
import java.util.Map;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Global dynamics provider for orbit and frame-aware propagation.
 */
public interface OrbitalDynamicsEngine extends AutoCloseable {

    /**
     * Propagates requested objects to the target simulation time.
     *
     * @param objectIds object identifiers to propagate
     * @param simulationTimeSeconds absolute simulation time in seconds
     * @param outputFrame desired output frame
     * @return mapping from object id to propagated state
     */
    Map<String, OrbitalState> propagateTo(
            Collection<String> objectIds,
            double simulationTimeSeconds,
            ReferenceFrame outputFrame);

    @Override
    default void close() {
        // no-op
    }
}
