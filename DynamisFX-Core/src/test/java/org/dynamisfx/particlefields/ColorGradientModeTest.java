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
package org.dynamisfx.particlefields;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for ColorGradientMode enum.
 */
@DisplayName("ColorGradientMode")
public class ColorGradientModeTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {
        @Test
        @DisplayName("has 5 modes")
        void hasFiveModes() {
            assertThat(ColorGradientMode.values(), arrayWithSize(5));
        }

        @Test
        @DisplayName("all modes exist")
        void allModesExist() {
            assertThat(ColorGradientMode.valueOf("LINEAR"), notNullValue());
            assertThat(ColorGradientMode.valueOf("RADIAL"), notNullValue());
            assertThat(ColorGradientMode.valueOf("NOISE_BASED"), notNullValue());
            assertThat(ColorGradientMode.valueOf("TEMPERATURE"), notNullValue());
            assertThat(ColorGradientMode.valueOf("MULTI_ZONE"), notNullValue());
        }
    }

    @Nested
    @DisplayName("Display names")
    class DisplayNames {
        @Test
        @DisplayName("each mode has a display name")
        void displayNames() {
            assertThat(ColorGradientMode.LINEAR.getDisplayName(), is("Linear"));
            assertThat(ColorGradientMode.RADIAL.getDisplayName(), is("Radial"));
            assertThat(ColorGradientMode.NOISE_BASED.getDisplayName(), is("Noise-Based"));
            assertThat(ColorGradientMode.TEMPERATURE.getDisplayName(), is("Temperature"));
            assertThat(ColorGradientMode.MULTI_ZONE.getDisplayName(), is("Multi-Zone"));
        }

        @Test
        @DisplayName("each mode has a description")
        void descriptions() {
            for (ColorGradientMode mode : ColorGradientMode.values()) {
                assertThat(mode.getDescription(), not(emptyOrNullString()));
            }
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {
        @Test
        @DisplayName("parses valid values")
        void parsesValid() {
            assertThat(ColorGradientMode.fromString("LINEAR"), is(ColorGradientMode.LINEAR));
            assertThat(ColorGradientMode.fromString("TEMPERATURE"), is(ColorGradientMode.TEMPERATURE));
        }

        @Test
        @DisplayName("handles case insensitivity")
        void caseInsensitive() {
            assertThat(ColorGradientMode.fromString("linear"), is(ColorGradientMode.LINEAR));
            assertThat(ColorGradientMode.fromString("temperature"), is(ColorGradientMode.TEMPERATURE));
        }

        @Test
        @DisplayName("handles hyphen to underscore")
        void hyphenToUnderscore() {
            assertThat(ColorGradientMode.fromString("noise-based"), is(ColorGradientMode.NOISE_BASED));
            assertThat(ColorGradientMode.fromString("multi-zone"), is(ColorGradientMode.MULTI_ZONE));
        }

        @Test
        @DisplayName("returns LINEAR for null")
        void nullReturnsLinear() {
            assertThat(ColorGradientMode.fromString(null), is(ColorGradientMode.LINEAR));
        }

        @Test
        @DisplayName("returns LINEAR for blank")
        void blankReturnsLinear() {
            assertThat(ColorGradientMode.fromString(""), is(ColorGradientMode.LINEAR));
            assertThat(ColorGradientMode.fromString("  "), is(ColorGradientMode.LINEAR));
        }

        @Test
        @DisplayName("returns LINEAR for unknown")
        void unknownReturnsLinear() {
            assertThat(ColorGradientMode.fromString("unknown"), is(ColorGradientMode.LINEAR));
        }
    }
}
