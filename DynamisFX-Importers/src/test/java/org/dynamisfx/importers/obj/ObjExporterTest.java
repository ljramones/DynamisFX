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

package org.dynamisfx.importers.obj;

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
 * Tests for ObjExporter.
 */
@ExtendWith(RunWithFX.class)
class ObjExporterTest {

    private ObjExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new ObjExporter();
    }

    @Test
    void testExport() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "test.obj");

        exporter.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("# DynamisFX OBJ Export"));
        assertTrue(content.contains("o test_mesh"));
        assertTrue(content.contains("mtllib test.mtl"));
        assertTrue(content.contains("v "));
        assertTrue(content.contains("vt "));
        assertTrue(content.contains("f "));

        // Check MTL file was created
        File mtlFile = new File(tempDir.toFile(), "test.mtl");
        assertTrue(mtlFile.exists(), "MTL file should exist at: " + mtlFile.getAbsolutePath());
    }

    @Test
    void testRoundTrip() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = new File(tempDir.toFile(), "roundtrip.obj");

        exporter.export(mesh, file, "roundtrip");

        ObjImporter importer = new ObjImporter();
        var model = importer.load(file.toURI().toURL());

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }

    @Test
    void testMetadata() {
        assertEquals("obj", exporter.getExtension());
        assertEquals("Wavefront OBJ", exporter.getFormatDescription());
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
