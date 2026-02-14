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

package org.dynamisfx.shapes.primitives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dynamisfx.scene.paint.Patterns;
import org.dynamisfx.shapes.primitives.helper.MarkerFactory;
import org.dynamisfx.shapes.primitives.helper.MeshHelper;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;
import org.dynamisfx.shapes.primitives.helper.PaletteTextureManager;
import org.dynamisfx.shapes.primitives.helper.PositionUpdateCache;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.helper.TextureMode;
import org.dynamisfx.scene.paint.Palette.ColorPalette;

/**
 * A 3D scatter plot mesh that displays markers at specified positions.
 * <p>
 * ScatterMesh renders a collection of 3D points as geometric markers (tetrahedra, cubes,
 * octahedra, etc.) positioned in 3D space. It's commonly used for visualizing point cloud
 * data, molecular structures, or any dataset where individual points need distinct 3D
 * representations.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li><b>Multiple marker types</b> - tetrahedra, cube, octahedron, sphere, and more via {@link MarkerFactory.Marker}</li>
 *   <li><b>Per-particle coloring</b> - assign individual colors using palette textures and colorIndex</li>
 *   <li><b>Per-particle scaling</b> - vary marker sizes using the scale field of each Point3D</li>
 *   <li><b>Per-particle opacity</b> - control transparency per particle using the opacity field</li>
 *   <li><b>Efficient updates</b> - update positions without full mesh rebuilds using {@link #updatePositions(List)}</li>
 *   <li><b>Joined segments</b> - combine all markers into a single mesh for better performance</li>
 * </ul>
 * <p>
 * <b>Basic Usage:</b>
 * <pre>{@code
 * List<Point3D> points = Arrays.asList(
 *     new Point3D(0, 0, 0),
 *     new Point3D(10, 0, 0),
 *     new Point3D(0, 10, 0)
 * );
 * ScatterMesh scatter = new ScatterMesh(points, 1.0);
 * scatter.setTextureModeNone(Color.RED);
 * scene.getRoot().getChildren().add(scatter);
 * }</pre>
 * <p>
 * <b>Per-Particle Coloring:</b>
 * <pre>{@code
 * // Create points with color indices (0-255)
 * List<Point3D> points = Arrays.asList(
 *     new Point3D(0, 0, 0, 0),      // colorIndex = 0 (red)
 *     new Point3D(10, 0, 0, 128),   // colorIndex = 128 (middle)
 *     new Point3D(0, 10, 0, 255)    // colorIndex = 255 (blue)
 * );
 * scatter.enablePerParticleColor(Arrays.asList(Color.RED, Color.BLUE));
 * }</pre>
 *
 * @author José Pereda
 * @see MarkerFactory
 * @see Point3D
 * @see TextureMode
 */
public class ScatterMesh extends Group implements TextureMode {

    private final static List<Point3D> DEFAULT_SCATTER_DATA = Arrays.asList(new Point3D(0f,0f,0f),
            new Point3D(1f,1f,1f), new Point3D(2f,2f,2f));
    private final static double DEFAULT_HEIGHT = 0.1d;
    private final static int DEFAULT_LEVEL = 0;
    private final static boolean DEFAULT_JOIN_SEGMENTS = true;

    private ObservableList<TexturedMesh> meshes = null;

    // Helper classes for extracted functionality
    private final PaletteTextureManager paletteManager;
    private final PositionUpdateCache positionCache;

    /**
     * Creates a ScatterMesh with default sample data.
     * Uses three points at (0,0,0), (1,1,1), and (2,2,2) with default marker size.
     */
    public ScatterMesh(){
        this(DEFAULT_SCATTER_DATA, DEFAULT_JOIN_SEGMENTS, DEFAULT_HEIGHT, DEFAULT_LEVEL);
    }

    /**
     * Creates a ScatterMesh with the specified data points.
     *
     * @param scatterData list of 3D points to display as markers
     */
    public ScatterMesh(List<Point3D> scatterData){
        this(scatterData, DEFAULT_JOIN_SEGMENTS, DEFAULT_HEIGHT, DEFAULT_LEVEL);
    }

