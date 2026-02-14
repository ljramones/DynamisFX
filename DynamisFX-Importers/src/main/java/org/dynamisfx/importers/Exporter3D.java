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

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.gltf.GltfExporter;
import org.dynamisfx.importers.obj.ObjExporter;
import org.dynamisfx.importers.off.OffExporter;
import org.dynamisfx.importers.ply.PlyExporter;
import org.dynamisfx.importers.stl.StlExporter;
import org.dynamisfx.importers.threemf.ThreeMfExporter;

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
