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

package org.dynamisfx.importers.cad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CadImporter.
 */
class CadImporterTest {

    private CadImporter importer;

    @BeforeEach
    void setUp() {
        importer = new CadImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("step"));
        assertTrue(importer.isSupported("STEP"));
        assertTrue(importer.isSupported("stp"));
        assertTrue(importer.isSupported("iges"));
        assertTrue(importer.isSupported("igs"));
        assertFalse(importer.isSupported("obj"));
        assertFalse(importer.isSupported("stl"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoadThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> importer.load(new URL("file:///test.step"))
        );

        assertTrue(exception.getMessage().contains("CAD geometry kernel"));
        assertTrue(exception.getMessage().contains("FreeCAD"));
    }

    @Test
    void testLoadAsPolyThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> importer.loadAsPoly(new URL("file:///test.iges"))
        );

        assertTrue(exception.getMessage().contains("CAD geometry kernel"));
    }
}
