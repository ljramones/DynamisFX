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

package org.dynamisfx.shapes.primitives;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.helper.LineSegment;
import org.dynamisfx.shapes.primitives.helper.MeshHelper;
import org.dynamisfx.shapes.primitives.helper.SVG3DHelper;
import org.dynamisfx.shapes.primitives.helper.TextureMode;
import org.dynamisfx.scene.paint.Palette;
import org.dynamisfx.scene.paint.Patterns;

/**
 *
 * @author José Pereda
 */
public class SVG3DMesh extends Group implements TextureMode {

    private final static String DEFAULT_CONTENT = "M40,60 C42,48 44,30 25,32";
    private final static double DEFAULT_HEIGHT = 50d;
    private final static int DEFAULT_LEVEL = 1;
    private final static boolean DEFAULT_JOIN_SEGMENTS = true;
    
    private ObservableList<TexturedMesh> meshes=null;

    public SVG3DMesh() {
        this(DEFAULT_CONTENT,DEFAULT_HEIGHT,DEFAULT_LEVEL,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(SVGPath svg){
        this(svg.getContent(),DEFAULT_HEIGHT,DEFAULT_LEVEL,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(String content){
        this(content,DEFAULT_HEIGHT,DEFAULT_LEVEL,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(SVGPath svg, double height){
        this(svg.getContent(),height,DEFAULT_LEVEL,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(String content, double height){
        this(content,height,DEFAULT_LEVEL,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(SVGPath svg, double height, int level){
        this(svg.getContent(),height,level,DEFAULT_JOIN_SEGMENTS);
    }
    
    public SVG3DMesh(String content, double height, int level){
        this(content,height,level,DEFAULT_JOIN_SEGMENTS);
    }
    public SVG3DMesh(SVGPath svg, double height, int level, boolean joinSegments){
        this(svg.getContent(),height,level,joinSegments);
    }
    
    public SVG3DMesh(String content, double height, int level, boolean joinSegments){
        setContent(content);
        setJoinSegments(joinSegments);
        setHeight(height);
        setLevel(level);
        
        updateMesh();
    }
    
    private final StringProperty content = new SimpleStringProperty(DEFAULT_CONTENT){
        @Override
        protected void invalidated() {
            if(meshes!=null){
                updateMesh();
            }
        }
    };

    public String getContent() {
        return content.get();
    }

    public final void setContent(String value) {
        content.set(value);
    }

    public StringProperty contentProperty() {
        return content;
    }
    
    private final DoubleProperty height = new SimpleDoubleProperty(DEFAULT_HEIGHT){
        @Override
        protected void invalidated() {
            if(meshes!=null){
                updateMesh();
            }
        }
    };

    public double getHeight() {
        return height.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
    }
    
    private final IntegerProperty level = new SimpleIntegerProperty(DEFAULT_LEVEL){

        @Override
        protected void invalidated() {
            if(meshes!=null){
                updateMesh();
            }
        }

    };
    
    public final int getLevel() {
        return level.get();
    }

    public final void setLevel(int value) {
        level.set(value);
    }

    public final IntegerProperty levelProperty() {
        return level;
    }
    
    private final BooleanProperty joinSegments = new SimpleBooleanProperty(DEFAULT_JOIN_SEGMENTS){
        @Override
        protected void invalidated() {
            if(meshes!=null){
                updateMesh();
            }
        }
    };

    public boolean isJoinSegments() {
        return joinSegments.get();
    }

    public final void setJoinSegments(boolean value) {
        joinSegments.set(value);
    }

    public BooleanProperty joinSegmentsProperty() {
        return joinSegments;
    }
    
    protected final void updateMesh() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(content.get());
        SVG3DHelper helper = new SVG3DHelper(svgPath);
        meshes=FXCollections.<TexturedMesh>observableArrayList();
        
        AtomicInteger indSegments = new AtomicInteger();
        helper.getLineSegment().stream().forEach(poly->{
            final List<Point3D> points=poly.getPoints();
            List<List<Point3D>> holes=null;
            if(poly.getHoles().size()>0){
                holes=poly.getHoles().stream().map(LineSegment::getPoints).collect(Collectors.toList());
            }
            List<Point3D> invert = IntStream.range(0,points.size())
                    .mapToObj(i->points.get(points.size()-1-i))
                    .distinct().collect(Collectors.toList());
            TriangulatedMesh polyMesh = new TriangulatedMesh(invert,holes,level.get(),height.get(),0d);
            if(indSegments.get()>0 && joinSegments.get()){
                /*
                Combine new polyMesh with previous polyMesh into one single polyMesh
                */
                MeshHelper mh = new MeshHelper((TriangleMesh)meshes.get(meshes.size()-1).getMesh());
                MeshHelper mh1 = new MeshHelper((TriangleMesh)polyMesh.getMesh());
                mh1.addMesh(mh);
                polyMesh.updateMesh(mh1);
                meshes.set(meshes.size()-1,polyMesh);
            } else {
                meshes.add(polyMesh);
            }
            polyMesh.getTransforms().addAll(new Scale(10,10,10));
            polyMesh.setCullFace(CullFace.BACK);
            polyMesh.setDrawMode(DrawMode.FILL);
            polyMesh.setDepthTest(DepthTest.ENABLE);
            polyMesh.setId(poly.getLetter());
            indSegments.getAndIncrement();
        });
        
        getChildren().setAll(meshes);
        updateTransforms();
        
    }
    
    @Override
    public void setTextureModeNone() {
        meshes.stream().forEach(m->m.setTextureModeNone());
    }

    @Override
    public void setTextureModeNone(Color color) {
        meshes.stream().forEach(m->m.setTextureModeNone(color));
    }
    
    public void setTextureModeNone(List<Color> colors) {
        AtomicInteger cont = new AtomicInteger();
        meshes.stream().forEach(m->m.setTextureModeNone(colors.get(cont.getAndIncrement()%colors.size())));
    }

    @Override
    public void setTextureModeNone(Color color, String image) {
        meshes.stream().forEach(m->m.setTextureModeNone(color,image));
    }

    @Override
    public void setTextureModeImage(String image) {
        meshes.stream().forEach(m->m.setTextureModeImage(image));
    }

    @Override
    public void setTextureModePattern(Patterns.CarbonPatterns pattern, double scale) {
        meshes.stream().forEach(m->m.setTextureModePattern(pattern, scale));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(colors, dens));
    }

    @Override
    public void setTextureModeVertices3D(Palette.ColorPalette palette, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(palette, dens));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens, double min, double max) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(colors, dens, min, max));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(colors, function));
    }

    @Override
    public void setTextureModeVertices1D(Palette.ColorPalette palette, Function<Number, Number> function) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(palette, function));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function, double min, double max) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(colors, function, min, max));
    }

