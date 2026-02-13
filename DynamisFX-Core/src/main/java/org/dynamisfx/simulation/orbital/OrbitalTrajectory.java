package org.dynamisfx.simulation.orbital;

import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Functional trajectory provider used by scripted orbital engine scaffolding.
 */
@FunctionalInterface
public interface OrbitalTrajectory {

    OrbitalState sample(double simulationTimeSeconds, ReferenceFrame outputFrame);
}
