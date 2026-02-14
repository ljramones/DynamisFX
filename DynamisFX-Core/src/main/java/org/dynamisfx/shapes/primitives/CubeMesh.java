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
import javafx.beans.property.FloatProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;

/**
 *
 * @author Dub
 */
public class CubeMesh extends TexturedMesh {

    private final static double DEFAULT_SIZE = 10;

    public CubeMesh() {
        this(DEFAULT_SIZE);
    }

    public CubeMesh(double size) {
        setSize(size);
        setDepthTest(DepthTest.ENABLE);
        updateMesh();
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createCube((float)getSize());
        setMesh(mesh);
    }

    private TriangleMesh createCube(float size) {
        TriangleMesh m = new TriangleMesh();

        float hw = size / 2,
                hh = hw,
                hd = hh;

        //create points
        m.getPoints().addAll(
            hw, hh, hd,
            hw, hh, -hd,
            hw, -hh, hd,
            hw, -hh, -hd,
            -hw, hh, hd,
            -hw, hh, -hd,
            -hw, -hh, hd,
            -hw, -hh, -hd
        );
        float x0 = 0.0f, x1 = 1.0f / 4.0f, x2 = 2.0f / 4.0f, x3 =  3.0f / 4.0f, x4 = 1.0f;
        float y0 = 0.0f, y1 = 1.0f /3.0f, y2 = 2.0f / 3.0f, y3 = 1.0f;



        m.getTexCoords().addAll(
            (x1 + getImagePadding()), (y0 + getImagePadding()), //0,1
            (x2 - getImagePadding()), (y0 + getImagePadding()), //2,3
            (x0)                    , (y1 + getImagePadding()), //4,5
            (x1 + getImagePadding()), (y1 + getImagePadding()), //6,7
            (x2 - getImagePadding()), (y1 + getImagePadding()), //8,9
            (x3),                     (y1 + getImagePadding()), //10,11
            (x4),                     (y1 + getImagePadding()),  //12,13
            (x0),                     (y2 - getImagePadding()), //14,15
            (x1 + getImagePadding()), (y2 - getImagePadding()), //16,17
            (x2 - getImagePadding()), (y2 - getImagePadding()), //18,19
            (x3),                     (y2 - getImagePadding()), //20,21
            (x4),                     (y2 - getImagePadding()), //22,23
            (x1 + getImagePadding()), (y3 - getImagePadding()), //24,25
            (x2),                     (y3 - getImagePadding())  //26,27

        );


        m.getFaces().addAll(
            0, 10, 2, 5, 1, 9,
            2, 5, 3, 4, 1, 9,

            4, 7, 5, 8, 6, 2,
            6, 2, 5, 8, 7, 3,

            0, 13, 1, 9, 4, 12,
            4, 12, 1, 9, 5, 8,

            2, 1, 6, 0, 3, 4,
            3, 4, 6, 0, 7, 3,

            0, 10, 4, 11, 2, 5,
            2, 5, 4, 11, 6, 6,

            1, 9, 3, 4, 5, 8,
            5, 8, 3, 4, 7, 3
        );

        return m;
    }

    private final DoubleProperty size = MeshProperty.createDoubleUnguarded(
            DEFAULT_SIZE, this::updateMesh);

    public final double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    private final FloatProperty imagePadding = MeshProperty.createFloatUnguarded(
            0.0015f, this::updateMesh);

    public float getImagePadding() {
        return imagePadding.get();
    }

    public void setImagePadding(float value) {
        imagePadding.set(value);
    }

    public FloatProperty imagePaddingProperty() {
        return imagePadding;
    }


}
