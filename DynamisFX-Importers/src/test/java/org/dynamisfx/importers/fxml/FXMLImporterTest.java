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

package org.dynamisfx.importers.fxml;

import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Model3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author JosePereda
 */
class FXMLImporterTest {

    private FXMLImporter importer;

    @BeforeEach
    void setUp() {
        importer = new FXMLImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("fxml"));
        assertTrue(importer.isSupported("FXML"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoad() throws Exception {
        Model3D model = importer.load(getClass().getResource("mesh.fxml"));

        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshViews().size());
        Node n = model.getMeshViews().get(0);
        assertTrue(n instanceof MeshView);
        MeshView mv = (MeshView) n;
        assertTrue(mv.getMesh() instanceof TriangleMesh);
        TriangleMesh t = (TriangleMesh) mv.getMesh();
        assertEquals(4, t.getPoints().size() / t.getPointElementSize());
        assertEquals(6, t.getTexCoords().size() / t.getTexCoordElementSize());
        assertEquals(4, t.getFaces().size() / t.getFaceElementSize());
        assertEquals(4, t.getFaceSmoothingGroups().size());
        assertTrue(mv.getMaterial() instanceof PhongMaterial);
    }
}
