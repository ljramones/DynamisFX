package org.dynamisfx.simulation.coupling;

/**
 * Samples terrain surface height in local zone coordinates.
 */
@FunctionalInterface
public interface TerrainHeightSampler {

    /**
     * @return surface height in meters at local (x,y)
     */
    double sampleHeightMeters(double localX, double localY);
}
