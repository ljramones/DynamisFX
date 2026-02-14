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

package org.dynamisfx.samples.shapes.compound;

import java.util.ArrayList;
import static javafx.application.Application.launch;
import javafx.scene.Node;
import org.dynamisfx.samples.shapes.ShapeBaseSample;
import org.dynamisfx.shapes.composites.ScatterPlotMesh;

/**
 *
 * @author Sean
 */
public class ScatterPlotMeshes extends ShapeBaseSample<ScatterPlotMesh> {

    

    public static void main(String[] args){launch(args);}
    
    @Override
    protected void createMesh() {
        model = new ScatterPlotMesh(250, 50, true);
        //model.getTransforms().add(rotateY);
        ArrayList<Double> dataX = new ArrayList<>();
        ArrayList<Double> dataY = new ArrayList<>();
        ArrayList<Double> dataZ = new ArrayList<>();
        for (int i = -250; i < 250; i++) {
            dataX.add((double)i);
            dataY.add(Math.sin(i * 2 * Math.PI) * (i != 0 ? i:1) / 50 + i);
            dataZ.add(Math.cos(i) * 50 + i);
        }

        model.setXYZData(dataX, dataY, dataZ);
        model.getTransforms().add(rotateY);
    }
    
    @Override
    protected void addMeshAndListeners() {
    }
    
    @Override
    protected Node buildControlPanel() {
        return null;
    }
}
