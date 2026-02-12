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
package org.fxyz3d.importers.threemf;

import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.RunWithFX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ThreeMfExporter.
 */
@ExtendWith(RunWithFX.class)
class ThreeMfExporterTest {

    private ThreeMfExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new ThreeMfExporter();
    }

    @Test
    void testExport() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test.3mf");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // Verify ZIP structure
        try (ZipFile zipFile = new ZipFile(file)) {
            assertNotNull(zipFile.getEntry("[Content_Types].xml"));
            assertNotNull(zipFile.getEntry("_rels/.rels"));
            assertNotNull(zipFile.getEntry("3D/3dmodel.model"));

            // Verify model content
            ZipEntry modelEntry = zipFile.getEntry("3D/3dmodel.model");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(zipFile.getInputStream(modelEntry)))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                String xml = content.toString();
                assertTrue(xml.contains("<vertex"));
                assertTrue(xml.contains("<triangle"));
                assertTrue(xml.contains("test_mesh"));
            }
        }
    }

    @Test
    void testRoundTrip() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "roundtrip.3mf");

        exporter.export(mesh, file, "roundtrip");

        ThreeMfImporter importer = new ThreeMfImporter();
        var model = importer.load(file.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }

    @Test
    void testMetadata() {
        assertEquals("3mf", exporter.getExtension());
        assertEquals("3D Manufacturing Format (3MF)", exporter.getFormatDescription());
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
