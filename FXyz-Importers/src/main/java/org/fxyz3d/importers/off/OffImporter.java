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
package org.fxyz3d.importers.off;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Importer;
import org.fxyz3d.importers.Model3D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for OFF (Object File Format) files.
 * <p>
 * OFF is a simple ASCII format for storing 3D geometry. It stores vertices
 * and faces, with optional per-face colors.
 * </p>
 * <p>
 * Format specification:
 * <pre>
 * OFF
 * vertex_count face_count edge_count
 * x1 y1 z1
 * x2 y2 z2
 * ...
 * n v1 v2 v3 ... [R G B A]
 * </pre>
 * </p>
 *
 * @author FXyz
 */
public class OffImporter implements Importer {

    private static final String SUPPORTED_EXT = "off";

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            // Read header - first non-empty, non-comment line should be "OFF" or "[C]OFF"
            String line = readNextLine(reader);
            if (line == null) {
                throw new IOException("Empty OFF file");
            }

            // Check for OFF or COFF header
            boolean hasVertexColors = false;
            if (line.equalsIgnoreCase("COFF")) {
                hasVertexColors = true;
            } else if (!line.equalsIgnoreCase("OFF")) {
                throw new IOException("Invalid OFF file: missing 'OFF' header, found: " + line);
            }

            // Read counts: vertex_count face_count edge_count
            line = readNextLine(reader);
            if (line == null) {
                throw new IOException("Invalid OFF file: missing counts");
            }

            String[] counts = line.split("\\s+");
            if (counts.length < 2) {
                throw new IOException("Invalid OFF file: expected at least vertex and face counts");
            }

            int vertexCount = Integer.parseInt(counts[0]);
            int faceCount = Integer.parseInt(counts[1]);
            // edge_count is ignored (often 0)

            // Read vertices
            List<Float> vertices = new ArrayList<>();
            List<Float> texCoords = new ArrayList<>();

            for (int i = 0; i < vertexCount; i++) {
                line = readNextLine(reader);
                if (line == null) {
                    throw new IOException("Unexpected end of file reading vertex " + i);
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 3) {
                    throw new IOException("Invalid vertex at line " + i + ": " + line);
                }

                vertices.add(Float.parseFloat(parts[0]));
                vertices.add(Float.parseFloat(parts[1]));
                vertices.add(Float.parseFloat(parts[2]));

                // Default texture coordinates
                texCoords.add(0f);
                texCoords.add(0f);
            }

            // Read faces and triangulate
            List<Integer> faces = new ArrayList<>();

            for (int i = 0; i < faceCount; i++) {
                line = readNextLine(reader);
                if (line == null) {
                    throw new IOException("Unexpected end of file reading face " + i);
                }

                String[] parts = line.split("\\s+");
                int verticesInFace = Integer.parseInt(parts[0]);

                if (parts.length < verticesInFace + 1) {
                    throw new IOException("Invalid face at index " + i + ": not enough vertex indices");
                }

                // Read vertex indices
                int[] faceVertices = new int[verticesInFace];
                for (int j = 0; j < verticesInFace; j++) {
                    faceVertices[j] = Integer.parseInt(parts[j + 1]);
                }

                // Triangulate using fan triangulation
                // For a polygon with vertices v0, v1, v2, v3, ...
                // Create triangles: (v0, v1, v2), (v0, v2, v3), (v0, v3, v4), ...
                int v0 = faceVertices[0];
                for (int j = 1; j < verticesInFace - 1; j++) {
                    int v1 = faceVertices[j];
                    int v2 = faceVertices[j + 1];
                    faces.add(v0);
                    faces.add(v1);
                    faces.add(v2);
                }
            }

            return buildModel(vertices, texCoords, faces);
        }
    }

    private String readNextLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // Skip empty lines and comments
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }

    private Model3D buildModel(List<Float> vertices, List<Float> texCoords, List<Integer> faces) {
        Model3D model = new Model3D();

        TriangleMesh mesh = new TriangleMesh();

        // Add vertices
        float[] points = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            points[i] = vertices.get(i);
        }
        mesh.getPoints().addAll(points);

        // Add texture coordinates
        float[] uvs = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            uvs[i] = texCoords.get(i);
        }
        mesh.getTexCoords().addAll(uvs);

        // Add faces (vertex index, texcoord index pairs)
        int[] faceArray = new int[faces.size() * 2];
        for (int i = 0; i < faces.size(); i++) {
            faceArray[i * 2] = faces.get(i);
            faceArray[i * 2 + 1] = faces.get(i); // Use same index for texcoords
        }
        mesh.getFaces().addAll(faceArray);

        MeshView meshView = new MeshView(mesh);
        meshView.setId("off_mesh");
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        model.addMeshView("off_mesh", meshView);
        model.addMaterial("default", material);

        return model;
    }
}
