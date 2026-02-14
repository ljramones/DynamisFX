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

package org.dynamisfx.shapes.primitives.helper.delaunay;

import org.dynamisfx.geometry.Point3D;

/**
 * a triangle consisting of three data points.
 * Holds the result of delaunay triangulation
 */
public class Triangle3D {

    private final Point3D p0, p1, p2;

    private Triangle3D(Point3D p0, Point3D p1, Point3D p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public static Triangle3D of(Point3D p1, Point3D p2, Point3D p3) {
        return new Triangle3D(p1, p2, p3);
    }

    public Point3D getP0() {
        return p0;
    }

    public Point3D getP1() {
        return p1;
    }

    public Point3D getP2() {
        return p2;
    }

}
