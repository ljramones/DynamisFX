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
package org.dynamisfx.samples.shapes.compound;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.dynamisfx.particlefields.ParticleFieldConfiguration;
import org.dynamisfx.particlefields.ParticleFieldFactory;
import org.dynamisfx.particlefields.ParticleFieldRenderer;
import org.dynamisfx.samples.DynamisFXSample;
import org.dynamisfx.utils.CameraTransformer;

import java.util.Random;

/**
 * Demo sample for the particle field system.
 * Demonstrates both orbital (planetary rings, nebulae, etc.) and
 * linear (rain, fire, explosions, etc.) particle fields with interactive preset switching.
 */
public class ParticleFields extends DynamisFXSample {

    private ParticleFieldRenderer renderer;
    private AnimationTimer timer;
    private boolean paused = false;

    @Override
    public Node getSample() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        final double sceneWidth = 800;
        final double sceneHeight = 600;
        final CameraTransformer cameraTransform = new CameraTransformer();

        Group sceneRoot = new Group();
        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);

        // Setup camera
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-200);
        cameraTransform.ry.setAngle(-30.0);
        cameraTransform.rx.setAngle(-20.0);

        // Lights
        PointLight light = new PointLight(Color.WHITE);
        cameraTransform.getChildren().add(light);
        cameraTransform.getChildren().add(new AmbientLight(Color.color(0.3, 0.3, 0.3)));
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(camera.getTranslateZ());
        scene.setCamera(camera);

        sceneRoot.getChildren().add(cameraTransform);

        // Initialize particle field renderer with first preset
        renderer = new ParticleFieldRenderer();
        renderer.setRenderingMode(ParticleFieldRenderer.RenderingMode.SCATTER_MESH_AUTO);
        String[] presets = ParticleFieldFactory.getPresetNames();
        switchPreset(presets[0]);

        sceneRoot.getChildren().add(renderer.getGroup());

        // Animation timer for particle updates
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    renderer.update(1.0);
                    renderer.updateMeshPositions();
                }
            }
        };
        timer.start();

        // Keyboard controls
        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            if (event.isShiftDown()) {
                change = 50.0;
            }
            KeyCode keycode = event.getCode();
            if (keycode == KeyCode.W) {
                camera.setTranslateZ(camera.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                camera.setTranslateZ(camera.getTranslateZ() - change);
            }
            if (keycode == KeyCode.A) {
                camera.setTranslateX(camera.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                camera.setTranslateX(camera.getTranslateX() + change);
            }
            if (keycode == KeyCode.SPACE) {
                paused = !paused;
            }
            // Number keys for quick preset switching
            if (keycode.isDigitKey()) {
                int index = keycode.ordinal() - KeyCode.DIGIT0.ordinal();
                if (index >= 0 && index < presets.length) {
                    switchPreset(presets[index]);
                }
            }
        });

        // Mouse controls
        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);
            }
        });

        StackPane sp = new StackPane();
        sp.setPrefSize(sceneWidth, sceneHeight);
        sp.setMaxSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setMinSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setBackground(Background.EMPTY);
        sp.getChildren().add(scene);
        sp.setPickOnBounds(false);

        scene.widthProperty().bind(sp.widthProperty());
        scene.heightProperty().bind(sp.heightProperty());

        return sp;
    }

    private void switchPreset(String presetName) {
        ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(presetName);
        renderer.initialize(config, new Random(42));
    }

    @Override
    public Node getPanel(Stage stage) {
        return getSample();
    }

    @Override
    public String getJavaDocURL() {
        return null;
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
