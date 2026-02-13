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

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.importers.Importer;
import org.dynamisfx.importers.Model3D;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importer for glTF 2.0 (GL Transmission Format) files.
 * <p>
 * Supports both .gltf (JSON + binary) and .glb (binary container) formats.
 * This is a simplified implementation supporting basic mesh geometry.
 * </p>
 *
 * @author FXyz
 */
public class GltfImporter implements Importer {

    private static final int GLB_MAGIC = 0x46546C67; // "glTF" in little-endian
    private static final int GLB_VERSION = 2;
    private static final int CHUNK_TYPE_JSON = 0x4E4F534A; // "JSON"
    private static final int CHUNK_TYPE_BIN = 0x004E4942; // "BIN\0"

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
        return "gltf".equalsIgnoreCase(extension) || "glb".equalsIgnoreCase(extension);
    }

    private Model3D read(URL url) throws IOException {
        String path = url.toExternalForm().toLowerCase();
        if (path.endsWith(".glb")) {
            return readGlb(url);
        } else {
            return readGltf(url);
        }
    }

    private Model3D readGlb(URL url) throws IOException {
        try (DataInputStream dis = new DataInputStream(url.openStream())) {
            // Read header
            int magic = Integer.reverseBytes(dis.readInt());
            if (magic != GLB_MAGIC) {
                throw new IOException("Invalid GLB file: bad magic number");
            }

            int version = Integer.reverseBytes(dis.readInt());
            if (version != GLB_VERSION) {
                throw new IOException("Unsupported GLB version: " + version);
            }

            int length = Integer.reverseBytes(dis.readInt());

            // Read JSON chunk
            int jsonChunkLength = Integer.reverseBytes(dis.readInt());
            int jsonChunkType = Integer.reverseBytes(dis.readInt());
            if (jsonChunkType != CHUNK_TYPE_JSON) {
                throw new IOException("Expected JSON chunk, got: " + Integer.toHexString(jsonChunkType));
            }

            byte[] jsonBytes = new byte[jsonChunkLength];
            dis.readFully(jsonBytes);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            // Read binary chunk (if present)
            byte[] binData = null;
            if (dis.available() > 0) {
                int binChunkLength = Integer.reverseBytes(dis.readInt());
                int binChunkType = Integer.reverseBytes(dis.readInt());
                if (binChunkType == CHUNK_TYPE_BIN) {
                    binData = new byte[binChunkLength];
                    dis.readFully(binData);
                }
            }

            return parseGltfJson(json, binData, url);
        }
    }

    private Model3D readGltf(URL url) throws IOException {
        String json;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            json = sb.toString();
        }

        return parseGltfJson(json, null, url);
    }

    private Model3D parseGltfJson(String json, byte[] embeddedBin, URL baseUrl) throws IOException {
        Model3D model = new Model3D();

        // Simple JSON parsing (avoiding external dependencies)
        Map<String, Object> root = parseJsonObject(json);

        // Parse buffers
        List<byte[]> buffers = new ArrayList<>();
        List<Map<String, Object>> bufferList = getJsonArray(root, "buffers");
        if (bufferList != null) {
            for (Map<String, Object> buffer : bufferList) {
                String uri = getString(buffer, "uri");
                if (uri == null && embeddedBin != null) {
                    buffers.add(embeddedBin);
                } else if (uri != null && uri.startsWith("data:")) {
                    // Base64 embedded data
                    int commaIdx = uri.indexOf(',');
                    String base64Data = uri.substring(commaIdx + 1);
                    buffers.add(Base64.getDecoder().decode(base64Data));
                } else if (uri != null) {
                    // External file
                    URL bufferUrl = new URL(baseUrl, uri);
                    buffers.add(loadBinaryFile(bufferUrl));
                }
            }
        }

        // Parse buffer views
        List<Map<String, Object>> bufferViews = getJsonArray(root, "bufferViews");

        // Parse accessors
        List<Map<String, Object>> accessors = getJsonArray(root, "accessors");

        // Parse meshes
        List<Map<String, Object>> meshes = getJsonArray(root, "meshes");
        if (meshes != null) {
            int meshIndex = 0;
            for (Map<String, Object> meshDef : meshes) {
                String meshName = getString(meshDef, "name");
                if (meshName == null) {
                    meshName = "mesh_" + meshIndex;
                }

                List<Map<String, Object>> primitives = getJsonArray(meshDef, "primitives");
                if (primitives != null) {
                    int primIndex = 0;
                    for (Map<String, Object> primitive : primitives) {
                        String primName = meshName + (primitives.size() > 1 ? "_" + primIndex : "");
                        TriangleMesh mesh = parsePrimitive(primitive, accessors, bufferViews, buffers);
                        if (mesh != null) {
                            MeshView meshView = new MeshView(mesh);
                            meshView.setId(primName);
                            PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
                            meshView.setMaterial(material);
                            meshView.setCullFace(CullFace.NONE);
                            model.addMeshView(primName, meshView);
                            model.addMaterial(primName, material);
                        }
                        primIndex++;
                    }
                }
                meshIndex++;
            }
        }

        return model;
    }

    private TriangleMesh parsePrimitive(Map<String, Object> primitive,
                                        List<Map<String, Object>> accessors,
                                        List<Map<String, Object>> bufferViews,
                                        List<byte[]> buffers) throws IOException {
        Map<String, Object> attributes = getJsonObject(primitive, "attributes");
        if (attributes == null) {
            return null;
        }

        Integer positionAccessorIdx = getInt(attributes, "POSITION");
        Integer texcoordAccessorIdx = getInt(attributes, "TEXCOORD_0");
        Integer indicesAccessorIdx = getInt(primitive, "indices");

        if (positionAccessorIdx == null) {
            return null;
        }

        // Read positions
        float[] positions = readFloatAccessor(positionAccessorIdx, accessors, bufferViews, buffers);
        if (positions == null) {
            return null;
        }

        // Read texture coordinates (optional)
        float[] texcoords = null;
        if (texcoordAccessorIdx != null) {
            texcoords = readFloatAccessor(texcoordAccessorIdx, accessors, bufferViews, buffers);
        }

        // Read indices (optional)
        int[] indices = null;
        if (indicesAccessorIdx != null) {
            indices = readIntAccessor(indicesAccessorIdx, accessors, bufferViews, buffers);
        }

        // Build TriangleMesh
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(positions);

        // Add texture coordinates
        if (texcoords != null) {
            mesh.getTexCoords().addAll(texcoords);
        } else {
            mesh.getTexCoords().addAll(0f, 0f);
        }

        // Build faces
        if (indices != null) {
            int[] faces = new int[indices.length * 2];
            for (int i = 0; i < indices.length; i++) {
                faces[i * 2] = indices[i]; // vertex index
                faces[i * 2 + 1] = texcoords != null ? indices[i] : 0; // texcoord index
            }
            mesh.getFaces().addAll(faces);
        } else {
            // Non-indexed geometry
            int numVertices = positions.length / 3;
            int[] faces = new int[numVertices * 2];
            for (int i = 0; i < numVertices; i++) {
                faces[i * 2] = i;
                faces[i * 2 + 1] = texcoords != null ? i : 0;
            }
            mesh.getFaces().addAll(faces);
        }

        return mesh;
    }

    private float[] readFloatAccessor(int accessorIdx, List<Map<String, Object>> accessors,
                                      List<Map<String, Object>> bufferViews, List<byte[]> buffers) {
        if (accessorIdx >= accessors.size()) return null;

        Map<String, Object> accessor = accessors.get(accessorIdx);
        int bufferViewIdx = getInt(accessor, "bufferView");
        int count = getInt(accessor, "count");
        int componentType = getInt(accessor, "componentType");
        String type = getString(accessor, "type");
        int byteOffset = getIntOrDefault(accessor, "byteOffset", 0);

        int numComponents = getNumComponents(type);
        if (numComponents == 0) return null;

        Map<String, Object> bufferView = bufferViews.get(bufferViewIdx);
        int bufferIdx = getInt(bufferView, "buffer");
        int viewByteOffset = getIntOrDefault(bufferView, "byteOffset", 0);
        int byteStride = getIntOrDefault(bufferView, "byteStride", 0);

        byte[] buffer = buffers.get(bufferIdx);
        ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
        bb.position(viewByteOffset + byteOffset);

        float[] result = new float[count * numComponents];
        int stride = byteStride > 0 ? byteStride : numComponents * 4;

        for (int i = 0; i < count; i++) {
            bb.position(viewByteOffset + byteOffset + i * stride);
            for (int j = 0; j < numComponents; j++) {
                if (componentType == 5126) { // FLOAT
                    result[i * numComponents + j] = bb.getFloat();
                }
            }
        }

        return result;
    }

    private int[] readIntAccessor(int accessorIdx, List<Map<String, Object>> accessors,
                                  List<Map<String, Object>> bufferViews, List<byte[]> buffers) {
        if (accessorIdx >= accessors.size()) return null;

        Map<String, Object> accessor = accessors.get(accessorIdx);
        int bufferViewIdx = getInt(accessor, "bufferView");
        int count = getInt(accessor, "count");
        int componentType = getInt(accessor, "componentType");
        int byteOffset = getIntOrDefault(accessor, "byteOffset", 0);

        Map<String, Object> bufferView = bufferViews.get(bufferViewIdx);
        int bufferIdx = getInt(bufferView, "buffer");
        int viewByteOffset = getIntOrDefault(bufferView, "byteOffset", 0);

        byte[] buffer = buffers.get(bufferIdx);
        ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
        bb.position(viewByteOffset + byteOffset);

        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            switch (componentType) {
                case 5121: // UNSIGNED_BYTE
                    result[i] = bb.get() & 0xFF;
                    break;
                case 5123: // UNSIGNED_SHORT
                    result[i] = bb.getShort() & 0xFFFF;
                    break;
                case 5125: // UNSIGNED_INT
                    result[i] = bb.getInt();
                    break;
            }
        }

        return result;
    }

    private int getNumComponents(String type) {
        switch (type) {
            case "SCALAR": return 1;
            case "VEC2": return 2;
            case "VEC3": return 3;
            case "VEC4": return 4;
            case "MAT2": return 4;
            case "MAT3": return 9;
            case "MAT4": return 16;
            default: return 0;
        }
    }

    private byte[] loadBinaryFile(URL url) throws IOException {
        try (InputStream is = url.openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    // Simple JSON parsing helpers (avoiding external dependencies)
    private Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }
        json = json.substring(1, json.length() - 1).trim();

        // Parse key-value pairs
        int depth = 0;
        int start = 0;
        String key = null;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;

            if (depth == 0 && c == ':' && key == null) {
                key = json.substring(start, i).trim();
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                start = i + 1;
            }
            if (depth == 0 && (c == ',' || i == json.length() - 1)) {
                if (key != null) {
                    String value = json.substring(start, c == ',' ? i : i + 1).trim();
                    result.put(key, parseJsonValue(value));
                    key = null;
                }
                start = i + 1;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return result;
        }
        json = json.substring(1, json.length() - 1).trim();

        int depth = 0;
        int start = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;

            if (depth == 0 && (c == ',' || i == json.length() - 1)) {
                String element = json.substring(start, c == ',' ? i : i + 1).trim();
                Object value = parseJsonValue(element);
                if (value instanceof Map) {
                    result.add((Map<String, Object>) value);
                }
                start = i + 1;
            }
        }

        return result;
    }

    private Object parseJsonValue(String value) {
        value = value.trim();
        if (value.startsWith("{")) {
            return parseJsonObject(value);
        } else if (value.startsWith("[")) {
            return parseJsonArray(value);
        } else if (value.startsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else if (value.equals("null")) {
            return null;
        } else {
            try {
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    return Integer.parseInt(value);
                }
            } catch (NumberFormatException e) {
                return value;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getJsonArray(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getJsonObject(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private String getString(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        return value instanceof String ? (String) value : null;
    }

    private Integer getInt(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return null;
    }

    private int getIntOrDefault(Map<String, Object> obj, String key, int defaultValue) {
        Integer value = getInt(obj, key);
        return value != null ? value : defaultValue;
    }
}
