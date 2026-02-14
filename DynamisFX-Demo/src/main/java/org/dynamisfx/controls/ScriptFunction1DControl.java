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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ScriptFunction1DControl extends ControlBase<Property<Function<Number,Number>>>{

    private ObjectProperty<Function<Number,Number>> function = new SimpleObjectProperty<>();
    
    private BooleanProperty change=new SimpleBooleanProperty();
    private BooleanProperty error=new SimpleBooleanProperty();
    
    public ScriptFunction1DControl(Property<Function<Number,Number>> prop, final Collection<String> items, boolean subControl) {
        super("/org/dynamisfx/controls/ScriptFunction1DControl.fxml", prop);

        if (engine != null) {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            if (bindings != null) {
                bindings.put("polyglot.js.allowAllAccess", true);
            }
        }

        Double x=1d;
        res1.setText("x: {"+x+"}");
                    
        selection.getItems().setAll(items);
        selection.getItems().add("Enter a valid expression");
        if (subControl) {
            subControlCache = FXCollections.observableHashMap();
            this.usesSubControls.set(subControl);
        }
        selection.getEditor().setEditable(false);
        selection.getStyleClass().add("noEditable-textField");
        selection.getSelectionModel().selectedIndexProperty().addListener((obs,n,n1)->{
            if(n1!=null){
                selection.getStyleClass().remove("noEditable-textField");
                selection.getEditor().setEditable(n1.intValue()==items.size());
                if(selection.getEditor().isEditable()){
                    selection.getEditor().selectAll();
                    Platform.runLater(()->{
                        selection.getEditor().setText("");
                        selection.getEditor().promptTextProperty().unbind();
                        selection.getEditor().setPromptText("Enter a valid expression");
                    });
                } else {
                    selection.getStyleClass().add("noEditable-textField");
                    change.set(true);
                }
                controlledProperty.unbind();
                controlledProperty.bindBidirectional(function);
            }
        });
        
        selection.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        if (event.getCode() == KeyCode.ENTER && !selection.getEditor().getText().isEmpty()){
                change.set(true);
            }
        });
        selection.getEditor().focusedProperty().addListener((obs,b,b1)->{
            if(!b1 && !selection.getEditor().getText().isEmpty()){
                change.set(true);
            }
        });
        change.addListener((obs,b,b1)->{
            if (engine == null) {
                ScriptEngineSupport.logNoEngineOnce();
                return;
            }
            if(b1){
                change.set(false);
                String text=selection.getValue();
                if(!selection.getEditor().getText().isEmpty()){
                    text=selection.getEditor().getText();
                }
                @SuppressWarnings("unchecked")
                Function<Number,Number> f;
                try {
                    f = (Function<Number,Number>)engine.eval(
                            String.format("new java.util.function.Function(%s)", "function(x) "+text));
                    // check if f is a valid function
                    try{
                        res2.setText("val: "+String.format("%.3f", f.apply(x)));
                        error.set(false);
                    } catch(Exception e){
                        res2.setText("val: error");
                        error.set(true);
                    }
                    function.set(f);
                } catch (RuntimeException | ScriptException ex) {
                    System.err.println("Script Error "+ex);
                }
            }
        });

        if (engine == null) {
            selection.setDisable(true);
            res2.setText("val: script engine unavailable");
        }
        
        selection.getSelectionModel().select(0);
        
        
    }
    
    public Property<Function<Number,Number>> functionProperty() { return function; }
            
    @FXML
    private Label res1;
    @FXML
    private Label res2;
    @FXML
    private ComboBox<String> selection;
    private final ScriptEngine engine = ScriptEngineSupport.sharedEngine();
    
    @FXML
    protected VBox subControls;

    protected ObservableMap<String, List<ControlBase<Property<String>>>> subControlCache;

    public void addSubControl(final ControlBase ... controls) {
        if (useSubControls()) {
                subControlCache.putIfAbsent(selection.getValue(), Arrays.asList(controls));       
            //subControls.getChildren().add(subControlCache.get(control.controlledProperty));
        }
    }

    protected ObservableMap<String, List<ControlBase<Property<String>>>> getSubControlCache() {
        if (useSubControls()) {
            return subControlCache;
        } else {
            return null;
        }
    }

    private final BooleanProperty usesSubControls = new SimpleBooleanProperty(this, "usesSubControls", false) {

        @Override
        protected void invalidated() {
            super.invalidated();

        }

    };

    protected boolean useSubControls() {
        return usesSubControls.get();
    }

    public BooleanProperty usingSubControlsProperty() {
        return usesSubControls;
    }

}