    /**
     * Creates a ScatterMesh with specified data and marker height.
     *
     * @param scatterData list of 3D points to display as markers
     * @param height the size of each marker
     */
    public ScatterMesh(List<Point3D> scatterData, double height){
        this(scatterData, DEFAULT_JOIN_SEGMENTS, height, DEFAULT_LEVEL);
    }

    /**
     * Creates a ScatterMesh with full control over all parameters.
     *
     * @param scatterData list of 3D points to display as markers
     * @param joinSegments if true, combines all markers into a single mesh for better performance
     * @param height the size of each marker
     * @param level subdivision level for marker geometry (0 = default)
     */
    public ScatterMesh(List<Point3D> scatterData, boolean joinSegments, double height, int level){
        // Initialize helper classes
        this.paletteManager = new PaletteTextureManager(() -> meshes, this::updateMesh);
        this.positionCache = new PositionUpdateCache();

        setScatterData(scatterData);
        setJoinSegments(joinSegments);
        setHeight(height);
        setLevel(level);

        idProperty().addListener(o -> updateMesh());
        updateMesh();
    }

    // ==================== Scatter Data Property ====================

    private final ObjectProperty<List<Point3D>> scatterData = MeshProperty.createObject(
            DEFAULT_SCATTER_DATA, () -> meshes, this::updateMesh);

    public List<Point3D> getScatterData() {
        return scatterData.get();
    }

    public final void setScatterData(List<Point3D> value) {
        scatterData.set(value);
    }

    public ObjectProperty<List<Point3D>> scatterDataProperty() {
        return scatterData;
    }

    // ==================== Function Data Property ====================

