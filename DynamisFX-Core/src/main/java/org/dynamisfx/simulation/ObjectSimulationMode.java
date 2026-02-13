package org.dynamisfx.simulation;

/**
 * Declares which subsystem currently owns an object's motion authority.
 */
public enum ObjectSimulationMode {
    ORBITAL_ONLY,
    PHYSICS_ACTIVE,
    KINEMATIC_DRIVEN
}
