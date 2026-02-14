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
 */

package org.dynamisfx.samples.utilities;

import javafx.scene.Node;
import org.dynamisfx.samples.shapes.ShapeBaseSample;
import org.dynamisfx.scene.Skybox;

/**
 *
 * @author Dub
 */
public class SkyBoxing extends ShapeBaseSample {

    public static void main(String[] args){SkyBoxing.launch(args);}

    @Override
    protected void createMesh() {
        // Load Skybox AFTER camera is initialized
        double size = 100000D;
        model = new Skybox(
                top,
                bottom,
                left,
                right,
                front,
                back,
                size,
                camera
        );
       
    }

    @Override
    protected void addMeshAndListeners() {
    }
    
    @Override
    protected Node buildControlPanel() {
        return null;
    }

}
