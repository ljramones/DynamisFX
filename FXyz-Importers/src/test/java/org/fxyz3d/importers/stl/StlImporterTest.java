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
package org.fxyz3d.importers.stl;

import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Model3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StlImporter.
 */
class StlImporterTest {

    private StlImporter importer;

    @BeforeEach
    void setUp() {
        importer = new StlImporter();
    }

    @Test
    void testExtensions() {
        assertTrue(importer.isSupported("stl"));
        assertTrue(importer.isSupported("STL"));
        assertFalse(importer.isSupported("obj"));
        assertFalse(importer.isSupported(null));
    }

    @Test
    void testLoadAscii() throws Exception {
        Model3D model = importer.load(getClass().getResource("cube.stl"));

        assertNotNull(model);
        assertEquals(1, model.getRoot().getChildren().size());
        assertEquals(1, model.getMeshNames().size());

        for (Node n : model.getMeshViews()) {
            assertTrue(n instanceof MeshView);
            MeshView meshView = (MeshView) n;
            assertTrue(meshView.getMesh() instanceof TriangleMesh);

            TriangleMesh mesh = (TriangleMesh) meshView.getMesh();
            // Cube has 12 triangles (2 per face * 6 faces)
            assertEquals(12 * 3, mesh.getFaces().size() / 2);
            // 12 triangles * 3 vertices = 36 vertices (with duplicates for STL)
            assertEquals(36, mesh.getPoints().size() / 3);
        }
    }

    @Test
    void testLoadAsPoly() throws Exception {
        // STL doesn't support polygon meshes, should return same as load
        Model3D model = importer.loadAsPoly(getClass().getResource("cube.stl"));

        assertNotNull(model);
        assertEquals(1, model.getMeshViews().size());
    }
}
