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

package org.dynamisfx.importers.stl;

import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Exporter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Exporter for STL (Stereolithography) files.
 * <p>
 * Supports both ASCII and binary STL formats. Binary format is recommended
 * for smaller file sizes and faster loading.
 * </p>
 *
 * @author FXyz
 */
public class StlExporter implements Exporter {

    /** Export format options */
    public enum Format {
        /** ASCII text format - human readable but larger files */
        ASCII,
        /** Binary format - smaller files, faster to load */
        BINARY
    }

    private Format format = Format.BINARY;

    /**
     * Creates an STL exporter with binary format (default).
     */
    public StlExporter() {
    }

    /**
     * Creates an STL exporter with the specified format.
     *
     * @param format the output format (ASCII or BINARY)
     */
    public StlExporter(Format format) {
        this.format = format;
    }

    /**
     * Sets the output format.
     *
     * @param format the output format
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Returns the current output format.
     *
     * @return the output format
     */
    public Format getFormat() {
        return format;
    }

    @Override
    public void export(TriangleMesh mesh, File file) throws IOException {
        export(mesh, file, "mesh");
    }

    @Override
    public void export(TriangleMesh mesh, File file, String meshName) throws IOException {
        if (format == Format.ASCII) {
            exportAscii(mesh, file, meshName);
        } else {
            exportBinary(mesh, file, meshName);
        }
    }

    @Override
    public String getExtension() {
        return "stl";
    }

    @Override
    public String getFormatDescription() {
        return "Stereolithography (STL)";
    }

    private void exportAscii(TriangleMesh mesh, File file, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("solid " + meshName);
            writer.newLine();

            // Each face has 6 elements: v1, t1, v2, t2, v3, t3
            int numTriangles = faces.length / 6;
            for (int i = 0; i < numTriangles; i++) {
                int v1Idx = faces[i * 6] * 3;
                int v2Idx = faces[i * 6 + 2] * 3;
                int v3Idx = faces[i * 6 + 4] * 3;

                float v1x = points[v1Idx], v1y = points[v1Idx + 1], v1z = points[v1Idx + 2];
                float v2x = points[v2Idx], v2y = points[v2Idx + 1], v2z = points[v2Idx + 2];
                float v3x = points[v3Idx], v3y = points[v3Idx + 1], v3z = points[v3Idx + 2];

                // Calculate face normal
                float[] normal = calculateNormal(v1x, v1y, v1z, v2x, v2y, v2z, v3x, v3y, v3z);

                writer.write("  facet normal " + normal[0] + " " + normal[1] + " " + normal[2]);
                writer.newLine();
                writer.write("    outer loop");
                writer.newLine();
                writer.write("      vertex " + v1x + " " + v1y + " " + v1z);
                writer.newLine();
                writer.write("      vertex " + v2x + " " + v2y + " " + v2z);
                writer.newLine();
                writer.write("      vertex " + v3x + " " + v3y + " " + v3z);
                writer.newLine();
                writer.write("    endloop");
                writer.newLine();
                writer.write("  endfacet");
                writer.newLine();
            }

            writer.write("endsolid " + meshName);
            writer.newLine();
        }
    }

    private void exportBinary(TriangleMesh mesh, File file, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int numTriangles = faces.length / 6;

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {

            // Write 80-byte header
            byte[] header = new byte[80];
            byte[] nameBytes = meshName.getBytes();
            System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 80));
            dos.write(header);

            // Write triangle count (little-endian)
            dos.write(intToLittleEndian(numTriangles));

            // Write triangles
            for (int i = 0; i < numTriangles; i++) {
                int v1Idx = faces[i * 6] * 3;
                int v2Idx = faces[i * 6 + 2] * 3;
                int v3Idx = faces[i * 6 + 4] * 3;

                float v1x = points[v1Idx], v1y = points[v1Idx + 1], v1z = points[v1Idx + 2];
                float v2x = points[v2Idx], v2y = points[v2Idx + 1], v2z = points[v2Idx + 2];
                float v3x = points[v3Idx], v3y = points[v3Idx + 1], v3z = points[v3Idx + 2];

                // Calculate and write normal
                float[] normal = calculateNormal(v1x, v1y, v1z, v2x, v2y, v2z, v3x, v3y, v3z);
                dos.write(floatToLittleEndian(normal[0]));
                dos.write(floatToLittleEndian(normal[1]));
                dos.write(floatToLittleEndian(normal[2]));

                // Write vertices
                dos.write(floatToLittleEndian(v1x));
                dos.write(floatToLittleEndian(v1y));
                dos.write(floatToLittleEndian(v1z));
                dos.write(floatToLittleEndian(v2x));
                dos.write(floatToLittleEndian(v2y));
                dos.write(floatToLittleEndian(v2z));
                dos.write(floatToLittleEndian(v3x));
                dos.write(floatToLittleEndian(v3y));
                dos.write(floatToLittleEndian(v3z));

                // Write attribute byte count (unused, set to 0)
                dos.write(0);
                dos.write(0);
            }
        }
    }

    private float[] calculateNormal(float v1x, float v1y, float v1z,
                                    float v2x, float v2y, float v2z,
                                    float v3x, float v3y, float v3z) {
        // Edge vectors
        float e1x = v2x - v1x, e1y = v2y - v1y, e1z = v2z - v1z;
        float e2x = v3x - v1x, e2y = v3y - v1y, e2z = v3z - v1z;

        // Cross product
        float nx = e1y * e2z - e1z * e2y;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;

        // Normalize
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        }

        return new float[]{nx, ny, nz};
    }

    private byte[] floatToLittleEndian(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    private byte[] intToLittleEndian(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }
}
