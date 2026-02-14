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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Demonstrates gyroscopic precession - a spinning body that precesses
 * when tilted due to angular momentum conservation.
 * Press G to apply spin, T to tilt the body.
 * Based on ODE4j DemoGyroscopic.
 */
public class Ode4jGyroscopicDemo extends Ode4jDemoBase {

    private static final double BODY_WIDTH = 100;
    private static final double BODY_HEIGHT = 20;
    private static final double BODY_DEPTH = 100;
    private static final double MASS = 2.0;

    private PhysicsBodyHandle gyroBody;
    private Slider spinSpeedSlider;
    private Slider tiltAngleSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected PhysicsWorldConfiguration defaultConfiguration() {
        // Use reduced gravity for gyroscopic demo to better see precession
        return new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -2.0, 0), // Reduced gravity
                1.0 / 120.0);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 800, 800, Color.DIMGRAY);

        // Create a pivot point (static)
        createStaticBox(0, -180, 0, 30, 100, 30, Color.DARKGRAY);

        // Create the gyroscopic body - a flat disk-like shape
        // Initial tilt
        PhysicsQuaternion initialTilt = quaternionFromAxisAngle(1, 0, 0, 10);
        gyroBody = createBox(0, -100, 0,
                BODY_WIDTH, BODY_HEIGHT, BODY_DEPTH,
                MASS, Color.ROYALBLUE, initialTilt);

        // Create a fixed joint to the pivot (ball joint allows rotation)
        createBallJoint(
                bodyNodes.keySet().stream()
                        .filter(h -> bodyNodes.get(h) instanceof javafx.scene.shape.Box b
                                && ((javafx.scene.paint.PhongMaterial) b.getMaterial()).getDiffuseColor().equals(Color.DARKGRAY))
                        .findFirst().orElse(null),
                gyroBody,
                new PhysicsVector3(0, -130, 0));

        // Apply initial spin
        setAngularVelocity(gyroBody, new PhysicsVector3(0, 20, 0));
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case G -> applySpin();
            case T -> applyTilt();
            default -> { }
        }
    }

    private void applySpin() {
        if (gyroBody != null) {
            double speed = spinSpeedSlider != null ? spinSpeedSlider.getValue() : 30;
            setAngularVelocity(gyroBody, new PhysicsVector3(0, speed, 0));
        }
    }

    private void applyTilt() {
        if (gyroBody != null) {
            // Apply a torque to tilt the body
            double tiltSpeed = tiltAngleSlider != null ? tiltAngleSlider.getValue() : 5;
            org.dynamisfx.physics.model.PhysicsBodyState state = world.getBodyState(gyroBody);
            if (state != null) {
                PhysicsVector3 currentAngVel = state.angularVelocity();
                world.setBodyState(gyroBody, new org.dynamisfx.physics.model.PhysicsBodyState(
                        state.position(),
                        state.orientation(),
                        state.linearVelocity(),
                        new PhysicsVector3(
                                currentAngVel.x() + tiltSpeed,
                                currentAngVel.y(),
                                currentAngVel.z()),
                        state.referenceFrame(),
                        state.timestampSeconds()));
            }
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        spinSpeedSlider = addSlider(grid, 0, "Spin Speed", 5, 100, 30);
        tiltAngleSlider = addSlider(grid, 1, "Tilt Force", 1, 20, 5);

        Button spinButton = new Button("Apply Spin (G)");
        spinButton.setOnAction(e -> applySpin());

        Button tiltButton = new Button("Apply Tilt (T)");
        tiltButton.setOnAction(e -> applyTilt());

        panel.getChildren().addAll(
                grid,
                spinButton,
                tiltButton,
                new Label(""),
                new Label("A spinning body precesses"),
                new Label("when tilted due to angular"),
                new Label("momentum conservation."),
                new Label(""),
                new Label("Watch the axis of rotation"),
                new Label("slowly rotate around vertical.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates gyroscopic precession. Press G to spin, T to tilt.";
    }
}
