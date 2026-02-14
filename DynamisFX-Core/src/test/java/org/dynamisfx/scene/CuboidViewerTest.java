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
 */

package org.dynamisfx.scene;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CuboidViewerTest {

    private CuboidViewer viewer;

    @BeforeEach
    void setUp() {
        viewer = new CuboidViewer();
    }

    @Test
    @DisplayName("Constructor builds cuboid viewer with children")
    void testConstruction() {
        assertThat(viewer.getChildren().isEmpty(), is(false));
        assertThat(viewer.getSelectionModel(), is(notNullValue()));
    }

    @Test
    @DisplayName("Selection support can be toggled")
    void testSelectionToggle() {
        assertThat(viewer.isSelectionEnabled(), is(true));
        viewer.setSelectionEnabled(false);
        assertThat(viewer.isSelectionEnabled(), is(false));
    }
}

