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

import java.io.IOException;
import java.net.URL;

public interface Importer {

    /**
     * Loads the 3D file
     *
     * @param url The url of the 3D file to load
     * @throws IOException If issue loading file
     * @return loaded 3d model
     */
    Model3D load(URL url) throws IOException;

    /**
     * Loads the 3D file as a polygonal mesh.
     *
     * @param url The url of the 3D file to load
     * @throws IOException If issue loading file
     * @return loaded 3d poly model
     */
    Model3D loadAsPoly(URL url) throws IOException;

    /**
     * Tests if the given 3D file extension is supported (e.g. "ma", "ase",
     * "obj", "fxml", "dae").
     *
     * @param supportType The file extension (e.g. "ma", "ase", "obj", "fxml", "dae")
     * @return True if the extension is of a supported type. False otherwise.
     */
    boolean isSupported(String supportType);
}
