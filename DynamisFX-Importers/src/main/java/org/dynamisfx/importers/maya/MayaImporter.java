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

package org.dynamisfx.importers.maya;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maya Importer with support for Maya "ma" format.
 */
public class MayaImporter implements Importer {

    private static final String SUPPORTED_EXT = "ma";

    @Override
    public Model3D load(URL url) throws IOException {
        return load(url, false);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return load(url, true);
    }

    private Model3D load(URL url, boolean asPolygonMesh) {
        Loader loader = new Loader();
        loader.load(url, asPolygonMesh);

        // This root is not automatically added to the scene.
        // It needs to be added by the user of MayaImporter.
        //            root = new Xform();

        MayaGroup mayaRoot = new MayaGroup();
        // Add top level nodes to the root
        int nodeCount = 0;
        for (Node n : loader.loaded.values()) {
            if (n != null) {
                // Only add a node if it has no parents, ie. top level node
                if (n.getParent() == null) {
                    log("Adding top level node " + n.getId() + " to root!");

                    n.setDepthTest(DepthTest.ENABLE);
                    if (!(n instanceof MeshView) || ((TriangleMesh)((MeshView)n).getMesh()).getPoints().size() > 0) {
                        mayaRoot.getChildren().add(n);
                    }
                }
                nodeCount++;
            }
        }
        // rootCharacter.setRootJoint(loader.rootJoint);
        log("There are " + nodeCount + " nodes.");

        // if meshes were not loaded in the code above
        // (which they now are) one would need to
        // set meshParents from the loader
        // meshParents.addAll(loader.meshParents.keySet());
        // this is not necessary at the moment

        Timeline timeline = new Timeline();

        // TODO: possibly add parallel option
        loader.keyFrameMap.entrySet().stream()
                .map(entry -> new KeyFrame(Duration.seconds(entry.getKey()), null, null, entry.getValue()))
                .forEach(timeline.getKeyFrames()::add);

        log("Loaded " + timeline.getKeyFrames().size() + " key frames.");

        Model3D model = new Model3D() {
            @Override
            public Optional<Timeline> getTimeline() {
                return Optional.of(timeline);
            }
        };

        model.addMeshView("default", mayaRoot);

        return model;
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private static void log(String message) {
        Logger.getLogger(MayaImporter.class.getName()).log(Level.FINEST, message);
    }
}
