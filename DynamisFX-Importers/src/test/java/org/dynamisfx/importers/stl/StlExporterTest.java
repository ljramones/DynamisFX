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

import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.RunWithFX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StlExporter.
 */
@ExtendWith(RunWithFX.class)
class StlExporterTest {

    private StlExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new StlExporter();
    }

    @Test
    void testExportAscii() throws Exception {
        exporter.setFormat(StlExporter.Format.ASCII);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test.stl");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        String content = Files.readString(file.toPath());
        assertTrue(content.startsWith("solid test_mesh"));
        assertTrue(content.contains("facet normal"));
        assertTrue(content.contains("vertex"));
        assertTrue(content.endsWith("endsolid test_mesh\n"));
    }

    @Test
    void testExportBinary() throws Exception {
        exporter.setFormat(StlExporter.Format.BINARY);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test_binary.stl");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        // Binary STL: 80 byte header + 4 byte count + (50 bytes per triangle)
        // 1 triangle = 80 + 4 + 50 = 134 bytes
        assertEquals(134, file.length());
    }

    @Test
    void testRoundTrip() throws Exception {
        exporter.setFormat(StlExporter.Format.ASCII);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "roundtrip.stl");

        exporter.export(mesh, file, "roundtrip");

        StlImporter importer = new StlImporter();
        var model = importer.load(file.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }

    @Test
    void testMetadata() {
        assertEquals("stl", exporter.getExtension());
        assertEquals("Stereolithography (STL)", exporter.getFormatDescription());
    }

    private TriangleMesh createSimpleTriangle() {
        TriangleMesh mesh = new TriangleMesh();

        // 3 vertices forming a triangle
        mesh.getPoints().addAll(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0.5f, 1f, 0f
        );

        // Texture coordinates (required by JavaFX)
        mesh.getTexCoords().addAll(
            0f, 0f,
            1f, 0f,
            0.5f, 1f
        );

        // One triangle face (vertex index, texcoord index pairs)
        mesh.getFaces().addAll(
            0, 0, 1, 1, 2, 2
        );

        return mesh;
    }
}
