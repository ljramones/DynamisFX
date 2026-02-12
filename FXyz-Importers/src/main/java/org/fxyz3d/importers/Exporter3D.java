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

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.gltf.GltfExporter;
import org.fxyz3d.importers.obj.ObjExporter;
import org.fxyz3d.importers.off.OffExporter;
import org.fxyz3d.importers.ply.PlyExporter;
import org.fxyz3d.importers.stl.StlExporter;
import org.fxyz3d.importers.threemf.ThreeMfExporter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified exporter for all supported 3D file formats.
 * <p>
 * Provides a simple API for exporting meshes to various formats based on
 * file extension.
 * </p>
 *
 * <pre>{@code
 * // Export based on file extension
 * Exporter3D.export(mesh, new File("model.stl"));
 * Exporter3D.export(mesh, new File("model.glb"));
 * Exporter3D.export(mesh, new File("model.obj"));
 * Exporter3D.export(mesh, new File("model.ply"));
 * }</pre>
 *
 * @author FXyz
 */
public final class Exporter3D {

    private static final Map<String, Exporter> EXPORTERS = new HashMap<>();

    static {
        registerExporter("stl", new StlExporter());
        registerExporter("obj", new ObjExporter());
        registerExporter("gltf", new GltfExporter(GltfExporter.Format.GLTF));
        registerExporter("glb", new GltfExporter(GltfExporter.Format.GLB));
        registerExporter("ply", new PlyExporter());
        registerExporter("off", new OffExporter());
        registerExporter("3mf", new ThreeMfExporter());
    }

    private Exporter3D() {
        // Utility class
    }

    /**
     * Registers an exporter for a file extension.
     *
     * @param extension the file extension (without dot)
     * @param exporter the exporter to use
     */
    public static void registerExporter(String extension, Exporter exporter) {
        EXPORTERS.put(extension.toLowerCase(), exporter);
    }

    /**
     * Get array of extension filters for supported export formats.
     *
     * @return array of extension filters for supported formats
     */
    public static String[] getSupportedFormatExtensionFilters() {
        return new String[]{"*.stl", "*.obj", "*.gltf", "*.glb", "*.ply", "*.off", "*.3mf"};
    }

    /**
     * Get array of supported export extensions.
     *
     * @return array of supported extensions
     */
    public static String[] getSupportedExtensions() {
        return EXPORTERS.keySet().toArray(new String[0]);
    }

    /**
     * Checks if a format is supported for export.
     *
     * @param extension the file extension (with or without dot)
     * @return true if the format is supported
     */
    public static boolean isSupported(String extension) {
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return EXPORTERS.containsKey(extension.toLowerCase());
    }

    /**
     * Exports a TriangleMesh to a file. The format is determined by the file extension.
     *
     * @param mesh the mesh to export
     * @param file the destination file
     * @throws IOException if an error occurs during export
     * @throws IllegalArgumentException if the format is not supported
     */
    public static void export(TriangleMesh mesh, File file) throws IOException {
        export(mesh, file, "mesh");
    }

    /**
     * Exports a TriangleMesh to a file with a custom mesh name.
     *
     * @param mesh the mesh to export
     * @param file the destination file
     * @param meshName the name for the mesh in the file
     * @throws IOException if an error occurs during export
     * @throws IllegalArgumentException if the format is not supported
     */
    public static void export(TriangleMesh mesh, File file, String meshName) throws IOException {
        String extension = getExtension(file);
        Exporter exporter = EXPORTERS.get(extension);

        if (exporter == null) {
            throw new IllegalArgumentException(
                "Unsupported export format: " + extension +
                ". Supported formats: " + String.join(", ", EXPORTERS.keySet()));
        }

        exporter.export(mesh, file, meshName);
    }

    /**
     * Exports a MeshView to a file. The mesh must be a TriangleMesh.
     *
     * @param meshView the mesh view to export
     * @param file the destination file
     * @throws IOException if an error occurs during export
     * @throws IllegalArgumentException if the format is not supported or mesh is not a TriangleMesh
     */
    public static void export(MeshView meshView, File file) throws IOException {
        if (!(meshView.getMesh() instanceof TriangleMesh)) {
            throw new IllegalArgumentException("MeshView must contain a TriangleMesh");
        }
        String name = meshView.getId() != null ? meshView.getId() : "mesh";
        export((TriangleMesh) meshView.getMesh(), file, name);
    }

    /**
     * Gets the exporter for a specific format.
     *
     * @param extension the file extension (without dot)
     * @return the exporter, or null if not found
     */
    public static Exporter getExporter(String extension) {
        return EXPORTERS.get(extension.toLowerCase());
    }

    private static String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("File must have an extension: " + name);
        }
        return name.substring(dot + 1).toLowerCase();
    }
}
