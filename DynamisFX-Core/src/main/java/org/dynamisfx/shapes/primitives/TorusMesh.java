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

package org.dynamisfx.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 *
 * @author jDub1581
 */
public class TorusMesh extends TexturedMesh {

    private static final int DEFAULT_DIVISIONS = 64;
    private static final int DEFAULT_T_DIVISIONS = 64;
    private static final double DEFAULT_RADIUS = 12.5;
    private static final double DEFAULT_T_RADIUS = 5.0;
    private static final double DEFAULT_START_ANGLE = 0.0;
    private static final double DEFAULT_X_OFFSET = 0.0;
    private static final double DEFAULT_Y_OFFSET = 0.0;
    private static final double DEFAULT_Z_OFFSET = 1.0;

    public TorusMesh() {
        this(DEFAULT_DIVISIONS, DEFAULT_T_DIVISIONS, DEFAULT_RADIUS, DEFAULT_T_RADIUS);
    }

    public TorusMesh(double radius, double tubeRadius) {
        this(DEFAULT_DIVISIONS, DEFAULT_T_DIVISIONS, radius, tubeRadius);
    }

    public TorusMesh(int radiusDivisions, int tubeDivisions, double radius, double tubeRadius) {
        setRadiusDivisions(radiusDivisions);
        setTubeDivisions(tubeDivisions);
        setRadius(radius);
        setTubeRadius(tubeRadius);

        setDepthTest(DepthTest.ENABLE);
        updateMesh();
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createTorusMesh(
                getRadiusDivisions(),
                getTubeDivisions(),
                (float) getRadius(),
                (float) getTubeRadius(),
                (float) getTubeStartAngleOffset(),
                (float) getxOffset(),
                (float) getyOffset(),
                (float) getzOffset()
        );
        setMesh(mesh);
    }

    private TriangleMesh createTorusMesh(
            final int radiusDivisions,
            final int tubeDivisions,
            final float radius,
            final float tRadius,
            final float tubeStartAngle,
            final float xOffset,
            final float yOffset,
            final float zOffset) {

        final int numVerts = tubeDivisions * radiusDivisions;
        int faceCount = numVerts * 2;

        float[] points = new float[numVerts * 3];
        float[] texCoords = new float[numVerts * 2];
        int[] faces = new int[faceCount * 6];

        int pointIndex = 0;
        int texIndex = 0;
        int faceIndex = 0;

        float tubeFraction = 1.0f / tubeDivisions;
        float radiusFraction = 1.0f / radiusDivisions;

        float TWO_PI = (float) (2 * Math.PI);

        // create points
        // create texCoords
        for (int tubeIndex = 0; tubeIndex < tubeDivisions; tubeIndex++) {

            float radian = tubeStartAngle + tubeFraction * tubeIndex * TWO_PI;

            for (int radiusIndex = 0; radiusIndex < radiusDivisions; radiusIndex++) {

                float localRadian = radiusFraction * radiusIndex * TWO_PI;

                points[pointIndex + 0] = (radius + tRadius * ((float) cos(radian))) * ((float) cos(localRadian) + xOffset);
                points[pointIndex + 1] = (radius + tRadius * ((float) cos(radian))) * ((float) sin(localRadian) + yOffset);
                points[pointIndex + 2] = (tRadius * (float) sin(radian) * zOffset);

                pointIndex += 3;

                float r = radiusIndex < tubeDivisions ? tubeFraction * radiusIndex * TWO_PI : 0.0f;
                texCoords[texIndex + 0] = (float) (sin(r) * 0.5) + 0.5f;
                texCoords[texIndex + 1] = (float) (cos(r) * 0.5) + 0.5f;

                texIndex += 2;
            }
        }

        //create faces
        for (int point = 0; point < tubeDivisions; point++) {
            for (int crossSection = 0; crossSection < radiusDivisions; crossSection++) {

                final int p0 = point * radiusDivisions + crossSection;

                int p1 = p0 >= 0 ? p0 + 1 : p0 - radiusDivisions;
                p1 = p1 % radiusDivisions != 0 ? p0 + 1 : p0 + 1 - radiusDivisions;

                final int p0r = p0 + radiusDivisions;

                final int p2 = p0r < numVerts ? p0r : p0r - numVerts;

                int p3 = p2 < (numVerts - 1) ? p2 + 1 : p2 + 1 - numVerts;
                p3 = p3 % radiusDivisions != 0 ? p2 + 1 : p2 + 1 - radiusDivisions;

                faces[faceIndex + 0] = p2;
                faces[faceIndex + 1] = p3;
                faces[faceIndex + 2] = p0;
                faces[faceIndex + 3] = p2;
                faces[faceIndex + 4] = p1;
                faces[faceIndex + 5] = p0;

                faceIndex += 6;

                faces[faceIndex + 0] = p2;
                faces[faceIndex + 1] = p3;
                faces[faceIndex + 2] = p1;
                faces[faceIndex + 3] = p0;
                faces[faceIndex + 4] = p3;
                faces[faceIndex + 5] = p1;

                faceIndex += 6;
            }
        }

        TriangleMesh m = new TriangleMesh();
        m.getPoints().setAll(points);
        m.getTexCoords().setAll(texCoords);
        m.getFaces().setAll(faces);

        return m;
    }

