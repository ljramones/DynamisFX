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

package org.dynamisfx.importers;

import javafx.scene.shape.TriangleMesh;

import java.io.File;
import java.io.IOException;

/**
 * Interface for 3D mesh exporters.
 * <p>
 * Implementations provide the ability to write TriangleMesh data to various
 * 3D file formats.
 * </p>
 *
 * @author FXyz
 */
public interface Exporter {

    /**
     * Exports a TriangleMesh to the specified file.
     *
     * @param mesh the mesh to export
     * @param file the destination file
     * @throws IOException if an error occurs during export
     */
    void export(TriangleMesh mesh, File file) throws IOException;

    /**
     * Exports a TriangleMesh to the specified file with a custom name.
     *
     * @param mesh the mesh to export
     * @param file the destination file
     * @param meshName the name to use for the mesh in the file
     * @throws IOException if an error occurs during export
     */
    void export(TriangleMesh mesh, File file, String meshName) throws IOException;

    /**
     * Returns the file extension this exporter produces (without the dot).
     *
     * @return the file extension (e.g., "stl", "obj", "gltf")
     */
    String getExtension();

    /**
     * Returns a description of the format this exporter produces.
     *
     * @return format description
     */
    String getFormatDescription();
}
