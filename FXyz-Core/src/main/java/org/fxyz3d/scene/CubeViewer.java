/**
 * CubeViewer.java
 *
 * Copyright (c) 2013-2016, F(X)yz
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

package org.fxyz3d.scene;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * A 3D cube viewer for scatter plot visualization with configurable axes, panels, and grid lines.
 *
 * @author SPhillips
 */
public class CubeViewer extends Group {

    private static final double DEFAULT_SIZE = 1000;
    private static final double DEFAULT_SPACING = 100;
    private static final double DEFAULT_SCATTER_RADIUS = 1;
    private static final double DEFAULT_AXES_THICKNESS = 5;
    private static final double DEFAULT_GRID_SIZE = 2;
    private static final double DEFAULT_GRID_LINES_OPACITY = 0.1;
    private static final double DEFAULT_PANELS_OPACITY = 0.5;

    private final Group scatterDataGroup = new Group();

    // Configuration
    private final double axesSize;
    private final double gridLineSpacing;
    private double scatterRadius;
    private final double axesThickness;
    private final double gridSize;
    private boolean selfLightEnabled;

    // Axis data
    private List<Double> xAxisData = new ArrayList<>();
    private List<Double> yAxisData = new ArrayList<>();
    private List<Double> zAxisData = new ArrayList<>();

    // Camera rotation state
    private double cameraRX = 0;
    private double cameraRY = 0;
    private double cameraRZ = 0;

    // Panels
    private final Panels panels = new Panels();

    // Axes groups
    private final AxesGroups axes = new AxesGroups();

    // Grid lines groups
    private final GridLines gridLines = new GridLines();

    // Corner spheres
    private final Group sphereGroup = new Group();
    private boolean showSphereGroup = true;

    public CubeViewer(boolean ambientLight) {
        this(DEFAULT_SIZE, DEFAULT_SPACING, ambientLight);
    }

    public CubeViewer(double size, double spacing, boolean selfLight) {
        this(size, spacing, selfLight, DEFAULT_SCATTER_RADIUS, DEFAULT_AXES_THICKNESS, DEFAULT_GRID_SIZE);
    }

    public CubeViewer(double axesSize, double spacing, boolean selfLight,
                      double scatterRadius, double axesThickness, double gridSize) {
        this.axesSize = axesSize;
        this.gridLineSpacing = spacing;
        this.selfLightEnabled = selfLight;
        this.scatterRadius = scatterRadius;
        this.axesThickness = axesThickness;
        this.gridSize = gridSize;
        init();
    }

    private void init() {
        buildAxes(axesSize, axesThickness);
        buildPanels(axesSize);
        buildGrids(axesSize, gridLineSpacing);
        buildEventHandlers();
        getChildren().add(scatterDataGroup);
        if (selfLightEnabled) {
            PointLight light = new PointLight(Color.WHITE);
            getChildren().add(light);
        }
        setDepthTest(DepthTest.ENABLE);
    }

    private void buildPanels(double size) {
        double halfSize = size / 2;

        panels.x1 = new Rectangle(size, size, panels.x1Color);
        panels.x2 = new Rectangle(size, size, panels.x2Color);
        panels.y1 = new Rectangle(size, size, panels.y1Color);
        panels.y2 = new Rectangle(size, size, panels.y2Color);
        panels.z1 = new Rectangle(size, size, panels.z1Color);
        panels.z2 = new Rectangle(size, size, panels.z2Color);

        panels.x1.setTranslateX(-halfSize);
        panels.x1.setTranslateY(-halfSize);
        panels.x1.setTranslateZ(-halfSize);
        panels.x2.setTranslateX(-halfSize);
        panels.x2.setTranslateY(-halfSize);
        panels.x2.setTranslateZ(halfSize);
        getChildren().addAll(panels.x1, panels.x2);

        panels.y2.setTranslateY(-halfSize);
        panels.y2.setRotationAxis(Rotate.Y_AXIS);
        panels.y2.setRotate(89.9);
        panels.y1.setTranslateX(-size);
        panels.y1.setTranslateY(-halfSize);
        panels.y1.setRotationAxis(Rotate.Y_AXIS);
        panels.y1.setRotate(89.9);
        getChildren().addAll(panels.y1, panels.y2);

        panels.z1.setTranslateX(-halfSize);
        panels.z1.setRotationAxis(Rotate.X_AXIS);
        panels.z1.setRotate(89.9);
        panels.z2.setTranslateX(-halfSize);
        panels.z2.setTranslateY(-size);
        panels.z2.setRotationAxis(Rotate.X_AXIS);
        panels.z2.setRotate(89.9);
        getChildren().addAll(panels.z1, panels.z2);
    }

