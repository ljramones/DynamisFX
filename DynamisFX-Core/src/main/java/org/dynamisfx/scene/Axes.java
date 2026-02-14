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

package org.dynamisfx.scene;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.dynamisfx.scene.selection.SelectableGroup3D;

/**
 *
 * @author jpereda
 */
public class Axes extends SelectableGroup3D {

    private final Cylinder axisX;
    private final Cylinder axisY;
    private final Cylinder axisZ;

    public Axes() {
        this(1);
    }

    public Axes(double scale) {
        axisX = new Cylinder(3, 60);
        axisY = new Cylinder(3, 60);
        axisZ = new Cylinder(3, 60);
        axisX.getTransforms().addAll(new Rotate(-90, Rotate.Z_AXIS), new Translate(0, 30, 0));
        axisX.setMaterial(new PhongMaterial(Color.RED));
        axisY.getTransforms().add(new Translate(0, 30, 0));
        axisY.setMaterial(new PhongMaterial(Color.GREEN));
        axisZ.setMaterial(new PhongMaterial(Color.BLUE));
        axisZ.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(0, 30, 0));
        getChildren().addAll(axisX, axisY, axisZ);
        getTransforms().add(new Scale(scale, scale, scale));
    }

    public void setHeight(double equalHeights) {
        double oldHeight = axisX.getHeight();
        axisX.setHeight(equalHeights);
        axisX.getTransforms().add(new Translate(0, (equalHeights/2.0)-(oldHeight/2.0), 0));
        axisY.setHeight(equalHeights);
        axisY.getTransforms().add(new Translate(0, (equalHeights/2.0)-(oldHeight/2.0), 0));
        axisZ.setHeight(equalHeights);
        axisZ.getTransforms().add(new Translate(0,(equalHeights/2.0)-(oldHeight/2.0), 0));
    }

    public void setRadius(double equalRadius) {
        axisX.setRadius(equalRadius);
        axisY.setRadius(equalRadius);
        axisZ.setRadius(equalRadius);
    }
}
