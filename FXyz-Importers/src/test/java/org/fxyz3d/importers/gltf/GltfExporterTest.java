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
package org.fxyz3d.importers.gltf;

import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.RunWithFX;
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
