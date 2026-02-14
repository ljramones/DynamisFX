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

package org.dynamisfx.controls;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 * @param <P>
 */
public abstract class ControlBase<P extends Property> extends StackPane{
    protected P controlledProperty;
    public ControlBase(final String fxml, final P prop) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setRoot(ControlBase.this);
            loader.setController(ControlBase.this);
            loader.load();
        } catch (IOException ex) {
            
            Logger.getLogger(CheckBoxControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.controlledProperty = prop;
//        this.setPrefSize(USE_PREF_SIZE, StackPane.BASELINE_OFFSET_SAME_AS_HEIGHT );
        this.setPrefWidth(USE_PREF_SIZE);
        //this.getStyleClass().add("fxyz3d-control");
        
    }

    private ControlBase() {
        throw new UnsupportedOperationException("Cannot assign");
    }

    private ControlBase(Node... children) {
        throw new UnsupportedOperationException("Cannot assign");
    }
    
}
