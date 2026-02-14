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

package org.dynamisfx.scene.paint;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PaletteTest {

    @Test
    @DisplayName("ListColorPalette returns correct colors")
    public void testListColorPalette() {
        Palette.ListColorPalette palette = new Palette.ListColorPalette(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(palette.getNumColors(), is(3));
        assertThat(palette.getColor(0), is(Color.RED));
        assertThat(palette.getColor(1), is(Color.GREEN));
        assertThat(palette.getColor(2), is(Color.BLUE));
    }

    @Test
    @DisplayName("ListColorPalette clamps out of bounds index")
    public void testListColorPaletteClamp() {
        Palette.ListColorPalette palette = new Palette.ListColorPalette(Color.RED, Color.BLUE);

        assertThat(palette.getColor(-1), is(Color.RED));
        assertThat(palette.getColor(100), is(Color.BLUE));
    }

    @Test
    @DisplayName("FunctionColorPalette generates colors")
    public void testFunctionColorPalette() {
        Palette.FunctionColorPalette palette = new Palette.FunctionColorPalette(100, d -> Color.hsb(360 * d, 1, 1));

        assertThat(palette.getNumColors(), is(100));
        assertThat(palette.getColor(0), is(notNullValue()));
        assertThat(palette.getColor(50), is(notNullValue()));
    }

    @Test
    @DisplayName("Default palette is available")
    public void testDefaultPalette() {
        assertThat(Palette.DEFAULT_COLOR_PALETTE, is(notNullValue()));
        assertThat(Palette.DEFAULT_COLOR_PALETTE.getNumColors(), greaterThan(0));
    }

    @Test
    @DisplayName("ColorPalette getLinearGradient works")
    public void testLinearGradient() {
        Palette.ListColorPalette palette = new Palette.ListColorPalette(Color.RED, Color.BLUE);

        assertThat(palette.getLinearGradient(), is(notNullValue()));
    }

    @Test
    @DisplayName("Palette constructor works")
    public void testPaletteConstruction() {
        Palette palette = new Palette();

        assertThat(palette, is(notNullValue()));
    }

    @Test
    @DisplayName("Palette with numColors works")
    public void testPaletteWithNumColors() {
        Palette palette = new Palette(256);

        assertThat(palette, is(notNullValue()));
    }
}
