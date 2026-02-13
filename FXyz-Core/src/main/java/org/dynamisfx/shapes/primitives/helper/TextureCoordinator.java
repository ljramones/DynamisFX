/**
 * TextureCoordinator.java
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

import java.util.List;
import java.util.function.Function;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Face3;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.paint.Palette.ColorPalette;
import org.dynamisfx.scene.paint.Patterns.CarbonPatterns;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.TextureType;

import static org.dynamisfx.scene.paint.Palette.DEFAULT_COLOR_PALETTE;
import static org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.*;

/**
 * Coordinates texture mode changes and manages texture-related properties for {@link TexturedMesh}.
 * <p>
 * This helper class encapsulates all texture mode logic including:
 * <ul>
 *   <li>Texture type management (none, image, pattern, colored vertices/faces)</li>
 *   <li>Color palette creation and application</li>
 *   <li>Density and function mapping for vertex coloring</li>
 *   <li>Pattern scaling for carbon fiber and similar textures</li>
 * </ul>
 * <p>
 * The coordinator uses a {@link MeshStateProvider} to access mesh state from the parent
 * class without tight coupling, enabling cleaner separation of concerns.
 * <p>
 * <b>Example usage:</b>
 * <pre>{@code
 * TextureCoordinator coordinator = new TextureCoordinator(helper, stateProvider, updateCallback);
 *
 * // Apply a density-based coloring with 100 colors
 * coordinator.setTextureModeVertices3D(100, point -> point.y);
 *
 * // Or use a color palette for face coloring
 * coordinator.setTextureModeFaces(ColorPalette.RAINBOW);
 * }</pre>
 *
 * @author FXyz contributors
 * @see TexturedMesh
 * @see TriangleMeshHelper
 */
public class TextureCoordinator {

    /**
     * Provides access to mesh state from the parent {@link TexturedMesh}.
     * <p>
     * This interface decouples the TextureCoordinator from the concrete TexturedMesh
     * implementation, allowing the coordinator to read mesh state and apply materials
     * without direct field access.
     */
    public interface MeshStateProvider {
        /**
         * Gets the current triangle mesh.
         * @return the mesh, or null if not yet created
         */
        TriangleMesh getMesh();

        /**
         * Gets the list of vertices in the mesh.
         * @return the vertex list
         */
        List<Point3D> getListVertices();

        /**
         * Gets the list of face indices.
         * @return the face list
         */
        List<Face3> getListFaces();

        /**
         * Gets the list of texture coordinate indices for faces.
         * @return the texture index list
         */
        List<Face3> getListTextures();

        /**
         * Gets the texture coordinates array.
         * @return the texture coordinates
         */
        float[] getTextureCoords();

        /**
         * Gets the rectangle defining mesh texture dimensions.
         * @return the rect mesh bounds
         */
        Rectangle getRectMesh();

        /**
         * Gets the rectangle defining mesh area for pattern scaling.
         * @return the area mesh bounds
         */
        Rectangle getAreaMesh();

        /**
         * Sets the material on the mesh view.
         * @param material the material to apply
         */
        void setMaterial(javafx.scene.paint.Material material);
    }

    private final TriangleMeshHelper helper;
    private final MeshStateProvider stateProvider;
    private final Runnable onTextureTypeChanged;

    // ==================== Properties ====================

    private final ObjectProperty<TextureType> textureType = new SimpleObjectProperty<>(TextureType.NONE);

    private final DoubleProperty patternScale = new SimpleDoubleProperty(DEFAULT_PATTERN_SCALE);

    private final IntegerProperty colors = new SimpleIntegerProperty(DEFAULT_COLORS) {
        @Override
        protected void invalidated() {
            createPalette(get());
        }
    };

    private final ObjectProperty<ColorPalette> colorPalette = new SimpleObjectProperty<>(DEFAULT_COLOR_PALETTE);

    private final ObjectProperty<CarbonPatterns> carbonPatterns = new SimpleObjectProperty<>(DEFAULT_PATTERN) {
        @Override
        protected void invalidated() {
            helper.getMaterialWithPattern(get());
        }
    };

    private final ObjectProperty<Function<Point3D, Number>> density =
            new SimpleObjectProperty<>(DEFAULT_DENSITY_FUNCTION);

    private final ObjectProperty<Function<Number, Number>> function =
            new SimpleObjectProperty<>(DEFAULT_UNIDIM_FUNCTION);

