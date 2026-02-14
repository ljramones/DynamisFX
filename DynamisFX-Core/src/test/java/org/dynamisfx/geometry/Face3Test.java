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

package org.dynamisfx.geometry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class Face3Test {

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3",
            "-1, 0, 1",
            "2147483647, 0, -2147483648"
    })
    public void testConstructor(int p0, int p1, int p2) {
        Face3 face = new Face3(p0, p1, p2);

        assertThat(face.p0, is(p0));
        assertThat(face.p1, is(p1));
        assertThat(face.p2, is(p2));
    }

    @ParameterizedTest
    @MethodSource("faceProvider")
    public void testGetFace(Face3 face) {
        assertThat(
                face.getFace().boxed().collect(toList()),
                contains(face.p0, 0, face.p1, 0, face.p2, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("faceAndTProvider")
    void testGetFace(Face3 face, int t) {
        assertThat(
                face.getFace(t).boxed().collect(toList()),
                contains(face.p0, t, face.p1, t, face.p2, t)
        );
    }

    @ParameterizedTest
    @MethodSource("facesProvider")
    void testGetFace(Face3 face1, Face3 face2) {
        assertThat(
                face1.getFace(face2).boxed().collect(toList()),
                contains(face1.p0, face2.p0, face1.p1, face2.p1, face1.p2, face2.p2)
        );

        assertThat(
                face1.getFace(face2.p2, face2.p0, face2.p1).boxed().collect(toList()),
                contains(face1.p0, face2.p2, face1.p1, face2.p0, face1.p2, face2.p1)
        );
    }

    static Stream<Arguments> faceAndTProvider() {
        return Stream.of(
                arguments(new Face3(1, 2, 3), 0),
                arguments(new Face3(-1, 0, 1), -2147483648),
                arguments(new Face3(2147483647, 0, -2147483648), 2147483647)
        );
    }

    static Stream<Face3> faceProvider() {
        return Stream.of(
                new Face3(1, 2, 3),
                new Face3(-1, 0, 1),
                new Face3(2147483647, 0, -2147483648)
        );
    }

    static Stream<Arguments> facesProvider() {
        return Stream.of(
                arguments(new Face3(1, 2, 3), new Face3(-1, 0, 1)),
                arguments(new Face3(-1, 0, 1), new Face3(2147483647, 0, -2147483648)),
                arguments(new Face3(2147483647, 0, -2147483648), new Face3(1, 2, 3))
        );
    }
}
