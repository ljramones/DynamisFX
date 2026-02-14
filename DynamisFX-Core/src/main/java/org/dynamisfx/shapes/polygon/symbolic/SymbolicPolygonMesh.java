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

package org.dynamisfx.shapes.polygon.symbolic;

import org.dynamisfx.shapes.polygon.PolygonMesh;

/**
 * Polygon mesh where the points are symbolic. That is, the values of the
 * points depend on other variables and they can be updated appropriately.
 */
public class SymbolicPolygonMesh {
    public SymbolicPointArray points;
    public float[] texCoords;
    public int[][] faces;
    public int[] faceSmoothingGroups;
    private int numEdgesInFaces = -1;

    public SymbolicPolygonMesh(SymbolicPointArray points, float[] texCoords, int[][] faces, int[] faceSmoothingGroups) {
        this.points = points;
        this.texCoords = texCoords;
        this.faces = faces;
        this.faceSmoothingGroups = faceSmoothingGroups;
    }

    public SymbolicPolygonMesh(PolygonMesh mesh) {
        this.points = new OriginalPointArray(mesh);
        this.texCoords = mesh.getTexCoords().toArray(this.texCoords);
        this.faces = mesh.getFaces();
        this.faceSmoothingGroups = mesh.getFaceSmoothingGroups().toArray(null);
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
}
