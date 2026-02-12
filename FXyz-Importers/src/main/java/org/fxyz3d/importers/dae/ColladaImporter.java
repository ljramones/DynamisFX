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
package org.fxyz3d.importers.dae;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Importer;
import org.fxyz3d.importers.Model3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer for COLLADA (.dae) files.
 * <p>
 * COLLADA is an XML-based interchange format for 3D assets. This importer
 * supports loading geometry from library_geometries, including vertices,
 * normals, texture coordinates, and triangle/polylist primitives.
 * </p>
 *
 * @author FXyz
 */
public class ColladaImporter implements Importer {

    private static final String SUPPORTED_EXT = "dae";

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
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            // Find all geometry elements
            NodeList geometries = doc.getElementsByTagName("geometry");

            for (int i = 0; i < geometries.getLength(); i++) {
                Element geometry = (Element) geometries.item(i);
                String geometryId = geometry.getAttribute("id");
                String geometryName = geometry.getAttribute("name");
                if (geometryName.isEmpty()) {
                    geometryName = geometryId;
                }
                if (geometryName.isEmpty()) {
                    geometryName = "mesh_" + meshIndex;
                }

                // Find mesh element within geometry
                NodeList meshElements = geometry.getElementsByTagName("mesh");
                if (meshElements.getLength() == 0) {
                    continue;
                }

                Element meshElement = (Element) meshElements.item(0);
                MeshView meshView = parseMesh(meshElement);
                if (meshView != null) {
                    meshView.setId(geometryName);
                    model.addMeshView(geometryName, meshView);
                    meshIndex++;
                }
            }

            if (meshIndex == 0) {
                throw new IOException("No meshes found in COLLADA file");
            }

