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
package org.fxyz3d.importers.stl;

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