    @Override
    public void setTextureModeFaces(int colors) {
        meshes.stream().forEach(m->m.setTextureModeFaces(colors));
    }
    
    @Override
    public void setTextureModeFaces(Palette.ColorPalette palette) {
        meshes.stream().forEach(m->m.setTextureModeFaces(palette));
    }

    @Override
    public void setTextureOpacity(double value) {
        meshes.stream().forEach(m->m.setTextureOpacity(value));
    }
    
    @Override
    public void updateF(List<Number> values) {
         meshes.stream().forEach(m->m.updateF(values));
    }
    
    public void setDrawMode(DrawMode mode) {
        meshes.stream().forEach(m->m.setDrawMode(mode));
    }
    
    public void setCullFace(CullFace face) {
        meshes.stream().forEach(m->m.setCullFace(face));
    }
    
    private void updateTransforms() {
        meshes.stream().forEach(m->m.updateTransforms());
    }
    
    public TexturedMesh getMeshFromLetter(String letter){
        return meshes.stream().filter(p->p.getId().equals(letter)).findFirst().orElse(meshes.get(0));
    }
    
    public TexturedMesh getMeshFromLetter(String letter, int order){
        return meshes.stream().filter(p->p.getId().equals(letter)).skip(order-1).findFirst().orElse(meshes.get(0));
    }
    
}
