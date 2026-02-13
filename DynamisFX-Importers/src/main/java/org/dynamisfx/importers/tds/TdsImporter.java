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
package org.dynamisfx.importers.tds;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for 3DS (3D Studio) files.
 * <p>
 * 3DS is a binary, chunk-based format used by Autodesk 3D Studio. This importer
 * supports loading geometry including vertices, faces, and texture coordinates.
 * </p>
 * <p>
 * Key chunk IDs:
 * <ul>
 *   <li>0x4D4D - Main chunk</li>
 *   <li>0x3D3D - 3D Editor chunk</li>
 *   <li>0x4000 - Object block</li>
 *   <li>0x4100 - Triangular mesh</li>
 *   <li>0x4110 - Vertices list</li>
 *   <li>0x4120 - Faces description</li>
 *   <li>0x4140 - Texture coordinates</li>
 * </ul>
 * </p>
 *
 * @author FXyz
 */
public class TdsImporter implements Importer {

    private static final String SUPPORTED_EXT = "3ds";

    // Chunk IDs
    private static final int CHUNK_MAIN = 0x4D4D;
    private static final int CHUNK_3D_EDITOR = 0x3D3D;
    private static final int CHUNK_OBJECT = 0x4000;
    private static final int CHUNK_TRIMESH = 0x4100;
    private static final int CHUNK_VERTICES = 0x4110;
    private static final int CHUNK_FACES = 0x4120;
    private static final int CHUNK_TEXCOORDS = 0x4140;

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
        Model3D model = new Model3D();
        int meshIndex = 0;

        try (InputStream is = url.openStream()) {
            byte[] data = is.readAllBytes();
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            // Parse main chunk
            if (buffer.remaining() < 6) {
                throw new IOException("Invalid 3DS file: too short");
            }

            int mainId = buffer.getShort() & 0xFFFF;
            int mainLength = buffer.getInt();

            if (mainId != CHUNK_MAIN) {
                throw new IOException("Invalid 3DS file: expected main chunk 0x4D4D, found 0x" +
                        Integer.toHexString(mainId));
            }

            // Parse chunks recursively
            List<MeshData> meshes = new ArrayList<>();
            parseChunks(buffer, mainLength - 6, meshes);

            // Build model from parsed meshes
            for (MeshData meshData : meshes) {
                MeshView meshView = buildMeshView(meshData);
                String name = meshData.name != null ? meshData.name : "mesh_" + meshIndex;
                meshView.setId(name);
                model.addMeshView(name, meshView);
                meshIndex++;
            }

            if (meshIndex == 0) {
                throw new IOException("No meshes found in 3DS file");
            }

            // Add default material
            PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
            model.addMaterial("default", material);
        }

