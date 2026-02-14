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

import org.dynamisfx.shapes.primitives.helper.delaunay.jdt.Point;
import org.dynamisfx.geometry.Point3D;

/**
 * Conversion between Point and Point3D.
 *
 * FXyz uses Y axis for F(x,z) values
 * Delaunay uses regular z = F(x,y)
 * So y,z are switched
 */
public class DelaunayPointAdapter {

    public Point convertPoint3DtoDelaunay(Point3D point) {
        return new Point(point.getX(), point.getZ(), point.getY());
    }

    public Point3D convertPointFromDelaunay(Point coord) {
        return new Point3D((float) coord.getX(), (float) coord.getZ(), (float) coord.getY());
    }
}