    private final ObjectProperty<List<Number>> functionData = new SimpleObjectProperty<List<Number>>(){
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateF(get());
            }
        }
    };

    public List<Number> getFunctionData() {
        return functionData.get();
    }

    public void setFunctionData(List<Number> value) {
        functionData.set(value);
    }

    public ObjectProperty<List<Number>> functionDataProperty() {
        return functionData;
    }

    // ==================== Marker Property ====================

    private final ObjectProperty<MarkerFactory.Marker> marker = MeshProperty.createObject(
            MarkerFactory.Marker.TETRAHEDRA, () -> meshes, this::updateMesh);

    public final ObjectProperty<MarkerFactory.Marker> markerProperty() {
        return marker;
    }

    public final MarkerFactory.Marker getMarker() {
        return marker.get();
    }

    public final void setMarker(MarkerFactory.Marker value) {
        marker.set(value);
    }

    // ==================== Height Property ====================

    private final DoubleProperty height = MeshProperty.createDouble(
            DEFAULT_HEIGHT, () -> meshes, this::updateMesh);

    public double getHeight() {
        return height.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    // ==================== Level Property ====================

    private final IntegerProperty level = MeshProperty.createInteger(
            DEFAULT_LEVEL, () -> meshes, this::updateMesh);

    public final int getLevel() {
        return level.get();
    }

    public final void setLevel(int value) {
        level.set(value);
    }

    public final IntegerProperty levelProperty() {
        return level;
    }

    // ==================== Join Segments Property ====================

    private final BooleanProperty joinSegments = new SimpleBooleanProperty(DEFAULT_JOIN_SEGMENTS){
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    public boolean isJoinSegments() {
        return joinSegments.get();
    }

    public final void setJoinSegments(boolean value) {
        joinSegments.set(value);
    }

    public BooleanProperty joinSegmentsProperty() {
        return joinSegments;
    }

    // ==================== CullFace Property ====================

    /**
     * CullFace property for controlling backface culling on all particle meshes.
     * Default is CullFace.NONE so particles are visible from all angles.
     * Set to CullFace.BACK if you want standard backface culling.
     */
    private final ObjectProperty<CullFace> cullFace = new SimpleObjectProperty<>(CullFace.NONE) {
        @Override
        protected void invalidated() {
            applyCullFace(get());
        }
    };

    public CullFace getCullFace() {
        return cullFace.get();
    }

    public void setCullFace(CullFace value) {
        cullFace.set(value);
    }

    public ObjectProperty<CullFace> cullFaceProperty() {
        return cullFace;
    }

    private void applyCullFace(CullFace cf) {
        if (meshes != null) {
            meshes.forEach(m -> m.setCullFace(cf));
        }
    }

    // ==================== Per-Particle Color (delegated to PaletteTextureManager) ====================

    public boolean isPerParticleColor() {
        return paletteManager.isPerParticleColor();
    }

    public void setPerParticleColor(boolean value) {
        paletteManager.setPerParticleColor(value);
    }

    public BooleanProperty perParticleColorProperty() {
        return paletteManager.perParticleColorProperty();
    }

    public void setColorPaletteColors(List<Color> colors) {
        paletteManager.setColorPaletteColors(colors);
        // If per-particle color is already enabled, rebuild the mesh
        if (paletteManager.isPerParticleColor() && meshes != null) {
            updateMesh();
        }
    }

    public List<Color> getColorPaletteColors() {
        return paletteManager.getColorPaletteColors();
    }

    public void enablePerParticleColor(List<Color> paletteColors) {
        paletteManager.enablePerParticleColor(paletteColors);
    }

    public void disablePerParticleColor() {
        paletteManager.disablePerParticleColor();
    }

    // ==================== Per-Particle Opacity (delegated to PaletteTextureManager) ====================

    public boolean isPerParticleOpacity() {
        return paletteManager.isPerParticleOpacity();
    }

    public void setPerParticleOpacity(boolean value) {
        paletteManager.setPerParticleOpacity(value);
    }

    public BooleanProperty perParticleOpacityProperty() {
        return paletteManager.perParticleOpacityProperty();
    }

    // ==================== Per-Particle Scale ====================

    private final BooleanProperty perParticleScale = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    public boolean isPerParticleScale() {
        return perParticleScale.get();
    }

    public void setPerParticleScale(boolean value) {
        perParticleScale.set(value);
    }

    public BooleanProperty perParticleScaleProperty() {
        return perParticleScale;
    }

    // ==================== Convenience Methods for Per-Particle Attributes ====================

    /**
     * Convenience method to enable per-particle color and scale with a palette.
     *
     * @param paletteColors the colors for the gradient palette (null to skip color)
     */
    public void enablePerParticleAttributes(List<Color> paletteColors) {
        if (paletteColors != null && !paletteColors.isEmpty()) {
            setColorPaletteColors(paletteColors);
            setPerParticleColor(true);
        }
        setPerParticleScale(true);
    }

    /**
     * Convenience method to enable all per-particle attributes: color, scale, and opacity.
     *
     * @param paletteColors the colors for the gradient palette (required for opacity)
     */
    public void enableAllPerParticleAttributes(List<Color> paletteColors) {
        if (paletteColors == null || paletteColors.isEmpty()) {
            throw new IllegalArgumentException("Color palette is required for opacity support");
        }
        setPerParticleOpacity(true);  // Set first so texture is created correctly
        setColorPaletteColors(paletteColors);
        setPerParticleColor(true);
        setPerParticleScale(true);
    }

    /**
     * Disables all per-particle attributes (color, scale, and opacity).
     */
    public void disablePerParticleAttributes() {
        setPerParticleColor(false);
        setPerParticleScale(false);
        setPerParticleOpacity(false);
    }

    // ==================== Efficient Position Updates (delegated to PositionUpdateCache) ====================

    /**
     * Updates particle positions efficiently without full mesh rebuild.
     *
     * @param newPositions the new positions for all particles (same count as original)
     * @return true if efficient update was used, false if full rebuild occurred
     */
    public boolean updatePositions(List<Point3D> newPositions) {
        // Validate preconditions for efficient update
        if (!joinSegments.get() || meshes == null || meshes.isEmpty()) {
            setScatterData(newPositions);
            return false;
        }

        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh == null) {
            setScatterData(newPositions);
            return false;
        }

        boolean result = positionCache.updatePositions(newPositions, mesh);
        if (!result) {
            // Fall back to full rebuild
            setScatterData(newPositions);
            return false;
        }

        // Update the property without triggering rebuild
        scatterData.set(newPositions);
        return true;
    }

    /**
     * Updates a single particle's position efficiently.
     *
     * @param particleIndex the index of the particle to update
     * @param newPosition the new position
     * @return true if update was successful, false if index out of bounds
     */
    public boolean updateParticlePosition(int particleIndex, Point3D newPosition) {
        if (!joinSegments.get() || meshes == null || meshes.isEmpty()) {
            return false;
        }

        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh == null) {
            return false;
        }

        return positionCache.updateParticlePosition(particleIndex, newPosition, mesh);
    }

    public int getVerticesPerMarker() {
        return positionCache.getVerticesPerMarker();
    }

    public int getParticleCount() {
        return positionCache.getParticleCount();
    }

    // ==================== Mesh Update Logic ====================

    protected final void updateMesh() {
        meshes = FXCollections.<TexturedMesh>observableArrayList();

        createMarkers();
        getChildren().setAll(meshes);
        applyCullFace(cullFace.get());
        updateTransforms();

        // Cache mesh info for efficient position updates
        cacheMeshInfo();
    }

    /**
     * Caches mesh info after creation for efficient updates.
     */
    private void cacheMeshInfo() {
        if (!joinSegments.get() || meshes == null || meshes.isEmpty()) {
            positionCache.clear();
            return;
        }

        List<Point3D> data = scatterData.get();
        if (data == null || data.isEmpty()) {
            positionCache.clear();
            return;
        }

        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh != null) {
            positionCache.cacheMeshInfo(data, mesh);
        }
    }

    private AtomicInteger index;

    private void createMarkers() {
        if (!joinSegments.get()) {
            List<TexturedMesh> markers = new ArrayList<>();
            index = new AtomicInteger();
            scatterData.get().forEach(point3d ->
                    markers.add(getMarker().getMarker(getId() + "-" + index.getAndIncrement(), height.get(), level.get(), point3d)));
            meshes.addAll(markers);
        } else if (paletteManager.isPerParticleColor() && paletteManager.getColorPaletteColors() != null) {
            // Per-particle color mode (also supports scale): use colorIndex from each Point3D
            createMarkersWithPerParticleColor();
        } else if (perParticleScale.get()) {
            // Per-particle scale mode (without color): use scale from each Point3D
            createMarkersWithPerParticleScale();
        } else {
            // Standard mode: single color and size for all particles
            AtomicInteger i = new AtomicInteger();
            List<Point3D> indexedData = scatterData.get().stream()
                    .map(p -> new Point3D(p.x, p.y, p.z, i.getAndIncrement()))
                    .collect(Collectors.toList());

            TexturedMesh marker = getMarker().getMarker(getId(), height.get(), level.get(), indexedData.get(0));
            MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());
            TexturedMesh dot1 = getMarker().getMarker("", height.get(), level.get(), null);
            MeshHelper mh1 = new MeshHelper((TriangleMesh) dot1.getMesh());
            mh.addMesh(mh1, indexedData.stream().skip(1).collect(Collectors.toList()));
            marker.updateMesh(mh);
            meshes.add(marker);
        }
    }

    /**
     * Creates markers with per-particle color support.
     */
    private void createMarkersWithPerParticleColor() {
        List<Point3D> data = scatterData.get();
        if (data.isEmpty()) {
            return;
        }

        Point3D firstPoint = data.get(0);
        TexturedMesh marker = getMarker().getMarker(getId(), height.get(), level.get(), firstPoint);
        TexturedMesh templateMarker = getMarker().getMarker("", height.get(), level.get(), null);
        MeshHelper template = new MeshHelper((TriangleMesh) templateMarker.getMesh());
        MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());

        float u = firstPoint.colorIndex / 255.0f;
        float v = paletteManager.isPerParticleOpacity() ? firstPoint.opacity : 0.5f;
        mh.setTexCoords(new float[]{u, v});

        int[] faces = mh.getFaces();
        for (int i = 1; i < faces.length; i += 2) {
            faces[i] = 0;
        }
        mh.setFaces(faces);

        if (data.size() > 1) {
            mh.addMeshWithColorIndex(template, data.subList(1, data.size()), paletteManager.isPerParticleOpacity());
        }

        marker.updateMesh(mh);
        meshes.add(marker);

        // Apply the palette texture
        applyPaletteTexture();
    }

    /**
     * Creates markers with per-particle scale support (without per-particle color).
     */
    private void createMarkersWithPerParticleScale() {
        List<Point3D> data = scatterData.get();
        if (data.isEmpty()) {
            return;
        }

        Point3D firstPoint = data.get(0);
        float firstScale = firstPoint.scale > 0 ? firstPoint.scale : 1.0f;
        TexturedMesh marker = getMarker().getMarker(getId(), height.get() * firstScale, level.get(), firstPoint);

        if (data.size() == 1) {
            meshes.add(marker);
            return;
        }

        TexturedMesh templateMarker = getMarker().getMarker("", height.get(), level.get(), null);
        MeshHelper template = new MeshHelper((TriangleMesh) templateMarker.getMesh());
        MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());
        mh.addMeshWithScale(template, data.subList(1, data.size()));

        marker.updateMesh(mh);
        meshes.add(marker);
    }

    /**
     * Applies the palette texture to all meshes.
     */
    private void applyPaletteTexture() {
        WritableImage paletteTexture = paletteManager.getPaletteTexture();
        if (paletteTexture != null && meshes != null) {
            meshes.forEach(m -> {
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseMap(paletteTexture);
                m.setMaterial(material);
            });
        }
    }

    // ==================== TextureMode Implementation ====================

    @Override
    public void setTextureModeNone() {
        meshes.stream().forEach(m -> m.setTextureModeNone());
    }

    @Override
    public void setTextureModeNone(Color color) {
        meshes.stream().forEach(m -> m.setTextureModeNone(color));
    }

    @Override
    public void setTextureModeNone(Color color, String image) {
        meshes.stream().forEach(m -> m.setTextureModeNone(color, image));
    }

    @Override
    public void setTextureModeImage(String image) {
        meshes.stream().forEach(m -> m.setTextureModeImage(image));
    }

    @Override
    public void setTextureModePattern(Patterns.CarbonPatterns pattern, double scale) {
        meshes.stream().forEach(m -> m.setTextureModePattern(pattern, scale));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m -> m.setTextureModeVertices3D(colors, dens));
    }

    @Override
    public void setTextureModeVertices3D(ColorPalette palette, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m -> m.setTextureModeVertices3D(palette, dens));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens, double min, double max) {
        meshes.stream().forEach(m -> m.setTextureModeVertices3D(colors, dens, min, max));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function) {
        meshes.stream().forEach(m -> m.setTextureModeVertices1D(colors, function));
    }

    @Override
    public void setTextureModeVertices1D(ColorPalette palette, Function<Number, Number> function) {
        meshes.stream().forEach(m -> m.setTextureModeVertices1D(palette, function));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function, double min, double max) {
        meshes.stream().forEach(m -> m.setTextureModeVertices1D(colors, function, min, max));
    }

    @Override
    public void setTextureModeFaces(int colors) {
        meshes.stream().forEach(m -> m.setTextureModeFaces(colors));
    }

    @Override
    public void setTextureModeFaces(ColorPalette palette) {
        meshes.stream().forEach(m -> m.setTextureModeFaces(palette));
    }

    @Override
    public void updateF(List<Number> values) {
        meshes.stream().forEach(m -> m.updateF(values));
    }

    @Override
    public void setTextureOpacity(double value) {
        meshes.stream().forEach(m -> m.setTextureOpacity(value));
    }

    // ==================== Other Public Methods ====================

    public void setDrawMode(DrawMode mode) {
        meshes.stream().forEach(m -> m.setDrawMode(mode));
    }

    private void updateTransforms() {
        meshes.stream().forEach(m -> m.updateTransforms());
    }

    public TexturedMesh getMeshFromId(String id) {
        return meshes.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(meshes.get(0));
    }

    public Color getDiffuseColor() {
        return meshes.stream()
                .findFirst()
                .map(m -> m.getDiffuseColor())
                .orElse(TriangleMeshHelper.DEFAULT_DIFFUSE_COLOR);
    }
}
