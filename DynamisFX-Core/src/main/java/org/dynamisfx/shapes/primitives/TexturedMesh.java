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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.dynamisfx.geometry.Face3;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.helper.MeshHelper;
import org.dynamisfx.shapes.primitives.helper.TextureCoordinator;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.SectionType;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.TextureType;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Callback;
import org.dynamisfx.shapes.primitives.helper.TextureMode;
import org.dynamisfx.scene.paint.Palette.ColorPalette;
import org.dynamisfx.scene.paint.Patterns.CarbonPatterns;

import static org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.*;

/**
 * Abstract base class for 3D meshes with advanced texture and material support.
 * <p>
 * TexturedMesh extends {@link MeshView} to provide a rich texturing system that supports
 * multiple rendering modes. It serves as the foundation for all FXyz 3D primitives
 * (spheres, tori, springs, surfaces, etc.) and handles the complexity of mapping
 * textures and colors to arbitrary 3D geometry.
 * <p>
 * <b>Texture Modes:</b>
 * <ul>
 *   <li><b>None</b> - solid color rendering with optional bump mapping</li>
 *   <li><b>Image</b> - texture mapping using an image file</li>
 *   <li><b>Pattern</b> - procedural patterns like carbon fiber</li>
 *   <li><b>Colored Vertices (3D)</b> - vertex colors based on a density function f(x,y,z)</li>
 *   <li><b>Colored Vertices (1D)</b> - vertex colors based on a function of stored f-values</li>
 *   <li><b>Colored Faces</b> - each face gets a distinct color from the palette</li>
 * </ul>
 * <p>
 * <b>For Subclass Implementors:</b>
 * <p>
 * Subclasses must implement {@link #updateMesh()} to generate the 3D geometry.
 * The protected fields {@code listVertices}, {@code listFaces}, and {@code listTextures}
 * should be populated with geometry data before calling {@link #createMesh()}.
 * <p>
 * Simple subclasses can directly create a {@link TriangleMesh} and assign it to {@code mesh}.
 * More complex subclasses should use the helper methods:
 * <ul>
 *   <li>{@link #createTexCoords(int, int)} - generate UV coordinates</li>
 *   <li>{@link #createMesh()} - build TriangleMesh from listVertices/listFaces</li>
 *   <li>{@link #precreateMesh()} - prepare a MeshHelper for mesh combination</li>
 * </ul>
 * <p>
 * <b>Example Usage:</b>
 * <pre>{@code
 * SpheroidMesh sphere = new SpheroidMesh(50);
 *
 * // Solid color
 * sphere.setTextureModeNone(Color.BLUE);
 *
 * // Image texture
 * sphere.setTextureModeImage("earth.jpg");
 *
 * // Heat map coloring based on Y position
 * sphere.setTextureModeVertices3D(100, p -> p.y);
 * }</pre>
 *
 * @author José Pereda
 * @see TextureMode
 * @see TriangleMeshHelper
 * @see TextureCoordinator
 */
public abstract class TexturedMesh extends MeshView implements TextureMode {

    // ==================== Core Mesh State ====================

    private final TriangleMeshHelper helper = new TriangleMeshHelper();
    private final TextureCoordinator textureCoordinator;
    protected TriangleMesh mesh;

    protected List<Point3D> listVertices = new ArrayList<>();
    protected final List<Face3> listTextures = new ArrayList<>();
    protected final List<Face3> listFaces = new ArrayList<>();
    protected float[] textureCoords;
    protected int[] smoothingGroups;

    protected final Rectangle rectMesh = new Rectangle(0, 0);
    protected final Rectangle areaMesh = new Rectangle(0, 0);

    // ==================== Transform State ====================

    protected Rotate rotateX, rotateY, rotateZ;
    protected Translate translate;
    protected Scale scale;

    // ==================== Constructor ====================

    protected TexturedMesh() {
        setMaterial(helper.getMaterial());

        // Initialize texture coordinator with state provider
        textureCoordinator = new TextureCoordinator(helper, new TextureCoordinator.MeshStateProvider() {
            @Override
            public TriangleMesh getMesh() {
                return mesh;
            }

            @Override
            public List<Point3D> getListVertices() {
                return listVertices;
            }

            @Override
            public List<Face3> getListFaces() {
                return listFaces;
            }

            @Override
            public List<Face3> getListTextures() {
                return listTextures;
            }

            @Override
            public float[] getTextureCoords() {
                return textureCoords;
            }

            @Override
            public Rectangle getRectMesh() {
                return rectMesh;
            }

            @Override
            public Rectangle getAreaMesh() {
                return areaMesh;
            }

            @Override
            public void setMaterial(Material material) {
                TexturedMesh.this.setMaterial(material);
            }
        }, this::onTextureTypeChanged);
    }

