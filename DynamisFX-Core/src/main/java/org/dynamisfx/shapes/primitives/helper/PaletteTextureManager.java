/**
 * PaletteTextureManager.java
 *
 * Copyright (c) 2013-2019, F(X)yz
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

package org.dynamisfx.shapes.primitives.helper;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Helper class for managing per-particle color and opacity via palette textures.
 * <p>
 * This class handles:
 * <ul>
 *   <li>Color palette management (interpolated 256-entry palettes)</li>
 *   <li>Palette texture generation (256x1 for color only, 256x256 for color+opacity)</li>
 *   <li>Per-particle color and opacity property state</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>{@code
 * PaletteTextureManager palette = new PaletteTextureManager(meshSupplier, updateAction);
 * palette.setColorPaletteColors(Arrays.asList(Color.RED, Color.BLUE));
 * palette.setPerParticleColor(true);
 * WritableImage texture = palette.getPaletteTexture();
 * }</pre>
 *
 * @author FXyz contributors
 */
public class PaletteTextureManager {

    /**
     * The color palette for per-particle coloring.
     * Colors are mapped to indices 0-255 based on their position in the list.
     */
    private List<Color> colorPaletteColors = null;

    /**
     * The generated palette texture.
     * When opacity support is disabled: 256x1 pixels (color only).
     * When opacity support is enabled: 256x256 pixels (color x opacity).
     */
    private WritableImage paletteTexture = null;

    /**
     * Whether per-particle coloring is enabled.
     * When true, each particle's colorIndex field is used to look up its color
     * from the color palette.
     */
    private final BooleanProperty perParticleColor;

    /**
     * Whether per-particle opacity is enabled.
     * When true, each particle's opacity field is used to set its transparency.
     */
    private final BooleanProperty perParticleOpacity;

