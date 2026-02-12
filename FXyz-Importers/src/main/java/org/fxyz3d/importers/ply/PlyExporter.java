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
package org.fxyz3d.importers.ply;

import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Exporter;

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
 * Exporter for PLY (Polygon File Format / Stanford Triangle Format) files.
 * <p>
 * Supports ASCII and binary little-endian PLY formats.
 * </p>
 *
 * @author FXyz
 */
public class PlyExporter implements Exporter {

    /** Export format options */
    public enum Format {
        /** ASCII text format - human readable but larger files */
        ASCII,
        /** Binary little-endian format - smaller files, faster to load */
        BINARY_LITTLE_ENDIAN
    }

    private Format format = Format.BINARY_LITTLE_ENDIAN;
    private boolean includeTexCoords = true;

    /**
     * Creates a PLY exporter with binary little-endian format (default).
     */
    public PlyExporter() {
    }

    /**
     * Creates a PLY exporter with the specified format.
     *
     * @param format the output format
     */
    public PlyExporter(Format format) {
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

    /**
     * Sets whether to include texture coordinates.
     *
     * @param include true to include texture coordinates
     */
    public void setIncludeTexCoords(boolean include) {
        this.includeTexCoords = include;
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
        return "ply";
    }

    @Override
    public String getFormatDescription() {
        return "Polygon File Format (PLY)";
    }

    private void exportAscii(TriangleMesh mesh, File file, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        float[] texCoords = new float[mesh.getTexCoords().size()];
        mesh.getTexCoords().toArray(texCoords);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int numVertices = points.length / 3;
        int numFaces = faces.length / 6;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("ply");
            writer.newLine();
            writer.write("format ascii 1.0");
            writer.newLine();
            writer.write("comment Created by FXyz3D");
            writer.newLine();
            writer.write("comment " + meshName);
            writer.newLine();
            writer.write("element vertex " + numVertices);
            writer.newLine();
            writer.write("property float x");
            writer.newLine();
            writer.write("property float y");
            writer.newLine();
            writer.write("property float z");
            writer.newLine();
            if (includeTexCoords && texCoords.length >= numVertices * 2) {
                writer.write("property float s");
                writer.newLine();
                writer.write("property float t");
                writer.newLine();
            }
            writer.write("element face " + numFaces);
            writer.newLine();
            writer.write("property list uchar int vertex_indices");
            writer.newLine();
            writer.write("end_header");
            writer.newLine();

            // Write vertices
            for (int i = 0; i < numVertices; i++) {
                writer.write(points[i * 3] + " " + points[i * 3 + 1] + " " + points[i * 3 + 2]);
                if (includeTexCoords && texCoords.length >= numVertices * 2) {
                    writer.write(" " + texCoords[i * 2] + " " + texCoords[i * 2 + 1]);
                }
                writer.newLine();
            }

            // Write faces
            for (int i = 0; i < numFaces; i++) {
                int v1 = faces[i * 6];
                int v2 = faces[i * 6 + 2];
                int v3 = faces[i * 6 + 4];
                writer.write("3 " + v1 + " " + v2 + " " + v3);
                writer.newLine();
            }
        }
    }

    private void exportBinary(TriangleMesh mesh, File file, String meshName) throws IOException {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        float[] texCoords = new float[mesh.getTexCoords().size()];
        mesh.getTexCoords().toArray(texCoords);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int numVertices = points.length / 3;
        int numFaces = faces.length / 6;
        boolean writeTexCoords = includeTexCoords && texCoords.length >= numVertices * 2;

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Write header as ASCII
            StringBuilder header = new StringBuilder();
            header.append("ply\n");
            header.append("format binary_little_endian 1.0\n");
            header.append("comment Created by FXyz3D\n");
            header.append("comment ").append(meshName).append("\n");
            header.append("element vertex ").append(numVertices).append("\n");
            header.append("property float x\n");
            header.append("property float y\n");
            header.append("property float z\n");
            if (writeTexCoords) {
                header.append("property float s\n");
                header.append("property float t\n");
            }
            header.append("element face ").append(numFaces).append("\n");
            header.append("property list uchar int vertex_indices\n");
            header.append("end_header\n");

            fos.write(header.toString().getBytes());

            // Write binary data
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));

            // Write vertices
            int vertexSize = writeTexCoords ? 20 : 12; // 3 or 5 floats
            ByteBuffer vertexBuffer = ByteBuffer.allocate(vertexSize).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < numVertices; i++) {
                vertexBuffer.clear();
                vertexBuffer.putFloat(points[i * 3]);
                vertexBuffer.putFloat(points[i * 3 + 1]);
                vertexBuffer.putFloat(points[i * 3 + 2]);
                if (writeTexCoords) {
                    vertexBuffer.putFloat(texCoords[i * 2]);
                    vertexBuffer.putFloat(texCoords[i * 2 + 1]);
                }
                dos.write(vertexBuffer.array());
            }

            // Write faces
            ByteBuffer faceBuffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN); // 1 byte + 3 ints

            for (int i = 0; i < numFaces; i++) {
                faceBuffer.clear();
                faceBuffer.put((byte) 3); // vertex count
                faceBuffer.putInt(faces[i * 6]);     // v1
                faceBuffer.putInt(faces[i * 6 + 2]); // v2
                faceBuffer.putInt(faces[i * 6 + 4]); // v3
                dos.write(faceBuffer.array());
            }

            dos.flush();
        }
    }
}
