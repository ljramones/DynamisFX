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
package org.dynamisfx.importers.dxf;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Importer for DXF (AutoCAD Drawing Exchange Format) files.
 * <p>
 * This importer supports 3DFACE and POLYLINE entities with 3D vertices.
 * Note: 3DSOLID entities use proprietary ACIS format and are not supported.
 * </p>
 *
 * @author FXyz
 */
public class DxfImporter implements Importer {

    private static final Logger LOGGER = Logger.getLogger(DxfImporter.class.getName());
    private static final String SUPPORTED_EXT = "dxf";

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
        List<float[]> allVertices = new ArrayList<>();
        List<int[]> allFaces = new ArrayList<>();
        int skippedEntities = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String groupCode;
            String value;

            while ((groupCode = reader.readLine()) != null) {
                groupCode = groupCode.trim();
                value = reader.readLine();
                if (value == null) break;
                value = value.trim();

                int code;
                try {
                    code = Integer.parseInt(groupCode);
                } catch (NumberFormatException e) {
                    continue;
                }

                // Entity type marker
                if (code == 0) {
                    if ("3DFACE".equals(value)) {
                        parse3DFace(reader, allVertices, allFaces);
                    } else if ("POLYLINE".equals(value)) {
                        parsePolyline(reader, allVertices, allFaces);
                    } else if ("3DSOLID".equals(value) || "REGION".equals(value)) {
                        skippedEntities++;
                        skipEntity(reader);
                    }
                }
            }
        }

        if (skippedEntities > 0) {
            LOGGER.warning("Skipped " + skippedEntities + " unsupported 3DSOLID/REGION entities");
        }

        if (allFaces.isEmpty()) {
            throw new IOException("No supported 3D entities (3DFACE/POLYLINE) found in DXF file");
        }

        return buildModel(allVertices, allFaces);
    }

    private void parse3DFace(BufferedReader reader, List<float[]> allVertices, List<int[]> allFaces) throws IOException {
        // 3DFACE has 4 corners (may be triangle if 4th equals 3rd)
        // Group codes: 10/20/30 = corner 1, 11/21/31 = corner 2, etc.
        Map<Integer, Float> coords = new HashMap<>();

        String groupCode;
        String value;

        while ((groupCode = reader.readLine()) != null) {
            groupCode = groupCode.trim();
            value = reader.readLine();
            if (value == null) break;
            value = value.trim();

            int code;
            try {
                code = Integer.parseInt(groupCode);
            } catch (NumberFormatException e) {
                continue;
            }

            // End of entity
            if (code == 0) {
                break;
            }

            // Coordinate group codes for 4 corners
            if ((code >= 10 && code <= 13) || (code >= 20 && code <= 23) || (code >= 30 && code <= 33)) {
                try {
                    coords.put(code, Float.parseFloat(value));
                } catch (NumberFormatException e) {
                    // Skip invalid coordinates
                }
            }
        }

        // Extract 4 corners
        float[] c1 = new float[] {
            coords.getOrDefault(10, 0f),
            coords.getOrDefault(20, 0f),
            coords.getOrDefault(30, 0f)
        };
        float[] c2 = new float[] {
            coords.getOrDefault(11, 0f),
            coords.getOrDefault(21, 0f),
            coords.getOrDefault(31, 0f)
        };
        float[] c3 = new float[] {
            coords.getOrDefault(12, 0f),
            coords.getOrDefault(22, 0f),
            coords.getOrDefault(32, 0f)
        };
        float[] c4 = new float[] {
            coords.getOrDefault(13, 0f),
            coords.getOrDefault(23, 0f),
            coords.getOrDefault(33, 0f)
        };

        int baseIdx = allVertices.size();
        allVertices.add(c1);
        allVertices.add(c2);
        allVertices.add(c3);

        // First triangle
        allFaces.add(new int[] {baseIdx, baseIdx + 1, baseIdx + 2});

        // Check if it's a quad (4th corner differs from 3rd)
        if (!arraysEqual(c3, c4)) {
            allVertices.add(c4);
            // Second triangle
            allFaces.add(new int[] {baseIdx, baseIdx + 2, baseIdx + 3});
        }
    }

    private void parsePolyline(BufferedReader reader, List<float[]> allVertices, List<int[]> allFaces) throws IOException {
        List<float[]> polyVertices = new ArrayList<>();
        boolean isMesh = false;
        int mCount = 0;
        int nCount = 0;

        String groupCode;
        String value;

        while ((groupCode = reader.readLine()) != null) {
            groupCode = groupCode.trim();
            value = reader.readLine();
            if (value == null) break;
            value = value.trim();

            int code;
            try {
                code = Integer.parseInt(groupCode);
            } catch (NumberFormatException e) {
                continue;
            }

            if (code == 0) {
                if ("VERTEX".equals(value)) {
                    float[] vertex = parseVertex(reader);
                    if (vertex != null) {
                        polyVertices.add(vertex);
                    }
                } else if ("SEQEND".equals(value)) {
                    break;
                }
            } else if (code == 70) {
                // Polyline flags - check for polygon mesh
                int flags = Integer.parseInt(value);
                isMesh = (flags & 16) != 0; // Bit 4 = polygon mesh
            } else if (code == 71) {
                mCount = Integer.parseInt(value);
            } else if (code == 72) {
                nCount = Integer.parseInt(value);
            }
        }

        if (polyVertices.size() < 3) {
            return;
        }

        int baseIdx = allVertices.size();
        allVertices.addAll(polyVertices);

        if (isMesh && mCount > 0 && nCount > 0) {
            // Create mesh grid faces
            for (int i = 0; i < mCount - 1; i++) {
                for (int j = 0; j < nCount - 1; j++) {
                    int idx00 = baseIdx + i * nCount + j;
                    int idx01 = baseIdx + i * nCount + (j + 1);
                    int idx10 = baseIdx + (i + 1) * nCount + j;
                    int idx11 = baseIdx + (i + 1) * nCount + (j + 1);

                    // Two triangles per quad
                    allFaces.add(new int[] {idx00, idx10, idx11});
                    allFaces.add(new int[] {idx00, idx11, idx01});
                }
            }
        } else {
            // Simple polygon - fan triangulation
            for (int i = 1; i < polyVertices.size() - 1; i++) {
                allFaces.add(new int[] {baseIdx, baseIdx + i, baseIdx + i + 1});
            }
        }
    }

    private float[] parseVertex(BufferedReader reader) throws IOException {
        float x = 0, y = 0, z = 0;

        String groupCode;
        String value;

        while ((groupCode = reader.readLine()) != null) {
            groupCode = groupCode.trim();
            value = reader.readLine();
            if (value == null) break;
            value = value.trim();

            int code;
            try {
                code = Integer.parseInt(groupCode);
            } catch (NumberFormatException e) {
                continue;
            }

            if (code == 0) {
                break;
            }

            try {
                switch (code) {
                    case 10: x = Float.parseFloat(value); break;
                    case 20: y = Float.parseFloat(value); break;
                    case 30: z = Float.parseFloat(value); break;
                }
            } catch (NumberFormatException e) {
                // Skip invalid coordinates
            }
        }

        return new float[] {x, y, z};
    }

    private void skipEntity(BufferedReader reader) throws IOException {
        String groupCode;
        String value;

        while ((groupCode = reader.readLine()) != null) {
            groupCode = groupCode.trim();
            value = reader.readLine();
            if (value == null) break;

            int code;
            try {
                code = Integer.parseInt(groupCode);
            } catch (NumberFormatException e) {
                continue;
            }

            if (code == 0) {
                break;
            }
        }
    }

    private boolean arraysEqual(float[] a, float[] b) {
        return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
    }

    private Model3D buildModel(List<float[]> vertices, List<int[]> faces) {
        Model3D model = new Model3D();
        TriangleMesh mesh = new TriangleMesh();

        // Add points
        float[] points = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            float[] v = vertices.get(i);
            points[i * 3] = v[0];
            points[i * 3 + 1] = v[1];
            points[i * 3 + 2] = v[2];
        }
        mesh.getPoints().addAll(points);

        // Single dummy texture coordinate
        mesh.getTexCoords().addAll(0f, 0f);

        // Add faces
        int[] facesArray = new int[faces.size() * 6];
        for (int i = 0; i < faces.size(); i++) {
            int[] face = faces.get(i);
            int base = i * 6;
            facesArray[base] = face[0];
            facesArray[base + 1] = 0;
            facesArray[base + 2] = face[1];
            facesArray[base + 3] = 0;
            facesArray[base + 4] = face[2];
            facesArray[base + 5] = 0;
        }
        mesh.getFaces().addAll(facesArray);

        MeshView meshView = new MeshView(mesh);
        meshView.setId("dxf_model");
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        model.addMeshView("dxf_model", meshView);
        model.addMaterial("default", material);

        return model;
    }
}