    private final DoubleProperty minGlobal = new SimpleDoubleProperty();
    private final DoubleProperty maxGlobal = new SimpleDoubleProperty();

    /**
     * Creates a new TextureCoordinator.
     *
     * @param helper the TriangleMeshHelper for texture operations
     * @param stateProvider provider for mesh state access
     * @param onTextureTypeChanged callback when texture type changes (for updateTexture/updateTextureOnFaces)
     */
    public TextureCoordinator(TriangleMeshHelper helper, MeshStateProvider stateProvider,
                              Runnable onTextureTypeChanged) {
        this.helper = helper;
        this.stateProvider = stateProvider;
        this.onTextureTypeChanged = onTextureTypeChanged;

        // Wire up property invalidation
        textureType.addListener((obs, oldVal, newVal) -> {
            if (stateProvider.getMesh() != null) {
                onTextureTypeChanged.run();
            }
        });

        patternScale.addListener((obs, oldVal, newVal) -> updateTexture());

        density.addListener((obs, oldVal, newVal) -> {
            helper.setDensity(newVal);
            updateTextureOnFaces();
        });

        function.addListener((obs, oldVal, newVal) -> {
            helper.setFunction(newVal);
            updateTextureOnFaces();
        });

        colorPalette.addListener((obs, oldVal, newVal) -> {
            createPalette(newVal.getNumColors());
            updateTexture();
            updateTextureOnFaces();
        });
    }

    // ==================== Texture Mode Methods ====================

    /**
     * Sets the texture mode to none with a white diffuse color.
     * The mesh will be rendered as a solid white surface.
     */
    public void setTextureModeNone() {
        setTextureModeNone(Color.WHITE);
    }

