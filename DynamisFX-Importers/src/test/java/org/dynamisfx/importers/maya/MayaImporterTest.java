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

package org.dynamisfx.importers.maya;

import javafx.scene.Group;
import javafx.scene.Node;
import org.dynamisfx.importers.Model3D;
import org.dynamisfx.importers.RunWithFX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
@ExtendWith(RunWithFX.class)
class MayaImporterTest {

    private MayaImporter importer;

    @BeforeEach
    void setUp() {
        importer = new MayaImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("ma"));
        assertTrue(importer.isSupported("MA"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoadAsPoly() throws Exception {
        Model3D model = importer.loadAsPoly(getClass().getResource("duke_king.ma"));

        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());
        assertTrue(model.getTimeline().isPresent());
        assertEquals(1, model.getMeshViews().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof Group);
        }
    }
}
