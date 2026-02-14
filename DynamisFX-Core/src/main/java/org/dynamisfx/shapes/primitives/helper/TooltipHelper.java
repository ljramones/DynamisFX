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

package org.dynamisfx.shapes.primitives.helper;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.logging.Logger;

public class TooltipHelper extends Pane {

    private static final Logger LOG = Logger.getLogger(TooltipHelper.class.getName());

    private static final double SIZE = 20;

    private final Node node;
    private final Node parent;
    private final Tooltip tooltip;

    public TooltipHelper(Node parent, Node node) {
        this.parent = parent;
        this.node = node;
        setPrefSize(SIZE, SIZE);
        setOpacity(0);

        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(this, tooltip);
    }

    public void updateTooltip(String id, Point3D point3D) {
        updateTooltip(point3D, String.format("%s :: %.3f %.3f %.3f", id == null ? "--" : id,
                point3D.getX(), point3D.getZ(), point3D.getY()));
    }

    public void updateTooltip(Point3D point3D, String tooltipText) {
        if (node == null) {
            LOG.warning("Node was null");
            return;
        }
        if (parent == null) {
            LOG.warning("Parent was null");
            return;
        }
        if (getScene() == null) {
            LOG.warning("TooltipHelper was not added to the scene graph");
            return;
        }
        Point3D coordinates = node.localToScene(point3D, true);
        Point3D p2 = parent.sceneToLocal(coordinates);
        getTransforms().setAll(new Translate(p2.getX() - SIZE / 2, p2.getY() - SIZE / 2));
        tooltip.setText(tooltipText);
    }
}
