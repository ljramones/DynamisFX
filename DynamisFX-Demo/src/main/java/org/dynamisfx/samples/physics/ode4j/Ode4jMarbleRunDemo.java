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

package org.dynamisfx.samples.physics.ode4j;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Marble run with ramps, funnels, and obstacles.
 * Drop marbles and watch them roll through the course.
 *
 * Controls:
 * M - Drop marble
 * SPACE - Pause/Resume
 */
public class Ode4jMarbleRunDemo extends Ode4jDemoBase {

    private static final double MARBLE_RADIUS = 12;
    private int marbleCount = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        marbleCount = 0;

        // Ground (catch basin)
        createFloor(-300, 600, 500, Color.DARKSLATEGRAY);

        // ====== BUILD THE MARBLE RUN ======

        // Starting platform
        createStaticBox(-200, 200, 0, 80, 10, 80, Color.FORESTGREEN);

        // Ramp 1 - steep initial drop (angled box approximation)
        createRamp(-150, 150, 0, 150, 15, 60, -30, Color.SADDLEBROWN);

        // Flat section 1
        createStaticBox(-20, 80, 0, 100, 10, 60, Color.SIENNA);

        // Ramp 2 - curves right
        createRamp(60, 40, 30, 120, 15, 60, -25, Color.SADDLEBROWN);

        // Bumper obstacles
        createStaticBox(100, 30, 60, 20, 30, 20, Color.CRIMSON);
        createStaticBox(140, 20, 10, 20, 30, 20, Color.CRIMSON);

        // Flat section 2
        createStaticBox(150, -10, 0, 80, 10, 80, Color.SIENNA);

        // Ramp 3 - back to center
        createRamp(100, -50, -30, 120, 15, 60, -20, Color.SADDLEBROWN);

        // Spiral section (approximated with multiple angled ramps)
        createStaticBox(0, -80, -60, 80, 10, 60, Color.BURLYWOOD);
        createRamp(-50, -110, -30, 100, 15, 50, -15, Color.SADDLEBROWN);
        createStaticBox(-100, -130, 0, 80, 10, 60, Color.BURLYWOOD);
        createRamp(-50, -160, 30, 100, 15, 50, -15, Color.SADDLEBROWN);

        // Final ramp to ground
        createRamp(30, -200, 0, 150, 15, 60, -25, Color.SADDLEBROWN);

        // Side rails (prevent marbles from falling off)
        createRails();

        // Drop some initial marbles
        dropMarble();
        dropMarble();
    }

    private void createRamp(double x, double y, double z, double length, double height, double width, double angleDeg, Color color) {
        // Create a rotated box to act as a ramp
        PhysicsQuaternion rotation = quaternionFromAxisAngle(0, 0, 1, angleDeg);
        createStaticBoxRotated(x, y, z, length, height, width, color, rotation);
    }

    private PhysicsBodyHandle createStaticBoxRotated(double x, double y, double z,
                                                      double w, double h, double d,
                                                      Color color, PhysicsQuaternion orientation) {
        javafx.scene.shape.Box box = new javafx.scene.shape.Box(w, h, d);
        box.setMaterial(new javafx.scene.paint.PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new org.dynamisfx.physics.model.PhysicsBodyDefinition(
                org.dynamisfx.physics.model.PhysicsBodyType.STATIC,
                0.0,
                new org.dynamisfx.physics.model.BoxShape(w, h, d),
                new org.dynamisfx.physics.model.PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        orientation,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        org.dynamisfx.physics.model.ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
        return handle;
    }

    private void createRails() {
        // Side rails along the main path
        // Starting area
        createStaticBox(-200, 210, -45, 80, 20, 10, Color.DIMGRAY);
        createStaticBox(-200, 210, 45, 80, 20, 10, Color.DIMGRAY);

        // Along ramps (simplified)
        createStaticBox(-100, 170, -35, 10, 20, 100, Color.DIMGRAY);
        createStaticBox(-100, 170, 35, 10, 20, 100, Color.DIMGRAY);
    }

    private void dropMarble() {
        double x = -200 + (Math.random() - 0.5) * 40;
        double z = (Math.random() - 0.5) * 30;
        Color color = Color.hsb(marbleCount * 30 % 360, 0.8, 0.9);

        createSphere(x, 250, z, MARBLE_RADIUS, 1.0, color);
        marbleCount++;
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case M -> dropMarble();
            default -> { }
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button dropBtn = new Button("Drop Marble (M)");
        dropBtn.setOnAction(e -> dropMarble());

        Button drop5Btn = new Button("Drop 5 Marbles");
        drop5Btn.setOnAction(e -> {
            for (int i = 0; i < 5; i++) {
                dropMarble();
            }
        });

        panel.getChildren().addAll(
                new Label("Marble Run Controls:"),
                new Label(""),
                dropBtn,
                drop5Btn,
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Watch the marbles"),
                new Label("roll through the"),
                new Label("course!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Marble run with ramps and obstacles. Press M to drop marbles!";
    }
}
