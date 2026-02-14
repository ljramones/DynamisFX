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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.selection.SelectableGroup3D;
import org.dynamisfx.shapes.composites.PolyLine3D;

/**
 *
 * @author sphillips
 */
public class Crosshair3D extends SelectableGroup3D {
    
    public double size;
    public Point3D centerPoint;
    public int lineWidth;
    public PolyLine3D xPositivePoly;
    public PolyLine3D xNegativePoly;
    public PolyLine3D yPositivePoly;
    public PolyLine3D yNegativePoly;
    public PolyLine3D zPositivePoly;
    public PolyLine3D zNegativePoly;
    public Color xPositiveColor = Color.ALICEBLUE;
    public Color xNegativeColor = Color.ALICEBLUE;
    public Color yPositiveColor = Color.ALICEBLUE;
    public Color yNegativeColor = Color.ALICEBLUE;
    public Color zPositiveColor = Color.ALICEBLUE;
    public Color zNegativeColor = Color.ALICEBLUE;
    
    public Crosshair3D(Point3D centerPoint, double size, int lineWidth) {
        this.centerPoint = centerPoint;
        this.size = size;
        this.lineWidth = lineWidth;

        setCenter(centerPoint);
    }
    /**
     * Recreates and sets the location of the 3D crosshair with the origin 
     * at the newCenter. If the crosshair must be moved frequently it may be
     * more efficient to update the translate of the group itself.
     * @param newCenter origin for new Crosshair 
     */
    public void setCenter(Point3D newCenter) {
        this.centerPoint = newCenter;
        //remove the current polylines so they detach and get GC'd
        getChildren().clear();
        //create set of polylines that create a 3D crosshair
        float half = Double.valueOf(size/2.0f).floatValue();
        float startPointX = centerPoint.x;
        float startPointY = centerPoint.y;
        float startPointZ = centerPoint.z;

        //x Axis - Positive direction from centerPoint
        List<Point3D> xPositiveData = new ArrayList<>();
        xPositiveData.add(new Point3D(startPointX, startPointY, startPointZ));
        xPositiveData.add(new Point3D(half, startPointY, startPointZ));
        xPositivePoly = new PolyLine3D(xPositiveData, Float.valueOf(lineWidth), xPositiveColor, PolyLine3D.LineType.TRIANGLE);
        //x Axis - Negative direction from centerPoint
        List<Point3D> xNegativeData = new ArrayList<>();
        xNegativeData.add(new Point3D(startPointX, startPointY, startPointZ));
        xNegativeData.add(new Point3D(-half, startPointY, startPointZ));
        xNegativePoly = new PolyLine3D(xNegativeData, Float.valueOf(lineWidth), xNegativeColor, PolyLine3D.LineType.TRIANGLE);

        //y Axis - Positive direction from centerPoint
        List<Point3D> yPositiveData = new ArrayList<>();
        yPositiveData.add(new Point3D(startPointX, startPointY, startPointZ));
        yPositiveData.add(new Point3D(startPointX, half, startPointZ));
        yPositivePoly = new PolyLine3D(yPositiveData, Float.valueOf(lineWidth), yPositiveColor, PolyLine3D.LineType.TRIANGLE);
        //y Axis - Negative direction from centerPoint
        List<Point3D> yNegativeData = new ArrayList<>();
        yNegativeData.add(new Point3D(startPointX, startPointY, startPointZ));
        yNegativeData.add(new Point3D(startPointX, -half, startPointZ));
        yNegativePoly = new PolyLine3D(yNegativeData, Float.valueOf(lineWidth), yNegativeColor, PolyLine3D.LineType.TRIANGLE);

        //z Axis - Positive direction from centerPoint
        List<Point3D> zPositiveData = new ArrayList<>();
        zPositiveData.add(new Point3D(startPointX, startPointY, startPointZ));
        zPositiveData.add(new Point3D(startPointX, startPointY, half));
        zPositivePoly = new PolyLine3D(zPositiveData, Float.valueOf(lineWidth), zPositiveColor, PolyLine3D.LineType.TRIANGLE);
        //z Axis - Negative direction from centerPoint
        List<Point3D> zNegativeData = new ArrayList<>();
        zNegativeData.add(new Point3D(startPointX, startPointY, startPointZ));
        zNegativeData.add(new Point3D(startPointX, startPointY, -half));
        zNegativePoly = new PolyLine3D(zNegativeData, Float.valueOf(lineWidth), zNegativeColor, PolyLine3D.LineType.TRIANGLE);        
        getChildren().addAll(
            xPositivePoly, xNegativePoly, 
            yPositivePoly, yNegativePoly, 
            zPositivePoly, zNegativePoly);
    }
}
