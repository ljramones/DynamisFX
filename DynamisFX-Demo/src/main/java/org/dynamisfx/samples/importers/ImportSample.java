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

package org.dynamisfx.samples.importers;

import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.HierarchyControl;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.importers.Importer3D;
import org.dynamisfx.importers.Model3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;
import org.dynamisfx.shapes.polygon.PolygonMeshView;
import org.dynamisfx.shapes.polygon.SkinningMesh;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JosePereda
 */
public abstract class ImportSample extends ShapeBaseSample<Group> {

    protected final IntegerProperty subdivision = new SimpleIntegerProperty(this, "Subdivision Level", 0);
    protected final ObjectProperty<URL> url = new SimpleObjectProperty<>(this, "URL");
    protected final ObjectProperty<Timeline> timeline = new SimpleObjectProperty<>(this, "timeline");
    protected final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    protected final BooleanProperty asPolygonMesh = new SimpleBooleanProperty(this, "asPolygonMesh", true);
    protected final BooleanProperty yUp = new SimpleBooleanProperty(this, "yUp", true);
    protected final BooleanProperty skeleton = new SimpleBooleanProperty(this, "Show skeleton", false) {
        @Override
        protected void invalidated() {
            setDrawMode(model, get() ? DrawMode.LINE : drawMode.getValue());
        }
    };
    private ControlCategory geomControls;

    @Override
    protected void addMeshAndListeners() {
        drawMode.addListener((obs, b, b1) -> {
            if (model != null) {
                setDrawMode(model, b1);
            }
        });
        culling.addListener((obs, b, b1) -> {
            if (model != null) {
                setCullFace(model, b1);
            }
        });
        subdivision.addListener((obs, ov, nv) -> setSubdivisionLevel(model, nv.intValue()));
        url.addListener((obs, ov, nv) -> {
            try {
                Model3D model3D = asPolygonMesh.get() ?
                        Importer3D.loadAsPoly(url.get()) : Importer3D.load(url.get());
                model.getChildren().setAll(model3D.getRoot().getChildren());
                model3D.getTimeline().ifPresentOrElse(t -> {
                    model.sceneProperty().addListener(new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (model.getScene() == null) {
                                model.sceneProperty().removeListener(this);
                                timeline.set(null);
                            }
                        }
                    });
                    timeline.set(t);
                }, () -> timeline.set(null));
                initModel();
            } catch (IOException e) {
                Logger.getLogger(ImportSample.class.getName()).log(Level.SEVERE,null, e);
            }
        });
        Rotate rt = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
        rt.angleProperty().bind(Bindings.when(yUp).then(180).otherwise(0));
        model.getTransforms().setAll(rt);
        initModel();
    }

    private void initModel() {
        setCullFace(model, culling.getValue());
        setDrawMode(model, drawMode.getValue());
        setSubdivisionLevel(model, subdivision.getValue());
        content.set(model);
        setSkeleton(model);
        double size = Math.max(Math.max(model.getBoundsInLocal().getWidth(),
                model.getBoundsInLocal().getHeight()), model.getBoundsInLocal().getDepth());
        camera.setTranslateZ(- size * 3);

        geomControls.removeIf(HierarchyControl.class::isInstance);
        HierarchyControl hierarchyControl = ControlFactory.buildHierarchyControl(content);
        geomControls.addControls(hierarchyControl);
    }

    private void setDrawMode(Node node, DrawMode value) {
        if (node instanceof PolygonMeshView) {
            ((PolygonMeshView) node).setDrawMode(value);
        } else if (node instanceof MeshView) {
            ((MeshView) node).setDrawMode(value);
        } else if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().forEach(n -> setDrawMode(n, value));
        }
    }

    private void setCullFace(Node node, CullFace value) {
        if (node instanceof PolygonMeshView) {
            ((PolygonMeshView) node).setCullFace(value);
        } else if (node instanceof MeshView) {
            ((MeshView) node).setCullFace(value);
        } else if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().forEach(n -> setCullFace(n, value));
        }
    }
    
    private void setSubdivisionLevel(Node node, int subdivisionLevel) {
        if (node instanceof PolygonMeshView) {
            ((PolygonMeshView) node).setSubdivisionLevel(subdivisionLevel);
        } else if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().forEach(n -> setSubdivisionLevel(n, subdivisionLevel));
        }
    }

    private void setSkeleton(Node node) {
        if (node instanceof PolygonMeshView && ((PolygonMeshView) node).getMesh() instanceof SkinningMesh) {
            ((SkinningMesh) ((PolygonMeshView) node).getMesh()).showSkeletonProperty().bind(skeleton);
        } else if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().forEach(this::setSkeleton);
        }
    }

    @Override
    protected Node buildControlPanel() {
        NumberSliderControl subdivisionSlider = ControlFactory.buildNumberSlider(subdivision, 0, 2);
        subdivisionSlider.getSlider().setMajorTickUnit(1);
        subdivisionSlider.getSlider().setMinorTickCount(0);
        subdivisionSlider.getSlider().setBlockIncrement(1);
        subdivisionSlider.getSlider().setSnapToTicks(true);
        geomControls = ControlFactory.buildCategory("3D Settings");
        geomControls.addControls(ControlFactory.buildFileLoadControl(url),
                ControlFactory.buildCheckBoxControl(asPolygonMesh),
                ControlFactory.buildCheckBoxControl(yUp),
                ControlFactory.buildCheckBoxControl(skeleton),
                subdivisionSlider);
        
        this.controlPanel = ControlFactory.buildControlPanel(
                ControlFactory.buildMeshViewCategory(
                        this.drawMode,
                        this.culling
                ), geomControls,
                ControlFactory.buildAnimationCategory(timeline));
        return controlPanel;
    }
}
