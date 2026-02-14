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

package org.dynamisfx.shapes.containers;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

/**
 *
 * @author Dub
 * @param <T>
 */
public class ShapeContainer<T extends MeshView> extends Group implements ShapeContainerBase<T> {
    
    private final T shape;
    private final PhongMaterial material;
    private final PointLight emissive;
    private final AmbientLight selfIllumination;

    public ShapeContainer(T shape) {
        this.shape = shape;
        this.material = new PhongMaterial();
        this.emissive = new PointLight();
        this.selfIllumination = new AmbientLight();
        
        this.selfIllumination.getScope().add(ShapeContainer.this);
        initialize();
    }

    @Override
    public T getShape() {
        return shape;
    }

    @Override
    public Group getContainer() {
        return this;
    }

    @Override
    public PhongMaterial getMaterial() {
        return material;
    }

    @Override
    public PointLight getEmissiveLight() {
        return emissive;
    }

    @Override
    public AmbientLight getSelfIlluminationLight() {
        return selfIllumination;
    }
    
}
