/*
 * Copyright (c) 2013-2026, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxyz3d.importers;

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