    private void buildGrids(double size, double spacing) {
        double halfSize = size / 2;

        // X-axis grid lines
        gridLines.xy1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, (-halfSize) + i, 0, -halfSize, null, 0));
        gridLines.xy2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, (-halfSize) + i, 0, halfSize, null, 0));
        gridLines.xx1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, 0, halfSize - i, -halfSize, Rotate.Z_AXIS, 90));
        gridLines.xx2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, 0, halfSize - i, halfSize, Rotate.Z_AXIS, 90));

        getChildren().addAll(gridLines.xy1, gridLines.xx1, gridLines.xy2, gridLines.xx2);

        // Y-axis grid lines
        gridLines.yy1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, -halfSize, 0, (-halfSize) + i, null, 0));
        gridLines.yy2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, halfSize, 0, (-halfSize) + i, null, 0));
        gridLines.yx1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, -halfSize, halfSize - i, 0, Rotate.X_AXIS, 90));
        gridLines.yx2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, halfSize, halfSize - i, 0, Rotate.X_AXIS, 90));

        getChildren().addAll(gridLines.yy1, gridLines.yx1, gridLines.yy2, gridLines.yx2);

        // Z-axis grid lines
        gridLines.zy1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, (-halfSize) + i, halfSize, 0, Rotate.X_AXIS, 90));
        gridLines.zy2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, (-halfSize) + i, -halfSize, 0, Rotate.X_AXIS, 90));
        gridLines.zx1 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, 0, halfSize, (-halfSize) + i, Rotate.Z_AXIS, 90));
        gridLines.zx2 = createGridLineGroup(size, spacing,
            i -> createCylinder(gridSize, size, 0, -halfSize, (-halfSize) + i, Rotate.Z_AXIS, 90));

        getChildren().addAll(gridLines.zy1, gridLines.zx1, gridLines.zy2, gridLines.zx2);
    }

    @FunctionalInterface
    private interface CylinderFactory {
        Cylinder create(int index);
    }

    private Group createGridLineGroup(double size, double spacing, CylinderFactory factory) {
        List<Node> cylinders = new ArrayList<>();
        for (int i = 0; i < size; i += spacing) {
            cylinders.add(factory.create(i));
        }
        return new Group(cylinders);
    }

    private Cylinder createCylinder(double radius, double height,
                                     double tx, double ty, double tz,
                                     javafx.geometry.Point3D rotationAxis, double rotationAngle) {
        Cylinder cyl = new Cylinder(radius, height);
        cyl.setTranslateX(tx);
        cyl.setTranslateY(ty);
        cyl.setTranslateZ(tz);
        if (rotationAxis != null) {
            cyl.setRotationAxis(rotationAxis);
            cyl.setRotate(rotationAngle);
        }
        return cyl;
    }

    private void buildAxes(double size, double axisThickness) {
        double halfSize = size / 2;

        // Axis materials
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        // Create 4 sets of XYZ axis cylinders for cube edges
        Cylinder[] xCylinders = new Cylinder[4];
        Cylinder[] yCylinders = new Cylinder[4];
        Cylinder[] zCylinders = new Cylinder[4];

        for (int i = 0; i < 4; i++) {
            xCylinders[i] = new Cylinder(axisThickness, size);
            yCylinders[i] = new Cylinder(axisThickness, size);
            zCylinders[i] = new Cylinder(axisThickness, size);

            xCylinders[i].setRotationAxis(Rotate.Z_AXIS);
            xCylinders[i].setRotate(90);
            zCylinders[i].setRotationAxis(Rotate.X_AXIS);
            zCylinders[i].setRotate(-90);

            xCylinders[i].setMaterial(redMaterial);
            yCylinders[i].setMaterial(greenMaterial);
            zCylinders[i].setMaterial(blueMaterial);
        }

        // Position axis 1
        xCylinders[0].setTranslateY(halfSize);
        xCylinders[0].setTranslateZ(-halfSize);
        yCylinders[0].setTranslateX(-halfSize);
        yCylinders[0].setTranslateZ(-halfSize);
        zCylinders[0].setTranslateX(-halfSize);
        zCylinders[0].setTranslateY(halfSize);

        // Position axis 2
        xCylinders[1].setTranslateY(halfSize);
        xCylinders[1].setTranslateZ(halfSize);
        yCylinders[1].setTranslateX(halfSize);
        yCylinders[1].setTranslateZ(halfSize);
        zCylinders[1].setTranslateX(halfSize);
        zCylinders[1].setTranslateY(halfSize);

        // Position axis 3
        xCylinders[2].setTranslateY(-halfSize);
        xCylinders[2].setTranslateZ(-halfSize);
        yCylinders[2].setTranslateX(halfSize);
        yCylinders[2].setTranslateZ(-halfSize);
        zCylinders[2].setTranslateX(halfSize);
        zCylinders[2].setTranslateY(-halfSize);

        // Position axis 4
        xCylinders[3].setTranslateY(-halfSize);
        xCylinders[3].setTranslateZ(halfSize);
        yCylinders[3].setTranslateX(-halfSize);
        yCylinders[3].setTranslateZ(halfSize);
        zCylinders[3].setTranslateX(-halfSize);
        zCylinders[3].setTranslateY(-halfSize);

        axes.x.getChildren().addAll(xCylinders);
        axes.y.getChildren().addAll(yCylinders);
        axes.z.getChildren().addAll(zCylinders);

        getChildren().addAll(axes.x, axes.y, axes.z);

        // Add corner spheres
        double sphereRadius = 2 * axisThickness;
        double[][] corners = {
            {-halfSize, -halfSize, -halfSize},
            {-halfSize, -halfSize, halfSize},
            {halfSize, -halfSize, halfSize},
            {halfSize, -halfSize, -halfSize},
            {-halfSize, halfSize, -halfSize},
            {-halfSize, halfSize, halfSize},
            {halfSize, halfSize, halfSize},
            {halfSize, halfSize, -halfSize}
        };

        for (double[] corner : corners) {
            Sphere sphere = new Sphere(sphereRadius);
            sphere.setTranslateX(corner[0]);
            sphere.setTranslateY(corner[1]);
            sphere.setTranslateZ(corner[2]);
            sphereGroup.getChildren().add(sphere);
        }
        getChildren().add(sphereGroup);
    }

    public void adjustPanelsByPos(double rx, double ry, double rz) {
        cameraRX = rx;
        cameraRY = ry;
        cameraRZ = rz;

        // X1 face visibility
        boolean showX1 = !(-85 < ry && ry < 85);
        if (showX1) {
            panels.x1.setVisible(panels.showX1);
            gridLines.xy1.setVisible(gridLines.showXY1);
            gridLines.xx1.setVisible(gridLines.showXX1);
        } else {
            panels.x1.setVisible(false);
            gridLines.xy1.setVisible(false);
            gridLines.xx1.setVisible(false);
        }

        // X2 face visibility
        boolean showX2 = !((95 < ry && ry < 180) || (-180 < ry && ry < -95));
        if (showX2) {
            panels.x2.setVisible(panels.showX1);
            gridLines.xy2.setVisible(gridLines.showXY2);
            gridLines.xx2.setVisible(gridLines.showXX2);
        } else {
            panels.x2.setVisible(false);
            gridLines.xy2.setVisible(false);
            gridLines.xx2.setVisible(false);
        }

        // Y1 face visibility
        boolean showY1 = !(5 < ry && ry < 175);
        if (showY1) {
            panels.y1.setVisible(panels.showY1);
            gridLines.yy1.setVisible(gridLines.showYY1);
            gridLines.yx1.setVisible(gridLines.showYX1);
        } else {
            panels.y1.setVisible(false);
            gridLines.yy1.setVisible(false);
            gridLines.yx1.setVisible(false);
        }

        // Y2 face visibility
        boolean showY2 = !(-175 < ry && ry < -5);
        if (showY2) {
            panels.y2.setVisible(panels.showY2);
            gridLines.yy2.setVisible(gridLines.showYY2);
            gridLines.yx2.setVisible(gridLines.showYX2);
        } else {
            panels.y2.setVisible(false);
            gridLines.yy2.setVisible(false);
            gridLines.yx2.setVisible(false);
        }

        // Z1 face visibility
        if (rx > 0) {
            panels.z1.setVisible(false);
            gridLines.zy1.setVisible(false);
            gridLines.zx1.setVisible(false);
        } else {
            panels.z1.setVisible(panels.showZ1);
            gridLines.zy1.setVisible(gridLines.showZY1);
            gridLines.zx1.setVisible(gridLines.showZX1);
        }

        // Z2 face visibility
        if (rx < 0) {
            panels.z2.setVisible(false);
            gridLines.zy2.setVisible(false);
            gridLines.zx2.setVisible(false);
        } else {
            panels.z2.setVisible(panels.showZ2);
            gridLines.zy2.setVisible(gridLines.showZY2);
            gridLines.zx2.setVisible(gridLines.showZX2);
        }
    }

    private void buildEventHandlers() {
        axes.x.setOnMouseEntered((MouseEvent t) -> {
            panels.x1.setVisible(true);
            panels.x2.setVisible(true);
            t.consume();
        });
        axes.x.setOnMouseExited((MouseEvent t) -> {
            adjustPanelsByPos(cameraRX, cameraRY, cameraRZ);
            t.consume();
        });
        axes.y.setOnMouseEntered((MouseEvent t) -> {
            panels.y1.setVisible(true);
            panels.y2.setVisible(true);
            t.consume();
        });
        axes.y.setOnMouseExited((MouseEvent t) -> {
            adjustPanelsByPos(cameraRX, cameraRY, cameraRZ);
            t.consume();
        });
        axes.z.setOnMouseEntered((MouseEvent t) -> {
            panels.z1.setVisible(true);
            panels.z2.setVisible(true);
            t.consume();
        });
        axes.z.setOnMouseExited((MouseEvent t) -> {
            adjustPanelsByPos(cameraRX, cameraRY, cameraRZ);
            t.consume();
        });
    }

    // Scatter data methods

    public Group getScatterDataGroup() {
        return scatterDataGroup;
    }

    public List<Double> getXAxisData() {
        return xAxisData;
    }

    public void setXAxisData(List<Double> data) {
        xAxisData = data;
        rebuildScatterData();
    }

    public List<Double> getYAxisData() {
        return yAxisData;
    }

    public void setYAxisData(List<Double> data) {
        yAxisData = data;
        rebuildScatterData();
    }

    public List<Double> getZAxisData() {
        return zAxisData;
    }

    public void setZAxisData(List<Double> data) {
        zAxisData = data;
        rebuildScatterData();
    }

    private void rebuildScatterData() {
        scatterDataGroup.getChildren().clear();
        int maxSize = Math.max(xAxisData.size(), Math.max(yAxisData.size(), zAxisData.size()));
        for (int i = 0; i < maxSize; i++) {
            Sphere dataSphere = new Sphere(scatterRadius);
            double translateX = i < xAxisData.size() ? xAxisData.get(i) : 0.0;
            double translateY = i < yAxisData.size() ? yAxisData.get(i) : 0.0;
            double translateZ = i < zAxisData.size() ? zAxisData.get(i) : 0.0;
            dataSphere.setTranslateX(translateX);
            dataSphere.setTranslateY(translateY);
            dataSphere.setTranslateZ(translateZ);
            scatterDataGroup.getChildren().add(dataSphere);
        }
    }

    // Panel color methods
    public void setX1PanelColor(Color color) {
        panels.x1Color = color;
        panels.x1.setFill(color);
    }

    public void setX2PanelColor(Color color) {
        panels.x2Color = color;
        panels.x2.setFill(color);
    }

    public void setY1PanelColor(Color color) {
        panels.y1Color = color;
        panels.y1.setFill(color);
    }

    public void setY2PanelColor(Color color) {
        panels.y2Color = color;
        panels.y2.setFill(color);
    }

    public void setZ1PanelColor(Color color) {
        panels.z1Color = color;
        panels.z1.setFill(color);
    }

    public void setZ2PanelColor(Color color) {
        panels.z2Color = color;
        panels.z2.setFill(color);
    }

    // Visibility methods
    public void showAll(boolean visible) {
        showSphereGroup(visible);
        showXAxesGroup(visible);
        showYAxesGroup(visible);
        showZAxesGroup(visible);
        showX1Panel(visible);
        showX2Panel(visible);
        showY1Panel(visible);
        showY2Panel(visible);
        showZ1Panel(visible);
        showZ2Panel(visible);
        showAllGridLines(visible);
    }

    public void showAllGridLines(boolean visible) {
        showXY1GridLinesGroup(visible);
        showXX1GridLinesGroup(visible);
        showYY1GridLinesGroup(visible);
        showYX1GridLinesGroup(visible);
        showZY1GridLinesGroup(visible);
        showZX1GridLinesGroup(visible);
        showXY2GridLinesGroup(visible);
        showXX2GridLinesGroup(visible);
        showYY2GridLinesGroup(visible);
        showYX2GridLinesGroup(visible);
        showZY2GridLinesGroup(visible);
        showZX2GridLinesGroup(visible);
    }

    public void showSphereGroup(boolean visible) {
        showSphereGroup = visible;
        sphereGroup.setVisible(visible);
    }

    public void showX1Panel(boolean visible) {
        panels.showX1 = visible;
        panels.x1.setVisible(visible);
    }

    public void showX2Panel(boolean visible) {
        panels.showX2 = visible;
        panels.x2.setVisible(visible);
    }

    public void showY1Panel(boolean visible) {
        panels.showY1 = visible;
        panels.y1.setVisible(visible);
    }

    public void showY2Panel(boolean visible) {
        panels.showY2 = visible;
        panels.y2.setVisible(visible);
    }

    public void showZ1Panel(boolean visible) {
        panels.showZ1 = visible;
        panels.z1.setVisible(visible);
    }

    public void showZ2Panel(boolean visible) {
        panels.showZ2 = visible;
        panels.z2.setVisible(visible);
    }

    public void showXAxesGroup(boolean visible) {
        axes.showX = visible;
        axes.x.setVisible(visible);
    }

    public void showYAxesGroup(boolean visible) {
        axes.showY = visible;
        axes.y.setVisible(visible);
    }

    public void showZAxesGroup(boolean visible) {
        axes.showZ = visible;
        axes.z.setVisible(visible);
    }

    public void showXY1GridLinesGroup(boolean visible) {
        gridLines.showXY1 = visible;
        gridLines.xy1.setVisible(visible);
    }

    public void showXX1GridLinesGroup(boolean visible) {
        gridLines.showXX1 = visible;
        gridLines.xx1.setVisible(visible);
    }

    public void showYY1GridLinesGroup(boolean visible) {
        gridLines.showYY1 = visible;
        gridLines.yy1.setVisible(visible);
    }

    public void showYX1GridLinesGroup(boolean visible) {
        gridLines.showYX1 = visible;
        gridLines.yx1.setVisible(visible);
    }

    public void showZY1GridLinesGroup(boolean visible) {
        gridLines.showZY1 = visible;
        gridLines.zy1.setVisible(visible);
    }

    public void showZX1GridLinesGroup(boolean visible) {
        gridLines.showZX1 = visible;
        gridLines.zx1.setVisible(visible);
    }

    public void showXY2GridLinesGroup(boolean visible) {
        gridLines.showXY2 = visible;
        gridLines.xy2.setVisible(visible);
    }

    public void showXX2GridLinesGroup(boolean visible) {
        gridLines.showXX2 = visible;
        gridLines.xx2.setVisible(visible);
    }

    public void showYY2GridLinesGroup(boolean visible) {
        gridLines.showYY2 = visible;
        gridLines.yy2.setVisible(visible);
    }

    public void showYX2GridLinesGroup(boolean visible) {
        gridLines.showYX2 = visible;
        gridLines.yx2.setVisible(visible);
    }

    public void showZY2GridLinesGroup(boolean visible) {
        gridLines.showZY2 = visible;
        gridLines.zy2.setVisible(visible);
    }

    public void showZX2GridLinesGroup(boolean visible) {
        gridLines.showZX2 = visible;
        gridLines.zx2.setVisible(visible);
    }

    // Property accessors
    public boolean isSelfLightEnabled() {
        return selfLightEnabled;
    }

    public void setSelfLightEnabled(boolean selfLightEnabled) {
        this.selfLightEnabled = selfLightEnabled;
    }

    public double getScatterRadius() {
        return scatterRadius;
    }

    public void setScatterRadius(double scatterRadius) {
        this.scatterRadius = scatterRadius;
    }

    // Inner classes to organize related data

    private static class Panels {
        Rectangle x1, x2, y1, y2, z1, z2;
        boolean showX1 = true, showX2 = true;
        boolean showY1 = true, showY2 = true;
        boolean showZ1 = true, showZ2 = true;

        Color x1Color = new Color(0.5, 1.0, 0.5, DEFAULT_PANELS_OPACITY);
        Color x2Color = new Color(0.5, 1.0, 0.5, DEFAULT_PANELS_OPACITY);
        Color y1Color = new Color(0.5, 0.5, 1.0, DEFAULT_PANELS_OPACITY);
        Color y2Color = new Color(0.5, 0.5, 1.0, DEFAULT_PANELS_OPACITY);
        Color z1Color = new Color(1.0, 0.5, 0.5, DEFAULT_PANELS_OPACITY);
        Color z2Color = new Color(1.0, 0.5, 0.5, DEFAULT_PANELS_OPACITY);
    }

    private static class AxesGroups {
        final Group x = new Group();
        final Group y = new Group();
        final Group z = new Group();
        boolean showX = true, showY = true, showZ = true;
    }

    private static class GridLines {
        Group xy1, xy2, xx1, xx2;
        Group yy1, yy2, yx1, yx2;
        Group zy1, zy2, zx1, zx2;

        boolean showXY1 = true, showXY2 = true, showXX1 = true, showXX2 = true;
        boolean showYY1 = true, showYY2 = true, showYX1 = true, showYX2 = true;
        boolean showZY1 = true, showZY2 = true, showZX1 = true, showZX2 = true;
    }
}
