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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 *
 * @author Jose Pereda
 */
public class TextureImage {
    
    private final ObjectProperty<Image> image;
    private final StringProperty name;
    private final StringProperty source;

    TextureImage(String imageSrc, String name) {
        this.image=new SimpleObjectProperty<>(new Image(imageSrc));
        this.name=new SimpleStringProperty(name);
        this.source = new SimpleStringProperty(imageSrc);
    }

    public Image getImage() {
        return image.get();
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSource() {
        return source.get();
    }
    
    public void setSource(String source) {
        this.source.set(source);
    }
    
    public ObjectProperty<Image> imageProperty(){
        return image;
    }
    
    public StringProperty nameProperty(){
        return name;
    }

    @Override
    public String toString() {
        return name.get(); 
    }
    
}
