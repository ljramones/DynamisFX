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

package org.dynamisfx.importers;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for unified Exporter3D class.
 */
@ExtendWith(RunWithFX.class)
class Exporter3DTest {

    @TempDir
    Path tempDir;

    @Test
    void testSupportedFormats() {
        assertTrue(Exporter3D.isSupported("stl"));
        assertTrue(Exporter3D.isSupported("STL"));
        assertTrue(Exporter3D.isSupported(".stl"));
        assertTrue(Exporter3D.isSupported("obj"));
        assertTrue(Exporter3D.isSupported("gltf"));
        assertTrue(Exporter3D.isSupported("glb"));
        assertTrue(Exporter3D.isSupported("ply"));
        assertTrue(Exporter3D.isSupported("off"));
        assertTrue(Exporter3D.isSupported("3mf"));

        assertFalse(Exporter3D.isSupported("xyz"));
        assertFalse(Exporter3D.isSupported("unknown"));
    }

    @Test
    void testGetSupportedExtensions() {
        String[] extensions = Exporter3D.getSupportedExtensions();
        assertNotNull(extensions);
        assertTrue(extensions.length >= 5);

        var list = Arrays.asList(extensions);
        assertTrue(list.contains("stl"));
        assertTrue(list.contains("obj"));
        assertTrue(list.contains("ply"));
    }

    @Test
    void testGetSupportedExtensionFilters() {
        String[] filters = Exporter3D.getSupportedFormatExtensionFilters();
        assertNotNull(filters);
        assertEquals(7, filters.length);

        var list = Arrays.asList(filters);
        assertTrue(list.contains("*.stl"));
        assertTrue(list.contains("*.obj"));
        assertTrue(list.contains("*.gltf"));
        assertTrue(list.contains("*.glb"));
        assertTrue(list.contains("*.ply"));
        assertTrue(list.contains("*.off"));
        assertTrue(list.contains("*.3mf"));
    }

    @Test
    void testExportStl() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = tempDir.resolve("test.stl").toFile();

        Exporter3D.export(mesh, file);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void testExportObj() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = tempDir.resolve("test.obj").toFile();

        Exporter3D.export(mesh, file, "test_mesh");

        assertTrue(file.exists());
    }

    @Test
    void testExportPly() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        File file = tempDir.resolve("test.ply").toFile();

        Exporter3D.export(mesh, file);

        assertTrue(file.exists());
    }

    @Test
    void testExportMeshView() throws Exception {
        TriangleMesh mesh = createSimpleTriangle();
        MeshView meshView = new MeshView(mesh);
        meshView.setId("triangle");

        File file = tempDir.resolve("meshview.stl").toFile();

        Exporter3D.export(meshView, file);

        assertTrue(file.exists());
    }

    @Test
    void testExportUnsupportedFormat() {
        TriangleMesh mesh = createSimpleTriangle();
        File file = tempDir.resolve("test.xyz").toFile();

        assertThrows(IllegalArgumentException.class, () -> Exporter3D.export(mesh, file));
    }

    @Test
    void testExportNoExtension() {
        TriangleMesh mesh = createSimpleTriangle();
        File file = tempDir.resolve("test").toFile();

        assertThrows(IllegalArgumentException.class, () -> Exporter3D.export(mesh, file));
    }

    @Test
    void testGetExporter() {
        assertNotNull(Exporter3D.getExporter("stl"));
        assertNotNull(Exporter3D.getExporter("obj"));
        assertNotNull(Exporter3D.getExporter("gltf"));
        assertNotNull(Exporter3D.getExporter("glb"));
        assertNotNull(Exporter3D.getExporter("ply"));
        assertNotNull(Exporter3D.getExporter("off"));
        assertNotNull(Exporter3D.getExporter("3mf"));
        assertNull(Exporter3D.getExporter("unknown"));
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
