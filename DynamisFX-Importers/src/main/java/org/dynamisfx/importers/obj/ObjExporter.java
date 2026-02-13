/*
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
package org.dynamisfx.importers.obj;

import javafx.scene.paint.Color;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exporter for OBJ (Wavefront) files.
 * <p>
 * Exports mesh geometry to OBJ format with an accompanying MTL material file.
 * </p>
 *
 * @author FXyz
 */
public class ObjExporter implements Exporter {

    private Color diffuseColor = Color.LIGHTGRAY;

    /**
     * Creates an OBJ exporter with default settings.
     */
    public ObjExporter() {
    }

    /**
     * Sets the diffuse color for the material.
     *
     * @param color the diffuse color
     */
    public void setDiffuseColor(Color color) {
        this.diffuseColor = color;
    }

    /**
     * Returns the diffuse color.
     *
     * @return the diffuse color
     */
    public Color getDiffuseColor() {
        return diffuseColor;
    }

    @Override
    public void export(TriangleMesh mesh, File file) throws IOException {
        export(mesh, file, "mesh");
    }

    @Override
    public void export(TriangleMesh mesh, File file, String meshName) throws IOException {
        // Ensure .obj extension
        String baseName = file.getName();
        if (baseName.toLowerCase().endsWith(".obj")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        File objFile = new File(file.getParentFile(), baseName + ".obj");
        File mtlFile = new File(file.getParentFile(), baseName + ".mtl");

        exportObjFile(mesh, objFile, baseName, meshName);
        exportMtlFile(mtlFile, baseName);
    }

    @Override
    public String getExtension() {
        return "obj";
    }

    @Override
    public String getFormatDescription() {
        return "Wavefront OBJ";
    }

    private void exportObjFile(TriangleMesh mesh, File file, String mtlName, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        float[] texCoords = new float[mesh.getTexCoords().size()];
        mesh.getTexCoords().toArray(texCoords);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int[] smoothingGroups = new int[mesh.getFaceSmoothingGroups().size()];
        mesh.getFaceSmoothingGroups().toArray(smoothingGroups);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write("# DynamisFX OBJ Export");
            writer.newLine();
            writer.write("# https://github.com/FXyz/FXyz");
            writer.newLine();
            writer.newLine();

            // Material reference
            writer.write("mtllib " + mtlName + ".mtl");
            writer.newLine();
            writer.newLine();

            // Object name
            writer.write("o " + meshName);
            writer.newLine();
            writer.newLine();

            // Vertices
            writer.write("# Vertices: " + (points.length / 3));
            writer.newLine();
            for (int i = 0; i < points.length; i += 3) {
                writer.write("v " + points[i] + " " + points[i + 1] + " " + points[i + 2]);
                writer.newLine();
            }
            writer.newLine();

            // Texture coordinates
            writer.write("# Texture coordinates: " + (texCoords.length / 2));
            writer.newLine();
            for (int i = 0; i < texCoords.length; i += 2) {
                // OBJ uses (1 - v) for texture coordinates
                writer.write("vt " + texCoords[i] + " " + (1.0f - texCoords[i + 1]));
                writer.newLine();
            }
            writer.newLine();

            // Use material
            writer.write("usemtl " + mtlName);
            writer.newLine();

            // Faces
            writer.write("# Faces: " + (faces.length / 6));
            writer.newLine();

            int currentSmoothGroup = -1;
            for (int i = 0; i < faces.length; i += 6) {
                // Check smoothing group
                int faceIndex = i / 6;
                if (faceIndex < smoothingGroups.length) {
                    int sg = smoothingGroups[faceIndex];
                    if (sg != currentSmoothGroup) {
                        currentSmoothGroup = sg;
                        writer.write("s " + (sg > 0 ? sg : "off"));
                        writer.newLine();
                    }
                }

                // OBJ indices are 1-based
                int v1 = faces[i] + 1;
                int t1 = faces[i + 1] + 1;
                int v2 = faces[i + 2] + 1;
                int t2 = faces[i + 3] + 1;
                int v3 = faces[i + 4] + 1;
                int t3 = faces[i + 5] + 1;

                writer.write("f " + v1 + "/" + t1 + " " + v2 + "/" + t2 + " " + v3 + "/" + t3);
                writer.newLine();
            }
        }
    }

    private void exportMtlFile(File file, String mtlName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# DynamisFX MTL Export");
            writer.newLine();
            writer.newLine();

            writer.write("newmtl " + mtlName);
            writer.newLine();
            writer.write("illum 2");
            writer.newLine();

            // Diffuse color
            writer.write("Kd " + diffuseColor.getRed() + " " +
                        diffuseColor.getGreen() + " " +
                        diffuseColor.getBlue());
            writer.newLine();

            // Ambient color (darker version of diffuse)
            writer.write("Ka " + (diffuseColor.getRed() * 0.1) + " " +
                        (diffuseColor.getGreen() * 0.1) + " " +
                        (diffuseColor.getBlue() * 0.1));
            writer.newLine();

            // Specular color
            writer.write("Ks 1.0 1.0 1.0");
            writer.newLine();

            // Specular exponent
            writer.write("Ns 32.0");
            writer.newLine();

            // Transparency
            writer.write("d " + diffuseColor.getOpacity());
            writer.newLine();
        }
    }
}
