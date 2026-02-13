/**
 * PolyLine3D.java
 *
 * Copyright (c) 2013-2018, F(X)yz
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

package org.dynamisfx.shapes.composites;

import java.util.List;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;

/**
 * A 3D polyline that can be rendered as either a ribbon or a triangular tube.
 * <p>
 * PolyLine3D creates a visible 3D line by constructing a mesh from a series of
 * 3D points. Two line types are supported:
 * <ul>
 *   <li>{@link LineType#RIBBON} - A flat ribbon that faces the camera</li>
 *   <li>{@link LineType#TRIANGLE} - A triangular tube with depth</li>
 * </ul>
 * </p>
 *
 * @author Sean
 */
public class PolyLine3D extends Group {

    /** Default line width */
    private static final float DEFAULT_WIDTH = 2.0f;

    /** Types of 3D line rendering */
    public enum LineType {
        /** A flat ribbon mesh */
        RIBBON,
        /** A triangular tube mesh */
        TRIANGLE
    }

    private final List<Point3D> points;
    private final float width;
    private final Color color;
    private final TriangleMesh mesh;
    private final MeshView meshView;
    private final PhongMaterial material;

    /**
     * Creates a ribbon-style PolyLine3D with the specified parameters.
     *
     * @param points the 3D points defining the line path
     * @param width the width of the line
     * @param color the color of the line
     */
    public PolyLine3D(List<Point3D> points, float width, Color color) {
        this(points, width, color, LineType.RIBBON);
    }

    /**
     * Creates a PolyLine3D with the specified parameters and line type.
     *
     * @param points the 3D points defining the line path
     * @param width the width of the line
     * @param color the color of the line
     * @param lineType the type of line rendering to use
     */
    public PolyLine3D(List<Point3D> points, float width, Color color, LineType lineType) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("points cannot be null or empty");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive, got: " + width);
        }
        if (color == null) {
            throw new IllegalArgumentException("color cannot be null");
        }

        this.points = points;
        this.width = width;
        this.color = color;
        setDepthTest(DepthTest.ENABLE);

        mesh = new TriangleMesh();
        switch (lineType) {
            case TRIANGLE:
                buildTriangleTube();
                break;
            case RIBBON:
            default:
                buildRibbon();
                break;
        }

        meshView = new MeshView(mesh);
        meshView.setDrawMode(DrawMode.FILL);
        material = new PhongMaterial(color);
        material.setDiffuseColor(color);
        material.setSpecularColor(color);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.BACK);

        AmbientLight light = new AmbientLight(Color.WHITE);
        light.getScope().add(meshView);
        getChildren().add(light);
        getChildren().add(meshView);
    }

    /**
     * Returns the points defining this polyline.
     *
     * @return the list of 3D points
     */
    public List<Point3D> getPoints() {
        return points;
    }

    /**
     * Returns the width of this polyline.
     *
     * @return the line width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Returns the color of this polyline.
     *
     * @return the line color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the mesh view used to render this polyline.
     *
     * @return the mesh view
     */
    public MeshView getMeshView() {
        return meshView;
    }

    /**
     * Returns the material used for this polyline.
     *
     * @return the phong material
     */
    public PhongMaterial getMaterial() {
        return material;
    }

    private void buildTriangleTube() {
        // Triangle tube geometry constants (equilateral triangle offsets)
        final float OFFSET_A = -0.288675f;
        final float OFFSET_B = 0.5f;
        final float OFFSET_C = -0.204124f;
        final float OFFSET_D = 0.57735f;

        float half = width / 2.0f;
        for (Point3D point : points) {
            mesh.getPoints().addAll(
                point.x + OFFSET_A * half, point.y - OFFSET_B * half, point.z + OFFSET_C * half);
            mesh.getPoints().addAll(
                point.x + OFFSET_A * half, point.y + OFFSET_B * half, point.z + OFFSET_C * half);
            mesh.getPoints().addAll(
                point.x + OFFSET_D * half, point.y + OFFSET_B * half, point.z + OFFSET_C * half);
        }

        mesh.getTexCoords().addAll(0, 0);

        // Beginning end cap
        mesh.getFaces().addAll(0, 0, 1, 0, 2, 0);

        // Generate triangle strips between each point
        for (int i = 3; i < points.size() * 3; i += 3) {
            // Triangle Tube Face 1
            mesh.getFaces().addAll(i + 2, 0, i - 2, 0, i + 1, 0);
            mesh.getFaces().addAll(i + 2, 0, i - 1, 0, i - 2, 0);
            // Triangle Tube Face 2
            mesh.getFaces().addAll(i + 2, 0, i - 3, 0, i - 1, 0);
            mesh.getFaces().addAll(i, 0, i - 3, 0, i + 2, 0);
            // Triangle Tube Face 3
            mesh.getFaces().addAll(i, 0, i + 1, 0, i - 3, 0);
            mesh.getFaces().addAll(i + 1, 0, i - 2, 0, i - 3, 0);
        }

        // Final end cap
        int last = points.size() * 3 - 1;
        mesh.getFaces().addAll(last, 0, last - 1, 0, last - 2, 0);
    }

    private void buildRibbon() {
        // Add each point with a shifted duplicate for ribbon width
        for (Point3D point : points) {
            mesh.getPoints().addAll(point.x, point.y, point.z);
            mesh.getPoints().addAll(point.x, point.y, point.z + width);
        }

        mesh.getTexCoords().addAll(0, 0);

        // Generate triangle strips for each line segment
        for (int i = 2; i < points.size() * 2; i += 2) {
            // Front faces (counter-clockwise winding)
            mesh.getFaces().addAll(i, 0, i - 2, 0, i + 1, 0);
            mesh.getFaces().addAll(i + 1, 0, i - 2, 0, i - 1, 0);
            // Back faces (clockwise winding for visibility from behind)
            mesh.getFaces().addAll(i + 1, 0, i - 2, 0, i, 0);
            mesh.getFaces().addAll(i - 1, 0, i - 2, 0, i + 1, 0);
        }
    }
}
