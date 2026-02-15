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
 */

package org.dynamisfx.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;

/**
 * Three perpendicular quads (XY, XZ, YZ planes) forming a cross shape.
 * <p>
 * This is the ideal marker for volumetric/atmospheric particles (fog, clouds, smoke).
 * Unlike a single flat quad, the cross shape ensures visible faces from all viewing
 * angles. Combined with a soft radial gradient texture and self-illumination,
 * overlapping cross-quads create convincing volumetric effects.
 * <p>
 * Geometry: 12 vertices, 6 triangles, 4 tex coords (shared across all 3 quads).
 * Each quad maps UV (0,0)-(1,1) so the same blob texture appears on every face.
 */
public class CrossQuadMesh extends TexturedMesh {

    private static final double DEFAULT_SIZE = 1.0;
    private static final Point3D DEFAULT_CENTER = new Point3D(0f, 0f, 0f);

    public CrossQuadMesh() {
        this(DEFAULT_SIZE, null);
    }

    public CrossQuadMesh(double size) {
        this(size, null);
    }

    public CrossQuadMesh(double size, Point3D center) {
        setSize(size);
        setCenter(center);

        updateMesh();
        setCullFace(CullFace.NONE);
        setDrawMode(DrawMode.FILL);
        setDepthTest(DepthTest.ENABLE);
    }

    private final DoubleProperty size = new SimpleDoubleProperty(DEFAULT_SIZE) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    private final ObjectProperty<Point3D> center = new SimpleObjectProperty<>(DEFAULT_CENTER) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public Point3D getCenter() {
        return center.get();
    }

    public final void setCenter(Point3D value) {
        center.set(value);
    }

    public ObjectProperty<Point3D> centerProperty() {
        return center;
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createCrossQuad((float) getSize());
        setMesh(mesh);
    }

    private TriangleMesh createCrossQuad(float size) {
        TriangleMesh mesh = new TriangleMesh();

        float h = size / 2f;
        float cx = center.get() != null ? center.get().x : 0f;
        float cy = center.get() != null ? center.get().y : 0f;
        float cz = center.get() != null ? center.get().z : 0f;

        // 12 vertices: 4 per quad, 3 quads (XY, XZ, YZ planes)
        mesh.getPoints().addAll(
                // Quad 1: XY plane (faces +Z/-Z)
                cx - h, cy - h, cz,    // 0
                cx + h, cy - h, cz,    // 1
                cx + h, cy + h, cz,    // 2
                cx - h, cy + h, cz,    // 3
                // Quad 2: XZ plane (faces +Y/-Y)
                cx - h, cy, cz - h,    // 4
                cx + h, cy, cz - h,    // 5
                cx + h, cy, cz + h,    // 6
                cx - h, cy, cz + h,    // 7
                // Quad 3: YZ plane (faces +X/-X)
                cx, cy - h, cz - h,    // 8
                cx, cy - h, cz + h,    // 9
                cx, cy + h, cz + h,    // 10
                cx, cy + h, cz - h     // 11
        );

        // 4 shared tex coords (all quads map the same blob texture)
        mesh.getTexCoords().addAll(
                0f, 1f,    // 0: bottom-left
                1f, 1f,    // 1: bottom-right
                1f, 0f,    // 2: top-right
                0f, 0f     // 3: top-left
        );

        // 6 triangles (2 per quad)
        mesh.getFaces().addAll(
                // Quad 1: XY plane
                0, 0, 1, 1, 2, 2,
                0, 0, 2, 2, 3, 3,
                // Quad 2: XZ plane
                4, 0, 5, 1, 6, 2,
                4, 0, 6, 2, 7, 3,
                // Quad 3: YZ plane
                8, 0, 9, 1, 10, 2,
                8, 0, 10, 2, 11, 3
        );

        // All faces in same smoothing group
        mesh.getFaceSmoothingGroups().addAll(1, 1, 1, 1, 1, 1);

        return mesh;
    }
}
