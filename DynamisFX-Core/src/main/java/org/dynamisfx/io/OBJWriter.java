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
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.TriangleMesh;
import javax.imageio.ImageIO;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.paint.Palette;
import org.dynamisfx.scene.paint.Patterns;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.TextureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jpereda
 */
public class OBJWriter {

    private static final Logger LOG = LoggerFactory.getLogger(OBJWriter.class);

    private final String newline = System.getProperty("line.separator");
    private float[] points0, texCoord0;
    private int[] faces0, sm0;
    private BufferedWriter writer = null;
    private final TriangleMesh mesh;
    private final String fileName;
    private String diffuseMap;
    private String diffuseColor="0.0 0.0 0.0"; // black
    private TextureType defaultTexture=TextureType.NONE;
        
    public OBJWriter(TriangleMesh mesh, String fileName){
        this.mesh=mesh;
        this.fileName=fileName;
    }
    
    public void setMaterialColor(Color color){
        diffuseColor=""+((float)(color.getRed()))+" "+((float)(color.getGreen()))+" "+((float)(color.getBlue()));
    }
    
    public void setTextureColors(int numColors){
        if(numColors>0){
            defaultTexture=TextureType.COLORED_VERTICES_3D;
            Palette palette=new Palette(numColors);
            palette.createPalette(true);
            diffuseMap="palette_"+numColors+".png";
        }
    }
    public void setTexturePattern(){
        defaultTexture=TextureType.PATTERN;
        Patterns pattern = new Patterns(12, 12);
        pattern.createPattern(true);
        diffuseMap="patterne_12x12.png";
    }
    
    /**
     * Sets a texture image for the material.
     * @param image the image path or URL
     * @throws IOException if the image cannot be saved
     */
    public void setTextureImage(String image) throws IOException {
        ImageIO.write(SwingFXUtils.fromFXImage(new Image(image), null), "png", new File("image.png"));
        diffuseMap = "image.png";
    }
    
    /**
     * Exports the mesh to OBJ and MTL files.
     * @throws IOException if an error occurs during export
     */
    public void exportMesh() throws IOException {
        exportObjFile();
        exportMtlFile();
    }

    private void exportObjFile() throws IOException {
        File objFile = new File(fileName + ".obj");
        try (BufferedWriter objWriter = new BufferedWriter(new FileWriter(objFile))) {
            objWriter.write("# Material" + newline);
            objWriter.write("mtllib " + fileName + ".mtl" + newline);

            // Write vertices
            points0 = new float[mesh.getPoints().size()];
            mesh.getPoints().toArray(points0);
            List<Point3D> points1 = IntStream.range(0, points0.length / 3)
                    .mapToObj(i -> new Point3D(points0[3 * i], points0[3 * i + 1], points0[3 * i + 2]))
                    .collect(Collectors.toList());

            objWriter.write("# Vertices (" + points1.size() + ")" + newline);
            for (Point3D p : points1) {
                objWriter.write("v " + p.x + " " + p.y + " " + p.z + newline);
            }
            objWriter.write("# End Vertices" + newline);
            objWriter.write(newline);

            // Write texture coordinates
            texCoord0 = new float[mesh.getTexCoords().size()];
            mesh.getTexCoords().toArray(texCoord0);
            List<Point2D> texCoord1 = IntStream.range(0, texCoord0.length / 2)
                    .mapToObj(i -> new Point2D(texCoord0[2 * i], texCoord0[2 * i + 1]))
                    .collect(Collectors.toList());

            objWriter.write("# Textures Coordinates (" + texCoord1.size() + ")" + newline);
            for (Point2D t : texCoord1) {
                objWriter.write("vt " + ((float) t.getX()) + " " + ((float) (1d - t.getY())) + newline);
            }
            objWriter.write("# End Texture Coordinates " + newline);
            objWriter.write(newline);

            // Write faces
            faces0 = new int[mesh.getFaces().size()];
            mesh.getFaces().toArray(faces0);
            List<Integer[]> faces1 = IntStream.range(0, faces0.length / 6)
                    .mapToObj(i -> new Integer[]{faces0[6 * i], faces0[6 * i + 1],
                            faces0[6 * i + 2], faces0[6 * i + 3],
                            faces0[6 * i + 4], faces0[6 * i + 5]})
                    .collect(Collectors.toList());

            objWriter.write("# Faces (" + faces1.size() + ")" + newline);
            objWriter.write("# Material" + newline);
            objWriter.write("usemtl " + fileName + newline);

            sm0 = new int[mesh.getFaces().size()];
            mesh.getFaceSmoothingGroups().toArray(sm0);
            if (sm0.length > 0 && sm0[0] > 0) {
                objWriter.write("s " + sm0[0] + newline);
            }

            for (int i = 0; i < faces1.size(); i++) {
                Integer[] f = faces1.get(i);
                objWriter.write("f " + (f[0] + 1) + "/" + (f[1] + 1) +
                        " " + (f[2] + 1) + "/" + (f[3] + 1) +
                        " " + (f[4] + 1) + "/" + (f[5] + 1) + newline);
                if (i < sm0.length - 1 && sm0[i] != sm0[i + 1]) {
                    objWriter.write("s " + (sm0[i + 1] > 0 ? sm0[i + 1] : "off") + newline);
                }
            }
            objWriter.write("# End Faces " + newline);
            objWriter.write(newline);
        }
    }

    private void exportMtlFile() throws IOException {
        File mtlFile = new File(fileName + ".mtl");
        try (BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(mtlFile))) {
            mtlWriter.write("# Material " + fileName + newline);
            mtlWriter.write("newmtl " + fileName + newline);
            mtlWriter.write("illum 4" + newline);
            mtlWriter.write("Kd " + diffuseColor + newline);
            mtlWriter.write("Ka 0.10 0.10 0.10" + newline);
            mtlWriter.write("Tf 1.00 1.00 1.00" + newline);
            if (diffuseMap != null) {
                mtlWriter.write("map_Kd " + diffuseMap + newline);
            }
            mtlWriter.write("Ni 1.00" + newline);
            mtlWriter.write("Ks 1.00 1.00 1.00" + newline);
            mtlWriter.write("Ns 32.00" + newline);
        }
    }
}
