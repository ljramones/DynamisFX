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

package org.dynamisfx.importers.vrml;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VrmlImporter.
 */
class VrmlImporterTest {

    private VrmlImporter importer;

    @BeforeEach
    void setUp() {
        importer = new VrmlImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("wrl"));
        assertTrue(importer.isSupported("WRL"));
        assertFalse(importer.isSupported("vrml"));
        assertFalse(importer.isSupported("obj"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoad() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube.wrl"));

        assertNotNull(model);
        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            MeshView meshView = (MeshView) n;
            assertTrue(meshView.getMesh() instanceof TriangleMesh);

            TriangleMesh mesh = (TriangleMesh) meshView.getMesh();
            // Cube has 8 vertices
            assertEquals(8, mesh.getPoints().size() / 3);
            // Each quad face becomes 2 triangles, 6 faces = 12 triangles
            assertEquals(12, mesh.getFaces().size() / 6);
        }
    }

    @Test
    void testLoadAsPoly() throws Exception {
        Model3D model = importer.loadAsPoly(getClass().getResource("cube.wrl"));

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }
}
