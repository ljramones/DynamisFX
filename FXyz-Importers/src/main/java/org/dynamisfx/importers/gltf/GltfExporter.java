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
package org.dynamisfx.importers.gltf;

import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Exporter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Exporter for glTF 2.0 (GL Transmission Format) files.
 * <p>
 * Supports both .gltf (JSON + .bin) and .glb (binary container) formats.
 * Exports mesh geometry with positions, texture coordinates, and indices.
 * </p>
 *
 * @author FXyz
 */
public class GltfExporter implements Exporter {

    private static final int GLB_MAGIC = 0x46546C67; // "glTF"
    private static final int GLB_VERSION = 2;
    private static final int CHUNK_TYPE_JSON = 0x4E4F534A; // "JSON"
    private static final int CHUNK_TYPE_BIN = 0x004E4942; // "BIN\0"

    /** Export format options */
    public enum Format {
        /** Separate JSON (.gltf) and binary (.bin) files */
        GLTF,
        /** Single binary container (.glb) */
        GLB
    }

    private Format format = Format.GLB;

    /**
     * Creates a glTF exporter with GLB format (default).
     */
    public GltfExporter() {
    }

    /**
     * Creates a glTF exporter with the specified format.
     *
     * @param format the output format (GLTF or GLB)
     */
    public GltfExporter(Format format) {
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
        if (format == Format.GLB) {
            exportGlb(mesh, file, meshName);
        } else {
            exportGltf(mesh, file, meshName);
        }
    }

    @Override
    public String getExtension() {
        return format == Format.GLB ? "glb" : "gltf";
    }

    @Override
    public String getFormatDescription() {
        return "GL Transmission Format (glTF 2.0)";
    }

    private void exportGltf(TriangleMesh mesh, File file, String meshName) throws IOException {
        // Build binary buffer
        byte[] binaryData = buildBinaryBuffer(mesh);

        // Write binary file
        String baseName = file.getName();
        if (baseName.toLowerCase().endsWith(".gltf")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        File parentDir = file.getParentFile();
        if (parentDir == null) {
            parentDir = file.getAbsoluteFile().getParentFile();
        }
        File binFile = new File(parentDir, baseName + ".bin");
        try (FileOutputStream fos = new FileOutputStream(binFile)) {
            fos.write(binaryData);
        }

        // Write JSON file
        String json = buildGltfJson(mesh, meshName, baseName + ".bin", binaryData.length);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(json);
        }
    }