    /**
     * Called when texture type changes to update texture coordinates and faces.
     */
    private void onTextureTypeChanged() {
        textureCoordinator.updateTexture();
        textureCoordinator.updateTextureOnFaces();
    }

    // ==================== Section Type Property ====================

    private final ObjectProperty<SectionType> sectionType = new SimpleObjectProperty<SectionType>(SectionType.CIRCLE) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public SectionType getSectionType() {
        return sectionType.get();
    }

    public void setSectionType(SectionType value) {
        sectionType.set(value);
    }

    public ObjectProperty<SectionType> sectionTypeProperty() {
        return sectionType;
    }

    // ==================== TextureMode Implementation (delegated) ====================

    @Override
    public void setTextureModeNone() {
        textureCoordinator.setTextureModeNone();
    }

    @Override
    public void setTextureModeNone(Color color) {
        textureCoordinator.setTextureModeNone(color);
        setDiffuseColor(color);
    }

    @Override
    public void setTextureModeNone(Color color, String image) {
        textureCoordinator.setTextureModeNone(color, image);
    }

    @Override
    public void setTextureModeImage(String image) {
        textureCoordinator.setTextureModeImage(image);
    }

    @Override
    public void setTextureModePattern(CarbonPatterns pattern, double scale) {
        textureCoordinator.setTextureModePattern(pattern, scale);
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens) {
        textureCoordinator.setTextureModeVertices3D(colors, dens);
    }

    @Override
    public void setTextureModeVertices3D(ColorPalette palette, Function<Point3D, Number> dens) {
        textureCoordinator.setTextureModeVertices3D(palette, dens);
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens, double min, double max) {
        textureCoordinator.setTextureModeVertices3D(colors, dens, min, max);
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function) {
        textureCoordinator.setTextureModeVertices1D(colors, function);
    }

    @Override
    public void setTextureModeVertices1D(ColorPalette palette, Function<Number, Number> function) {
        textureCoordinator.setTextureModeVertices1D(palette, function);
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function, double min, double max) {
        textureCoordinator.setTextureModeVertices1D(colors, function, min, max);
    }

    @Override
    public void setTextureModeFaces(int colors) {
        textureCoordinator.setTextureModeFaces(colors);
    }

    @Override
    public void setTextureModeFaces(ColorPalette palette) {
        textureCoordinator.setTextureModeFaces(palette);
    }

    @Override
    public void updateF(List<Number> values) {
        listVertices = IntStream.range(0, values.size()).mapToObj(i -> {
            Point3D p = listVertices.get(i);
            p.f = values.get(i).floatValue();
            return p;
        }).collect(Collectors.toList());
        textureCoordinator.updateTextureOnFaces();
    }

    @Override
    public void setTextureOpacity(double value) {
        textureCoordinator.setTextureOpacity(value);
    }

    // ==================== Texture Property Accessors (delegated) ====================

    public TextureType getTextureType() {
        return textureCoordinator.getTextureType();
    }

    public void setTextureType(TextureType value) {
        textureCoordinator.setTextureType(value);
    }

    public ObjectProperty<TextureType> textureTypeProperty() {
        return textureCoordinator.textureTypeProperty();
    }

    public final double getPatternScale() {
        return textureCoordinator.getPatternScale();
    }

    public final void setPatternScale(double scale) {
        textureCoordinator.setPatternScale(scale);
    }

    public DoubleProperty patternScaleProperty() {
        return textureCoordinator.patternScaleProperty();
    }

    public final int getColors() {
        return textureCoordinator.getColors();
    }

    public final void setColors(int value) {
        textureCoordinator.setColors(value);
    }

    public IntegerProperty colorsProperty() {
        return textureCoordinator.colorsProperty();
    }

    public ColorPalette getColorPalette() {
        return textureCoordinator.getColorPalette();
    }

