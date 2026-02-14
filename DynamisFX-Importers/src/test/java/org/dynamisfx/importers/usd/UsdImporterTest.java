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

package org.dynamisfx.importers.usd;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.dynamisfx.importers.RunWithFX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UsdImporter.
 */
@ExtendWith(RunWithFX.class)
class UsdImporterTest {

    private UsdImporter importer;

    @BeforeEach
    void setUp() {
        importer = new UsdImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("usd"));
        assertTrue(importer.isSupported("USD"));
        assertTrue(importer.isSupported("usda"));
        assertTrue(importer.isSupported("usdc"));
        assertTrue(importer.isSupported("usdz"));
        assertFalse(importer.isSupported("obj"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoadThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> importer.load(new URL("file:///test.usd"))
        );

        assertTrue(exception.getMessage().contains(".usda"));
        assertTrue(exception.getMessage().contains("glTF"));
    }

    @Test
    void testLoadAsPolyThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> importer.loadAsPoly(new URL("file:///test.usdz"))
        );

        assertTrue(exception.getMessage().contains(".usda"));
    }

    @Test
    void testLoadUsda() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube.usda"));

        assertNotNull(model);
        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            MeshView meshView = (MeshView) n;
            assertTrue(meshView.getMesh() instanceof TriangleMesh);

            TriangleMesh mesh = (TriangleMesh) meshView.getMesh();
            assertEquals(8, mesh.getPoints().size() / 3);
            assertEquals(12, mesh.getFaces().size() / 6);
        }
    }

    @Test
    void testLoadUsdaInvalidTopologyThrowsIOException() {
        IOException exception = assertThrows(
            IOException.class,
            () -> importer.load(getClass().getResource("invalid_topology.usda"))
        );

        assertTrue(exception.getMessage().contains("faceVertexCounts sum does not match faceVertexIndices count"));
    }

    @Test
    void testLoadUsdaOutOfRangeIndexThrowsIOException() {
        IOException exception = assertThrows(
            IOException.class,
            () -> importer.load(getClass().getResource("invalid_index.usda"))
        );

        assertTrue(exception.getMessage().contains("Invalid USDA face index"));
    }
}
