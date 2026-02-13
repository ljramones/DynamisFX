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
package org.dynamisfx.importers.ply;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for PLY (Polygon File Format / Stanford Triangle Format) files.
 * <p>
 * Supports ASCII and binary PLY formats. PLY is commonly used for 3D scanning
 * data and point clouds.
 * </p>
 *
 * @author FXyz
 */
public class PlyImporter implements Importer {

    private static final String SUPPORTED_EXT = "ply";

    private enum Format { ASCII, BINARY_LITTLE_ENDIAN, BINARY_BIG_ENDIAN }

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return load(url);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            // Parse header
            String line = reader.readLine();
            if (!"ply".equals(line.trim().toLowerCase())) {
                throw new IOException("Invalid PLY file: missing 'ply' magic");
            }

            Format format = Format.ASCII;
            int vertexCount = 0;
            int faceCount = 0;
            List<String> vertexProperties = new ArrayList<>();
            List<String> vertexPropertyTypes = new ArrayList<>();
            boolean inVertexElement = false;
            boolean inFaceElement = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("comment")) {
                    continue;
                }
                if (line.equals("end_header")) {
                    break;
                }

                String[] parts = line.split("\\s+");
                if (parts[0].equals("format")) {
                    if (parts[1].equals("ascii")) {
                        format = Format.ASCII;
                    } else if (parts[1].equals("binary_little_endian")) {
                        format = Format.BINARY_LITTLE_ENDIAN;
                    } else if (parts[1].equals("binary_big_endian")) {
                        format = Format.BINARY_BIG_ENDIAN;
                    }
                } else if (parts[0].equals("element")) {
                    inVertexElement = false;
                    inFaceElement = false;
                    if (parts[1].equals("vertex")) {
                        vertexCount = Integer.parseInt(parts[2]);
                        inVertexElement = true;
                    } else if (parts[1].equals("face")) {
                        faceCount = Integer.parseInt(parts[2]);
                        inFaceElement = true;
                    }
                } else if (parts[0].equals("property") && inVertexElement) {
                    vertexPropertyTypes.add(parts[1]);
                    vertexProperties.add(parts[parts.length - 1]);
                }
            }

            // Find property indices
            int xIdx = vertexProperties.indexOf("x");
            int yIdx = vertexProperties.indexOf("y");
            int zIdx = vertexProperties.indexOf("z");
            int sIdx = vertexProperties.indexOf("s");
            int tIdx = vertexProperties.indexOf("t");
            if (sIdx < 0) sIdx = vertexProperties.indexOf("texture_u");
            if (tIdx < 0) tIdx = vertexProperties.indexOf("texture_v");

            if (xIdx < 0 || yIdx < 0 || zIdx < 0) {
                throw new IOException("PLY file missing x, y, or z vertex properties");
            }

            // Read data
            List<Float> vertices = new ArrayList<>();
            List<Float> texCoords = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            if (format == Format.ASCII) {
                readAsciiData(reader, vertexCount, faceCount, vertexProperties.size(),
                        xIdx, yIdx, zIdx, sIdx, tIdx, vertices, texCoords, faces);
            } else {
                // For binary, we need to re-open the stream and skip the header
                readBinaryData(url, format, vertexCount, faceCount, vertexProperties, vertexPropertyTypes,
                        xIdx, yIdx, zIdx, sIdx, tIdx, vertices, texCoords, faces);
            }

            return buildModel(vertices, texCoords, faces);
        }
    }

    private void readAsciiData(BufferedReader reader, int vertexCount, int faceCount,
                               int propertyCount, int xIdx, int yIdx, int zIdx, int sIdx, int tIdx,
                               List<Float> vertices, List<Float> texCoords, List<Integer> faces) throws IOException {
        // Read vertices
        for (int i = 0; i < vertexCount; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of file reading vertices");
            }
            String[] parts = line.trim().split("\\s+");
            vertices.add(Float.parseFloat(parts[xIdx]));
            vertices.add(Float.parseFloat(parts[yIdx]));
            vertices.add(Float.parseFloat(parts[zIdx]));

            if (sIdx >= 0 && tIdx >= 0 && sIdx < parts.length && tIdx < parts.length) {
                texCoords.add(Float.parseFloat(parts[sIdx]));
                texCoords.add(Float.parseFloat(parts[tIdx]));
            } else {
                texCoords.add(0f);
                texCoords.add(0f);
            }
        }

        // Read faces
        for (int i = 0; i < faceCount; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of file reading faces");
            }
            String[] parts = line.trim().split("\\s+");
            int count = Integer.parseInt(parts[0]);

            // Triangulate if necessary
            int v0 = Integer.parseInt(parts[1]);
            for (int j = 2; j < count; j++) {
                int v1 = Integer.parseInt(parts[j]);
                int v2 = Integer.parseInt(parts[j + 1]);
                faces.add(v0);
                faces.add(v1);
                faces.add(v2);
            }
        }
    }

    private void readBinaryData(URL url, Format format, int vertexCount, int faceCount,
                                List<String> vertexProperties, List<String> vertexPropertyTypes,
                                int xIdx, int yIdx, int zIdx, int sIdx, int tIdx,
                                List<Float> vertices, List<Float> texCoords, List<Integer> faces) throws IOException {
        try (InputStream is = url.openStream()) {
            // Skip to end of header
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("end_header")) {
                    break;
                }
            }

            // Now read binary data
            DataInputStream dis = new DataInputStream(is);
            ByteOrder order = (format == Format.BINARY_LITTLE_ENDIAN) ?
                    ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

            // Calculate vertex size
            int vertexSize = 0;
            for (String type : vertexPropertyTypes) {
                vertexSize += getTypeSize(type);
            }

            // Read vertices
            byte[] vertexData = new byte[vertexSize];
            for (int i = 0; i < vertexCount; i++) {
                dis.readFully(vertexData);
                ByteBuffer bb = ByteBuffer.wrap(vertexData).order(order);

                float[] values = new float[vertexProperties.size()];
                for (int j = 0; j < vertexProperties.size(); j++) {
                    values[j] = readFloat(bb, vertexPropertyTypes.get(j));
                }

                vertices.add(values[xIdx]);
                vertices.add(values[yIdx]);
                vertices.add(values[zIdx]);

                if (sIdx >= 0 && tIdx >= 0) {
                    texCoords.add(values[sIdx]);
                    texCoords.add(values[tIdx]);
                } else {
                    texCoords.add(0f);
                    texCoords.add(0f);
                }
            }

            // Read faces
            for (int i = 0; i < faceCount; i++) {
                int count = dis.readUnsignedByte();
                int[] faceIndices = new int[count];
                for (int j = 0; j < count; j++) {
                    if (order == ByteOrder.LITTLE_ENDIAN) {
                        faceIndices[j] = Integer.reverseBytes(dis.readInt());
                    } else {
                        faceIndices[j] = dis.readInt();
                    }
                }

                // Triangulate
                for (int j = 1; j < count - 1; j++) {
                    faces.add(faceIndices[0]);
                    faces.add(faceIndices[j]);
                    faces.add(faceIndices[j + 1]);
                }
            }
        }
    }

    private int getTypeSize(String type) {
        switch (type) {
            case "char":
            case "uchar":
            case "int8":
            case "uint8":
                return 1;
            case "short":
            case "ushort":
            case "int16":
            case "uint16":
                return 2;
            case "int":
            case "uint":
            case "int32":
            case "uint32":
            case "float":
            case "float32":
                return 4;
            case "double":
            case "float64":
                return 8;
            default:
                return 4;
        }
    }

    private float readFloat(ByteBuffer bb, String type) {
        switch (type) {
            case "char":
            case "int8":
                return bb.get();
            case "uchar":
            case "uint8":
                return bb.get() & 0xFF;
            case "short":
            case "int16":
                return bb.getShort();
            case "ushort":
            case "uint16":
                return bb.getShort() & 0xFFFF;
            case "int":
            case "int32":
                return bb.getInt();
            case "uint":
            case "uint32":
                return bb.getInt() & 0xFFFFFFFFL;
            case "float":
            case "float32":
                return bb.getFloat();
            case "double":
            case "float64":
                return (float) bb.getDouble();
            default:
                return bb.getFloat();
        }
    }

    private Model3D buildModel(List<Float> vertices, List<Float> texCoords, List<Integer> faces) {
        Model3D model = new Model3D();

        TriangleMesh mesh = new TriangleMesh();

        // Add vertices
        float[] points = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            points[i] = vertices.get(i);
        }
        mesh.getPoints().addAll(points);

        // Add texture coordinates
        float[] uvs = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            uvs[i] = texCoords.get(i);
        }
        mesh.getTexCoords().addAll(uvs);

        // Add faces (vertex index, texcoord index pairs)
        int[] faceArray = new int[faces.size() * 2];
        for (int i = 0; i < faces.size(); i++) {
            faceArray[i * 2] = faces.get(i);
            faceArray[i * 2 + 1] = faces.get(i); // Use same index for texcoords
        }
        mesh.getFaces().addAll(faceArray);

        MeshView meshView = new MeshView(mesh);
        meshView.setId("ply_mesh");
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        model.addMeshView("ply_mesh", meshView);
        model.addMaterial("default", material);

        return model;
    }
}