    public final void setColorPalette(ColorPalette value) {
        textureCoordinator.setColorPalette(value);
    }

    public ObjectProperty<ColorPalette> colorPaletteProperty() {
        return textureCoordinator.colorPaletteProperty();
    }

    public final CarbonPatterns getCarbonPattern() {
        return textureCoordinator.getCarbonPattern();
    }

    public final void setCarbonPattern(CarbonPatterns cp) {
        textureCoordinator.setCarbonPattern(cp);
    }

    public ObjectProperty<CarbonPatterns> carbonPatternsProperty() {
        return textureCoordinator.carbonPatternsProperty();
    }

    public final Function<Point3D, Number> getDensity() {
        return textureCoordinator.getDensity();
    }

    public final void setDensity(Function<Point3D, Number> value) {
        textureCoordinator.setDensity(value);
    }

    public final ObjectProperty<Function<Point3D, Number>> densityProperty() {
        return textureCoordinator.densityProperty();
    }

    public Function<Number, Number> getFunction() {
        return textureCoordinator.getFunction();
    }

    public void setFunction(Function<Number, Number> value) {
        textureCoordinator.setFunction(value);
    }

    public ObjectProperty<Function<Number, Number>> functionProperty() {
        return textureCoordinator.functionProperty();
    }

    public double getMinGlobal() {
        return textureCoordinator.getMinGlobal();
    }

    public void setMinGlobal(double value) {
        textureCoordinator.setMinGlobal(value);
    }

    public DoubleProperty minGlobalProperty() {
        return textureCoordinator.minGlobalProperty();
    }

    public double getMaxGlobal() {
        return textureCoordinator.getMaxGlobal();
    }

    public void setMaxGlobal(double value) {
        textureCoordinator.setMaxGlobal(value);
    }

    public DoubleProperty maxGlobalProperty() {
        return textureCoordinator.maxGlobalProperty();
    }

    // ==================== Diffuse Color Property ====================

    private final ObjectProperty<Color> diffuseColor = new SimpleObjectProperty<Color>(DEFAULT_DIFFUSE_COLOR) {
        @Override
        protected void invalidated() {
            updateMaterial();
        }
    };

    public Color getDiffuseColor() {
        return diffuseColor.get();
    }

    public void setDiffuseColor(Color value) {
        diffuseColor.set(value);
    }

    public ObjectProperty<Color> diffuseColorProperty() {
        return diffuseColor;
    }

    public void updateMaterial() {
        helper.getMaterialWithColor(diffuseColor.get());
    }

    // ==================== Mesh Operations ====================

    protected abstract void updateMesh();

    protected void updateMesh(MeshHelper meshHelper) {
        setMesh(null);
        mesh = createMesh(meshHelper);
        setMesh(mesh);
    }

    public void updateVertices(float factor) {
        if (mesh != null) {
            mesh.getPoints().setAll(helper.updateVertices(listVertices, factor));
        }
    }

    protected void createTexCoords(int width, int height) {
        rectMesh.setWidth(width);
        rectMesh.setHeight(height);
        textureCoords = helper.createTexCoords(width, height);
    }

    protected void createReverseTexCoords(int width, int height) {
        rectMesh.setWidth(width);
        rectMesh.setHeight(height);
        textureCoords = helper.createReverseTexCoords(width, height);
    }

