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
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates motor-driven hinge and slider joints.
 * Shows a rotating arm (hinge motor) and a sliding platform (slider motor).
 */
public class Ode4jMotorDemo extends Ode4jDemoBase {

    private PhysicsConstraintHandle hingeMotor;
    private PhysicsConstraintHandle sliderMotor;
    private double hingeSpeed = 2.0;  // rad/s
    private double sliderSpeed = 1.0; // m/s
    private boolean hingeRunning = true;
    private boolean sliderRunning = true;

    private Slider hingeSpeedSlider;
    private Slider sliderSpeedSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Floor
        createFloor(-250, 600, 400, Color.DARKSLATEGRAY);

        // ====== HINGE MOTOR DEMO (left side) ======
        // Static base
        PhysicsBodyHandle hingeBase = createStaticBox(-150, -150, 0, 40, 100, 40, Color.DIMGRAY);

        // Rotating arm
        PhysicsBodyHandle arm = createBox(-80, -100, 0, 120, 20, 30, 2.0, Color.STEELBLUE);

        // Hinge joint with motor
        hingeMotor = createHingeJoint(
                hingeBase, arm,
                new PhysicsVector3(-130, -100, 0),
                new PhysicsVector3(0, 0, 1),  // Z-axis rotation
                null, null);
        enableMotor(hingeMotor, hingeSpeed, 100.0);

        // Weight on the arm end
        createSphere(-30, -80, 0, 25, 1.5, Color.CORAL);

        // ====== SLIDER MOTOR DEMO (right side) ======
        // Static base for slider
        PhysicsBodyHandle sliderBase = createStaticBox(150, -150, 0, 40, 100, 40, Color.DIMGRAY);

        // Sliding platform
        PhysicsBodyHandle platform = createBox(150, -50, 0, 80, 20, 80, 3.0, Color.FORESTGREEN);

        // Slider joint with motor
        sliderMotor = createSliderJoint(
                sliderBase, platform,
                new PhysicsVector3(0, 1, 0),  // Y-axis sliding
                -100.0, 100.0);
        enableMotor(sliderMotor, sliderSpeed, 200.0);

        // Box riding on the platform
        createBox(150, -30, 0, 30, 30, 30, 0.5, Color.GOLD);
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        panel.getChildren().add(new Label("Motor Controls"));

        // Hinge motor controls
        GridPane hingeGrid = new GridPane();
        hingeGrid.setHgap(8);
        hingeGrid.setVgap(4);

        hingeSpeedSlider = new Slider(-10, 10, hingeSpeed);
        hingeSpeedSlider.setPrefWidth(120);
        hingeSpeedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            hingeSpeed = newV.doubleValue();
            if (hingeRunning) {
                setMotorVelocity(hingeMotor, hingeSpeed);
            }
        });
        hingeGrid.add(new Label("Hinge Speed:"), 0, 0);
        hingeGrid.add(hingeSpeedSlider, 1, 0);

        Button hingeToggle = new Button("Stop Hinge");
        hingeToggle.setOnAction(e -> {
            hingeRunning = !hingeRunning;
            if (hingeRunning) {
                enableMotor(hingeMotor, hingeSpeed, 100.0);
                hingeToggle.setText("Stop Hinge");
            } else {
                disableMotor(hingeMotor);
                hingeToggle.setText("Start Hinge");
            }
        });
        hingeGrid.add(hingeToggle, 0, 1, 2, 1);

        panel.getChildren().add(hingeGrid);
        panel.getChildren().add(new Label(""));

        // Slider motor controls
        GridPane sliderGrid = new GridPane();
        sliderGrid.setHgap(8);
        sliderGrid.setVgap(4);

        sliderSpeedSlider = new Slider(-5, 5, sliderSpeed);
        sliderSpeedSlider.setPrefWidth(120);
        sliderSpeedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            sliderSpeed = newV.doubleValue();
            if (sliderRunning) {
                setMotorVelocity(sliderMotor, sliderSpeed);
            }
        });
        sliderGrid.add(new Label("Slider Speed:"), 0, 0);
        sliderGrid.add(sliderSpeedSlider, 1, 0);

        Button sliderToggle = new Button("Stop Slider");
        sliderToggle.setOnAction(e -> {
            sliderRunning = !sliderRunning;
            if (sliderRunning) {
                enableMotor(sliderMotor, sliderSpeed, 200.0);
                sliderToggle.setText("Stop Slider");
            } else {
                disableMotor(sliderMotor);
                sliderToggle.setText("Start Slider");
            }
        });
        sliderGrid.add(sliderToggle, 0, 1, 2, 1);

        panel.getChildren().add(sliderGrid);

        Button reverseAll = new Button("Reverse All");
        reverseAll.setOnAction(e -> {
            hingeSpeed = -hingeSpeed;
            sliderSpeed = -sliderSpeed;
            hingeSpeedSlider.setValue(hingeSpeed);
            sliderSpeedSlider.setValue(sliderSpeed);
            if (hingeRunning) {
                setMotorVelocity(hingeMotor, hingeSpeed);
            }
            if (sliderRunning) {
                setMotorVelocity(sliderMotor, sliderSpeed);
            }
        });
        panel.getChildren().add(reverseAll);

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Motor-driven hinge and slider joints with adjustable speed controls.";
    }
}
