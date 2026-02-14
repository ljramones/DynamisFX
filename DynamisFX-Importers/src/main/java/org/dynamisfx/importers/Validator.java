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

package org.dynamisfx.importers;

import javafx.collections.ObservableIntegerArray;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * Mesh data validator
 */
public class Validator {

    public void validate(Node node) {
        if (node instanceof MeshView) {
            MeshView meshView = (MeshView) node;
            validate(meshView.getMesh());
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                validate(child);
            }
        }
    }

    public void validate(Mesh mesh) {
        if (!(mesh instanceof TriangleMesh)) {
            throw new AssertionError("Mesh is not TriangleMesh: " + mesh.getClass() + ", mesh = " + mesh);
        }
        TriangleMesh tMesh = (TriangleMesh) mesh;
        int numPoints = tMesh.getPoints().size() / tMesh.getPointElementSize();
        int numTexCoords = tMesh.getTexCoords().size() / tMesh.getTexCoordElementSize();
        int numFaces = tMesh.getFaces().size() / tMesh.getFaceElementSize();
        if (numPoints == 0 || numPoints * tMesh.getPointElementSize() != tMesh.getPoints().size()) {
            throw new AssertionError("Points array size is not correct: " + tMesh.getPoints().size());
        }
        if (numTexCoords == 0 || numTexCoords * tMesh.getTexCoordElementSize() != tMesh.getTexCoords().size()) {
            throw new AssertionError("TexCoords array size is not correct: " + tMesh.getPoints().size());
        }
        if (numFaces == 0 || numFaces * tMesh.getFaceElementSize() != tMesh.getFaces().size()) {
            throw new AssertionError("Faces array size is not correct: " + tMesh.getPoints().size());
        }
        if (numFaces != tMesh.getFaceSmoothingGroups().size() && tMesh.getFaceSmoothingGroups().size() > 0) {
            throw new AssertionError("FaceSmoothingGroups array size is not correct: " + tMesh.getPoints().size() + ", numFaces = " + numFaces);
        }
        ObservableIntegerArray faces = tMesh.getFaces();
        for (int i = 0; i < faces.size(); i += 2) {
            int pIndex = faces.get(i);
            if (pIndex < 0 || pIndex > numPoints) {
                throw new AssertionError("Incorrect point index: " + pIndex + ", numPoints = " + numPoints);
            }
            int tcIndex = faces.get(i + 1);
            if (tcIndex < 0 || tcIndex > numTexCoords) {
                throw new AssertionError("Incorrect texCoord index: " + tcIndex + ", numTexCoords = " + numTexCoords);
            }
        }
//        System.out.println("Validation successfull of " + mesh);
    }

}
