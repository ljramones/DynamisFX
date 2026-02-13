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
