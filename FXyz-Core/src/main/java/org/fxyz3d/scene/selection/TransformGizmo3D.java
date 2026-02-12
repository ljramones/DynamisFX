/**
 * TransformGizmo3D.java
 *
 * Copyright (c) 2013-2026, F(X)yz
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

package org.fxyz3d.scene.selection;

import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Simple transform gizmo with translate/rotate/scale handles.
 */
public class TransformGizmo3D extends Group {

    public static final String HANDLE_PROPERTY_KEY = "org.fxyz3d.scene.selection.gizmoHandle";

    public enum Mode {
        TRANSLATE,
        ROTATE,
        SCALE,
        ALL
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private enum HandleKind {
        TRANSLATE,
        ROTATE,
        SCALE
    }

    private record HandleId(HandleKind kind, Axis axis) { }

    private final Group translateHandles = new Group();
    private final Group rotateHandles = new Group();
    private final Group scaleHandles = new Group();

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.ALL);
    private final DoubleProperty size = new SimpleDoubleProperty(80.0);
    private final DoubleProperty translateSensitivity = new SimpleDoubleProperty(1.0);
    private final DoubleProperty rotateSensitivity = new SimpleDoubleProperty(0.35);
    private final DoubleProperty scaleSensitivity = new SimpleDoubleProperty(0.01);
    private final BooleanProperty snapEnabled = new SimpleBooleanProperty(false);
    private final DoubleProperty translationSnapIncrement = new SimpleDoubleProperty(1.0);
    private final DoubleProperty rotationSnapIncrement = new SimpleDoubleProperty(15.0);
    private final DoubleProperty scaleSnapIncrement = new SimpleDoubleProperty(0.1);

    private SelectionModel3D selectionModel;
    private final InvalidationListener targetBoundsListener = obs -> syncToTarget();
    private final SetChangeListener<Node> selectionListener = change ->
            setTarget(selectionModel == null ? null : selectionModel.getPrimarySelection().orElse(null));

    private Node target;
    private HandleId activeHandle;
    private double dragStartSceneX;
    private double dragStartSceneY;
    private double startTranslateX;
    private double startTranslateY;
    private double startTranslateZ;
    private double startRotateX;
    private double startRotateY;
    private double startRotateZ;
    private double startScaleX;
    private double startScaleY;
    private double startScaleZ;

    public TransformGizmo3D() {
        buildHandles();
        getChildren().addAll(translateHandles, rotateHandles, scaleHandles);
        mode.addListener((obs, oldValue, newValue) -> updateModeVisibility());
        size.addListener((obs, oldValue, newValue) -> rebuildHandleGeometry());
        updateModeVisibility();
        setVisible(false);
        setManaged(false);
        setMouseTransparent(false);
    }

    public ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public Mode getMode() {
        return mode.get();
    }

