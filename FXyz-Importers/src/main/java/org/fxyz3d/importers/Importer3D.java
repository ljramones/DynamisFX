/*
 * Copyright (c) 2019 F(X)yz
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxyz3d.importers;

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
                 "org.fxyz3d.importers.maya.MayaImporter",
                 "org.fxyz3d.importers.obj.ObjImporter",
                 "org.fxyz3d.importers.fxml.FXMLImporter",
                 "org.fxyz3d.importers.stl.StlImporter",
                 "org.fxyz3d.importers.gltf.GltfImporter",
                 "org.fxyz3d.importers.ply.PlyImporter",
                 "org.fxyz3d.importers.off.OffImporter",
                 "org.fxyz3d.importers.tds.TdsImporter",
                 "org.fxyz3d.importers.dae.ColladaImporter",
                 "org.fxyz3d.importers.x3d.X3dImporter",
                 "org.fxyz3d.importers.threemf.ThreeMfImporter",
                 "org.fxyz3d.importers.vrml.VrmlImporter",
                 "org.fxyz3d.importers.dxf.DxfImporter",
                 "org.fxyz3d.importers.usd.UsdImporter",
                 "org.fxyz3d.importers.cad.CadImporter",
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
