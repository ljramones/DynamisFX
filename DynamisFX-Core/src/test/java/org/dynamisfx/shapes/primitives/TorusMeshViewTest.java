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

package org.dynamisfx.shapes.primitives;

import javafx.scene.shape.Mesh;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TorusMeshViewTest {

    @Test
    public void testTorusConstruction() {
        TorusMesh torus = new TorusMesh(100, 80, 15.0, 13.0);

        assertThat(torus.getRadiusDivisions(), is(100));
        assertThat(torus.getTubeDivisions(), is(80));
        assertThat(torus.getRadius(), is(15.0));
        assertThat(torus.getTubeRadius(), is(13.0));
    }

    @Test
    @DisplayName("Mesh is updated when radius changes")
    public void testTorusUpdateMesh() {
        TorusMesh torus = new TorusMesh();

        Mesh oldMesh = torus.getMesh();

        torus.setRadius(25.0);

        assertThat(torus.getMesh(), is(not(oldMesh)));
    }
}
