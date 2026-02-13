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
package org.dynamisfx.importers.off;

import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exporter for OFF (Object File Format) files.
 * <p>
 * Exports TriangleMesh data to ASCII OFF format.
 * </p>
 *
 * @author FXyz
 */
public class OffExporter implements Exporter {

    @Override
    public void export(TriangleMesh mesh, File file) throws IOException {
        export(mesh, file, "mesh");
    }

    @Override
    public void export(TriangleMesh mesh, File file, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int numVertices = points.length / 3;
        int numFaces = faces.length / 6; // 6 ints per triangle (v0, t0, v1, t1, v2, t2)

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("OFF");
            writer.newLine();
            writer.write("# Created by DynamisFX");
            writer.newLine();
            writer.write("# " + meshName);
            writer.newLine();

            // Write counts: vertex_count face_count edge_count
            writer.write(numVertices + " " + numFaces + " 0");
            writer.newLine();

            // Write vertices
            for (int i = 0; i < numVertices; i++) {
                writer.write(points[i * 3] + " " + points[i * 3 + 1] + " " + points[i * 3 + 2]);
                writer.newLine();
            }

            // Write faces (triangle format: 3 v0 v1 v2)
            for (int i = 0; i < numFaces; i++) {
                int v0 = faces[i * 6];
                int v1 = faces[i * 6 + 2];
                int v2 = faces[i * 6 + 4];
                writer.write("3 " + v0 + " " + v1 + " " + v2);
                writer.newLine();
            }
        }
    }

    @Override
    public String getExtension() {
        return "off";
    }

    @Override
    public String getFormatDescription() {
        return "Object File Format (OFF)";
    }
}
