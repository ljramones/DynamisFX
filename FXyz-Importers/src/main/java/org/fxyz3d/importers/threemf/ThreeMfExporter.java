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
package org.fxyz3d.importers.threemf;

import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.importers.Exporter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

/**
 * Exporter for 3MF (3D Manufacturing Format) files.
 * <p>
 * 3MF is a modern 3D printing file format that uses a ZIP container with XML files.
 * This exporter creates a valid 3MF file with the mesh data stored in "3D/3dmodel.model".
 * </p>
 *
 * @author FXyz
 */
public class ThreeMfExporter implements Exporter {

    private static final String NAMESPACE = "http://schemas.microsoft.com/3dmanufacturing/core/2015/02";
    private static final String CONTENT_TYPES_NS = "http://schemas.openxmlformats.org/package/2006/content-types";
    private static final String RELATIONSHIPS_NS = "http://schemas.openxmlformats.org/package/2006/relationships";

    @Override
    public void export(TriangleMesh mesh, File file) throws IOException {
        export(mesh, file, "mesh");
    }

    @Override
    public void export(TriangleMesh mesh, File file, String meshName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            // Write [Content_Types].xml
            writeContentTypes(zos);

            // Write _rels/.rels
            writeRelationships(zos);

            // Write 3D/3dmodel.model
            writeModel(zos, mesh, meshName);
        }
    }

    @Override
    public String getExtension() {
        return "3mf";
    }

    @Override
    public String getFormatDescription() {
        return "3D Manufacturing Format (3MF)";
    }

    private void writeContentTypes(ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<Types xmlns=\"" + CONTENT_TYPES_NS + "\">");
        writer.println("  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        writer.println("  <Default Extension=\"model\" ContentType=\"application/vnd.ms-package.3dmanufacturing-3dmodel+xml\"/>");
        writer.println("</Types>");

        writer.flush();
        zos.closeEntry();
    }

    private void writeRelationships(ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry("_rels/.rels"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<Relationships xmlns=\"" + RELATIONSHIPS_NS + "\">");
        writer.println("  <Relationship Target=\"/3D/3dmodel.model\" Id=\"rel0\" Type=\"http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel\"/>");
        writer.println("</Relationships>");

        writer.flush();
        zos.closeEntry();
    }

    private void writeModel(ZipOutputStream zos, TriangleMesh mesh, String meshName) throws IOException {
        zos.putNextEntry(new ZipEntry("3D/3dmodel.model"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));

        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);

        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<model unit=\"millimeter\" xml:lang=\"en-US\" xmlns=\"" + NAMESPACE + "\">");
        writer.println("  <metadata name=\"Title\">" + escapeXml(meshName) + "</metadata>");
        writer.println("  <resources>");
        writer.println("    <object id=\"1\" type=\"model\" name=\"" + escapeXml(meshName) + "\">");
        writer.println("      <mesh>");

        // Write vertices
        writer.println("        <vertices>");
        int numVertices = points.length / 3;
        for (int i = 0; i < numVertices; i++) {
            float x = points[i * 3];
            float y = points[i * 3 + 1];
            float z = points[i * 3 + 2];
            writer.println("          <vertex x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\"/>");
        }
        writer.println("        </vertices>");

        // Write triangles
        writer.println("        <triangles>");
        int numTriangles = faces.length / 6;
        for (int i = 0; i < numTriangles; i++) {
            // faces array: v1, t1, v2, t2, v3, t3
            int v1 = faces[i * 6];
            int v2 = faces[i * 6 + 2];
            int v3 = faces[i * 6 + 4];
            writer.println("          <triangle v1=\"" + v1 + "\" v2=\"" + v2 + "\" v3=\"" + v3 + "\"/>");
        }
        writer.println("        </triangles>");

        writer.println("      </mesh>");
        writer.println("    </object>");
        writer.println("  </resources>");
        writer.println("  <build>");
        writer.println("    <item objectid=\"1\"/>");
        writer.println("  </build>");
        writer.println("</model>");

        writer.flush();
        zos.closeEntry();
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
