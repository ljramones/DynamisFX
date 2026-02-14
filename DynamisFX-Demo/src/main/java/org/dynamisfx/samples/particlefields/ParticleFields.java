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
 */
package org.dynamisfx.samples.particlefields;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.dynamisfx.particlefields.ParticleFieldConfiguration;
import org.dynamisfx.particlefields.ParticleFieldFactory;

import java.util.Random;

/**
 * Demo sample for the particle field system.
 * Demonstrates all particle field presets with a ComboBox for interactive switching.
 */
public class ParticleFields extends ParticleFieldDemoBase {

    @Override
    protected ParticleFieldConfiguration createConfiguration() {
        return ParticleFieldFactory.getPreset(ParticleFieldFactory.getPresetNames()[0]);
    }

    @Override
    public String getSampleDescription() {
        return "Interactive particle field browser with all presets.";
    }

    private void switchPreset(String presetName) {
        ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(presetName);
        renderer.initialize(config, new Random(42));
    }

    @Override
    protected Node buildControlPanel() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));

        Label presetLabel = new Label("Preset:");
        ComboBox<String> presetCombo = new ComboBox<>();
        presetCombo.getItems().addAll(ParticleFieldFactory.getPresetNames());
        presetCombo.setValue(ParticleFieldFactory.getPresetNames()[0]);
        presetCombo.setOnAction(e -> switchPreset(presetCombo.getValue()));

        controls.getChildren().addAll(presetLabel, presetCombo);
        return controls;
    }
}
