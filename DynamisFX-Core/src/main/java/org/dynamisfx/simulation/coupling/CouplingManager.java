package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.Optional;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Coordinates object ownership across orbital and zone-local rigid simulations.
 */
public interface CouplingManager {

    void registerZone(PhysicsZone zone);

    boolean removeZone(ZoneId zoneId);

    Collection<PhysicsZone> zones();

    Optional<ObjectSimulationMode> modeFor(String objectId);

    void setMode(String objectId, ObjectSimulationMode mode);

    void update(double simulationTimeSeconds);
}