    /**
     * Sets the texture mode to none with a specified diffuse color.
     * The mesh will be rendered as a solid colored surface.
     *
     * @param color the diffuse color to apply
     */
    public void setTextureModeNone(Color color) {
        if (color != null) {
            helper.setTextureType(TextureType.NONE);
            helper.getMaterialWithColor(color);
        }
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to none with a diffuse color and bump map image.
     *
     * @param color the diffuse color
     * @param image path to the bump map image
     */
    public void setTextureModeNone(Color color, String image) {
        if (color != null) {
            helper.setTextureType(TextureType.NONE);
            stateProvider.setMaterial(helper.getMaterialWithColor(color, image));
        }
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to use an image texture.
     * The image will be mapped to the mesh using its UV coordinates.
     *
     * @param image path to the texture image
     */
    public void setTextureModeImage(String image) {
        if (image != null && !image.isEmpty()) {
            helper.setTextureType(TextureType.IMAGE);
            helper.getMaterialWithImage(image);
            textureType.set(helper.getTextureType());
        }
    }

    /**
     * Sets the texture mode to use a carbon fiber or similar pattern.
     *
     * @param pattern the pattern type (e.g., DARK_CARBON, LIGHT_CARBON)
     * @param scale the pattern scale factor (1.0 = default size)
     */
    public void setTextureModePattern(CarbonPatterns pattern, double scale) {
        helper.setTextureType(TextureType.PATTERN);
        patternScale.set(scale);
        carbonPatterns.set(pattern);
        helper.getMaterialWithPattern(pattern);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color vertices based on a 3D density function.
     * Each vertex is colored according to the function value at its position.
     *
     * @param numColors number of colors in the generated palette
     * @param dens density function mapping Point3D to a numeric value
     */
    public void setTextureModeVertices3D(int numColors, Function<Point3D, Number> dens) {
        helper.setTextureType(TextureType.COLORED_VERTICES_3D);
        colors.set(numColors);
        createPalette(numColors);
        density.set(dens);
        helper.setDensity(dens);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color vertices based on a 3D density function using a predefined palette.
     *
     * @param palette the color palette to use
     * @param dens density function mapping Point3D to a numeric value
     */
    public void setTextureModeVertices3D(ColorPalette palette, Function<Point3D, Number> dens) {
        helper.setTextureType(TextureType.COLORED_VERTICES_3D);
        colorPalette.set(palette);
        createPalette(palette.getNumColors());
        density.set(dens);
        helper.setDensity(dens);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color vertices based on a 3D density function with explicit range.
     * Values are clamped to [min, max] before mapping to colors.
     *
     * @param numColors number of colors in the generated palette
     * @param dens density function mapping Point3D to a numeric value
     * @param min minimum value for color mapping
     * @param max maximum value for color mapping
     */
    public void setTextureModeVertices3D(int numColors, Function<Point3D, Number> dens, double min, double max) {
        helper.setTextureType(TextureType.COLORED_VERTICES_3D);
        boolean refresh = min != minGlobal.get() || max != maxGlobal.get();
        minGlobal.set(min);
        maxGlobal.set(max);
        colors.set(numColors);
        createPalette(numColors);
        density.set(dens);
        helper.setDensity(dens);
        textureType.set(helper.getTextureType());
        if (refresh) {
            updateTexture();
            updateTextureOnFaces();
        }
    }

    /**
     * Sets the texture mode to color vertices based on a 1D function of the vertex's f-value.
     * Useful for coloring based on a single parameter stored in each vertex.
     *
     * @param numColors number of colors in the generated palette
     * @param func function mapping the vertex f-value to a color index
     */
    public void setTextureModeVertices1D(int numColors, Function<Number, Number> func) {
        helper.setTextureType(TextureType.COLORED_VERTICES_1D);
        colors.set(numColors);
        createPalette(numColors);
        function.set(func);
        helper.setFunction(func);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color vertices based on a 1D function using a predefined palette.
     *
     * @param palette the color palette to use
     * @param func function mapping the vertex f-value to a color index
     */
    public void setTextureModeVertices1D(ColorPalette palette, Function<Number, Number> func) {
        helper.setTextureType(TextureType.COLORED_VERTICES_1D);
        colorPalette.set(palette);
        createPalette(palette.getNumColors());
        function.set(func);
        helper.setFunction(func);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color vertices based on a 1D function with explicit range.
     *
     * @param numColors number of colors in the generated palette
     * @param func function mapping the vertex f-value to a color index
     * @param min minimum value for color mapping
     * @param max maximum value for color mapping
     */
    public void setTextureModeVertices1D(int numColors, Function<Number, Number> func, double min, double max) {
        helper.setTextureType(TextureType.COLORED_VERTICES_1D);
        minGlobal.set(min);
        maxGlobal.set(max);
        colors.set(numColors);
        createPalette(numColors);
        function.set(func);
        helper.setFunction(func);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color each face with a different color from the palette.
     * Faces are colored sequentially using modulo arithmetic if there are more faces than colors.
     *
     * @param numColors number of colors in the generated palette
     */
    public void setTextureModeFaces(int numColors) {
        helper.setTextureType(TextureType.COLORED_FACES);
        colors.set(numColors);
        createPalette(numColors);
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture mode to color each face using a predefined palette.
     *
     * @param palette the color palette to use
     */
    public void setTextureModeFaces(ColorPalette palette) {
        helper.setTextureType(TextureType.COLORED_FACES);
        colorPalette.set(palette);
        createPalette(palette.getNumColors());
        textureType.set(helper.getTextureType());
    }

    /**
     * Sets the texture opacity (transparency level).
     *
     * @param value opacity value from 0.0 (transparent) to 1.0 (opaque)
     */
    public void setTextureOpacity(double value) {
        helper.setTextureOpacity(value);
    }

    // ==================== Internal Methods ====================

    /**
     * Creates a color palette with the specified number of colors.
     */
    private void createPalette(int numColors) {
        helper.createPalette(numColors, false, colorPalette.get());
        helper.getMaterialWithPalette();
    }

    /**
     * Updates texture coordinates based on the current texture type.
     */
    public void updateTexture() {
        TriangleMesh mesh = stateProvider.getMesh();
        if (mesh == null) {
            return;
        }

        Rectangle rectMesh = stateProvider.getRectMesh();
        Rectangle areaMesh = stateProvider.getAreaMesh();

        switch (textureType.get()) {
            case NONE:
                mesh.getTexCoords().setAll(0f, 0f);
                break;
            case IMAGE:
                mesh.getTexCoords().setAll(stateProvider.getTextureCoords());
                break;
            case PATTERN:
                if (areaMesh.getHeight() > 0 && areaMesh.getWidth() > 0) {
                    mesh.getTexCoords().setAll(
                            helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                                    (int) rectMesh.getHeight(), patternScale.get(),
                                    areaMesh.getHeight() / areaMesh.getWidth()));
                } else {
                    mesh.getTexCoords().setAll(
                            helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                                    (int) rectMesh.getHeight(), patternScale.get()));
                }
                break;
            case COLORED_VERTICES_1D:
            case COLORED_VERTICES_3D:
            case COLORED_FACES:
                mesh.getTexCoords().setAll(helper.getTexturePaletteArray());
                break;
        }
    }

    /**
     * Updates face texture indices based on the current texture type.
     */
    public void updateTextureOnFaces() {
        TriangleMesh mesh = stateProvider.getMesh();
        if (mesh == null) {
            return;
        }

        List<Point3D> listVertices = stateProvider.getListVertices();
        List<Face3> listFaces = stateProvider.getListFaces();
        List<Face3> listTextures = stateProvider.getListTextures();

        switch (textureType.get()) {
            case NONE:
                mesh.getFaces().setAll(helper.updateFacesWithoutTexture(listFaces));
                break;
            case IMAGE:
                if (listTextures.size() > 0) {
                    mesh.getFaces().setAll(helper.updateFacesWithTextures(listFaces, listTextures));
                } else {
                    mesh.getFaces().setAll(helper.updateFacesWithVertices(listFaces));
                }
                break;
            case PATTERN:
                mesh.getFaces().setAll(helper.updateFacesWithTextures(listFaces, listTextures));
                break;
            case COLORED_VERTICES_1D:
                if (minGlobal.get() < maxGlobal.get()) {
                    mesh.getFaces().setAll(helper.updateFacesWithFunctionMap(listVertices, listFaces,
                            minGlobal.get(), maxGlobal.get()));
                } else {
                    mesh.getFaces().setAll(helper.updateFacesWithFunctionMap(listVertices, listFaces));
                }
                break;
            case COLORED_VERTICES_3D:
                if (minGlobal.get() < maxGlobal.get()) {
                    mesh.getFaces().setAll(helper.updateFacesWithDensityMap(listVertices, listFaces,
                            minGlobal.get(), maxGlobal.get()));
                } else {
                    mesh.getFaces().setAll(helper.updateFacesWithDensityMap(listVertices, listFaces));
                }
                break;
            case COLORED_FACES:
                mesh.getFaces().setAll(helper.updateFacesWithFaces(listFaces));
                break;
        }
    }

    // ==================== Property Accessors ====================

    public TextureType getTextureType() {
        return textureType.get();
    }

    public void setTextureType(TextureType value) {
        textureType.set(value);
    }

    public ObjectProperty<TextureType> textureTypeProperty() {
        return textureType;
    }

    public double getPatternScale() {
        return patternScale.get();
    }

    public void setPatternScale(double value) {
        patternScale.set(value);
    }

    public DoubleProperty patternScaleProperty() {
        return patternScale;
    }

    public int getColors() {
        return colors.get();
    }

    public void setColors(int value) {
        colors.set(value);
    }

    public IntegerProperty colorsProperty() {
        return colors;
    }

    public ColorPalette getColorPalette() {
        return colorPalette.get();
    }

    public void setColorPalette(ColorPalette value) {
        colorPalette.set(value);
    }

    public ObjectProperty<ColorPalette> colorPaletteProperty() {
        return colorPalette;
    }

    public CarbonPatterns getCarbonPattern() {
        return carbonPatterns.get();
    }

    public void setCarbonPattern(CarbonPatterns value) {
        carbonPatterns.set(value);
    }

    public ObjectProperty<CarbonPatterns> carbonPatternsProperty() {
        return carbonPatterns;
    }

    public Function<Point3D, Number> getDensity() {
        return density.get();
    }

    public void setDensity(Function<Point3D, Number> value) {
        density.set(value);
    }

    public ObjectProperty<Function<Point3D, Number>> densityProperty() {
        return density;
    }

    public Function<Number, Number> getFunction() {
        return function.get();
    }

    public void setFunction(Function<Number, Number> value) {
        function.set(value);
    }

    public ObjectProperty<Function<Number, Number>> functionProperty() {
        return function;
    }

    public double getMinGlobal() {
        return minGlobal.get();
    }

    public void setMinGlobal(double value) {
        minGlobal.set(value);
    }

    public DoubleProperty minGlobalProperty() {
        return minGlobal;
    }

    public double getMaxGlobal() {
        return maxGlobal.get();
    }

    public void setMaxGlobal(double value) {
        maxGlobal.set(value);
    }

    public DoubleProperty maxGlobalProperty() {
        return maxGlobal;
    }

    /**
     * Gets the TriangleMeshHelper used by this coordinator.
     *
     * @return the helper instance
     */
    public TriangleMeshHelper getHelper() {
        return helper;
    }
}
