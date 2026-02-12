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
package org.fxyz3d.importers.usd;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Importer;
import org.fxyz3d.importers.Model3D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stub importer for USD (Universal Scene Description) files.
 * <p>
 * USD is Pixar's format for scene interchange. Full support requires the
 * native OpenUSD library (C++), which is not available in pure Java.
 * </p>
 * <p>
 * Workaround: Convert USD files to glTF using external tools like:
 * <ul>
 *   <li>usdview (from OpenUSD)</li>
 *   <li>Blender (import USD, export glTF)</li>
 *   <li>Apple's Reality Converter (macOS)</li>
 * </ul>
 * </p>
 *
 * @author FXyz
 */
public class UsdImporter implements Importer {

    private static final Pattern POINTS_PATTERN = Pattern.compile(
            "(?:point3[fd]|float3)\\[\\]\\s+points\\s*=\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern COUNTS_PATTERN = Pattern.compile(
            "int\\[\\]\\s+faceVertexCounts\\s*=\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern INDICES_PATTERN = Pattern.compile(
            "int\\[\\]\\s+faceVertexIndices\\s*=\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "[-+]?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?\\d+");

    @Override
    public Model3D load(URL url) throws IOException {
        String extension = extractExtension(url);
        if ("usda".equals(extension)) {
            return readUsda(url);
        }

        throw new UnsupportedOperationException(
                "Only ASCII .usda mesh import is currently supported. " +
                "For ." + extension + " use OpenUSD tooling and convert to glTF.");
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
        return "usd".equals(ext) || "usda".equals(ext) || "usdc".equals(ext) || "usdz".equals(ext);
    }

    private Model3D readUsda(URL url) throws IOException {
        String content = readAll(url);
        String noComments = stripLineComments(content);

        List<Float> points = extractPointArray(noComments);
        List<Integer> faceVertexCounts = extractIntArray(COUNTS_PATTERN, noComments, "faceVertexCounts");
        List<Integer> faceVertexIndices = extractIntArray(INDICES_PATTERN, noComments, "faceVertexIndices");

        validateTopology(faceVertexCounts, faceVertexIndices);
        return buildModel(points, faceVertexCounts, faceVertexIndices);
    }

    private static String extractExtension(URL url) {
        String extForm = url.toExternalForm().toLowerCase(Locale.ROOT);
        int dot = extForm.lastIndexOf('.');
        return dot >= 0 ? extForm.substring(dot + 1) : "";
    }

    private static String readAll(URL url) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private static String stripLineComments(String source) {
        StringBuilder builder = new StringBuilder();
        String[] lines = source.split("\\R");
        for (String line : lines) {
            int commentIndex = line.indexOf('#');
            if (commentIndex >= 0) {
                line = line.substring(0, commentIndex);
            }
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    private static List<Float> extractPointArray(String source) throws IOException {
        String body = extractArrayBody(POINTS_PATTERN, source, "points");
        List<Float> values = extractFloats(body);
        if (values.isEmpty() || values.size() % 3 != 0) {
            throw new IOException("Invalid USDA points array: expected triplets of xyz coordinates");
        }
        return values;
    }

    private static List<Integer> extractIntArray(Pattern pattern, String source, String name) throws IOException {
        String body = extractArrayBody(pattern, source, name);
        List<Integer> values = new ArrayList<>();
        Matcher matcher = INTEGER_PATTERN.matcher(body);
        while (matcher.find()) {
            values.add(Integer.parseInt(matcher.group()));
        }
        if (values.isEmpty()) {
            throw new IOException("Invalid USDA " + name + " array: no values found");
        }
        return values;
    }

    private static String extractArrayBody(Pattern pattern, String source, String name) throws IOException {
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            throw new IOException("Unsupported USDA content: missing " + name + " array");
        }
        return matcher.group(1);
    }

    private static List<Float> extractFloats(String source) {
        List<Float> values = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(source);
        while (matcher.find()) {
            values.add(Float.parseFloat(matcher.group()));
        }
        return values;
    }

    private static void validateTopology(List<Integer> counts, List<Integer> indices) throws IOException {
        int expectedIndexCount = 0;
        for (int count : counts) {
            if (count < 3) {
                throw new IOException("Invalid USDA faceVertexCounts entry: " + count + " (expected >= 3)");
            }
            expectedIndexCount += count;
        }
        if (expectedIndexCount != indices.size()) {
            throw new IOException("Invalid USDA topology: faceVertexCounts sum does not match faceVertexIndices count");
        }
    }

    private static Model3D buildModel(List<Float> points, List<Integer> counts, List<Integer> indices) throws IOException {
        TriangleMesh mesh = new TriangleMesh();

        float[] pointArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointArray[i] = points.get(i);
        }
        mesh.getPoints().addAll(pointArray);
        mesh.getTexCoords().addAll(0f, 0f);

        int triangleCount = 0;
        for (int count : counts) {
            triangleCount += count - 2;
        }
        int[] faces = new int[triangleCount * 6];

        int indexCursor = 0;
        int faceCursor = 0;
        int pointCount = points.size() / 3;
        for (int count : counts) {
            int v0 = indices.get(indexCursor);
            validateVertexIndex(v0, pointCount);
            for (int i = 1; i < count - 1; i++) {
                int v1 = indices.get(indexCursor + i);
                int v2 = indices.get(indexCursor + i + 1);
                validateVertexIndex(v1, pointCount);
                validateVertexIndex(v2, pointCount);

                faces[faceCursor++] = v0;
                faces[faceCursor++] = 0;
                faces[faceCursor++] = v1;
                faces[faceCursor++] = 0;
                faces[faceCursor++] = v2;
                faces[faceCursor++] = 0;
            }
            indexCursor += count;
        }
        mesh.getFaces().addAll(faces);

        MeshView meshView = new MeshView(mesh);
        meshView.setId("usd_mesh");
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        Model3D model = new Model3D();
        model.addMeshView("usd_mesh", meshView);
        model.addMaterial("default", material);
        return model;
    }

    private static void validateVertexIndex(int index, int pointCount) throws IOException {
        if (index < 0 || index >= pointCount) {
            throw new IOException("Invalid USDA face index " + index + " for " + pointCount + " points");
        }
    }
}
