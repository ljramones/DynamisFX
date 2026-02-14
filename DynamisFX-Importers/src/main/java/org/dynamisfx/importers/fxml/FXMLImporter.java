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

package org.dynamisfx.importers.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.IOException;
import java.net.URL;

public class FXMLImporter implements Importer {

    private static final String SUPPORTED_EXT = "fxml";

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return read(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        final Object fxmlRoot = FXMLLoader.load(url);

        Model3D model = new Model3D();

        if (fxmlRoot instanceof Node) {
            model.addMeshView("default", (Node) fxmlRoot);
            return model;
        } else if (fxmlRoot instanceof TriangleMesh) {
            model.addMeshView("default", new MeshView((TriangleMesh) fxmlRoot));
            return model;
        }

        throw new IOException("Unknown object in FXML file [" + fxmlRoot.getClass().getName() + "]");
    }
}
