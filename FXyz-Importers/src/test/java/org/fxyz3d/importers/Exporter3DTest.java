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
package org.fxyz3d.importers;

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
