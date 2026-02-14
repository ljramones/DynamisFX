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

import java.util.Collection;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ImagePreviewControl extends ControlBase<Property<TextureImage>> {

    @FXML
    private ImageView preview;
    @FXML
    private ComboBox<TextureImage> imageSelector;
    

    public ImagePreviewControl(final Property<TextureImage> img, String name, final Collection<TextureImage> items) {
        super("/org/dynamisfx/controls/ImageMapPreview.fxml", img);
       
        imageSelector.getItems().addAll(items);
        imageSelector.getSelectionModel().selectedItemProperty().addListener((obs,t,t1)->{
            preview.setImage(t1.getImage());
        });
        imageSelector.getSelectionModel().selectFirst();
        imageSelector.setCellFactory(new Callback<ListView<TextureImage>,ListCell<TextureImage>>() {
            
            @Override
            public ListCell<TextureImage> call(ListView<TextureImage> param) {
                return new ListCell<TextureImage>(){
                    {
                        this.setFocusTraversable(false);
                    }
                    @Override
                    public boolean isResizable() {
                        return false; //To change body of generated methods, choose Tools | Templates.
                    }
                    
                    @Override
                    public void updateSelected(boolean selected) {
                        //do nothing...
                    }
                    
                    @Override
                    protected void updateItem(TextureImage item, boolean empty) {
                        if(item != null && !empty){
                            super.updateItem(item, empty);  
                            final ImageView view = new ImageView(item.getImage());
                            view.setFitHeight(75);
                            view.setPreserveRatio(true);
                            view.setSmooth(true);
                            super.setGraphic(view);                            
                            super.setText(item.getName());
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        
//        preview.imageProperty().bind(imageSelector.getSelectionModel().getSelectedItem().imageProperty());
        if(controlledProperty!=null){
            controlledProperty.bind(imageSelector.valueProperty());
        }
        preview.setOnMouseClicked(e->imageSelector.show());
    }

    public final ComboBox<TextureImage> getImageSelector() {
        return imageSelector;
    }

    
}
