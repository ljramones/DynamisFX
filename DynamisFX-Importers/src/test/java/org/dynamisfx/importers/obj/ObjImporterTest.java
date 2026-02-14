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
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.importers.obj;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.dynamisfx.shapes.polygon.PolygonMesh;
import org.dynamisfx.shapes.polygon.PolygonMeshView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class ObjImporterTest {

    private ObjImporter importer;

    @BeforeEach
    void setUp() {
        importer = new ObjImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("obj"));
        assertTrue(importer.isSupported("OBJ"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoad() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube.obj"));

        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());
        assertTrue(model.getMeshNames().contains("cube"));
        assertSame(model.getMeshView("cube"), model.getRoot().getChildren().get(0));

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            assertTrue(((MeshView) n).getMesh() instanceof TriangleMesh);
        }
    }

    @Test
    void testLoadAsPoly() throws Exception {
        Model3D model = importer.loadAsPoly(getClass().getResource("duke_king_poly.obj"));

        assertEquals(9, model.getRoot().getChildren().size());
        assertEquals(9, model.getMeshNames().size());
        assertEquals(9, model.getMeshViews().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof PolygonMeshView);
        }
    }

    @Test
    void testLoadMaterial() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube_with_mtl.obj"));

        assertEquals(6, model.getMaterials().size());
    }

    @Test
    void testMeshes() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube_with_mtl.obj"));

        assertEquals(6, model.getMeshNames().size());
        assertNotNull(model.getMeshView("front"));
        assertNotNull(model.getMeshView("back"));
        assertNotNull(model.getMeshView("top"));
        assertNotNull(model.getMeshView("bottom"));
        assertNotNull(model.getMeshView("left"));
        assertNotNull(model.getMeshView("right"));
    }
}