    protected MeshHelper precreateMesh() {
        MeshHelper mh = new MeshHelper();
        mh.setPoints(helper.updateVertices(listVertices));

        switch (getTextureType()) {
            case NONE:
                mh.setTexCoords(textureCoords);
                mh.setFaces(helper.updateFacesWithTextures(listFaces, listTextures));
                break;
            case PATTERN:
                if (areaMesh.getHeight() > 0 && areaMesh.getWidth() > 0) {
                    mh.setTexCoords(helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                            (int) rectMesh.getHeight(), getPatternScale(),
                            areaMesh.getHeight() / areaMesh.getWidth()));
                } else {
                    mh.setTexCoords(helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                            (int) rectMesh.getHeight(), getPatternScale()));
                }
                mh.setFaces(helper.updateFacesWithTextures(listFaces, listTextures));
                break;
            case IMAGE:
                mh.setTexCoords(textureCoords);
                if (listTextures.size() > 0) {
                    mh.setFaces(helper.updateFacesWithTextures(listFaces, listTextures));
                } else {
                    mh.setFaces(helper.updateFacesWithVertices(listFaces));
                }
                break;
            case COLORED_VERTICES_1D:
                mh.setTexCoords(helper.getTexturePaletteArray());
                mh.setFaces(helper.updateFacesWithFunctionMap(listVertices, listFaces));
                break;
            case COLORED_VERTICES_3D:
                mh.setTexCoords(helper.getTexturePaletteArray());
                mh.setFaces(helper.updateFacesWithDensityMap(listVertices, listFaces));
                break;
            case COLORED_FACES:
                mh.setTexCoords(helper.getTexturePaletteArray());
                mh.setFaces(helper.updateFacesWithFaces(listFaces));
                break;
        }

        int[] faceSmoothingGroups = new int[listFaces.size()];
        Arrays.fill(faceSmoothingGroups, 1);
        if (smoothingGroups != null) {
            mh.setFaceSmoothingGroups(smoothingGroups);
        } else {
            mh.setFaceSmoothingGroups(faceSmoothingGroups);
        }

        return mh;
    }

    protected TriangleMesh createMesh(MeshHelper mh) {
        float[] points0 = mh.getPoints();
        float[] f = mh.getF();
        listVertices.clear();
        listVertices.addAll(IntStream.range(0, points0.length / 3)
                .mapToObj(i -> new Point3D(points0[3 * i], points0[3 * i + 1], points0[3 * i + 2], f[i]))
                .collect(Collectors.toList()));

        textureCoords = mh.getTexCoords();

        int[] faces0 = mh.getFaces();
        listFaces.clear();
        listFaces.addAll(IntStream.range(0, faces0.length / 6)
                .mapToObj(i -> new Face3(faces0[6 * i], faces0[6 * i + 2], faces0[6 * i + 4]))
                .collect(Collectors.toList()));

        listTextures.clear();
        listTextures.addAll(IntStream.range(0, faces0.length / 6)
                .mapToObj(i -> new Face3(faces0[6 * i + 1], faces0[6 * i + 3], faces0[6 * i + 5]))
                .collect(Collectors.toList()));

        smoothingGroups = mh.getFaceSmoothingGroups();

        return createMesh();
    }

    protected TriangleMesh createMesh() {
        TriangleMesh triangleMesh = new TriangleMesh();
        triangleMesh.getPoints().setAll(helper.updateVertices(listVertices));

        switch (getTextureType()) {
            case NONE:
                triangleMesh.getTexCoords().setAll(textureCoords);
                triangleMesh.getFaces().setAll(helper.updateFacesWithTextures(listFaces, listTextures));
                break;
            case PATTERN:
                if (areaMesh.getHeight() > 0 && areaMesh.getWidth() > 0) {
                    triangleMesh.getTexCoords().setAll(
                            helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                                    (int) rectMesh.getHeight(), getPatternScale(),
                                    areaMesh.getHeight() / areaMesh.getWidth()));
                } else {
                    triangleMesh.getTexCoords().setAll(
                            helper.updateTexCoordsWithPattern((int) rectMesh.getWidth(),
                                    (int) rectMesh.getHeight(), getPatternScale()));
                }
                triangleMesh.getFaces().setAll(helper.updateFacesWithTextures(listFaces, listTextures));
                break;
            case IMAGE:
                triangleMesh.getTexCoords().setAll(textureCoords);
                if (listTextures.size() > 0) {
                    triangleMesh.getFaces().setAll(helper.updateFacesWithTextures(listFaces, listTextures));
                } else {
                    triangleMesh.getFaces().setAll(helper.updateFacesWithVertices(listFaces));
                }
                break;
            case COLORED_VERTICES_1D:
                triangleMesh.getTexCoords().setAll(helper.getTexturePaletteArray());
                triangleMesh.getFaces().setAll(helper.updateFacesWithFunctionMap(listVertices, listFaces));
                break;
            case COLORED_VERTICES_3D:
                triangleMesh.getTexCoords().setAll(helper.getTexturePaletteArray());
                triangleMesh.getFaces().setAll(helper.updateFacesWithDensityMap(listVertices, listFaces));
                break;
            case COLORED_FACES:
                triangleMesh.getTexCoords().setAll(helper.getTexturePaletteArray());
                triangleMesh.getFaces().setAll(helper.updateFacesWithFaces(listFaces));
                break;
        }

        int[] faceSmoothingGroups = new int[listFaces.size()];
        Arrays.fill(faceSmoothingGroups, 1);
        if (smoothingGroups != null) {
            triangleMesh.getFaceSmoothingGroups().addAll(smoothingGroups);
        } else {
            triangleMesh.getFaceSmoothingGroups().addAll(faceSmoothingGroups);
        }

        vertCountBinding.invalidate();
        faceCountBinding.invalidate();

        return triangleMesh;
    }

    // ==================== Transform Operations ====================

    protected void updateTransforms() {
        getTransforms().removeAll(rotateX, rotateY, rotateZ, scale);
        Bounds bounds = getBoundsInLocal();
        javafx.geometry.Point3D p = new javafx.geometry.Point3D(
                (bounds.getMaxX() + bounds.getMinX()) / 2d,
                (bounds.getMaxY() + bounds.getMinY()) / 2d,
                (bounds.getMaxZ() + bounds.getMinZ()) / 2d);
        translate = new Translate(0, 0, 0);
        rotateX = new Rotate(0, p.getX(), p.getY(), p.getZ(), Rotate.X_AXIS);
        rotateY = new Rotate(0, p.getX(), p.getY(), p.getZ(), Rotate.Y_AXIS);
        rotateZ = new Rotate(0, p.getX(), p.getY(), p.getZ(), Rotate.Z_AXIS);
        scale = new Scale(1, 1, 1, p.getX(), p.getY(), p.getZ());
        getTransforms().addAll(translate, rotateZ, rotateY, rotateX, scale);
    }

    public Translate getTranslate() {
        if (translate == null) {
            updateTransforms();
        }
        return translate;
    }

    public Rotate getRotateX() {
        if (rotateX == null) {
            updateTransforms();
        }
        return rotateX;
    }

    public Rotate getRotateY() {
        if (rotateY == null) {
            updateTransforms();
        }
        return rotateY;
    }

    public Rotate getRotateZ() {
        if (rotateZ == null) {
            updateTransforms();
        }
        return rotateZ;
    }

    public Scale getScale() {
        if (scale == null) {
            updateTransforms();
        }
        return scale;
    }

    // ==================== Geometry Utilities ====================

    protected double polygonalSection(double angle) {
        if (sectionType.get().equals(SectionType.CIRCLE)) {
            return 1d;
        }
        int n = sectionType.get().getSides();
        return Math.cos(Math.PI / n) / Math.cos((2d * Math.atan(1d / Math.tan((n * angle) / 2d))) / n);
    }

    protected double polygonalSize(double radius) {
        if (sectionType.get().equals(SectionType.CIRCLE)) {
            return 2d * Math.PI * radius;
        }
        int n = sectionType.get().getSides();
        return n * Math.cos(Math.PI / n) * Math.log(-1d - 2d / (-1d + Math.sin(Math.PI / n))) * radius;
    }

    public Point3D getOrigin() {
        if (listVertices.size() > 0) {
            return listVertices.get(0);
        }
        return new Point3D(0f, 0f, 0f);
    }

    public int getIntersections(Point3D origin, Point3D direction) {
        setTextureModeFaces(10);
        int[] faces = helper.updateFacesWithIntersections(origin, direction, listVertices, listFaces);
        mesh.getFaces().setAll(faces);
        List<Face3> listIntersections = helper.getListIntersections(origin, direction, listVertices, listFaces);
        listIntersections.forEach(System.out::println);
        return listIntersections.size();
    }

    // ==================== Bindings ====================

    private final Callback<List<Point3D>, Integer> vertexCount = (List<Point3D> param) -> param.size();
    private final Callback<List<Face3>, Integer> faceCount = (List<Face3> param) -> param.size();

    protected final StringBinding vertCountBinding = new StringBinding() {
        @Override
        protected String computeValue() {
            return String.valueOf(vertexCount.call(listVertices));
        }
    };

    protected final StringBinding faceCountBinding = new StringBinding() {
        @Override
        protected String computeValue() {
            return String.valueOf(faceCount.call(listFaces));
        }
    };

    public final StringBinding faceCountBinding() {
        return faceCountBinding;
    }

    public final StringBinding vertCountBinding() {
        return vertCountBinding;
    }
}
