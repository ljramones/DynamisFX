/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.shapes.primitives.helper;

import javafx.scene.DepthTest;
import javafx.scene.shape.DrawMode;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.CuboidMesh;
import org.dynamisfx.shapes.primitives.FrustumMesh;
import org.dynamisfx.shapes.primitives.OctahedronMarkerMesh;
import org.dynamisfx.shapes.primitives.QuadMesh;
import org.dynamisfx.shapes.primitives.SegmentedSphereMesh;
import org.dynamisfx.shapes.primitives.TetrahedraMesh;
import org.dynamisfx.shapes.primitives.TexturedMesh;

public class MarkerFactory {

    /**
     * Base multiplier for DIAMOND marker segments.
     * Diamond uses fewer segments than sphere for a faceted look.
     */
    private static final int DIAMOND_SEGMENT_MULTIPLIER = 3;

    /**
     * Base multiplier for SPHERE marker segments.
     * Sphere uses more segments for a smoother appearance.
     */
    private static final int SPHERE_SEGMENT_MULTIPLIER = 6;

    /**
     * Calculates the number of segments for a sphere mesh based on level.
     * Formula: multiplier * (level² + 1) provides quadratic detail increase.
     *
     * @param multiplier base segment multiplier (3 for diamond, 6 for sphere)
     * @param level subdivision level (higher = more detail)
     * @return number of segments for the mesh
     */
    private static int calculateSegments(int multiplier, int level) {
        return multiplier * (level * level + 1);
    }

    private interface Marker3D {
        TexturedMesh getMarker(String id, double size, int level, Point3D point3D);
    }

    public enum Marker implements Marker3D {
        TETRAHEDRA {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                TexturedMesh dot = new TetrahedraMesh(size, level, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        CUBE {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                TexturedMesh dot = new CuboidMesh(size, size, size, level, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        DIAMOND {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                int segments = calculateSegments(DIAMOND_SEGMENT_MULTIPLIER, level);
                TexturedMesh dot = new SegmentedSphereMesh(segments, 0, 0, size / 2d, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        SPHERE {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                int segments = calculateSegments(SPHERE_SEGMENT_MULTIPLIER, level);
                TexturedMesh dot = new SegmentedSphereMesh(segments, 0, 0, size / 2d, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        CONE {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                TexturedMesh dot = new FrustumMesh(size / 3d, 0, size, level,
                        point3D != null ? point3D.add((float)(-size / 2d), 0f, 0f) : null,
                        point3D != null ? point3D.add((float)(size / 2d), 0f, 0f) : null);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        /**
         * Low-polygon sphere approximation using a regular octahedron.
         * <p>
         * Much more efficient than SPHERE or DIAMOND:
         * <ul>
         *   <li>Only 6 vertices and 8 faces</li>
         *   <li>Looks roughly spherical from a distance</li>
         *   <li>Ideal for particle systems with many particles</li>
         * </ul>
         */
        OCTAHEDRON {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                TexturedMesh dot = new OctahedronMarkerMesh(size / 2d, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        },
        /**
         * Simple flat quad (two triangles) for billboard-style markers.
         * <p>
         * The most lightweight marker type:
         * <ul>
         *   <li>Only 4 vertices and 2 faces</li>
         *   <li>Flat quad in XY plane, facing +Z</li>
         *   <li>Ideal for 2D sprites or when combined with camera-facing rotation</li>
         * </ul>
         * <p>
         * Note: CullFace is set to NONE by default so the quad is visible from both sides.
         */
        QUAD {
            @Override
            public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
                TexturedMesh dot = new QuadMesh(size, point3D);
                dot.setId(id);
                dot.setDrawMode(DrawMode.FILL);
                dot.setDepthTest(DepthTest.ENABLE);
                return dot;
            }
        };
    }

}