    /**
     * Creates a new PaletteTextureManager with mesh-guarded property updates.
     *
     * @param meshSupplier supplier that returns the mesh object (used for null check)
     * @param updateAction the action to run when properties change and mesh exists
     */
    public PaletteTextureManager(java.util.function.Supplier<Object> meshSupplier, Runnable updateAction) {
        this.perParticleColor = new SimpleBooleanProperty(false) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    updateAction.run();
                }
            }
        };

        this.perParticleOpacity = new SimpleBooleanProperty(false) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    // Rebuild palette texture with opacity support
                    if (colorPaletteColors != null) {
                        paletteTexture = createPaletteTexture(colorPaletteColors);
                        if (perParticleColor.get()) {
                            updateAction.run();
                        }
                    }
                }
            }
        };
    }

    /**
     * Gets whether per-particle coloring is enabled.
     * @return true if per-particle coloring is enabled
     */
    public boolean isPerParticleColor() {
        return perParticleColor.get();
    }

    /**
     * Enables or disables per-particle coloring.
     * When enabled, each Point3D's colorIndex field determines its color from the palette.
     * @param value true to enable per-particle coloring
     */
    public void setPerParticleColor(boolean value) {
        perParticleColor.set(value);
    }

    /**
     * Property for per-particle color mode.
     * @return the perParticleColor property
     */
    public BooleanProperty perParticleColorProperty() {
        return perParticleColor;
    }

    /**
     * Gets whether per-particle opacity is enabled.
     * @return true if per-particle opacity is enabled
     */
    public boolean isPerParticleOpacity() {
        return perParticleOpacity.get();
    }

    /**
     * Enables or disables per-particle opacity.
     * When enabled, each Point3D's opacity field (0.0-1.0) determines its transparency.
     * @param value true to enable per-particle opacity
     */
    public void setPerParticleOpacity(boolean value) {
        perParticleOpacity.set(value);
    }

    /**
     * Property for per-particle opacity mode.
     * @return the perParticleOpacity property
     */
    public BooleanProperty perParticleOpacityProperty() {
        return perParticleOpacity;
    }

    /**
     * Sets the color palette for per-particle coloring.
     * <p>
     * The palette colors are interpolated to fill 256 entries. For example,
     * a 2-color palette creates a gradient from color 0 to color 1.
     * <p>
     * After setting the palette, particles can use colorIndex 0-255 to select colors.
     *
     * @param colors the list of colors for the palette (at least 1 color required)
     * @throws IllegalArgumentException if colors is null or empty
     */
    public void setColorPaletteColors(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("Color palette must have at least one color");
        }
        this.colorPaletteColors = new ArrayList<>(colors);
        this.paletteTexture = createPaletteTexture(colors);
    }

    /**
     * Gets the current color palette.
     * @return the list of palette colors, or null if not set
     */
    public List<Color> getColorPaletteColors() {
        return colorPaletteColors;
    }

    /**
     * Gets the generated palette texture.
     * @return the palette texture, or null if no palette has been set
     */
    public WritableImage getPaletteTexture() {
        return paletteTexture;
    }

    /**
     * Creates a palette texture from the given colors.
     * <p>
     * When per-particle opacity is disabled: Creates a 256x1 texture (color only).
     * When per-particle opacity is enabled: Creates a 256x256 texture where:
     * <ul>
     *   <li>X (U coord) = color index (0-255)</li>
     *   <li>Y (V coord) = opacity level (row 0 = transparent, row 255 = opaque)</li>
     * </ul>
     *
     * @param colors the source colors
     * @return the palette texture
     */
    private WritableImage createPaletteTexture(List<Color> colors) {
        if (perParticleOpacity.get()) {
            return createPaletteTextureWithOpacity(colors);
        } else {
            return createPaletteTextureColorOnly(colors);
        }
    }

    /**
     * Creates a 256x1 pixel palette texture (color only, no opacity support).
     */
    private WritableImage createPaletteTextureColorOnly(List<Color> colors) {
        WritableImage img = new WritableImage(256, 1);
        PixelWriter pw = img.getPixelWriter();

        for (int i = 0; i < 256; i++) {
            Color c = interpolateColor(colors, i);
            pw.setColor(i, 0, c);
        }

        return img;
    }

    /**
     * Creates a 256x256 pixel palette texture with opacity support.
     * X dimension = color index (0-255)
     * Y dimension = opacity level (0 = transparent, 255 = opaque)
     */
    private WritableImage createPaletteTextureWithOpacity(List<Color> colors) {
        WritableImage img = new WritableImage(256, 256);
        PixelWriter pw = img.getPixelWriter();

        for (int x = 0; x < 256; x++) {
            // Get the base color for this column
            Color baseColor = interpolateColor(colors, x);

            for (int y = 0; y < 256; y++) {
                // y = 0 is top of texture (v = 0) = transparent
                // y = 255 is bottom of texture (v = 1) = opaque
                // So opacity = y / 255.0
                double alpha = y / 255.0;
                Color c = new Color(
                        baseColor.getRed(),
                        baseColor.getGreen(),
                        baseColor.getBlue(),
                        alpha
                );
                pw.setColor(x, y, c);
            }
        }

        return img;
    }

    /**
     * Interpolates a color from the palette at the given index (0-255).
     */
    private Color interpolateColor(List<Color> colors, int index) {
        if (colors.size() == 1) {
            return colors.get(0);
        }
        // Interpolate between colors
        double t = index / 255.0;
        double scaledIndex = t * (colors.size() - 1);
        int lowerIdx = (int) Math.floor(scaledIndex);
        int upperIdx = Math.min(lowerIdx + 1, colors.size() - 1);
        double fraction = scaledIndex - lowerIdx;
        return colors.get(lowerIdx).interpolate(colors.get(upperIdx), fraction);
    }

    /**
     * Convenience method to enable per-particle coloring with a specified palette.
     * <p>
     * Example usage:
     * <pre>{@code
     * palette.enablePerParticleColor(Arrays.asList(
     *     Color.RED,      // colorIndex 0-85 → red to orange
     *     Color.ORANGE,   // colorIndex 86-170 → orange to yellow
     *     Color.YELLOW    // colorIndex 171-255 → yellow
     * ));
     * }</pre>
     *
     * @param paletteColors the colors for the gradient palette
     */
    public void enablePerParticleColor(List<Color> paletteColors) {
        setColorPaletteColors(paletteColors);
        setPerParticleColor(true);
    }

    /**
     * Disables per-particle coloring.
     */
    public void disablePerParticleColor() {
        setPerParticleColor(false);
    }
}
