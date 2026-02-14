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

package org.dynamisfx.importers.x3d;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for X3D files.
 * <p>
 * X3D is an XML-based file format for representing 3D computer graphics.
 * This importer supports IndexedFaceSet and IndexedTriangleSet geometry nodes.
 * </p>
 *
 * @author FXyz
 */
public class X3dImporter implements Importer {

    private static final String SUPPORTED_EXT = "x3d";

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
        Model3D model = new Model3D();
        int meshIndex = 0;

        try (InputStream is = url.openStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            // Disable external DTD loading to avoid network access and validation errors
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            // Find all Shape elements
            NodeList shapes = doc.getElementsByTagName("Shape");

            for (int i = 0; i < shapes.getLength(); i++) {
                Element shape = (Element) shapes.item(i);
                MeshView meshView = parseShape(shape);

                if (meshView != null) {
                    String name = "mesh_" + meshIndex;

                    // Try to get DEF attribute as name
                    String def = shape.getAttribute("DEF");
                    if (def != null && !def.isEmpty()) {
                        name = def;
                    }

                    meshView.setId(name);
                    model.addMeshView(name, meshView);
                    meshIndex++;
                }
            }

            if (meshIndex == 0) {
                throw new IOException("No meshes found in X3D file");
            }

            // Add default material
            PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
            model.addMaterial("default", material);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error parsing X3D file: " + e.getMessage(), e);
        }

        return model;
    }

    private MeshView parseShape(Element shape) {
        // Look for IndexedFaceSet or IndexedTriangleSet
        Element geometryElement = null;
        String geometryType = null;

        NodeList faceSetNodes = shape.getElementsByTagName("IndexedFaceSet");
        if (faceSetNodes.getLength() > 0) {
            geometryElement = (Element) faceSetNodes.item(0);
            geometryType = "IndexedFaceSet";
        }

        if (geometryElement == null) {
            NodeList triSetNodes = shape.getElementsByTagName("IndexedTriangleSet");
            if (triSetNodes.getLength() > 0) {
                geometryElement = (Element) triSetNodes.item(0);
                geometryType = "IndexedTriangleSet";
            }
        }

        if (geometryElement == null) {
            return null;
        }

        // Parse coordinates
        float[] coordinates = null;
        NodeList coordNodes = geometryElement.getElementsByTagName("Coordinate");
        if (coordNodes.getLength() > 0) {
            Element coordElement = (Element) coordNodes.item(0);
            String pointAttr = coordElement.getAttribute("point");
            if (pointAttr != null && !pointAttr.isEmpty()) {
                coordinates = parseFloatArray(pointAttr);
            }
        }

        if (coordinates == null || coordinates.length == 0) {
            return null;
        }

        // Parse texture coordinates
        float[] texCoords = null;
        NodeList texCoordNodes = geometryElement.getElementsByTagName("TextureCoordinate");
        if (texCoordNodes.getLength() > 0) {
            Element texCoordElement = (Element) texCoordNodes.item(0);
            String pointAttr = texCoordElement.getAttribute("point");
            if (pointAttr != null && !pointAttr.isEmpty()) {
                texCoords = parseFloatArray(pointAttr);
            }
        }

        // Parse indices
        List<Integer> triangleIndices = new ArrayList<>();

        if ("IndexedFaceSet".equals(geometryType)) {
            String coordIndex = geometryElement.getAttribute("coordIndex");
            if (coordIndex != null && !coordIndex.isEmpty()) {
                triangleIndices = parseIndexedFaceSet(coordIndex);
            }
        } else if ("IndexedTriangleSet".equals(geometryType)) {
            String index = geometryElement.getAttribute("index");
            if (index != null && !index.isEmpty()) {
                int[] indices = parseIntArray(index);
                for (int idx : indices) {
                    triangleIndices.add(idx);
                }
            }
        }

        if (triangleIndices.isEmpty()) {
            return null;
        }

        return buildMeshView(coordinates, texCoords, triangleIndices);
    }

    private List<Integer> parseIndexedFaceSet(String coordIndex) {
        // IndexedFaceSet uses -1 as face delimiter
        int[] indices = parseIntArray(coordIndex);
        List<Integer> triangles = new ArrayList<>();

        List<Integer> currentFace = new ArrayList<>();
        for (int idx : indices) {
            if (idx == -1) {
                // End of face - triangulate
                if (currentFace.size() >= 3) {
                    // Fan triangulation
                    int v0 = currentFace.get(0);
                    for (int j = 1; j < currentFace.size() - 1; j++) {
                        triangles.add(v0);
                        triangles.add(currentFace.get(j));
                        triangles.add(currentFace.get(j + 1));
                    }
                }
                currentFace.clear();
            } else {
                currentFace.add(idx);
            }
        }

        // Handle last face if no trailing -1
        if (currentFace.size() >= 3) {
            int v0 = currentFace.get(0);
            for (int j = 1; j < currentFace.size() - 1; j++) {
                triangles.add(v0);
                triangles.add(currentFace.get(j));
                triangles.add(currentFace.get(j + 1));
            }
        }

        return triangles;
    }

    private float[] parseFloatArray(String text) {
        // X3D uses comma and/or space as delimiter
        String[] parts = text.replace(",", " ").trim().split("\\s+");
        List<Float> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(Float.parseFloat(part));
            }
        }
        float[] arr = new float[result.size()];
        for (int i = 0; i < result.size(); i++) {
            arr[i] = result.get(i);
        }
        return arr;
    }

    private int[] parseIntArray(String text) {
        // X3D uses comma and/or space as delimiter
        String[] parts = text.replace(",", " ").trim().split("\\s+");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(Integer.parseInt(part));
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    private MeshView buildMeshView(float[] coordinates, float[] texCoords,
                                   List<Integer> triangleIndices) {
        TriangleMesh mesh = new TriangleMesh();

        // Add coordinates
        mesh.getPoints().addAll(coordinates);

        // Add texture coordinates (or default)
        if (texCoords != null && texCoords.length >= 2) {
            mesh.getTexCoords().addAll(texCoords);
        } else {
            // Default tex coords for each vertex
            int numVertices = coordinates.length / 3;
            float[] defaultTexCoords = new float[numVertices * 2];
            mesh.getTexCoords().addAll(defaultTexCoords);
        }

        // Build faces
        int numTriangles = triangleIndices.size() / 3;
        int[] faces = new int[numTriangles * 6];

        for (int i = 0; i < numTriangles; i++) {
            int v0 = triangleIndices.get(i * 3);
            int v1 = triangleIndices.get(i * 3 + 1);
            int v2 = triangleIndices.get(i * 3 + 2);

            // Use vertex index as texcoord index
            faces[i * 6] = v0;
            faces[i * 6 + 1] = v0;
            faces[i * 6 + 2] = v1;
            faces[i * 6 + 3] = v1;
            faces[i * 6 + 4] = v2;
            faces[i * 6 + 5] = v2;
        }

        mesh.getFaces().addAll(faces);

        MeshView meshView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        return meshView;
    }
}