    public void setMode(Mode mode) {
        this.mode.set(mode);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public double getSize() {
        return size.get();
    }

    public void setSize(double size) {
        this.size.set(size);
    }

    public DoubleProperty translateSensitivityProperty() {
        return translateSensitivity;
    }

    public double getTranslateSensitivity() {
        return translateSensitivity.get();
    }

    public void setTranslateSensitivity(double sensitivity) {
        translateSensitivity.set(sensitivity);
    }

    public DoubleProperty rotateSensitivityProperty() {
        return rotateSensitivity;
    }

    public double getRotateSensitivity() {
        return rotateSensitivity.get();
    }

    public void setRotateSensitivity(double sensitivity) {
        rotateSensitivity.set(sensitivity);
    }

    public DoubleProperty scaleSensitivityProperty() {
        return scaleSensitivity;
    }

    public double getScaleSensitivity() {
        return scaleSensitivity.get();
    }

    public void setScaleSensitivity(double sensitivity) {
        scaleSensitivity.set(sensitivity);
    }

    public BooleanProperty snapEnabledProperty() {
        return snapEnabled;
    }

    public boolean isSnapEnabled() {
        return snapEnabled.get();
    }

    public void setSnapEnabled(boolean enabled) {
        snapEnabled.set(enabled);
    }

    public DoubleProperty translationSnapIncrementProperty() {
        return translationSnapIncrement;
    }

    public double getTranslationSnapIncrement() {
        return translationSnapIncrement.get();
    }

    public void setTranslationSnapIncrement(double value) {
        translationSnapIncrement.set(value);
    }

    public DoubleProperty rotationSnapIncrementProperty() {
        return rotationSnapIncrement;
    }

    public double getRotationSnapIncrement() {
        return rotationSnapIncrement.get();
    }

    public void setRotationSnapIncrement(double value) {
        rotationSnapIncrement.set(value);
    }

    public DoubleProperty scaleSnapIncrementProperty() {
        return scaleSnapIncrement;
    }

    public double getScaleSnapIncrement() {
        return scaleSnapIncrement.get();
    }

    public void setScaleSnapIncrement(double value) {
        scaleSnapIncrement.set(value);
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        if (this.target == target) {
            syncToTarget();
            return;
        }
        removeTargetListeners();
        this.target = target;
        if (this.target != null) {
            addTargetListeners();
            syncToTarget();
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void bindToSelectionModel(SelectionModel3D model) {
        unbindSelectionModel();
        selectionModel = Objects.requireNonNull(model, "selectionModel must not be null");
        selectionModel.getSelectedNodes().addListener(selectionListener);
        setTarget(selectionModel.getPrimarySelection().orElse(null));
    }

    public void unbindSelectionModel() {
        if (selectionModel != null) {
            selectionModel.getSelectedNodes().removeListener(selectionListener);
            selectionModel = null;
        }
    }

    private void buildHandles() {
        rebuildHandleGeometry();
    }

    private void rebuildHandleGeometry() {
        translateHandles.getChildren().setAll(
                createTranslateHandle(Axis.X),
                createTranslateHandle(Axis.Y),
                createTranslateHandle(Axis.Z));
        rotateHandles.getChildren().setAll(
                createRotateHandle(Axis.X),
                createRotateHandle(Axis.Y),
                createRotateHandle(Axis.Z));
        scaleHandles.getChildren().setAll(
                createScaleHandle(Axis.X),
                createScaleHandle(Axis.Y),
                createScaleHandle(Axis.Z));
    }

    private Node createTranslateHandle(Axis axis) {
        double length = getSize() * 0.65;
        double radius = Math.max(1.0, getSize() * 0.03);
        Cylinder c = new Cylinder(radius, length);
        c.setMaterial(new PhongMaterial(axisColor(axis)));
        orientAlongAxis(c, axis);
        moveAlongAxis(c, axis, length * 0.5);
        installHandleEvents(c, new HandleId(HandleKind.TRANSLATE, axis));
        return c;
    }

    private Node createRotateHandle(Axis axis) {
        double radius = Math.max(1.0, getSize() * 0.06);
        Sphere s = new Sphere(radius);
        s.setMaterial(new PhongMaterial(axisColor(axis).darker()));
        moveAlongAxis(s, axis, -getSize() * 0.85);
        installHandleEvents(s, new HandleId(HandleKind.ROTATE, axis));
        return s;
    }

    private Node createScaleHandle(Axis axis) {
        double side = Math.max(2.0, getSize() * 0.09);
        Box b = new Box(side, side, side);
        b.setMaterial(new PhongMaterial(axisColor(axis).brighter()));
        moveAlongAxis(b, axis, getSize() * 0.95);
        installHandleEvents(b, new HandleId(HandleKind.SCALE, axis));
        return b;
    }

    private void installHandleEvents(Shape3D shape, HandleId id) {
        shape.getProperties().put(HANDLE_PROPERTY_KEY, Boolean.TRUE);
        shape.setPickOnBounds(true);
        shape.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> onHandlePressed(event, id));
        shape.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onHandleDragged);
        shape.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onHandleReleased);
    }

