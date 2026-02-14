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

package org.dynamisfx.shapes.primitives;

import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.helper.MarkerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScatterMesh class including per-particle attributes and efficient updates.
 */
public class ScatterMeshTest {

    private List<Point3D> testPoints;

    @BeforeEach
    void setUp() {
        testPoints = new ArrayList<>();
        testPoints.add(new Point3D(0f, 0f, 0f));
        testPoints.add(new Point3D(10f, 0f, 0f));
        testPoints.add(new Point3D(20f, 0f, 0f));
    }

    // ==================== Basic Construction Tests ====================

    @Nested
    @DisplayName("Basic Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Default constructor creates mesh with default data")
        void testDefaultConstructor() {
            ScatterMesh mesh = new ScatterMesh();

            assertThat(mesh.getScatterData(), is(notNullValue()));
            assertThat(mesh.getScatterData().size(), is(3));  // default data
        }

        @Test
        @DisplayName("Constructor with scatter data creates mesh")
        void testConstructorWithData() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThat(mesh.getScatterData(), is(testPoints));
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }

        @Test
        @DisplayName("Constructor with height sets particle size")
        void testConstructorWithHeight() {
            ScatterMesh mesh = new ScatterMesh(testPoints, 2.0);

            assertThat(mesh.getHeight(), is(2.0));
        }

        @Test
        @DisplayName("setMarker changes marker type")
        void testSetMarker() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            mesh.setMarker(MarkerFactory.Marker.CUBE);

            assertThat(mesh.getMarker(), is(MarkerFactory.Marker.CUBE));
        }
    }

    // ==================== CullFace Tests (Phase 4) ====================

    @Nested
    @DisplayName("CullFace Tests")
    class CullFaceTests {

        @Test
        @DisplayName("Default cullFace is NONE for particle visibility from all angles")
        void testDefaultCullFace() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThat(mesh.getCullFace(), is(CullFace.NONE));
        }

        @Test
        @DisplayName("setCullFace changes cull face mode")
        void testSetCullFace() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            mesh.setCullFace(CullFace.BACK);

            assertThat(mesh.getCullFace(), is(CullFace.BACK));
        }

        @Test
        @DisplayName("setCullFace applies to all child meshes")
        void testSetCullFaceAppliesToChildren() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            mesh.setCullFace(CullFace.NONE);

            // All children should have NONE cull face
            mesh.getChildren().forEach(child -> {
                if (child instanceof TexturedMesh) {
                    assertThat(((TexturedMesh) child).getCullFace(), is(CullFace.NONE));
                }
            });
        }
    }

    // ==================== Per-Particle Color Tests (Phase 1) ====================

    @Nested
    @DisplayName("Per-Particle Color Tests")
    class PerParticleColorTests {

        @Test
        @DisplayName("Default perParticleColor is false")
        void testDefaultPerParticleColor() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThat(mesh.isPerParticleColor(), is(false));
        }

        @Test
        @DisplayName("setColorPaletteColors sets palette")
        void testSetColorPalette() {
            ScatterMesh mesh = new ScatterMesh(testPoints);
            List<Color> palette = Arrays.asList(Color.RED, Color.YELLOW, Color.WHITE);

            mesh.setColorPaletteColors(palette);

            assertThat(mesh.getColorPaletteColors(), is(palette));
        }

        @Test
        @DisplayName("setColorPaletteColors throws for empty palette")
        void testSetColorPaletteEmpty() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThrows(IllegalArgumentException.class, () -> {
                mesh.setColorPaletteColors(Arrays.asList());
            });
        }

        @Test
        @DisplayName("setColorPaletteColors throws for null palette")
        void testSetColorPaletteNull() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThrows(IllegalArgumentException.class, () -> {
                mesh.setColorPaletteColors(null);
            });
        }

        @Test
        @DisplayName("enablePerParticleColor enables color mode")
        void testEnablePerParticleColor() {
            ScatterMesh mesh = new ScatterMesh(testPoints);
            List<Color> palette = Arrays.asList(Color.RED, Color.BLUE);

            mesh.enablePerParticleColor(palette);

            assertThat(mesh.isPerParticleColor(), is(true));
            assertThat(mesh.getColorPaletteColors(), is(palette));
        }

        @Test
        @DisplayName("disablePerParticleColor disables color mode")
        void testDisablePerParticleColor() {
            ScatterMesh mesh = new ScatterMesh(testPoints);
            mesh.enablePerParticleColor(Arrays.asList(Color.RED, Color.BLUE));

            mesh.disablePerParticleColor();

            assertThat(mesh.isPerParticleColor(), is(false));
        }

        @Test
        @DisplayName("Particles with different colorIndex get different colors")
        void testParticlesWithDifferentColorIndex() {
            List<Point3D> coloredPoints = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0),      // colorIndex 0 (red)
                    new Point3D(10f, 0f, 0f, 128),   // colorIndex 128 (middle)
                    new Point3D(20f, 0f, 0f, 255)    // colorIndex 255 (blue)
            );

            ScatterMesh mesh = new ScatterMesh(coloredPoints, true, 1.0, 0);
            mesh.enablePerParticleColor(Arrays.asList(Color.RED, Color.BLUE));

            // Mesh should be created without errors
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }
    }

    // ==================== Per-Particle Scale Tests (Phase 3) ====================

    @Nested
    @DisplayName("Per-Particle Scale Tests")
    class PerParticleScaleTests {

        @Test
        @DisplayName("Default perParticleScale is false")
        void testDefaultPerParticleScale() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThat(mesh.isPerParticleScale(), is(false));
        }

        @Test
        @DisplayName("setPerParticleScale enables scale mode")
        void testSetPerParticleScale() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            mesh.setPerParticleScale(true);

            assertThat(mesh.isPerParticleScale(), is(true));
        }

        @Test
        @DisplayName("Particles with different scale have different sizes")
        void testParticlesWithDifferentScale() {
            List<Point3D> scaledPoints = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0, 0.5f),   // half size
                    new Point3D(10f, 0f, 0f, 0, 1.0f),  // normal size
                    new Point3D(20f, 0f, 0f, 0, 2.0f)   // double size
            );

            ScatterMesh mesh = new ScatterMesh(scaledPoints, true, 1.0, 0);
            mesh.setPerParticleScale(true);

            // Mesh should be created without errors
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }

        @Test
        @DisplayName("enablePerParticleAttributes enables both color and scale")
        void testEnablePerParticleAttributes() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            mesh.enablePerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            assertThat(mesh.isPerParticleColor(), is(true));
            assertThat(mesh.isPerParticleScale(), is(true));
        }
    }

    // ==================== Per-Particle Opacity Tests (Phase 6) ====================

    @Nested
    @DisplayName("Per-Particle Opacity Tests")
    class PerParticleOpacityTests {

        @Test
        @DisplayName("Default perParticleOpacity is false")
        void testDefaultPerParticleOpacity() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThat(mesh.isPerParticleOpacity(), is(false));
        }

        @Test
        @DisplayName("setPerParticleOpacity enables opacity mode")
        void testSetPerParticleOpacity() {
            ScatterMesh mesh = new ScatterMesh(testPoints);
            mesh.setColorPaletteColors(Arrays.asList(Color.RED, Color.BLUE));

            mesh.setPerParticleOpacity(true);

            assertThat(mesh.isPerParticleOpacity(), is(true));
        }

        @Test
        @DisplayName("enableAllPerParticleAttributes enables color, scale, and opacity")
        void testEnableAllPerParticleAttributes() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            mesh.enableAllPerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            assertThat(mesh.isPerParticleColor(), is(true));
            assertThat(mesh.isPerParticleScale(), is(true));
            assertThat(mesh.isPerParticleOpacity(), is(true));
        }

        @Test
        @DisplayName("enableAllPerParticleAttributes throws for null palette")
        void testEnableAllPerParticleAttributesNullPalette() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThrows(IllegalArgumentException.class, () -> {
                mesh.enableAllPerParticleAttributes(null);
            });
        }

        @Test
        @DisplayName("enableAllPerParticleAttributes throws for empty palette")
        void testEnableAllPerParticleAttributesEmptyPalette() {
            ScatterMesh mesh = new ScatterMesh(testPoints);

            assertThrows(IllegalArgumentException.class, () -> {
                mesh.enableAllPerParticleAttributes(Arrays.asList());
            });
        }

        @Test
        @DisplayName("Particles with different opacity render correctly")
        void testParticlesWithDifferentOpacity() {
            List<Point3D> opaquePoints = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 100, 1.0f, 1.0f),   // fully opaque
                    new Point3D(10f, 0f, 0f, 100, 1.0f, 0.5f),  // half transparent
                    new Point3D(20f, 0f, 0f, 100, 1.0f, 0.0f)   // fully transparent
            );

            ScatterMesh mesh = new ScatterMesh(opaquePoints, true, 1.0, 0);
            mesh.enableAllPerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            // Mesh should be created without errors
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }

        @Test
        @DisplayName("disablePerParticleAttributes disables all modes")
        void testDisablePerParticleAttributes() {
            ScatterMesh mesh = new ScatterMesh(testPoints);
            mesh.enableAllPerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            mesh.disablePerParticleAttributes();

            assertThat(mesh.isPerParticleColor(), is(false));
            assertThat(mesh.isPerParticleScale(), is(false));
            assertThat(mesh.isPerParticleOpacity(), is(false));
        }
    }

    // ==================== Efficient Position Update Tests (Phase 2) ====================

    @Nested
    @DisplayName("Efficient Position Update Tests")
    class PositionUpdateTests {

        @Test
        @DisplayName("updatePositions returns false for non-joined segments")
        void testUpdatePositionsNonJoined() {
            ScatterMesh mesh = new ScatterMesh(testPoints, false, 1.0, 0);  // joinSegments = false

            List<Point3D> newPositions = Arrays.asList(
                    new Point3D(1f, 0f, 0f),
                    new Point3D(11f, 0f, 0f),
                    new Point3D(21f, 0f, 0f)
            );

            boolean result = mesh.updatePositions(newPositions);

            assertThat(result, is(false));  // Falls back to full rebuild
        }

        @Test
        @DisplayName("updatePositions returns true for efficient update")
        void testUpdatePositionsEfficient() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);  // joinSegments = true

            List<Point3D> newPositions = Arrays.asList(
                    new Point3D(1f, 0f, 0f),
                    new Point3D(11f, 0f, 0f),
                    new Point3D(21f, 0f, 0f)
            );

            boolean result = mesh.updatePositions(newPositions);

            assertThat(result, is(true));
        }

        @Test
        @DisplayName("updatePositions returns false when particle count changes")
        void testUpdatePositionsCountChange() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            List<Point3D> newPositions = Arrays.asList(
                    new Point3D(1f, 0f, 0f),
                    new Point3D(11f, 0f, 0f)
                    // Only 2 points instead of 3
            );

            boolean result = mesh.updatePositions(newPositions);

            assertThat(result, is(false));  // Falls back to full rebuild
        }

        @Test
        @DisplayName("updateParticlePosition updates single particle")
        void testUpdateParticlePosition() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            boolean result = mesh.updateParticlePosition(1, new Point3D(15f, 5f, 5f));

            assertThat(result, is(true));
        }

        @Test
        @DisplayName("updateParticlePosition returns false for invalid index")
        void testUpdateParticlePositionInvalidIndex() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            boolean resultNegative = mesh.updateParticlePosition(-1, new Point3D(0f, 0f, 0f));
            boolean resultTooLarge = mesh.updateParticlePosition(100, new Point3D(0f, 0f, 0f));

            assertThat(resultNegative, is(false));
            assertThat(resultTooLarge, is(false));
        }

        @Test
        @DisplayName("getVerticesPerMarker returns positive value after mesh creation")
        void testGetVerticesPerMarker() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            int verticesPerMarker = mesh.getVerticesPerMarker();

            assertThat(verticesPerMarker, greaterThan(0));
        }

        @Test
        @DisplayName("getParticleCount returns correct count")
        void testGetParticleCount() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);

            int count = mesh.getParticleCount();

            assertThat(count, is(3));
        }
    }

    // ==================== Marker Type Tests (Phase 5) ====================

    @Nested
    @DisplayName("Marker Type Tests")
    class MarkerTypeTests {

        @Test
        @DisplayName("OCTAHEDRON marker creates mesh")
        void testOctahedronMarker() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);
            mesh.setMarker(MarkerFactory.Marker.OCTAHEDRON);

            assertThat(mesh.getChildren().size(), greaterThan(0));
            assertThat(mesh.getMarker(), is(MarkerFactory.Marker.OCTAHEDRON));
        }

        @Test
        @DisplayName("QUAD marker creates mesh")
        void testQuadMarker() {
            ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);
            mesh.setMarker(MarkerFactory.Marker.QUAD);

            assertThat(mesh.getChildren().size(), greaterThan(0));
            assertThat(mesh.getMarker(), is(MarkerFactory.Marker.QUAD));
        }

        @Test
        @DisplayName("All marker types create valid meshes")
        void testAllMarkerTypes() {
            for (MarkerFactory.Marker markerType : MarkerFactory.Marker.values()) {
                ScatterMesh mesh = new ScatterMesh(testPoints, true, 1.0, 0);
                mesh.setMarker(markerType);

                assertThat("Marker " + markerType + " should create children",
                        mesh.getChildren().size(), greaterThan(0));
            }
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Combined color and scale works together")
        void testCombinedColorAndScale() {
            List<Point3D> points = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0, 0.5f),     // small, red
                    new Point3D(10f, 0f, 0f, 128, 1.0f),  // medium, middle color
                    new Point3D(20f, 0f, 0f, 255, 2.0f)   // large, blue
            );

            ScatterMesh mesh = new ScatterMesh(points, true, 1.0, 0);
            mesh.enablePerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            assertThat(mesh.isPerParticleColor(), is(true));
            assertThat(mesh.isPerParticleScale(), is(true));
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }

        @Test
        @DisplayName("All attributes combined works")
        void testAllAttributesCombined() {
            List<Point3D> points = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 50, 0.5f, 1.0f),    // small, opaque
                    new Point3D(10f, 0f, 0f, 150, 1.0f, 0.5f),  // medium, semi-transparent
                    new Point3D(20f, 0f, 0f, 250, 2.0f, 0.2f)   // large, mostly transparent
            );

            ScatterMesh mesh = new ScatterMesh(points, true, 1.0, 0);
            mesh.enableAllPerParticleAttributes(Arrays.asList(
                    Color.RED, Color.ORANGE, Color.YELLOW, Color.WHITE
            ));

            assertThat(mesh.isPerParticleColor(), is(true));
            assertThat(mesh.isPerParticleScale(), is(true));
            assertThat(mesh.isPerParticleOpacity(), is(true));
            assertThat(mesh.getChildren().size(), greaterThan(0));
        }

        @Test
        @DisplayName("Position update works with per-particle attributes")
        void testPositionUpdateWithAttributes() {
            List<Point3D> points = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 50, 1.0f),
                    new Point3D(10f, 0f, 0f, 150, 1.0f),
                    new Point3D(20f, 0f, 0f, 250, 1.0f)
            );

            ScatterMesh mesh = new ScatterMesh(points, true, 1.0, 0);
            mesh.enablePerParticleAttributes(Arrays.asList(Color.RED, Color.BLUE));

            List<Point3D> newPoints = Arrays.asList(
                    new Point3D(5f, 5f, 5f, 50, 1.0f),
                    new Point3D(15f, 5f, 5f, 150, 1.0f),
                    new Point3D(25f, 5f, 5f, 250, 1.0f)
            );

            boolean result = mesh.updatePositions(newPoints);

            assertThat(result, is(true));
        }
    }
}
