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

package org.dynamisfx.importers.ply;

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
 * Tests for PlyExporter.
 */
@ExtendWith(RunWithFX.class)
class PlyExporterTest {

    private PlyExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new PlyExporter();
    }

    @Test
    void testExportAscii() throws Exception {
        exporter.setFormat(PlyExporter.Format.ASCII);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test.ply");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        String content = Files.readString(file.toPath());
        assertTrue(content.startsWith("ply"));
        assertTrue(content.contains("format ascii 1.0"));
        assertTrue(content.contains("element vertex 3"));
        assertTrue(content.contains("element face 1"));
        assertTrue(content.contains("end_header"));
    }

    @Test
    void testExportBinary() throws Exception {
        exporter.setFormat(PlyExporter.Format.BINARY_LITTLE_ENDIAN);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test_binary.ply");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // Read just the header portion (first few bytes are ASCII)
        byte[] bytes = Files.readAllBytes(file.toPath());
        String header = new String(bytes, 0, Math.min(bytes.length, 300), java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("ply"));
        assertTrue(header.contains("format binary_little_endian 1.0"));
    }

    @Test
    void testRoundTrip() throws Exception {
        exporter.setFormat(PlyExporter.Format.ASCII);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "roundtrip.ply");

        exporter.export(mesh, file, "roundtrip");

        PlyImporter importer = new PlyImporter();
        var model = importer.load(file.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }

    @Test
    void testMetadata() {
        assertEquals("ply", exporter.getExtension());
        assertEquals("Polygon File Format (PLY)", exporter.getFormatDescription());
    }

    private TriangleMesh createSimpleTriangle() {
        TriangleMesh mesh = new TriangleMesh();

        mesh.getPoints().addAll(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0.5f, 1f, 0f
        );

        mesh.getTexCoords().addAll(
            0f, 0f,
            1f, 0f,
            0.5f, 1f
        );

        mesh.getFaces().addAll(
            0, 0, 1, 1, 2, 2
        );

        return mesh;
    }
}
