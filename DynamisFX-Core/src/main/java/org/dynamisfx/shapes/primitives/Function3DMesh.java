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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.polygon.PolygonMesh;
import org.dynamisfx.shapes.polygon.PolygonMeshView;

public class Function3DMesh extends Group {

    public Function3DMesh(SurfacePlotMesh surfacePlotMesh, boolean wireframe) {
        setWireframe(wireframe);
        setSurface(surfacePlotMesh);
        idProperty().addListener((obs, ov, nv) -> {
            if (getSurface() != null) {
                getSurface().setId(nv);
            }
        });
    }

    // surface
    private final ObjectProperty<SurfacePlotMesh> surface = new SimpleObjectProperty<SurfacePlotMesh>() {
        @Override
        protected void invalidated() {
            getChildren().setAll(get());
            if (isWireframe()) {
                addWireframe();
            }
            getSurface().setId(getId());
            function3DData.setAll(getSurface().listVertices);
        }
    };
    public final SurfacePlotMesh getSurface() { return surface.get(); }
    public final void setSurface(SurfacePlotMesh value) { surface.set(value); }
    public final ObjectProperty<SurfacePlotMesh> surfaceProperty() { return surface; }

    // wireframe
    private final BooleanProperty wireframe = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            getChildren().removeIf(PolygonMeshView.class::isInstance);
            if (get()) {
                addWireframe();
            }
        }
    };
    public final boolean isWireframe() { return wireframe.get(); }
    public final void setWireframe(boolean value) { wireframe.set(value); }
    public final BooleanProperty wireframeProperty() { return wireframe; }

    private final ObservableList<Point3D> function3DData = FXCollections.observableArrayList();

    public ObservableList<Point3D> getFunction3DData() {
        return function3DData;
    }

    private void addWireframe() {
        if (getSurface() == null) {
            return;
        }
        PolygonMesh polygonMesh = getSurface().getPolygonMesh();
        PolygonMeshView polygonMeshView = new PolygonMeshView(polygonMesh);
        polygonMeshView.setMaterial(new PhongMaterial(Color.BLACK));
        polygonMeshView.setDrawMode(DrawMode.LINE);
        polygonMeshView.setCullFace(CullFace.NONE);
        polygonMeshView.setId(getId());
        getChildren().addAll(polygonMeshView);
    }

}