        return model;
    }

    private void parseChunks(ByteBuffer buffer, int remainingBytes, List<MeshData> meshes) throws IOException {
        int bytesRead = 0;
        MeshData currentMesh = null;

        while (bytesRead < remainingBytes && buffer.remaining() >= 6) {
            int chunkId = buffer.getShort() & 0xFFFF;
            int chunkLength = buffer.getInt();

            if (chunkLength < 6) {
                throw new IOException("Invalid chunk length: " + chunkLength);
            }

            int dataLength = chunkLength - 6;
            int startPos = buffer.position();

            switch (chunkId) {
                case CHUNK_3D_EDITOR:
                    // Recurse into 3D editor chunk
                    parseChunks(buffer, dataLength, meshes);
                    break;

                case CHUNK_OBJECT:
                    // Read object name
                    String name = readNullTerminatedString(buffer);
                    currentMesh = new MeshData();
                    currentMesh.name = name;
                    meshes.add(currentMesh);
                    // Recurse into object chunk
                    int remaining = dataLength - (buffer.position() - startPos);
                    parseObjectChunks(buffer, remaining, currentMesh);
                    break;

                default:
                    // Skip unknown chunk
                    skipBytes(buffer, dataLength);
                    break;
            }

            bytesRead += chunkLength;
        }
    }

    private void parseObjectChunks(ByteBuffer buffer, int remainingBytes, MeshData mesh) throws IOException {
        int bytesRead = 0;

        while (bytesRead < remainingBytes && buffer.remaining() >= 6) {
            int chunkId = buffer.getShort() & 0xFFFF;
            int chunkLength = buffer.getInt();

            if (chunkLength < 6) {
                throw new IOException("Invalid chunk length: " + chunkLength);
            }

            int dataLength = chunkLength - 6;
            int startPos = buffer.position();

            switch (chunkId) {
                case CHUNK_TRIMESH:
                    // Recurse into triangular mesh chunk
                    parseMeshChunks(buffer, dataLength, mesh);
                    break;

                default:
                    skipBytes(buffer, dataLength);
                    break;
            }

            bytesRead += chunkLength;
        }
    }

    private void parseMeshChunks(ByteBuffer buffer, int remainingBytes, MeshData mesh) throws IOException {
        int bytesRead = 0;

        while (bytesRead < remainingBytes && buffer.remaining() >= 6) {
            int chunkId = buffer.getShort() & 0xFFFF;
            int chunkLength = buffer.getInt();

            if (chunkLength < 6) {
                throw new IOException("Invalid chunk length: " + chunkLength);
            }

            int dataLength = chunkLength - 6;

            switch (chunkId) {
                case CHUNK_VERTICES:
                    readVertices(buffer, mesh);
                    break;

                case CHUNK_FACES:
                    readFaces(buffer, mesh, dataLength);
                    break;

                case CHUNK_TEXCOORDS:
                    readTexCoords(buffer, mesh);
                    break;

                default:
                    skipBytes(buffer, dataLength);
                    break;
            }

            bytesRead += chunkLength;
        }
    }

    private void readVertices(ByteBuffer buffer, MeshData mesh) {
        int count = buffer.getShort() & 0xFFFF;
        mesh.vertices = new float[count * 3];

        for (int i = 0; i < count; i++) {
            mesh.vertices[i * 3] = buffer.getFloat();
            mesh.vertices[i * 3 + 1] = buffer.getFloat();
            mesh.vertices[i * 3 + 2] = buffer.getFloat();
        }
    }

    private void readFaces(ByteBuffer buffer, MeshData mesh, int dataLength) {
        int count = buffer.getShort() & 0xFFFF;
        mesh.faces = new int[count * 3];

        for (int i = 0; i < count; i++) {
            mesh.faces[i * 3] = buffer.getShort() & 0xFFFF;
            mesh.faces[i * 3 + 1] = buffer.getShort() & 0xFFFF;
            mesh.faces[i * 3 + 2] = buffer.getShort() & 0xFFFF;
            // Skip face flags
            buffer.getShort();
        }

        // Skip any sub-chunks within the faces chunk
        int bytesRead = 2 + count * 8; // 2 for count + 8 bytes per face (3 shorts + flags)
        int remaining = dataLength - bytesRead;
        if (remaining > 0) {
            skipBytes(buffer, remaining);
        }
    }

    private void readTexCoords(ByteBuffer buffer, MeshData mesh) {
        int count = buffer.getShort() & 0xFFFF;
        mesh.texCoords = new float[count * 2];

        for (int i = 0; i < count; i++) {
            mesh.texCoords[i * 2] = buffer.getFloat();
            mesh.texCoords[i * 2 + 1] = buffer.getFloat();
        }
    }

    private String readNullTerminatedString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == 0) {
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }

    private void skipBytes(ByteBuffer buffer, int count) {
        int toSkip = Math.min(count, buffer.remaining());
        buffer.position(buffer.position() + toSkip);
    }

    private MeshView buildMeshView(MeshData meshData) {
        TriangleMesh mesh = new TriangleMesh();

        // Add vertices
        if (meshData.vertices != null) {
            mesh.getPoints().addAll(meshData.vertices);
        }

        // Add texture coordinates (or default)
        if (meshData.texCoords != null && meshData.texCoords.length > 0) {
            mesh.getTexCoords().addAll(meshData.texCoords);
        } else {
            // Default tex coords for each vertex
            int numVertices = meshData.vertices != null ? meshData.vertices.length / 3 : 0;
            float[] defaultTexCoords = new float[numVertices * 2];
            mesh.getTexCoords().addAll(defaultTexCoords);
        }

        // Add faces
        if (meshData.faces != null) {
            int numFaces = meshData.faces.length / 3;
            int[] faceArray = new int[numFaces * 6];

            for (int i = 0; i < numFaces; i++) {
                int v0 = meshData.faces[i * 3];
                int v1 = meshData.faces[i * 3 + 1];
                int v2 = meshData.faces[i * 3 + 2];

                // Each face entry: vertex_index, texcoord_index (x3)
                faceArray[i * 6] = v0;
                faceArray[i * 6 + 1] = v0;
                faceArray[i * 6 + 2] = v1;
                faceArray[i * 6 + 3] = v1;
                faceArray[i * 6 + 4] = v2;
                faceArray[i * 6 + 5] = v2;
            }
            mesh.getFaces().addAll(faceArray);
        }

        MeshView meshView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        return meshView;
    }

    private static class MeshData {
        String name;
        float[] vertices;
        float[] texCoords;
        int[] faces;
    }
}
