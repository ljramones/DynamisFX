package org.dynamisfx.simulation;

import java.util.Objects;
import org.dynamisfx.simulation.orbital.OrbitalStateBuffer;
import org.dynamisfx.simulation.rigid.RigidStateBuffer;

/**
 * Aggregate holder for orbital and rigid state buffers used by simulation orchestration.
 */
public final class SimulationStateBuffers {

    private final OrbitalStateBuffer orbital;
    private final RigidStateBuffer rigid;

    public SimulationStateBuffers() {
        this(new OrbitalStateBuffer(), new RigidStateBuffer());
    }

    public SimulationStateBuffers(OrbitalStateBuffer orbital, RigidStateBuffer rigid) {
        this.orbital = Objects.requireNonNull(orbital, "orbital must not be null");
        this.rigid = Objects.requireNonNull(rigid, "rigid must not be null");
    }

    public OrbitalStateBuffer orbital() {
        return orbital;
    }

    public RigidStateBuffer rigid() {
        return rigid;
    }
}
