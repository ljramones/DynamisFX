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

package org.dynamisfx.importers.vrml;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for VRML 2.0 (.wrl) files.
 * <p>
 * VRML (Virtual Reality Modeling Language) is a text-based format for 3D graphics.
 * This importer supports IndexedFaceSet geometry nodes with Coordinate points.
 * </p>
 *
 * @author FXyz
 */
public class VrmlImporter implements Importer {

    private static final String SUPPORTED_EXT = "wrl";

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        // VRML triangulation handled during import
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Remove comments
                int commentIdx = line.indexOf('#');
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx);
                }
                content.append(line).append(" ");
            }
        }

        return parseVrml(content.toString());
    }

    private Model3D parseVrml(String content) throws IOException {
        Model3D model = new Model3D();
        int meshIndex = 0;

        // Find all IndexedFaceSet nodes using brace matching
        int searchStart = 0;
        while (true) {
            int ifsStart = content.indexOf("IndexedFaceSet", searchStart);
            if (ifsStart < 0) break;

            // Find the opening brace
            int braceStart = content.indexOf('{', ifsStart);
            if (braceStart < 0) break;

            // Match braces to find end of IndexedFaceSet
            String ifsContent = extractBraceContent(content, braceStart);
            if (ifsContent == null) break;

            searchStart = braceStart + ifsContent.length() + 2;

            // Extract coordinates
            List<Float> points = extractCoordinates(ifsContent);
            if (points.isEmpty()) {
                continue;
            }

            // Extract face indices
            List<Integer> coordIndex = extractCoordIndex(ifsContent);
            if (coordIndex.isEmpty()) {
                continue;
            }

            // Build triangulated mesh
            TriangleMesh mesh = buildMesh(points, coordIndex);
            if (mesh != null) {
                String name = "vrml_mesh_" + meshIndex;
                MeshView meshView = new MeshView(mesh);
                meshView.setId(name);
                PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
                meshView.setMaterial(material);
                meshView.setCullFace(CullFace.NONE);

                model.addMeshView(name, meshView);
                model.addMaterial("material_" + meshIndex, material);
                meshIndex++;
            }
        }

        if (meshIndex == 0) {
            throw new IOException("No IndexedFaceSet geometry found in VRML file");
        }

        return model;
    }

    private String extractBraceContent(String content, int openBracePos) {
        int depth = 0;
        int start = openBracePos + 1;

        for (int i = openBracePos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return content.substring(start, i);
                }
            }
        }
        return null;
    }

    private List<Float> extractCoordinates(String ifsContent) {
        List<Float> points = new ArrayList<>();

        // Find "point [" within Coordinate node
        int coordStart = ifsContent.indexOf("Coordinate");
        if (coordStart < 0) {
            // Try lowercase
            coordStart = ifsContent.indexOf("coordinate");
        }
        if (coordStart < 0) return points;

        int pointStart = ifsContent.indexOf("point", coordStart);
        if (pointStart < 0) return points;

        int bracketStart = ifsContent.indexOf('[', pointStart);
        if (bracketStart < 0) return points;

        int bracketEnd = ifsContent.indexOf(']', bracketStart);
        if (bracketEnd < 0) return points;

        String pointData = ifsContent.substring(bracketStart + 1, bracketEnd);
        parseNumberArray(pointData, points);

        return points;
    }

    private List<Integer> extractCoordIndex(String ifsContent) {
        List<Integer> indices = new ArrayList<>();

        // Find coordIndex array
        int indexStart = ifsContent.indexOf("coordIndex");
        if (indexStart < 0) return indices;

        int bracketStart = ifsContent.indexOf('[', indexStart);
        if (bracketStart < 0) return indices;

        int bracketEnd = ifsContent.indexOf(']', bracketStart);
        if (bracketEnd < 0) return indices;

        String indexData = ifsContent.substring(bracketStart + 1, bracketEnd);
        parseIntArray(indexData, indices);

        return indices;
    }

    private void parseNumberArray(String data, List<Float> result) {
        // Split by commas and whitespace
        String[] tokens = data.trim().split("[,\\s]+");
        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                try {
                    result.add(Float.parseFloat(token));
                } catch (NumberFormatException e) {
                    // Skip invalid tokens
                }
            }
        }
    }

    private void parseIntArray(String data, List<Integer> result) {
        // Split by commas and whitespace
        String[] tokens = data.trim().split("[,\\s]+");
        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                try {
                    result.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    // Skip invalid tokens
                }
            }
        }
    }

    private TriangleMesh buildMesh(List<Float> points, List<Integer> coordIndex) {
        TriangleMesh mesh = new TriangleMesh();

        // Add points
        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }
        mesh.getPoints().addAll(pointsArray);

        // Single dummy texture coordinate
        mesh.getTexCoords().addAll(0f, 0f);

        // Parse faces (separated by -1)
        List<Integer> faces = new ArrayList<>();
        List<Integer> currentFace = new ArrayList<>();

        for (int idx : coordIndex) {
            if (idx == -1) {
                // End of face - triangulate if needed
                triangulate(currentFace, faces);
                currentFace.clear();
            } else {
                currentFace.add(idx);
            }
        }

        // Handle last face if no trailing -1
        if (!currentFace.isEmpty()) {
            triangulate(currentFace, faces);
        }

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    private void triangulate(List<Integer> faceIndices, List<Integer> triangles) {
        if (faceIndices.size() < 3) {
            return;
        }

        // Fan triangulation from first vertex
        int v0 = faceIndices.get(0);
        for (int i = 1; i < faceIndices.size() - 1; i++) {
            int v1 = faceIndices.get(i);
            int v2 = faceIndices.get(i + 1);

            // vertex index, texcoord index pairs
            triangles.add(v0);
            triangles.add(0);
            triangles.add(v1);
            triangles.add(0);
            triangles.add(v2);
            triangles.add(0);
        }
    }
}
