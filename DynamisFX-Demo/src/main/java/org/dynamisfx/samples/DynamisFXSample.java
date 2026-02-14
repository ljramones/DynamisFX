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

package org.dynamisfx.samples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.dynamisfx.DynamisFXSampleBase;
import org.dynamisfx.samples.utilities.SkyBoxing;
import org.dynamisfx.scene.Skybox;

public abstract class DynamisFXSample extends DynamisFXSampleBase {

    
    protected final ThreadFactory threadFactory;
    public static ExecutorService serviceExecutor;
    
    protected final Image 
            top = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/top.png").toExternalForm()),
            bottom = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/bottom.png").toExternalForm()),
            left = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/left.png").toExternalForm()),
            right = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/right.png").toExternalForm()),
            front = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/front.png").toExternalForm()),
            back = new Image(SkyBoxing.class.getResource("/org/dynamisfx/images/skyboxes/back.png").toExternalForm());

    protected Skybox skyBox;
    
    protected double mousePosX;
    protected double mousePosY;
    protected double mouseOldX;
    protected double mouseOldY;
    protected double mouseDeltaX;
    protected double mouseDeltaY;
    
    protected Node controlPanel;
    
    public DynamisFXSample(){
        threadFactory = new SampleThreadFactory(getSampleName());
        if(serviceExecutor == null){
            serviceExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        }
    }
    
    @Override
    public String getProjectName() {
        return "DynamisFX-Demo";
    }

    @Override
    public String getProjectVersion() {
        return "1.0";
    }
    
    public abstract Node getSample();
    
    @Override
    public Node getPanel(Stage stage) {
        return getSample();
    }
    
    @Override
    public final String getSampleName() {
        String name = getClass().toGenericString();
        return name.substring(name.lastIndexOf(".") + 1, name.length());
    }
    
    // create a util class to retreive, or provide..
    @Override
    public String getSampleSourceURL() {
        return null;
    }

    @Override
    public String getJavaDocURL() {
        return null;
    }       
    
    @Override
    public String getControlStylesheetURL() {
        return null;
    }    
   
    protected abstract Node buildControlPanel();
    
    @Override
    public Node getControlPanel() {
        return controlPanel;       
    }

    public static ExecutorService getServiceExecutor() {
        return serviceExecutor;
    }
            
    static class SampleThreadFactory implements ThreadFactory{
        final String name;
        public SampleThreadFactory(String name) {
            this.name = name;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(name);
            t.setPriority(Thread.NORM_PRIORITY + 1);
            return t;
        }    
    }
    
    
    
}
