/**
 * DynamisFXProject.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.dynamisfx.samples;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.dynamisfx.DynamisFXSamplerProject;
import org.dynamisfx.model.WelcomePage;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class DynamisFXProject implements DynamisFXSamplerProject {
    private static final String LEGACY_CONTROLS_THEME_PROPERTY = "dynamisfx.client.legacyControlsTheme";
    
         
    @Override
    public final String getProjectName() {
        return "DynamisFX-Samples";
    }

    @Override
    public final String getSampleBasePackage() {
        return "org.dynamisfx.samples";
    }

    @Override
    public WelcomePage getWelcomePage() {
        VBox vBox = new VBox();
        Parent logo = null;
        try {
            logo = FXMLLoader.load(DynamisFXProject.class.getResource("Logo.fxml"));
        } catch (IOException ex) {
            Logger.getLogger(DynamisFXProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        StackPane pane = new StackPane();
        pane.setPrefHeight(207);
        if(logo != null){
            pane.getChildren().add(logo);
        }
        StackPane lCon = new StackPane();
        Label label = new Label();
        label.setWrapText(true);
        lCon.getStyleClass().add("fxyz3d-welcome-label");
        StringBuilder desc = new StringBuilder();
        desc.append("F(X)yz is an open source project for JavaFX that aims ");
        desc.append("to provide really high quality UI controls and other tools to ");
        desc.append("complement the core JavaFX 3D distribution.");
        desc.append("\n\n");
        desc.append("Explore the available tools and shapes by clicking on the options to the left.");
        label.setText(desc.toString());
        label.setStyle("-fx-font-size: 1.5em; -fx-padding: 20 0 0 5;");
        label.setPrefWidth(625);
        label.setAlignment(Pos.CENTER);
        lCon.setPadding(new Insets(10));
        lCon.getChildren().add(label);
        lCon.setMaxWidth(label.getPrefWidth());
        vBox.getChildren().addAll(pane, lCon);
        vBox.setAlignment(Pos.CENTER);
        WelcomePage wPage = new WelcomePage("Welcome to DynamisFX!", vBox);
        
        pane.sceneProperty().addListener(l -> {
            if (pane.getScene() != null && Boolean.getBoolean(LEGACY_CONTROLS_THEME_PROPERTY)) {
                pane.getScene().getStylesheets().add(
                        DynamisFXProject.class
                                .getResource("/org/dynamisfx/controls/smokeBlackGlassControls.css")
                                .toExternalForm());
            }
        });
        
        return wPage;
    }
    
}
