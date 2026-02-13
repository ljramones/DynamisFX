/**
 * PositionUpdateCache.java
 *
 * Copyright (c) 2013-2019, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dynamisfx.shapes.primitives.helper;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;

/**
 * Helper class for efficient position updates in particle systems.
 * <p>
 * This class caches mesh information after creation to enable fast position
 * updates without requiring a full mesh rebuild. It tracks the number of
 * vertices per particle and maintains cached positions to calculate deltas.
 * <p>
 * Usage:
 * <pre>{@code
 * PositionUpdateCache cache = new PositionUpdateCache();
 * // After mesh creation:
 * cache.cacheMeshInfo(scatterData, triangleMesh);
 * // For position updates:
 * boolean efficient = cache.updatePositions(newPositions, triangleMesh);
 * }</pre>
 *
 * @author FXyz contributors
 */
public class PositionUpdateCache {

    /**
     * Number of vertices per marker shape (cached after mesh creation).
     * Used for efficient position updates.
     */
    private int verticesPerMarker = -1;

    /**
     * Cached positions from last mesh build.
     * Used to calculate deltas for efficient updates.
     */
    private List<Point3D> cachedPositions = null;

    /**
     * Creates a new PositionUpdateCache.
     */
    public PositionUpdateCache() {
    }

    /**
     * Caches mesh info after creation for efficient updates.
     * <p>
     * Call this method after building/rebuilding the mesh to enable
     * efficient position updates.
     *
     * @param data the scatter data (particle positions)
     * @param mesh the triangle mesh
     */
    public void cacheMeshInfo(List<Point3D> data, TriangleMesh mesh) {
        if (data == null || data.isEmpty() || mesh == null) {
            verticesPerMarker = -1;
            cachedPositions = null;
            return;
        }

        // Calculate vertices per marker from the mesh
        int totalVertices = mesh.getPoints().size() / 3;
        verticesPerMarker = totalVertices / data.size();

        // Cache positions (make copies to track deltas)
        cachedPositions = new ArrayList<>(data.size());
        for (Point3D p : data) {
            cachedPositions.add(new Point3D(p.x, p.y, p.z, p.f, p.colorIndex));
        }
    }

    /**
     * Clears the cached mesh info.
     * <p>
     * Call this when the mesh is rebuilt in non-joined mode or when
     * the cache should be invalidated.
     */
    public void clear() {
        verticesPerMarker = -1;
        cachedPositions = null;
    }

    /**
     * Updates particle positions efficiently without full mesh rebuild.
     * <p>
     * This method is much faster than rebuilding the mesh because it directly
     * modifies the mesh's points array rather than rebuilding the entire mesh.
     * <p>
     * Requirements for efficient update:
     * <ul>
     *   <li>Cache must be initialized (call cacheMeshInfo after mesh build)</li>
     *   <li>Particle count must match the cached count</li>
     * </ul>
     *
     * @param newPositions the new positions for all particles (same count as original)
     * @param mesh the triangle mesh to update
     * @return true if efficient update was used, false if update could not be performed
     */
    public boolean updatePositions(List<Point3D> newPositions, TriangleMesh mesh) {
        if (!canUseEfficientUpdate(newPositions) || mesh == null) {
            return false;
        }

        // Get the points array
        ObservableFloatArray points = mesh.getPoints();

        // Update each particle's vertices by applying delta
        for (int p = 0; p < newPositions.size(); p++) {
            Point3D oldPos = cachedPositions.get(p);
            Point3D newPos = newPositions.get(p);

            float dx = newPos.x - oldPos.x;
            float dy = newPos.y - oldPos.y;
            float dz = newPos.z - oldPos.z;

            // Skip if no change
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }

            // Update all vertices for this particle
            int baseIdx = p * verticesPerMarker * 3;
            for (int v = 0; v < verticesPerMarker; v++) {
                int idx = baseIdx + v * 3;
                points.set(idx, points.get(idx) + dx);
                points.set(idx + 1, points.get(idx + 1) + dy);
                points.set(idx + 2, points.get(idx + 2) + dz);
            }
        }

        // Update cached positions (copy the new positions)
        for (int i = 0; i < newPositions.size(); i++) {
            Point3D newPos = newPositions.get(i);
            Point3D cached = cachedPositions.get(i);
            cached.x = newPos.x;
            cached.y = newPos.y;
            cached.z = newPos.z;
        }

        return true;
    }

    /**
     * Updates a single particle's position efficiently.
     * <p>
     * This is even more efficient than updatePositions() when only one or a few
     * particles need to move.
     *
     * @param particleIndex the index of the particle to update
     * @param newPosition the new position
     * @param mesh the triangle mesh to update
     * @return true if update was successful, false if index out of bounds or cache not ready
     */
    public boolean updateParticlePosition(int particleIndex, Point3D newPosition, TriangleMesh mesh) {
        if (!canUseEfficientUpdate(cachedPositions) || mesh == null) {
            return false;
        }

        if (particleIndex < 0 || particleIndex >= cachedPositions.size()) {
            return false;
        }

        Point3D oldPos = cachedPositions.get(particleIndex);
        float dx = newPosition.x - oldPos.x;
        float dy = newPosition.y - oldPos.y;
        float dz = newPosition.z - oldPos.z;

        if (dx == 0 && dy == 0 && dz == 0) {
            return true; // No change needed
        }

        ObservableFloatArray points = mesh.getPoints();
        int baseIdx = particleIndex * verticesPerMarker * 3;

        for (int v = 0; v < verticesPerMarker; v++) {
            int idx = baseIdx + v * 3;
            points.set(idx, points.get(idx) + dx);
            points.set(idx + 1, points.get(idx + 1) + dy);
            points.set(idx + 2, points.get(idx + 2) + dz);
        }

        // Update cached position
        oldPos.x = newPosition.x;
        oldPos.y = newPosition.y;
        oldPos.z = newPosition.z;

        return true;
    }

    /**
     * Checks if efficient position update can be used.
     *
     * @param newPositions the proposed new positions
     * @return true if efficient update is possible
     */
    public boolean canUseEfficientUpdate(List<Point3D> newPositions) {
        // Must have cached info from previous build
        if (verticesPerMarker <= 0 || cachedPositions == null) {
            return false;
        }

        // Particle count must match
        if (newPositions == null || newPositions.size() != cachedPositions.size()) {
            return false;
        }

        return true;
    }

    /**
     * Gets the number of vertices per marker shape.
     * Useful for debugging and understanding mesh structure.
     *
     * @return vertices per marker, or -1 if not yet computed
     */
    public int getVerticesPerMarker() {
        return verticesPerMarker;
    }

    /**
     * Gets the total number of particles currently cached.
     *
     * @return particle count, or 0 if no cache exists
     */
    public int getParticleCount() {
        return cachedPositions != null ? cachedPositions.size() : 0;
    }
}
