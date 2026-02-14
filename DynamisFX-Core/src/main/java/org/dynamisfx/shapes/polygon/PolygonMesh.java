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

package org.dynamisfx.shapes.polygon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;

/**
 * A Mesh where each face can be a Polygon
 *
 * can convert to using ObservableIntegerArray
 */
public class PolygonMesh {
    private final ObservableFloatArray points;
    private final ObservableFloatArray texCoords;
    private int[][] faces;
    private final ObservableIntegerArray faceSmoothingGroups = FXCollections.observableIntegerArray();
    // Cached value; call getNumEdgesInFaces() which computes lazily if needed
    protected int numEdgesInFaces = -1;

    public PolygonMesh() {
        this(FXCollections.observableFloatArray(), FXCollections.observableFloatArray(), new int[0][0]);
    }

    public PolygonMesh(float[] points, float[] texCoords, int[][] faces) {
        this(FXCollections.observableFloatArray(points), FXCollections.observableFloatArray(texCoords), faces);
    }

    public PolygonMesh(ObservableFloatArray points, ObservableFloatArray texCoords, int[][] faces) {
        this.points = points;
        this.texCoords = texCoords;
        this.faces = faces;
    }

    public ObservableFloatArray getPoints() {
        return points;
    }

    public ObservableFloatArray getTexCoords() {
        return texCoords;
    }

    public int[][] getFaces() {
        return faces;
    }

    public void setFaces(int[][] faces) {
        this.faces = faces;
    }

    public ObservableIntegerArray getFaceSmoothingGroups() {
        return faceSmoothingGroups;
    }

    public int getNumEdgesInFaces() {
        if (numEdgesInFaces == -1) {
            numEdgesInFaces = 0;
            for(int[] face : faces) {
                numEdgesInFaces += face.length;
            }
           numEdgesInFaces /= 2;
        }
        return numEdgesInFaces;
    }

    // Vertex format constants (JavaFX uses a single vertex format)
    private static final int NUM_COMPONENTS_PER_POINT = 3;
    private static final int NUM_COMPONENTS_PER_TEXCOORD = 2;
    private static final int NUM_COMPONENTS_PER_FACE = 6;

    public int getPointElementSize() {
        return NUM_COMPONENTS_PER_POINT;
    }

    public int getTexCoordElementSize() {
        return NUM_COMPONENTS_PER_TEXCOORD;
    }

    public int getFaceElementSize() {
        return NUM_COMPONENTS_PER_FACE;
    }
}
