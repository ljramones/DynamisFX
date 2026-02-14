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

package org.dynamisfx.importers.cad;

import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.IOException;
import java.net.URL;

/**
 * Stub importer for STEP and IGES CAD exchange formats.
 * <p>
 * STEP (ISO 10303) and IGES are CAD exchange formats that use BREP
 * (Boundary Representation) geometry. Full support requires a CAD geometry
 * kernel like OpenCASCADE, which is not available in pure Java.
 * </p>
 * <p>
 * Workaround: Convert STEP/IGES files to mesh formats using:
 * <ul>
 *   <li>FreeCAD (open STEP/IGES, export OBJ/STL)</li>
 *   <li>OpenSCAD (via OCCT)</li>
 *   <li>Blender with CAD add-ons</li>
 *   <li>Online converters (e.g., Aspose, Convertio)</li>
 * </ul>
 * </p>
 *
 * @author FXyz
 */
public class CadImporter implements Importer {

    @Override
    public Model3D load(URL url) throws IOException {
        throw new UnsupportedOperationException(
            "STEP/IGES formats require a CAD geometry kernel (e.g., OpenCASCADE). " +
            "Consider converting to OBJ or STL using FreeCAD or similar tools.");
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.toLowerCase();
        return "step".equals(ext) || "stp".equals(ext) || "iges".equals(ext) || "igs".equals(ext);
    }
}