    private void onHandlePressed(MouseEvent event, HandleId id) {
        if (target == null || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        activeHandle = id;
        dragStartSceneX = event.getSceneX();
        dragStartSceneY = event.getSceneY();
        startTranslateX = target.getTranslateX();
        startTranslateY = target.getTranslateY();
        startTranslateZ = target.getTranslateZ();
        startRotateX = target.getRotate();
        startRotateY = target.getRotate();
        startRotateZ = target.getRotate();
        startScaleX = target.getScaleX();
        startScaleY = target.getScaleY();
        startScaleZ = target.getScaleZ();
        event.consume();
    }

    private void onHandleDragged(MouseEvent event) {
        if (target == null || activeHandle == null || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        final double dx = event.getSceneX() - dragStartSceneX;
        final double dy = event.getSceneY() - dragStartSceneY;
        final double delta = (dx - dy) * 0.5;
        final boolean snap = isSnapRequested(event);

        switch (activeHandle.kind()) {
            case TRANSLATE -> applyTranslate(activeHandle.axis(), dx, dy, snap);
            case ROTATE -> applyRotate(activeHandle.axis(), delta, snap);
            case SCALE -> applyScale(activeHandle.axis(), delta, snap);
            default -> {
            }
        }
        syncToTarget();
        event.consume();
    }

    private void onHandleReleased(MouseEvent event) {
        activeHandle = null;
        event.consume();
    }

    private void applyTranslate(Axis axis, double dx, double dy, boolean snap) {
        double s = getTranslateSensitivity();
        switch (axis) {
            case X -> target.setTranslateX(maybeSnap(startTranslateX + (dx * s), getTranslationSnapIncrement(), snap));
            case Y -> target.setTranslateY(maybeSnap(startTranslateY + (dy * s), getTranslationSnapIncrement(), snap));
            case Z -> target.setTranslateZ(maybeSnap(startTranslateZ + ((dx - dy) * 0.5 * s), getTranslationSnapIncrement(), snap));
            default -> {
            }
        }
    }

    private void applyRotate(Axis axis, double delta, boolean snap) {
        double s = getRotateSensitivity();
        switch (axis) {
            case X -> {
                target.setRotationAxis(Rotate.X_AXIS);
                target.setRotate(maybeSnap(startRotateX + (delta * s), getRotationSnapIncrement(), snap));
            }
            case Y -> {
                target.setRotationAxis(Rotate.Y_AXIS);
                target.setRotate(maybeSnap(startRotateY + (delta * s), getRotationSnapIncrement(), snap));
            }
            case Z -> {
                target.setRotationAxis(Rotate.Z_AXIS);
                target.setRotate(maybeSnap(startRotateZ + (delta * s), getRotationSnapIncrement(), snap));
            }
            default -> {
            }
        }
    }

    private void applyScale(Axis axis, double delta, boolean snap) {
        double s = getScaleSensitivity();
        switch (axis) {
            case X -> target.setScaleX(clampScale(maybeSnap(startScaleX + (delta * s), getScaleSnapIncrement(), snap)));
            case Y -> target.setScaleY(clampScale(maybeSnap(startScaleY + (delta * s), getScaleSnapIncrement(), snap)));
            case Z -> target.setScaleZ(clampScale(maybeSnap(startScaleZ + (delta * s), getScaleSnapIncrement(), snap)));
            default -> {
            }
        }
    }

    private double clampScale(double value) {
        return Math.max(0.01, value);
    }

    private boolean isSnapRequested(MouseEvent event) {
        if (event.isAltDown()) {
            return false;
        }
        return isSnapEnabled() || event.isShiftDown();
    }

    private double maybeSnap(double value, double increment, boolean snapRequested) {
        if (!snapRequested || increment <= 0) {
            return value;
        }
        return Math.round(value / increment) * increment;
    }

    private void syncToTarget() {
        if (target == null) {
            return;
        }
        Bounds b = target.getBoundsInParent();
        setTranslateX((b.getMinX() + b.getMaxX()) * 0.5);
        setTranslateY((b.getMinY() + b.getMaxY()) * 0.5);
        setTranslateZ((b.getMinZ() + b.getMaxZ()) * 0.5);
    }

    private void addTargetListeners() {
        target.boundsInParentProperty().addListener(targetBoundsListener);
        target.translateXProperty().addListener(targetBoundsListener);
        target.translateYProperty().addListener(targetBoundsListener);
        target.translateZProperty().addListener(targetBoundsListener);
        target.rotateProperty().addListener(targetBoundsListener);
    }

    private void removeTargetListeners() {
        if (target == null) {
            return;
        }
        target.boundsInParentProperty().removeListener(targetBoundsListener);
        target.translateXProperty().removeListener(targetBoundsListener);
        target.translateYProperty().removeListener(targetBoundsListener);
        target.translateZProperty().removeListener(targetBoundsListener);
        target.rotateProperty().removeListener(targetBoundsListener);
    }

    private void updateModeVisibility() {
        Mode current = getMode();
        translateHandles.setVisible(current == Mode.ALL || current == Mode.TRANSLATE);
        rotateHandles.setVisible(current == Mode.ALL || current == Mode.ROTATE);
        scaleHandles.setVisible(current == Mode.ALL || current == Mode.SCALE);
    }

    private void orientAlongAxis(Cylinder c, Axis axis) {
        switch (axis) {
            case X -> {
                c.setRotationAxis(Rotate.Z_AXIS);
                c.setRotate(90.0);
            }
            case Y -> {
                c.setRotationAxis(Rotate.X_AXIS);
                c.setRotate(0.0);
            }
            case Z -> {
                c.setRotationAxis(Rotate.X_AXIS);
                c.setRotate(90.0);
            }
            default -> {
            }
        }
    }

    private void moveAlongAxis(Node node, Axis axis, double amount) {
        switch (axis) {
            case X -> node.setTranslateX(amount);
            case Y -> node.setTranslateY(amount);
            case Z -> node.setTranslateZ(amount);
            default -> {
            }
        }
    }

    private Color axisColor(Axis axis) {
        return switch (axis) {
            case X -> Color.RED;
            case Y -> Color.LIMEGREEN;
            case Z -> Color.DODGERBLUE;
        };
    }
}