    private void exportGlb(TriangleMesh mesh, File file, String meshName) throws IOException {
        // Build binary buffer
        byte[] binaryData = buildBinaryBuffer(mesh);

        // Build JSON
        String json = buildGltfJson(mesh, meshName, null, binaryData.length);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        // Pad JSON to 4-byte boundary
        int jsonPadding = (4 - (jsonBytes.length % 4)) % 4;
        byte[] paddedJson = new byte[jsonBytes.length + jsonPadding];
        System.arraycopy(jsonBytes, 0, paddedJson, 0, jsonBytes.length);
        for (int i = 0; i < jsonPadding; i++) {
            paddedJson[jsonBytes.length + i] = 0x20; // Space padding for JSON
        }

        // Pad binary to 4-byte boundary
        int binPadding = (4 - (binaryData.length % 4)) % 4;
        byte[] paddedBin = new byte[binaryData.length + binPadding];
        System.arraycopy(binaryData, 0, paddedBin, 0, binaryData.length);

        // Calculate total length
        int totalLength = 12 + // Header
                8 + paddedJson.length + // JSON chunk
                8 + paddedBin.length; // Binary chunk

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            // Write header
            dos.writeInt(Integer.reverseBytes(GLB_MAGIC));
            dos.writeInt(Integer.reverseBytes(GLB_VERSION));
            dos.writeInt(Integer.reverseBytes(totalLength));

            // Write JSON chunk
            dos.writeInt(Integer.reverseBytes(paddedJson.length));
            dos.writeInt(Integer.reverseBytes(CHUNK_TYPE_JSON));
            dos.write(paddedJson);

            // Write binary chunk
            dos.writeInt(Integer.reverseBytes(paddedBin.length));
            dos.writeInt(Integer.reverseBytes(CHUNK_TYPE_BIN));
            dos.write(paddedBin);
        }
    }

    private byte[] buildBinaryBuffer(TriangleMesh mesh) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        float[] texCoords = new float[mesh.getTexCoords().size()];
        mesh.getTexCoords().toArray(texCoords);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        // Write indices (as unsigned short if possible, otherwise unsigned int)
        int numIndices = faces.length / 2; // faces has vertex/texcoord pairs
        int maxIndex = 0;
        for (int i = 0; i < faces.length; i += 2) {
            maxIndex = Math.max(maxIndex, faces[i]);
        }

        ByteBuffer bb;
        if (maxIndex <= 65535) {
            // Use unsigned short
            bb = ByteBuffer.allocate(numIndices * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < faces.length; i += 2) {
                bb.putShort((short) faces[i]);
            }
        } else {
            // Use unsigned int
            bb = ByteBuffer.allocate(numIndices * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < faces.length; i += 2) {
                bb.putInt(faces[i]);
            }
        }
        baos.write(bb.array());

        // Pad to 4-byte boundary
        int padding = (4 - (baos.size() % 4)) % 4;
        for (int i = 0; i < padding; i++) {
            baos.write(0);
        }

        // Write positions
        bb = ByteBuffer.allocate(points.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : points) {
            bb.putFloat(f);
        }
        baos.write(bb.array());

        // Write texture coordinates
        bb = ByteBuffer.allocate(texCoords.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : texCoords) {
            bb.putFloat(f);
        }
        baos.write(bb.array());

        return baos.toByteArray();
    }

    private String buildGltfJson(TriangleMesh mesh, String meshName, String binUri, int bufferLength) {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        float[] texCoords = new float[mesh.getTexCoords().size()];
        mesh.getTexCoords().toArray(texCoords);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        int numVertices = points.length / 3;
        int numTexCoords = texCoords.length / 2;
        int numIndices = faces.length / 2;

        // Determine index type
        int maxIndex = 0;
        for (int i = 0; i < faces.length; i += 2) {
            maxIndex = Math.max(maxIndex, faces[i]);
        }
        boolean useShortIndices = maxIndex <= 65535;
        int indexComponentType = useShortIndices ? 5123 : 5125; // UNSIGNED_SHORT or UNSIGNED_INT
        int indexByteLength = numIndices * (useShortIndices ? 2 : 4);
        int indexPadding = (4 - (indexByteLength % 4)) % 4;

        // Calculate offsets
        int positionByteOffset = indexByteLength + indexPadding;
        int positionByteLength = points.length * 4;
        int texcoordByteOffset = positionByteOffset + positionByteLength;
        int texcoordByteLength = texCoords.length * 4;

        // Calculate bounds
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i += 3) {
            minX = Math.min(minX, points[i]);
            maxX = Math.max(maxX, points[i]);
            minY = Math.min(minY, points[i + 1]);
            maxY = Math.max(maxY, points[i + 1]);
            minZ = Math.min(minZ, points[i + 2]);
            maxZ = Math.max(maxZ, points[i + 2]);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"asset\": {\n");
        sb.append("    \"version\": \"2.0\",\n");
        sb.append("    \"generator\": \"DynamisFX\"\n");
        sb.append("  },\n");

        // Scene
        sb.append("  \"scene\": 0,\n");
        sb.append("  \"scenes\": [{\"nodes\": [0]}],\n");

        // Nodes
        sb.append("  \"nodes\": [{\"mesh\": 0, \"name\": \"").append(meshName).append("\"}],\n");

        // Meshes
        sb.append("  \"meshes\": [{\n");
        sb.append("    \"name\": \"").append(meshName).append("\",\n");
        sb.append("    \"primitives\": [{\n");
        sb.append("      \"attributes\": {\n");
        sb.append("        \"POSITION\": 1");
        if (numTexCoords > 0) {
            sb.append(",\n        \"TEXCOORD_0\": 2");
        }
        sb.append("\n      },\n");
        sb.append("      \"indices\": 0\n");
        sb.append("    }]\n");
        sb.append("  }],\n");

        // Accessors
        sb.append("  \"accessors\": [\n");
        // Indices accessor
        sb.append("    {\n");
        sb.append("      \"bufferView\": 0,\n");
        sb.append("      \"componentType\": ").append(indexComponentType).append(",\n");
        sb.append("      \"count\": ").append(numIndices).append(",\n");
        sb.append("      \"type\": \"SCALAR\"\n");
        sb.append("    },\n");
        // Position accessor
        sb.append("    {\n");
        sb.append("      \"bufferView\": 1,\n");
        sb.append("      \"componentType\": 5126,\n");
        sb.append("      \"count\": ").append(numVertices).append(",\n");
        sb.append("      \"type\": \"VEC3\",\n");
        sb.append("      \"min\": [").append(minX).append(", ").append(minY).append(", ").append(minZ).append("],\n");
        sb.append("      \"max\": [").append(maxX).append(", ").append(maxY).append(", ").append(maxZ).append("]\n");
        sb.append("    }");
        if (numTexCoords > 0) {
            sb.append(",\n");
            // Texcoord accessor
            sb.append("    {\n");
            sb.append("      \"bufferView\": 2,\n");
            sb.append("      \"componentType\": 5126,\n");
            sb.append("      \"count\": ").append(numTexCoords).append(",\n");
            sb.append("      \"type\": \"VEC2\"\n");
            sb.append("    }");
        }
        sb.append("\n  ],\n");

        // Buffer views
        sb.append("  \"bufferViews\": [\n");
        // Indices buffer view
        sb.append("    {\n");
        sb.append("      \"buffer\": 0,\n");
        sb.append("      \"byteOffset\": 0,\n");
        sb.append("      \"byteLength\": ").append(indexByteLength).append(",\n");
        sb.append("      \"target\": 34963\n"); // ELEMENT_ARRAY_BUFFER
        sb.append("    },\n");
        // Position buffer view
        sb.append("    {\n");
        sb.append("      \"buffer\": 0,\n");
        sb.append("      \"byteOffset\": ").append(positionByteOffset).append(",\n");
        sb.append("      \"byteLength\": ").append(positionByteLength).append(",\n");
        sb.append("      \"target\": 34962\n"); // ARRAY_BUFFER
        sb.append("    }");
        if (numTexCoords > 0) {
            sb.append(",\n");
            // Texcoord buffer view
            sb.append("    {\n");
            sb.append("      \"buffer\": 0,\n");
            sb.append("      \"byteOffset\": ").append(texcoordByteOffset).append(",\n");
            sb.append("      \"byteLength\": ").append(texcoordByteLength).append(",\n");
            sb.append("      \"target\": 34962\n");
            sb.append("    }");
        }
        sb.append("\n  ],\n");

        // Buffers
        sb.append("  \"buffers\": [{\n");
        sb.append("    \"byteLength\": ").append(bufferLength);
        if (binUri != null) {
            sb.append(",\n    \"uri\": \"").append(binUri).append("\"");
        }
        sb.append("\n  }]\n");

        sb.append("}\n");
        return sb.toString();
    }
}
