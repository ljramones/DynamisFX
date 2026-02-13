package org.dynamisfx.simulation.coupling;

/**
 * Tiling parameters for a local terrain patch.
 */
public record TerrainPatchSpec(
        double halfExtentMeters,
        double tileSizeMeters,
        double tileThicknessMeters) {

    public TerrainPatchSpec {
        if (!Double.isFinite(halfExtentMeters) || halfExtentMeters <= 0.0) {
            throw new IllegalArgumentException("halfExtentMeters must be finite and > 0");
        }
        if (!Double.isFinite(tileSizeMeters) || tileSizeMeters <= 0.0) {
            throw new IllegalArgumentException("tileSizeMeters must be finite and > 0");
        }
        if (!Double.isFinite(tileThicknessMeters) || tileThicknessMeters <= 0.0) {
            throw new IllegalArgumentException("tileThicknessMeters must be finite and > 0");
        }
    }
}
