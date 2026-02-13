package org.dynamisfx.physics.api;

/**
 * Optional terrain/mesh ingestion capability.
 */
public interface MeshTerrainCapability {

    boolean supportsHeightfieldTerrain();

    boolean supportsTriangleMeshTerrain();
}
