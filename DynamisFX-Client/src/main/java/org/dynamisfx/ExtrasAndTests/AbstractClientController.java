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

package org.dynamisfx.ExtrasAndTests;

import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public abstract class AbstractClientController extends AnchorPane implements Client, Initializable{
    
    protected Stage stage;
    
    protected abstract void loadClientProperties();
    protected abstract void saveClientProperties();
    
    protected abstract void initHeader();
    protected abstract void initLeftPanel();
    protected abstract void initCenterContentPane();
    protected abstract void initCenterContentHeaderOverlay();
    protected abstract void initRightPanel();
    protected abstract void initFooter();   
    
    protected abstract void changeContent();
    protected abstract String getFXMLPath();
            
    protected abstract void buildProjectTree(String searchText);

    public final Stage getStage() {
        return stage;
    }

    public final void setStage(Stage stage) {
        if(null == this.stage){
            this.stage = stage;
        }
    }
    
    
}
