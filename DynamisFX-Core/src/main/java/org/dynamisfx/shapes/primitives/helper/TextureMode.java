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

import java.util.List;
import java.util.function.Function;

import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.paint.Patterns;
import javafx.scene.paint.Color;
import org.dynamisfx.scene.paint.Palette;

/**
 *
 * @author jpereda
 */
public interface TextureMode {

    void setTextureModeNone();
    void setTextureModeNone(Color color);
    void setTextureModeNone(Color color, String image);
    void setTextureModeImage(String image);
    void setTextureModePattern(Patterns.CarbonPatterns pattern, double scale);
    void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens);
    void setTextureModeVertices3D(Palette.ColorPalette palette, Function<Point3D, Number> dens);
    void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens, double min, double max);
    void setTextureModeVertices1D(int colors, Function<Number, Number> function);
    void setTextureModeVertices1D(Palette.ColorPalette palette, Function<Number, Number> function);
    void setTextureModeVertices1D(int colors, Function<Number, Number> function, double min, double max);
    void setTextureModeFaces(int colors);
    void setTextureModeFaces(Palette.ColorPalette palette);
    void setTextureOpacity(double value);

    void updateF(List<Number> values);
}
