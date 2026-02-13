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

