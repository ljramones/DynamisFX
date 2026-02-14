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

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MtlReaderTest {

    private MtlReader reader;

    @BeforeEach
    void setUp() {
        reader = new MtlReader("cube.mtl", getClass().getResource("cube.mtl").toExternalForm());
    }

    @Test
    void testReadMaterials() {
        Map<String, Material> materials = reader.getMaterials();
        assertEquals(6, materials.size());
    }

    @ParameterizedTest
    @CsvSource({
            "blue, 0, 0, 0.7, 1, 1, 1, 64",
            "red, 0.7, 0, 0.022779, 1, 1, 1, 80",
            "white, 1, 1, 1, 1, 1, 1, 80",
            "black, 0, 0, 0, 1, 1, 0.7, 33",
            "green, 0, 1, 0, 1, 0.9, 1, 62",
            "purple, 0.7, 0, 0.522779, 1, 1, 1, 80"
    })
    void testEachMaterial(String mtlName,
                          double Kdr, double Kdg, double Kdb,
                          double Ksr, double Ksg, double Ksb,
                          int Ns) {

        PhongMaterial mat = (PhongMaterial) reader.getMaterials().get(mtlName);
        assertEquals(Color.color(Kdr, Kdg, Kdb), mat.getDiffuseColor());
        assertEquals(Color.color(Ksr, Ksg, Ksb), mat.getSpecularColor());
        assertEquals(Ns, mat.getSpecularPower());
    }
}