    private final IntegerProperty radiusDivisions = MeshProperty.createIntegerUnguarded(
            DEFAULT_DIVISIONS, this::updateMesh);

    public final int getRadiusDivisions() {
        return radiusDivisions.get();
    }

    public final void setRadiusDivisions(int value) {
        radiusDivisions.set(value);
    }

    public IntegerProperty radiusDivisionsProperty() {
        return radiusDivisions;
    }

    private final IntegerProperty tubeDivisions = MeshProperty.createIntegerUnguarded(
            DEFAULT_T_DIVISIONS, this::updateMesh);

    public final int getTubeDivisions() {
        return tubeDivisions.get();
    }

    public final void setTubeDivisions(int value) {
        tubeDivisions.set(value);
    }

    public IntegerProperty tubeDivisionsProperty() {
        return tubeDivisions;
    }

    private final DoubleProperty radius = MeshProperty.createDoubleUnguarded(
            DEFAULT_RADIUS, this::updateMesh);

    public final double getRadius() {
        return radius.get();
    }

    public final void setRadius(double value) {
        radius.set(value);
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    private final DoubleProperty tubeRadius = MeshProperty.createDoubleUnguarded(
            DEFAULT_T_RADIUS, this::updateMesh);

    public final double getTubeRadius() {
        return tubeRadius.get();
    }

    public final void setTubeRadius(double value) {
        tubeRadius.set(value);
    }

    public DoubleProperty tubeRadiusProperty() {
        return tubeRadius;
    }

    private final DoubleProperty tubeStartAngleOffset = MeshProperty.createDoubleUnguarded(
            DEFAULT_START_ANGLE, this::updateMesh);

    public final double getTubeStartAngleOffset() {
        return tubeStartAngleOffset.get();
    }

    public void setTubeStartAngleOffset(double value) {
        tubeStartAngleOffset.set(value);
    }

    public DoubleProperty tubeStartAngleOffsetProperty() {
        return tubeStartAngleOffset;
    }

    private final DoubleProperty xOffset = MeshProperty.createDoubleUnguarded(
            DEFAULT_X_OFFSET, this::updateMesh);

    public final double getxOffset() {
        return xOffset.get();
    }

    public void setxOffset(double value) {
        xOffset.set(value);
    }

    public DoubleProperty xOffsetProperty() {
        return xOffset;
    }

    private final DoubleProperty yOffset = MeshProperty.createDoubleUnguarded(
            DEFAULT_Y_OFFSET, this::updateMesh);

    public final double getyOffset() {
        return yOffset.get();
    }

    public void setyOffset(double value) {
        yOffset.set(value);
    }

    public DoubleProperty yOffsetProperty() {
        return yOffset;
    }

    private final DoubleProperty zOffset = MeshProperty.createDoubleUnguarded(
            DEFAULT_Z_OFFSET, this::updateMesh);

    public final double getzOffset() {
        return zOffset.get();
    }

    public void setzOffset(double value) {
        zOffset.set(value);
    }

    public DoubleProperty zOffsetProperty() {
        return zOffset;
    }
}
