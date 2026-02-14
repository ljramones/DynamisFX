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

import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.RunWithFX;
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
