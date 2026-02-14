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

package org.dynamisfx.utils;

import java.util.Arrays;
import java.util.List;
import org.dynamisfx.geometry.Point3D;

/**
 *
 * @author JosePereda
 */
public class DataBox {

    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;

    public DataBox(Point3D... ini) {
        this(Arrays.asList(ini));
    }

    public DataBox(List<Point3D> dataPoints) {
        reset();
        updateExtremes(dataPoints);
    }

    public static DataBox getDefaultDataBox() {
        return new DataBox(new Point3D(-50, -50, -50), new Point3D(50, 50, 50));
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    public double getSizeX() {
        return (maxX - minX);
    }

    public double getSizeY() {
        return (maxY - minY);
    }

    public double getSizeZ() {
        return (maxZ - minZ);
    }

    public double getCenterX() {
        return (maxX + minX) / 2d;
    }

    public double getCenterY() {
        return (maxY + minY) / 2d;
    }

    public double getCenterZ() {
        return (maxZ + minZ) / 2d;
    }

    public double getMaxSize() {
        return Math.max(Math.max(getSizeX(), getSizeY()), getSizeZ());
    }

    public final void reset() {
        minX = 0; minY = 0; minZ = 0;
        maxX = 1; maxY = 1; maxZ = 1;
    }

    public final void updateExtremes(List<Point3D> points) {
        double max = points.parallelStream()
                .mapToDouble(p -> p.x)
                .max()
                .orElse(1.0);
        setMaxX(Math.max(max, getMaxX()));
        max = points.parallelStream()
                .mapToDouble(p -> p.y)
                .max()
                .orElse(1.0);
        setMaxY(Math.max(max, getMaxY()));
        max = points.parallelStream()
                .mapToDouble(p -> p.z)
                .max()
                .orElse(1.0);
        setMaxZ(Math.max(max, getMaxZ()));
        double min = points.parallelStream()
                .mapToDouble(p -> p.x)
                .min()
                .orElse(0.0);
        setMinX(Math.min(min, getMinX()));
        min = points.parallelStream()
                .mapToDouble(p -> p.y)
                .min()
                .orElse(0.0);
        setMinY(Math.min(min, getMinY()));
        min = points.parallelStream()
                .mapToDouble(p -> p.z)
                .min()
                .orElse(0.0);
        setMinZ(Math.min(min, getMinZ()));
    }

    @Override
    public String toString() {
        return "DataBox{" + "minX=" + minX + ", minY=" + minY + ", minZ=" + minZ + ", maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ + '}';
    }

}
