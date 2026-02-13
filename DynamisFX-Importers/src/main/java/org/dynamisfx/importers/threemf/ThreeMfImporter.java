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
package org.dynamisfx.importers.threemf;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Importer for 3MF (3D Manufacturing Format) files.
 * <p>
 * 3MF is a modern 3D printing file format that uses a ZIP container with XML files.
 * The primary model data is stored in "3D/3dmodel.model" as XML containing vertices
 * and triangles.
 * </p>
 *
 * @author FXyz
 */
public class ThreeMfImporter implements Importer {

    private static final String SUPPORTED_EXT = "3mf";

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        // 3MF only contains triangles
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        File tempFile = null;
        try {
            // 3MF files are ZIP archives, need to access as file
            if ("file".equals(url.getProtocol())) {
                return readFromFile(new File(url.toURI()));
            } else {
                // Download to temp file for non-file URLs
                tempFile = File.createTempFile("fxyz3d_3mf_", ".3mf");
                tempFile.deleteOnExit();
                try (InputStream in = url.openStream();
                     java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                    in.transferTo(out);
                }
                return readFromFile(tempFile);
            }
        } catch (Exception e) {
            throw new IOException("Failed to read 3MF file: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    private Model3D readFromFile(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            // Look for model file in standard locations
            ZipEntry modelEntry = findModelEntry(zipFile);
            if (modelEntry == null) {
                throw new IOException("No 3D model found in 3MF file");
            }

            try (InputStream modelStream = zipFile.getInputStream(modelEntry)) {
                return parseModel(modelStream);
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse 3MF file: " + e.getMessage(), e);
        }
    }

    private ZipEntry findModelEntry(ZipFile zipFile) {
        // Standard locations for model file
        String[] possiblePaths = {
            "3D/3dmodel.model",
            "3D/Models/model.xml",
            "3dmodel.model"
        };

        for (String path : possiblePaths) {
            ZipEntry entry = zipFile.getEntry(path);
            if (entry != null) {
                return entry;
            }
        }

        // Fallback: look for any .model file
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".model")) {
                return entry;
            }
        }

        return null;
    }

    private Model3D parseModel(InputStream modelStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(modelStream);

        Model3D model = new Model3D();
        int objectIndex = 0;

        // Find all mesh objects
        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            NodeList meshNodes = objectElement.getElementsByTagName("mesh");

            for (int j = 0; j < meshNodes.getLength(); j++) {
                Element meshElement = (Element) meshNodes.item(j);
                TriangleMesh triangleMesh = parseMesh(meshElement);

                if (triangleMesh != null) {
                    String objectId = objectElement.getAttribute("id");
                    String name = objectId.isEmpty() ? "object_" + objectIndex : "object_" + objectId;

                    MeshView meshView = new MeshView(triangleMesh);
                    meshView.setId(name);
                    PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
                    meshView.setMaterial(material);
                    meshView.setCullFace(CullFace.NONE);

                    model.addMeshView(name, meshView);
                    model.addMaterial("material_" + objectIndex, material);
                    objectIndex++;
                }
            }
        }

        return model;
    }

    private TriangleMesh parseMesh(Element meshElement) {
        // Parse vertices
        NodeList verticesNodes = meshElement.getElementsByTagName("vertices");
        if (verticesNodes.getLength() == 0) {
            return null;
        }

        Element verticesElement = (Element) verticesNodes.item(0);
        NodeList vertexNodes = verticesElement.getElementsByTagName("vertex");

        List<Float> points = new ArrayList<>();
        for (int i = 0; i < vertexNodes.getLength(); i++) {
            Element vertex = (Element) vertexNodes.item(i);
            points.add(Float.parseFloat(vertex.getAttribute("x")));
            points.add(Float.parseFloat(vertex.getAttribute("y")));
            points.add(Float.parseFloat(vertex.getAttribute("z")));
        }

        // Parse triangles
        NodeList trianglesNodes = meshElement.getElementsByTagName("triangles");
        if (trianglesNodes.getLength() == 0) {
            return null;
        }

        Element trianglesElement = (Element) trianglesNodes.item(0);
        NodeList triangleNodes = trianglesElement.getElementsByTagName("triangle");

        List<Integer> faces = new ArrayList<>();
        for (int i = 0; i < triangleNodes.getLength(); i++) {
            Element triangle = (Element) triangleNodes.item(i);
            int v1 = Integer.parseInt(triangle.getAttribute("v1"));
            int v2 = Integer.parseInt(triangle.getAttribute("v2"));
            int v3 = Integer.parseInt(triangle.getAttribute("v3"));

            // JavaFX faces: vertex index, texcoord index pairs
            faces.add(v1);
            faces.add(0);
            faces.add(v2);
            faces.add(0);
            faces.add(v3);
            faces.add(0);
        }

        // Build mesh
        TriangleMesh mesh = new TriangleMesh();

        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }
        mesh.getPoints().addAll(pointsArray);

        // Single dummy texture coordinate
        mesh.getTexCoords().addAll(0f, 0f);

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }
}
