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
 */

package org.dynamisfx.importers.stl;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for STL (Stereolithography) files.
 * <p>
 * Supports both ASCII and binary STL formats. STL files contain triangulated
 * surface geometry, commonly used for 3D printing and CAD applications.
 * </p>
 *
 * @author FXyz
 */
public class StlImporter implements Importer {

    private static final String SUPPORTED_EXT = "stl";

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        // STL doesn't support polygons, just triangles
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        // Peek at the file to determine if it's ASCII or binary
        try (InputStream is = url.openStream()) {
            byte[] header = new byte[80];
            int bytesRead = is.read(header);
            if (bytesRead < 80) {
                throw new IOException("Invalid STL file: too short");
            }

            // Check if it starts with "solid" (ASCII format)
            String headerStr = new String(header, StandardCharsets.US_ASCII).trim();
            if (headerStr.startsWith("solid")) {
                // Could be ASCII, but need to verify (binary files can also start with "solid")
                // Read more to check for "facet" keyword
                byte[] peek = new byte[256];
                is.read(peek);
                String peekStr = new String(peek, StandardCharsets.US_ASCII);
                if (peekStr.contains("facet") || peekStr.contains("endsolid")) {
                    return readAscii(url);
                }
            }
            return readBinary(url);
        }
    }

    private Model3D readAscii(URL url) throws IOException {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        String modelName = "stl_model";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String line;
            float nx = 0, ny = 0, nz = 0;
            int vertexCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();

                if (line.startsWith("solid ")) {
                    modelName = line.substring(6).trim();
                    if (modelName.isEmpty()) {
                        modelName = "stl_model";
                    }
                } else if (line.startsWith("facet normal ")) {
                    String[] parts = line.substring(13).trim().split("\\s+");
                    nx = Float.parseFloat(parts[0]);
                    ny = Float.parseFloat(parts[1]);
                    nz = Float.parseFloat(parts[2]);
                    vertexCount = 0;
                } else if (line.startsWith("vertex ")) {
                    String[] parts = line.substring(7).trim().split("\\s+");
                    vertices.add(Float.parseFloat(parts[0]));
                    vertices.add(Float.parseFloat(parts[1]));
                    vertices.add(Float.parseFloat(parts[2]));
                    normals.add(nx);
                    normals.add(ny);
                    normals.add(nz);
                    vertexCount++;
                }
            }
        }

        return buildModel(modelName, vertices, normals);
    }

    private Model3D readBinary(URL url) throws IOException {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(url.openStream())) {
            // Skip 80-byte header
            dis.skipBytes(80);

            // Read triangle count (little-endian)
            byte[] countBytes = new byte[4];
            dis.readFully(countBytes);
            int triangleCount = ByteBuffer.wrap(countBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

            // Each triangle: 12 floats (normal + 3 vertices) + 2 byte attribute
            byte[] triangleData = new byte[50]; // 12*4 + 2

            for (int i = 0; i < triangleCount; i++) {
                dis.readFully(triangleData);
                ByteBuffer bb = ByteBuffer.wrap(triangleData).order(ByteOrder.LITTLE_ENDIAN);

                // Normal
                float nx = bb.getFloat();
                float ny = bb.getFloat();
                float nz = bb.getFloat();

                // Three vertices
                for (int v = 0; v < 3; v++) {
                    vertices.add(bb.getFloat());
                    vertices.add(bb.getFloat());
                    vertices.add(bb.getFloat());
                    normals.add(nx);
                    normals.add(ny);
                    normals.add(nz);
                }
                // Skip attribute byte count (2 bytes, already read in triangleData)
            }
        }

        return buildModel("stl_model", vertices, normals);
    }

    private Model3D buildModel(String name, List<Float> vertices, List<Float> normals) {
        Model3D model = new Model3D();

        TriangleMesh mesh = new TriangleMesh();

        // Add vertices
        float[] points = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            points[i] = vertices.get(i);
        }
        mesh.getPoints().addAll(points);

        // STL doesn't have texture coordinates, use dummy values
        mesh.getTexCoords().addAll(0f, 0f);

        // Build faces - each 3 consecutive vertices form a triangle
        int numTriangles = vertices.size() / 9;
        int[] faces = new int[numTriangles * 6];
        for (int i = 0; i < numTriangles; i++) {
            int baseVertex = i * 3;
            int baseFace = i * 6;
            // vertex index, texcoord index (always 0)
            faces[baseFace] = baseVertex;
            faces[baseFace + 1] = 0;
            faces[baseFace + 2] = baseVertex + 1;
            faces[baseFace + 3] = 0;
            faces[baseFace + 4] = baseVertex + 2;
            faces[baseFace + 5] = 0;
        }
        mesh.getFaces().addAll(faces);

        // Create mesh view
        MeshView meshView = new MeshView(mesh);
        meshView.setId(name);
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        model.addMeshView(name, meshView);
        model.addMaterial("default", material);

        return model;
    }
}
