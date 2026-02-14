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
