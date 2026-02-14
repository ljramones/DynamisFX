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

import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Material;

import java.util.*;

/**
 * Represents a loader-independent 3D model data.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class Model3D {

    private final Group root = new Group();
    private final Map<String, Material> materials = new HashMap<>();
    private final Map<String, Node> meshViews = new HashMap<>();

    /**
     * The root that may contain Node / MeshView / PolygonMeshView.
     * This root can be added to the scene graph.
     *
     * @return root node
     */
    public Group getRoot() {
        return root;
    }

    public final Set<String> getMeshNames() {
        return meshViews.keySet();
    }

    public final void addMeshView(String key, Node view) {
        meshViews.put(key, view);
        root.getChildren().add(view);
    }

    /**
     * Mesh names can be obtained by calling getMeshNames().
     *
     * @param key mesh name
     * @return a specific view (part) of this model
     */
    public final Node getMeshView(String key) {
        return meshViews.get(key);
    }

    /**
     * @return all views that this model contains
     */
    public final List<Node> getMeshViews() {
        return new ArrayList<>(meshViews.values());
    }

    public final void addMaterial(String key, Material material) {
        materials.put(key, material);
    }

    public final Set<String> getMaterialNames() {
        return materials.keySet();
    }

    public final Material getMaterial(String key) {
        return materials.get(key);
    }

    public final List<Material> getMaterials() {
        return new ArrayList<>(materials.values());
    }

    /**
     * @return animation timeline associated with this model
     */
    public Optional<Timeline> getTimeline() {
        return Optional.empty();
    }
}
