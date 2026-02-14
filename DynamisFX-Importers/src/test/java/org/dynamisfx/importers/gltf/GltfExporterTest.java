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

package org.dynamisfx.importers.gltf;

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
 * Tests for GltfExporter.
 */
@ExtendWith(RunWithFX.class)
class GltfExporterTest {

    private GltfExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new GltfExporter();
    }

    @Test
    void testExportGltf() throws Exception {
        exporter.setFormat(GltfExporter.Format.GLTF);

        TriangleMesh mesh = createSimpleTriangle();
        File gltfFile = new File(tempDir.toFile(), "test.gltf");

        exporter.export(mesh, gltfFile, "test_mesh");

        assertTrue(gltfFile.exists(), "GLTF file should exist");
        assertTrue(gltfFile.length() > 0);

        String content = Files.readString(gltfFile.toPath());
        assertTrue(content.contains("\"asset\""));
        assertTrue(content.contains("\"version\"") && content.contains("2.0"), "Should contain version 2.0");
        assertTrue(content.contains("\"meshes\""));
        assertTrue(content.contains("\"buffers\""));
        assertTrue(content.contains("test.bin"), "GLTF should reference bin file");

        // Check binary file was created in same directory
        File binFile = new File(gltfFile.getParentFile(), "test.bin");
        assertTrue(binFile.exists(), "Binary file should exist at: " + binFile.getAbsolutePath() +
            ". Files in dir: " + java.util.Arrays.toString(tempDir.toFile().list()));
    }

    @Test
    void testExportGlb() throws Exception {
        exporter.setFormat(GltfExporter.Format.GLB);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test.glb");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // GLB starts with magic number "glTF" (0x46546C67)
        byte[] bytes = Files.readAllBytes(file.toPath());
        assertEquals('g', bytes[0]);
        assertEquals('l', bytes[1]);
        assertEquals('T', bytes[2]);
        assertEquals('F', bytes[3]);
    }

    @Test
    void testExportGlbContentValid() throws Exception {
        // Verify GLB structure is valid
        exporter.setFormat(GltfExporter.Format.GLB);

        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "structure.glb");

        exporter.export(mesh, file, "structure");
        assertTrue(file.exists());

        // Verify minimum size (header + at least some content)
        assertTrue(file.length() > 20, "GLB should have valid content");

        // Verify GLB header structure
        byte[] header = new byte[12];
        try (var fis = new java.io.FileInputStream(file)) {
            fis.read(header);
        }
        // Magic bytes check (already done in testExportGlb)
        assertEquals('g', header[0]);
        assertEquals('l', header[1]);
        assertEquals('T', header[2]);
        assertEquals('F', header[3]);
    }

    @Test
    void testMetadata() {
        // Default format is GLB
        assertEquals("glb", exporter.getExtension());
        assertEquals("GL Transmission Format (glTF 2.0)", exporter.getFormatDescription());

        // When set to GLTF format
        exporter.setFormat(GltfExporter.Format.GLTF);
        assertEquals("gltf", exporter.getExtension());
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
