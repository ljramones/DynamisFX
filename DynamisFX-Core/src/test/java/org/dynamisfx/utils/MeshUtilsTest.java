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

package org.dynamisfx.utils;

import eu.mihosoft.jcsg.CSG;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MeshUtilsTest {

    @Test
    @DisplayName("mesh2CSG converts triangle mesh to CSG")
    public void testMesh2CSG() {
        TriangleMesh mesh = createSimpleTriangleMesh();

        CSG csg = MeshUtils.mesh2CSG(mesh);

        assertThat(csg, is(notNullValue()));
        assertThat(csg.getPolygons().size(), greaterThan(0));
    }

    @Test
    @DisplayName("mesh2CSG handles empty mesh")
    public void testMesh2CSGEmpty() {
        TriangleMesh mesh = new TriangleMesh();

        CSG csg = MeshUtils.mesh2CSG(mesh);

        assertThat(csg, is(notNullValue()));
    }

    private TriangleMesh createSimpleTriangleMesh() {
        TriangleMesh mesh = new TriangleMesh();

        // Simple triangle
        mesh.getPoints().addAll(
            0, 0, 0,
            1, 0, 0,
            0, 1, 0
        );

        mesh.getTexCoords().addAll(
            0, 0,
            1, 0,
            0, 1
        );

        mesh.getFaces().addAll(
            0, 0, 1, 1, 2, 2
        );

        return mesh;
    }
}
