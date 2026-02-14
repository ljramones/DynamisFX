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

package org.dynamisfx.utils.geom;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * A node that can be used as a visible link between two joints
 *
 * @author Jose Pereda
 */
public class JointChain extends Group {

    private static final Point3D POINT_Y = new Point3D(0, 1, 0);
    private final Box origin;
    private final Cylinder bone;
    private final Sphere end;
    private final double scale;
    private final Rotate rotate;
    private final Translate translate;

    public JointChain(Joint joint, double scale) {
        this.scale = scale == 0 ? 1 : scale;

        origin = new Box(16, 16, 16);
        origin.setMaterial(new PhongMaterial(getColor()));

        bone = new Cylinder(5, 1);
        rotate = new Rotate();
        translate = new Translate();
        bone.getTransforms().setAll(rotate, translate);
        bone.setMaterial(new PhongMaterial(getColor()));
        end = new Sphere(6);
        end.setMaterial(new PhongMaterial(getColor()));
        end.translateXProperty().bind(bone.translateXProperty());
        end.translateYProperty().bind(bone.translateYProperty());
        end.translateZProperty().bind(bone.translateZProperty());

        getChildren().addAll(origin, bone, end);
        getTransforms().add(new Scale(scale, scale, scale));

        joint.localToParentTransformProperty().addListener((obs, ov, nv) -> updateChain(joint));
        updateChain(joint);
    }

    private void updateChain(Joint joint) {
        Point3D scaled = getJointLocation(joint).multiply(1d / scale);
        final double magnitude = scaled.magnitude();
        origin.setTranslateX(scaled.getX());
        origin.setTranslateY(scaled.getY());
        origin.setTranslateZ(scaled.getZ());
        bone.setHeight(magnitude);
        double angle = Math.toDegrees(Math.acos(POINT_Y.dotProduct(scaled) / magnitude));
        rotate.setAngle(angle);
        rotate.setAxis(POINT_Y.crossProduct(scaled));
        translate.setY(magnitude / 2d);
    }

    private Point3D getJointLocation(Joint joint) {
        try {
            Affine a = new Affine();
            joint.getTransforms().forEach(a::append);
            return a.inverseTransform(Point3D.ZERO);
        } catch (NonInvertibleTransformException e) { }
        return Point3D.ZERO;
    }

    // drawMode
    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL) {
        @Override
        protected void invalidated() {
            getChildren().stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(n -> n.setDrawMode(get()));
        }
    };
    public final ObjectProperty<DrawMode> drawModeProperty() {
       return drawMode;
    }
    public final DrawMode getDrawMode() {
       return drawMode.get();
    }
    public final void setDrawMode(DrawMode value) {
        drawMode.set(value);
    }

    // color
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.DARKBLUE) {
        @Override
        protected void invalidated() {
            getChildren().stream()
                    .filter(Shape3D.class::isInstance)
                    .map(Shape3D.class::cast)
                    .map(s -> (PhongMaterial) s.getMaterial())
                    .forEach(m -> m.setDiffuseColor(get()));
        }
    };
    public final ObjectProperty<Color> colorProperty() {
       return color;
    }
    public final Color getColor() {
       return color.get();
    }
    public final void setColor(Color value) {
        color.set(value);
    }

}