            // Add default material
            PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
            model.addMaterial("default", material);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error parsing COLLADA file: " + e.getMessage(), e);
        }

        return model;
    }

    private MeshView parseMesh(Element meshElement) throws IOException {
        // Parse all source elements into a map
        Map<String, float[]> sources = new HashMap<>();
        NodeList sourceNodes = meshElement.getElementsByTagName("source");

        for (int i = 0; i < sourceNodes.getLength(); i++) {
            Element source = (Element) sourceNodes.item(i);
            String sourceId = source.getAttribute("id");

            NodeList floatArrays = source.getElementsByTagName("float_array");
            if (floatArrays.getLength() > 0) {
                Element floatArray = (Element) floatArrays.item(0);
                String text = floatArray.getTextContent().trim();
                float[] data = parseFloatArray(text);
                sources.put(sourceId, data);
            }
        }

        // Parse vertices element to get position source reference
        String positionSourceId = null;
        NodeList verticesNodes = meshElement.getElementsByTagName("vertices");
        if (verticesNodes.getLength() > 0) {
            Element vertices = (Element) verticesNodes.item(0);
            String verticesId = vertices.getAttribute("id");

            NodeList inputs = vertices.getElementsByTagName("input");
            for (int i = 0; i < inputs.getLength(); i++) {
                Element input = (Element) inputs.item(i);
                if ("POSITION".equals(input.getAttribute("semantic"))) {
                    positionSourceId = input.getAttribute("source").substring(1); // Remove #
                    break;
                }
            }

            // Also map the vertices id to the position source
            if (positionSourceId != null && sources.containsKey(positionSourceId)) {
                sources.put(verticesId, sources.get(positionSourceId));
            }
        }

        // Parse triangles or polylist
        List<Integer> vertexIndices = new ArrayList<>();
        List<Integer> texCoordIndices = new ArrayList<>();
        String positionSource = null;
        String texCoordSource = null;
        int positionOffset = 0;
        int texCoordOffset = -1;
        int stride = 1;

        // Try triangles first
        NodeList trianglesNodes = meshElement.getElementsByTagName("triangles");
        Element primitiveElement = null;

        if (trianglesNodes.getLength() > 0) {
            primitiveElement = (Element) trianglesNodes.item(0);
        } else {
            // Try polylist
            NodeList polylistNodes = meshElement.getElementsByTagName("polylist");
            if (polylistNodes.getLength() > 0) {
                primitiveElement = (Element) polylistNodes.item(0);
            }
        }

        if (primitiveElement == null) {
            return null;
        }

        // Parse input elements to get source references and offsets
        NodeList inputs = primitiveElement.getElementsByTagName("input");
        for (int i = 0; i < inputs.getLength(); i++) {
            Element input = (Element) inputs.item(i);
            String semantic = input.getAttribute("semantic");
            String source = input.getAttribute("source").substring(1); // Remove #
            int offset = 0;
            if (input.hasAttribute("offset")) {
                offset = Integer.parseInt(input.getAttribute("offset"));
            }
            stride = Math.max(stride, offset + 1);

            if ("VERTEX".equals(semantic) || "POSITION".equals(semantic)) {
                positionSource = source;
                positionOffset = offset;
            } else if ("TEXCOORD".equals(semantic)) {
                texCoordSource = source;
                texCoordOffset = offset;
            }
        }

        // Parse the p (primitive) element containing indices
        NodeList pNodes = primitiveElement.getElementsByTagName("p");
        if (pNodes.getLength() == 0) {
            return null;
        }

        Element pElement = (Element) pNodes.item(0);
        String pText = pElement.getTextContent().trim();
        int[] indices = parseIntArray(pText);

        // Handle polylist vcount if present
        int[] vcounts = null;
        if ("polylist".equals(primitiveElement.getTagName())) {
            NodeList vcountNodes = primitiveElement.getElementsByTagName("vcount");
            if (vcountNodes.getLength() > 0) {
                String vcountText = ((Element) vcountNodes.item(0)).getTextContent().trim();
                vcounts = parseIntArray(vcountText);
            }
        }

        // Extract vertex and texcoord indices
        if (vcounts != null) {
            // Polylist: triangulate polygons
            int indexPos = 0;
            for (int vcount : vcounts) {
                // Fan triangulation
                int v0Pos = indexPos;
                for (int j = 1; j < vcount - 1; j++) {
                    // Triangle: v0, vj, vj+1
                    vertexIndices.add(indices[v0Pos + positionOffset]);
                    vertexIndices.add(indices[indexPos + j * stride + positionOffset]);
                    vertexIndices.add(indices[indexPos + (j + 1) * stride + positionOffset]);

                    if (texCoordOffset >= 0) {
                        texCoordIndices.add(indices[v0Pos + texCoordOffset]);
                        texCoordIndices.add(indices[indexPos + j * stride + texCoordOffset]);
                        texCoordIndices.add(indices[indexPos + (j + 1) * stride + texCoordOffset]);
                    }
                }
                indexPos += vcount * stride;
            }
        } else {
            // Triangles: direct
            for (int i = 0; i < indices.length; i += stride) {
                vertexIndices.add(indices[i + positionOffset]);
                if (texCoordOffset >= 0) {
                    texCoordIndices.add(indices[i + texCoordOffset]);
                }
            }
        }

        // Get position data
        float[] positions = sources.get(positionSource);
        if (positions == null) {
            throw new IOException("Position source not found: " + positionSource);
        }

        // Get texcoord data (or create default)
        float[] texCoords;
        if (texCoordSource != null && sources.containsKey(texCoordSource)) {
            texCoords = sources.get(texCoordSource);
        } else {
            // Default tex coords
            texCoords = new float[]{0, 0};
        }

        return buildMeshView(positions, texCoords, vertexIndices, texCoordIndices);
    }

    private float[] parseFloatArray(String text) {
        String[] parts = text.split("\\s+");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result[i] = Float.parseFloat(parts[i]);
            }
        }
        return result;
    }

    private int[] parseIntArray(String text) {
        String[] parts = text.split("\\s+");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(Integer.parseInt(part));
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    private MeshView buildMeshView(float[] positions, float[] texCoords,
                                   List<Integer> vertexIndices, List<Integer> texCoordIndices) {
        TriangleMesh mesh = new TriangleMesh();

        // Add positions
        mesh.getPoints().addAll(positions);

        // Add texture coordinates
        if (texCoords.length >= 2) {
            mesh.getTexCoords().addAll(texCoords);
        } else {
            mesh.getTexCoords().addAll(0, 0);
        }

        // Build faces
        int numTriangles = vertexIndices.size() / 3;
        int[] faces = new int[numTriangles * 6];

        boolean hasTexCoords = !texCoordIndices.isEmpty();

        for (int i = 0; i < numTriangles; i++) {
            int v0 = vertexIndices.get(i * 3);
            int v1 = vertexIndices.get(i * 3 + 1);
            int v2 = vertexIndices.get(i * 3 + 2);

            int t0 = hasTexCoords ? texCoordIndices.get(i * 3) : 0;
            int t1 = hasTexCoords ? texCoordIndices.get(i * 3 + 1) : 0;
            int t2 = hasTexCoords ? texCoordIndices.get(i * 3 + 2) : 0;

            faces[i * 6] = v0;
            faces[i * 6 + 1] = t0;
            faces[i * 6 + 2] = v1;
            faces[i * 6 + 3] = t1;
            faces[i * 6 + 4] = v2;
            faces[i * 6 + 5] = t2;
        }

        mesh.getFaces().addAll(faces);

        MeshView meshView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        return meshView;
    }
}
