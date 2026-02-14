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
import java.util.function.Function;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.easybind.EasyBind;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.scene.paint.Patterns.CarbonPatterns;
import org.dynamisfx.shapes.primitives.helper.TriangleMeshHelper.TextureType;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class TextureTypeControl extends ComboBoxControl<TextureType>{

    private static final TextureImage 
            //animatedWater = new Image(TextureTypeControl.class.getResource("/org/dynamisfx/images/anim.gif").toExternalForm()),
            texture01 = new TextureImage(TextureTypeControl.class.getResource("/org/dynamisfx/images/textures/texture002.jpg").toExternalForm(),"Sand"),
            texture02 = new TextureImage(TextureTypeControl.class.getResource("/org/dynamisfx/images/textures/diamondPlate.jpg").toExternalForm(),"Diamond Plate"),
            texture03 = new TextureImage(TextureTypeControl.class.getResource("/org/dynamisfx/images/textures/tiled.jpg").toExternalForm(),"Tiled"),
            texture04 = new TextureImage(TextureTypeControl.class.getResource("/org/dynamisfx/images/textures/water.jpg").toExternalForm(),"Water"),
            texture05 = new TextureImage(TextureTypeControl.class.getResource("/org/dynamisfx/images/textures/metal-scale-tile.jpg").toExternalForm(),"Metal Tile");
    protected final ObservableList<TextureImage> textures;
    
    protected ColorSliderControl colorSlider;
    
    protected ImagePreviewControl diffMapControl;  
    
    protected NumberSliderControl patternScaler;
    protected ComboBoxControl patternChooser;
    
    protected ScriptFunction3DControl densFunct;
    protected ScriptFunction1DControl funcFunct;
    
    protected ColorSliderControl specColor;
    protected NumberSliderControl specSlider;
    
    protected CheckBoxControl bumpMap;
    protected CheckBoxControl invertBumpMap;
    protected NumberSliderControl bumpScale;
    protected NumberSliderControl bumpFine;    
    
    private final BooleanBinding 
            useColorSlider,
            useImage,             
            useDensScriptor, 
            useFuncScriptor,
            usePatternChooser,
            usePatternScaler,
            useSpecColor,
            useSpecPower,
            useBumpMapping;
    
    public TextureTypeControl(String lbl, 
            Property<TextureType> type, 
            Collection<TextureType> items,
            final Property<Number> colors,
            final Property<TextureImage> diffMap,
            final Property<Boolean> bmpMap,
            final Property<Number> bmpScale,
            final Property<Number> bmpFineScale,
            final Property<Boolean> invBmp,
            final Property<CarbonPatterns> patt,
            final Property<Number> pScale,
            final Property<Number> spColor,
            final Property<Number> specP,
            final Property<Function<Point3D,Number>> densFunc,
            final Property<Function<Number,Number>> funcFunc
    ) {
        super(lbl, type, items, true);
        this.textures = FXCollections.observableArrayList(texture01,texture02,texture03,texture04, texture05);
        
        buildSubControls(
                colors,
                diffMap,
                bmpMap,bmpScale,bmpFineScale,invBmp,
                pScale, patt, 
                spColor, specP, 
                densFunc, funcFunc
        );
        
        
        this.useColorSlider = selection.valueProperty().isEqualTo(TextureType.NONE);
        
        this.useImage = selection.valueProperty().isEqualTo(TextureType.IMAGE);
        
        this.usePatternChooser = selection.valueProperty().isEqualTo(TextureType.PATTERN);
        this.usePatternScaler = selection.valueProperty().isEqualTo(TextureType.PATTERN);   
        
        this.useBumpMapping = selection.valueProperty().isEqualTo(TextureType.IMAGE)
                .or(selection.valueProperty().isEqualTo(TextureType.PATTERN));
        
        this.useDensScriptor = selection.valueProperty().isEqualTo(TextureType.COLORED_VERTICES_3D);
        this.useFuncScriptor = selection.valueProperty().isEqualTo(TextureType.COLORED_VERTICES_1D);
        
        this.useSpecColor = selection.valueProperty().isNotNull();
        this.useSpecPower = selection.valueProperty().isNotNull();
        
        EasyBind.includeWhen(subControls.getChildren(), colorSlider, useColorSlider);
        
        EasyBind.includeWhen(subControls.getChildren(), diffMapControl, useImage);
        
        EasyBind.includeWhen(subControls.getChildren(), patternChooser, usePatternChooser);
        EasyBind.includeWhen(subControls.getChildren(), patternScaler, usePatternScaler);
        
        EasyBind.includeWhen(subControls.getChildren(), densFunct, useDensScriptor);
        EasyBind.includeWhen(subControls.getChildren(), funcFunct, useFuncScriptor);
        
        EasyBind.includeWhen(subControls.getChildren(), specColor, useSpecColor);
        EasyBind.includeWhen(subControls.getChildren(), specSlider, useSpecPower);
        
        EasyBind.includeWhen(subControls.getChildren(), bumpMap, useBumpMapping);//.or(diffMapControl.getImageSelector().valueProperty().isNotEqualTo(animatedWater)));
        EasyBind.includeWhen(subControls.getChildren(), invertBumpMap, useBumpMapping);//.or(diffMapControl.getImageSelector().valueProperty().isNotEqualTo(animatedWater)));
        EasyBind.includeWhen(subControls.getChildren(), bumpScale, useBumpMapping);//.or(diffMapControl.getImageSelector().valueProperty().isNotEqualTo(animatedWater)));
        EasyBind.includeWhen(subControls.getChildren(), bumpFine, useBumpMapping);//.or(diffMapControl.getImageSelector().valueProperty().isNotEqualTo(animatedWater)));
    }

    @Override
    protected boolean useSubControls() {
        return true;
    }

    private void buildSubControls(
            final Property<Number> colors,
            final Property<TextureImage> img, 
            final Property<Boolean> bmpMap,
            final Property<Number> bmpScale,
            final Property<Number> bmpFineScale,
            final Property<Boolean> invBmp, 
            final Property<Number> pScale, 
            final Property<CarbonPatterns> patt,
            final Property<Number> spColor,
            final Property<Number> specP,
            final Property<Function<Point3D,Number>> densFunc,
            final Property<Function<Number,Number>> funcFunc
    ) {
        /*
            Lay out controls in the order you want them to be seen
        */
        diffMapControl = ControlFactory.buildImageViewToggle(img, "Image", textures);
        
        patternChooser = ControlFactory.buildPatternChooser(patt);
        patternScaler = ControlFactory.buildNumberSlider(pScale, 1, 100);        
        // only if image or pattern        
        bumpMap = ControlFactory.buildCheckBoxControl(bmpMap);
        
        invertBumpMap = ControlFactory.buildCheckBoxControl(invBmp);
        bumpScale = ControlFactory.buildNumberSlider(bmpScale, 0, 100);
        bumpFine = ControlFactory.buildNumberSlider(bmpFineScale, 0.01, 100);
        // only if texture none
        colorSlider = ControlFactory.buildColorSliderControl(colors, 0l, 1530l);
        //
               
        densFunct = ControlFactory.buildScriptFunction3DControl(densFunc);
        funcFunct = ControlFactory.buildScriptFunction1DControl(funcFunc);
        
        specColor = new ColorSliderControl(spColor, 0, 1530);
        specColor.setPrefSize(USE_COMPUTED_SIZE, USE_PREF_SIZE);
        specSlider = ControlFactory.buildNumberSlider(specP, 32, 10000);
    }

    public void  resetBumpMap() {
        bumpMap.setSelected(false);
    }
    
    
}
