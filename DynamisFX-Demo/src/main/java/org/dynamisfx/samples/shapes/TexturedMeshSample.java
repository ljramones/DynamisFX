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

package org.dynamisfx.samples.shapes;

import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.dynamisfx.controls.TextureImage;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.paint.Patterns.CarbonPatterns;
import org.dynamisfx.shapes.primitives.TexturedMesh;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.TextureType;
import org.dynamisfx.tools.NormalMap;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public abstract class TexturedMeshSample extends ShapeBaseSample<TexturedMesh> {

    static {
        TriangleMeshHelper.setParallelAllowed(false);
    }

    //specific
    protected final Property<TriangleMeshHelper.SectionType> sectionType = new SimpleObjectProperty<TriangleMeshHelper.SectionType>(model, "secType", TriangleMeshHelper.SectionType.CIRCLE) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                model.setSectionType(sectionType.getValue());
            }
        }
    };
    protected final Property<TextureType> textureType = new SimpleObjectProperty<TextureType>(model, "texType", TriangleMeshHelper.TextureType.NONE) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                switch (getValue()) {
                    case NONE:
                        model.setTextureModeNone(colorBinding.get());
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                    case IMAGE:
                        model.setTextureModeImage(textureImage.getValue()==null?null:textureImage.getValue().getSource());
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                    case PATTERN:
                        model.setTextureModePattern(patterns.get(), pattScale.getValue());
                        material.setSpecularColor(specColorBinding.get());
                        material.setSpecularPower(specularPower.doubleValue());
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                    case COLORED_VERTICES_1D:
                        model.setTextureModeVertices1D(1530, func.getValue());
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                    case COLORED_VERTICES_3D:
                        model.setTextureModeVertices3D(1530, dens.getValue());
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                    case COLORED_FACES:
                        model.setTextureModeFaces(1530);
                        if (useBumpMap.getValue() || invert.getValue()) {
                            useBumpMap.setValue(false);
                            invert.setValue(false);
                        }
                        break;
                }
            }
        }
    };

    /*
     TriangleMeshHelper.TextureType.NONE 
     */
    protected final ObjectProperty<Color> colorBinding = new SimpleObjectProperty<Color>(Color.BROWN) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.NONE)) {
                model.setDiffuseColor(get());
            }
        }
    };
    protected final IntegerProperty colors = new SimpleIntegerProperty(model, "Color :", 700) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                colorBinding.set(Color.hsb(360 * (1d - get() / 1530d), 1, 0.5));
            }
        }
    };

    /*
     TriangleMeshHelper.TextureType.IMAGE 
     */
    protected final Property<TextureImage> textureImage = new SimpleObjectProperty(this, "Texture") {
        @Override
        protected void invalidated() {
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.IMAGE)) {
                //material.setDiffuseMap(textureImage.getValue());
                model.setTextureModeImage(textureImage.getValue().getSource());
                if (useBumpMap.getValue() || invert.getValue()) {
                    useBumpMap.setValue(false);
                    invert.setValue(false);
                }
            }
        }
    };
    protected final ObjectProperty<CarbonPatterns> patterns = new SimpleObjectProperty<CarbonPatterns>(CarbonPatterns.DARK_CARBON) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.PATTERN)) {
                model.setCarbonPattern(patterns.getValue());
                material.setSpecularColor(specColorBinding.get());
                material.setSpecularPower(specularPower.doubleValue());
                if (useBumpMap.get()) {
                    material.setBumpMap(new NormalMap(
                            bumpScale.doubleValue(), bumpFineScale.doubleValue(),
                            invert.getValue(), ((PhongMaterial) model.getMaterial()).getDiffuseMap()
                    ));
                }
            }
        }
    };

    /*
     TriangleMeshHelper.TextureType.PATTERN 
     */
    protected final DoubleProperty pattScale = new SimpleDoubleProperty(this, "Pattern Scale: ", 2.0d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.PATTERN)) {
                model.setPatternScale(pattScale.doubleValue());
            }
        }
    };

    /*
     TriangleMeshHelper.TextureType.COLORED_VERTICES_3D 
     */
    protected final DoubleProperty densMax = new SimpleDoubleProperty(this, "Density Scale: ");
    protected final Property<Function<Point3D, Number>> dens = new SimpleObjectProperty<Function<Point3D, Number>>(p -> p.x * p.y * p.z) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.COLORED_VERTICES_3D)) {
                model.setDensity(dens.getValue());
            }
        }
    };
    /*
     TriangleMeshHelper.TextureType.COLORED_VERTICES_1D 
     */
    protected final Property<Function<Number, Number>> func = new SimpleObjectProperty<Function<Number, Number>>(t -> t) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null && model.getTextureType().equals(TriangleMeshHelper.TextureType.COLORED_VERTICES_1D)) {
                model.setFunction(func.getValue());
            }
        }
    };

    protected final Property<Boolean> invert = new SimpleBooleanProperty(this, "Invert Bump Map", false) {
        @Override
        protected void invalidated() {
            if (model != null && useBumpMap.getValue()) {
                if (model.getMaterial() != null && ((PhongMaterial) model.getMaterial()).getDiffuseMap() != null) {
                    ((PhongMaterial) model.getMaterial()).setBumpMap(
                            new NormalMap(
                                    bumpScale.doubleValue(), bumpFineScale.doubleValue(),
                                    invert.getValue(), ((PhongMaterial) model.getMaterial()).getDiffuseMap()
                            )
                    );
                }
            }
        }
    };
    protected final DoubleProperty bumpScale = new SimpleDoubleProperty(this, "Bump Scale", 27d) {
        @Override
        protected void invalidated() {
            if (model != null) {
                if (model.getMaterial() != null && ((PhongMaterial) model.getMaterial()).getDiffuseMap() != null) {
                    ((PhongMaterial) model.getMaterial()).setBumpMap(
                            new NormalMap(
                                    bumpScale.doubleValue(), bumpFineScale.doubleValue(),
                                    invert.getValue(), ((PhongMaterial) model.getMaterial()).getDiffuseMap()
                            )
                    );
                }
            }
        }
    };
    protected final ObjectProperty<Image> bumpMap = new SimpleObjectProperty<Image>(this, "bumpMap", null) {
        @Override
        protected void invalidated() {
            if (model != null) {
            }
        }
    };
    protected final DoubleProperty bumpFineScale = new SimpleDoubleProperty(this, "Bump Fine Scale", 9d) {
        @Override
        protected void invalidated() {
            if (model != null) {
                if (model.getMaterial() != null && ((PhongMaterial) model.getMaterial()).getDiffuseMap() != null) {
                    ((PhongMaterial) model.getMaterial()).setBumpMap(
                            new NormalMap(
                                    bumpScale.doubleValue(), bumpFineScale.doubleValue(),
                                    invert.getValue(), ((PhongMaterial) model.getMaterial()).getDiffuseMap()
                            )
                    );
                }
            }
        }
    };
    protected final BooleanProperty useBumpMap = new SimpleBooleanProperty(this, "Generate Bump Map", false) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (model != null) {
                    if (model.getMaterial() != null && ((PhongMaterial) model.getMaterial()).getDiffuseMap() != null) {
                        ((PhongMaterial) model.getMaterial()).setBumpMap(
                                new NormalMap(
                                        bumpScale.doubleValue(), bumpFineScale.doubleValue(),
                                        invert.getValue(), ((PhongMaterial) model.getMaterial()).getDiffuseMap()
                                )
                        );
                    }
                }
            } else {
                if (model != null) {
                    ((PhongMaterial) model.getMaterial()).setBumpMap(null);
                }
            }
        }

    };

    protected final DoubleProperty specularPower = new SimpleDoubleProperty(this, "Specular Power") {
        @Override
        protected void invalidated() {
            if (model != null) {
                if (model.getMaterial() != null) {
                    ((PhongMaterial) model.getMaterial()).setSpecularPower(specularPower.getValue());
                }
            }
        }
    };

    protected final ObjectProperty<Color> specColorBinding = new SimpleObjectProperty<Color>(Color.BLACK) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                material.setSpecularColor(get());
            }
        }
    };
    protected final IntegerProperty specColor = new SimpleIntegerProperty(this, "Specular Color", 1) {

        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                specColorBinding.set(Color.hsb(360 * (1d - get() / 1530d), 1, 1));
            }
        }

    };

}
