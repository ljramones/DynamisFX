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

package org.dynamisfx.importers.x3d;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for X3dImporter.
 */
class X3dImporterTest {

    private X3dImporter importer;

    @BeforeEach
    void setUp() {
        importer = new X3dImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("x3d"));
        assertTrue(importer.isSupported("X3D"));
        assertFalse(importer.isSupported("obj"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoad() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube.x3d"));

        assertNotNull(model);
        assertTrue(model.getRoot().getChildren().size() >= 1);
        assertTrue(model.getMeshNames().size() >= 1);

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            MeshView meshView = (MeshView) n;
            assertTrue(meshView.getMesh() instanceof TriangleMesh);

            TriangleMesh mesh = (TriangleMesh) meshView.getMesh();
            // 8 vertices for a cube
            assertEquals(8, mesh.getPoints().size() / 3);
            // 12 faces (triangles)
            assertEquals(12, mesh.getFaces().size() / 6);
        }
    }

    @Test
    void testLoadAsPoly() throws Exception {
        // X3D returns same as load
        Model3D model = importer.loadAsPoly(getClass().getResource("cube.x3d"));

        assertNotNull(model);
        assertTrue(model.getMeshViews().size() >= 1);
    }
}
