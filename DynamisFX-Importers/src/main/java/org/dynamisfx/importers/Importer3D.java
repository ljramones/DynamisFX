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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Base Importer for all supported 3D file formats
 */
public final class Importer3D {

    /**
     * Get array of extension filters for supported file formats.
     *
     * @return array of extension filters for supported file formats.
     */
    public static String[] getSupportedFormatExtensionFilters() {
        return new String[]{"*.ma", "*.ase", "*.obj", "*.fxml", "*.stl", "*.gltf", "*.glb", "*.ply",
                "*.off", "*.3ds", "*.dae", "*.x3d", "*.3mf", "*.wrl", "*.dxf",
                "*.usd", "*.usda", "*.usdc", "*.usdz", "*.step", "*.stp", "*.iges", "*.igs"};
    }

    /**
     * Load a 3D file, always loaded as TriangleMesh.
     *
     * @param fileUrl The url of the 3D file to load
     * @return The loaded Node which could be a MeshView or a Group
     * @throws IOException if issue loading file
     */
    public static Model3D load(URL fileUrl) throws IOException {
        return loadIncludingAnimation(fileUrl, false);
    }

    /**
     * Load a 3D file, load as a PolygonMesh if the loader supports.
     *
     * @param fileUrl The url of the 3D file to load
     * @return The loaded Node which could be a MeshView or a Group
     * @throws IOException if issue loading file
     */
    public static Model3D loadAsPoly(URL fileUrl) throws IOException {
        return loadIncludingAnimation(fileUrl, true);
    }

    /**
     * Load a 3D file.
     *
     * @param fileUrl The url of the 3D file to load
     * @param asPolygonMesh When true load as a PolygonMesh if the loader supports
     * @return The loaded Node which could be a MeshView or a Group and the Timeline animation
     * @throws IOException if issue loading file
     */
    private static Model3D loadIncludingAnimation(URL fileUrl, boolean asPolygonMesh) throws IOException {
        Objects.requireNonNull(fileUrl, "URL must not be null");

        String extForm = fileUrl.toExternalForm();

        // get extension
        final int dot = extForm.lastIndexOf('.');
        if (dot <= 0) {
            throw new IOException("Unknown 3D file format, url missing extension [" + fileUrl + "]");
        }
        final String extension = extForm.substring(dot + 1).toLowerCase();
        // Reference all the importer jars
        ImporterFinder finder = new ImporterFinder();
        URLClassLoader classLoader = finder.addUrlToClassPath();

        ServiceLoader<Importer> servantLoader = ServiceLoader.load(Importer.class, classLoader);
        // Check if we have an implementation for this file type
        Importer importer = null;
        for (Importer plugin : servantLoader) {
            if (plugin.isSupported(extension)) {
                importer = plugin;
                break;
            }
        }

        // Check well known loaders that might not be in a jar (ie. running from an IDE)
        if (importer == null) {
            String [] names = {
                 "org.dynamisfx.importers.maya.MayaImporter",
                 "org.dynamisfx.importers.obj.ObjImporter",
                 "org.dynamisfx.importers.fxml.FXMLImporter",
                 "org.dynamisfx.importers.stl.StlImporter",
                 "org.dynamisfx.importers.gltf.GltfImporter",
                 "org.dynamisfx.importers.ply.PlyImporter",
                 "org.dynamisfx.importers.off.OffImporter",
                 "org.dynamisfx.importers.tds.TdsImporter",
                 "org.dynamisfx.importers.dae.ColladaImporter",
                 "org.dynamisfx.importers.x3d.X3dImporter",
                 "org.dynamisfx.importers.threemf.ThreeMfImporter",
                 "org.dynamisfx.importers.vrml.VrmlImporter",
                 "org.dynamisfx.importers.dxf.DxfImporter",
                 "org.dynamisfx.importers.usd.UsdImporter",
                 "org.dynamisfx.importers.cad.CadImporter",
            };
            boolean fail = true;
            for (String name : names) {
                try {
                    Class<?> clazz = Class.forName(name);
                    Object obj = clazz.getDeclaredConstructor().newInstance();
                    if (obj instanceof Importer) {
                        Importer plugin = (Importer) obj;
                        if (plugin.isSupported(extension)) {
                            importer = plugin;
                            fail = false;
                            break;
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                        NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
                    // FAIL SILENTLY
                }
            }
            if (fail) throw new IOException("Unknown 3D file format [" + extension + "]");
        }


        return asPolygonMesh ? importer.loadAsPoly(fileUrl) : importer.load(fileUrl);
    }
}
