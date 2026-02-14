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

package org.dynamisfx.importers.threemf;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ThreeMfImporter.
 */
class ThreeMfImporterTest {

    private ThreeMfImporter importer;

    @TempDir
    static Path tempDir;

    static File cubeFile;

    @BeforeAll
    static void createTestFile() throws Exception {
        cubeFile = new File(tempDir.toFile(), "cube.3mf");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(cubeFile))) {
            // Write [Content_Types].xml
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
            writer.println("  <Default Extension=\"model\" ContentType=\"application/vnd.ms-package.3dmanufacturing-3dmodel+xml\"/>");
            writer.println("</Types>");
            writer.flush();
            zos.closeEntry();

            // Write 3D/3dmodel.model
            zos.putNextEntry(new ZipEntry("3D/3dmodel.model"));
            writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<model xmlns=\"http://schemas.microsoft.com/3dmanufacturing/core/2015/02\">");
            writer.println("  <resources>");
            writer.println("    <object id=\"1\" type=\"model\">");
            writer.println("      <mesh>");
            writer.println("        <vertices>");
            // Cube vertices
            writer.println("          <vertex x=\"0\" y=\"0\" z=\"0\"/>");
            writer.println("          <vertex x=\"1\" y=\"0\" z=\"0\"/>");
            writer.println("          <vertex x=\"1\" y=\"1\" z=\"0\"/>");
            writer.println("          <vertex x=\"0\" y=\"1\" z=\"0\"/>");
            writer.println("          <vertex x=\"0\" y=\"0\" z=\"1\"/>");
            writer.println("          <vertex x=\"1\" y=\"0\" z=\"1\"/>");
            writer.println("          <vertex x=\"1\" y=\"1\" z=\"1\"/>");
            writer.println("          <vertex x=\"0\" y=\"1\" z=\"1\"/>");
            writer.println("        </vertices>");
            writer.println("        <triangles>");
            // 12 triangles (2 per face * 6 faces)
            writer.println("          <triangle v1=\"0\" v2=\"2\" v3=\"1\"/>");
            writer.println("          <triangle v1=\"0\" v2=\"3\" v3=\"2\"/>");
            writer.println("          <triangle v1=\"4\" v2=\"5\" v3=\"6\"/>");
            writer.println("          <triangle v1=\"4\" v2=\"6\" v3=\"7\"/>");
            writer.println("          <triangle v1=\"0\" v2=\"1\" v3=\"5\"/>");
            writer.println("          <triangle v1=\"0\" v2=\"5\" v3=\"4\"/>");
            writer.println("          <triangle v1=\"2\" v2=\"3\" v3=\"7\"/>");
            writer.println("          <triangle v1=\"2\" v2=\"7\" v3=\"6\"/>");
            writer.println("          <triangle v1=\"0\" v2=\"4\" v3=\"7\"/>");
            writer.println("          <triangle v1=\"0\" v2=\"7\" v3=\"3\"/>");
            writer.println("          <triangle v1=\"1\" v2=\"2\" v3=\"6\"/>");
            writer.println("          <triangle v1=\"1\" v2=\"6\" v3=\"5\"/>");
            writer.println("        </triangles>");
            writer.println("      </mesh>");
            writer.println("    </object>");
            writer.println("  </resources>");
            writer.println("  <build>");
            writer.println("    <item objectid=\"1\"/>");
            writer.println("  </build>");
            writer.println("</model>");
            writer.flush();
            zos.closeEntry();
        }
    }

    @BeforeEach
    void setUp() {
        importer = new ThreeMfImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("3mf"));
        assertTrue(importer.isSupported("3MF"));
        assertFalse(importer.isSupported("stl"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoad() throws Exception {
        Model3D model = importer.load(cubeFile.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            MeshView meshView = (MeshView) n;
            assertTrue(meshView.getMesh() instanceof TriangleMesh);

            TriangleMesh mesh = (TriangleMesh) meshView.getMesh();
            // Cube has 8 vertices
            assertEquals(8, mesh.getPoints().size() / 3);
            // Cube has 12 triangles (2 per face * 6 faces)
            assertEquals(12, mesh.getFaces().size() / 6);
        }
    }

    @Test
    void testLoadAsPoly() throws Exception {
        Model3D model = importer.loadAsPoly(cubeFile.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }
}
