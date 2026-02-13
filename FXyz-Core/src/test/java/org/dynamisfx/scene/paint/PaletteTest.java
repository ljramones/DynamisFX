/*
 * F(X)yz
 *
 * Copyright (c) 2013-2021, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
