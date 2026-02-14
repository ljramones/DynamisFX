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
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Balancing challenge - stack objects on unstable platforms.
 * Demonstrates equilibrium and center of mass concepts.
 *
 * Controls:
 * 1/2/3 - Select platform to drop on
 * D - Drop random object
 * T - Tilt platforms
 */
public class Ode4jBalancingDemo extends Ode4jDemoBase {

    private PhysicsConstraintHandle[] platformHinges = new PhysicsConstraintHandle[3];
    private PhysicsBodyHandle[] platforms = new PhysicsBodyHandle[3];
    private int selectedPlatform = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        selectedPlatform = 0;

        // Ground
        createFloor(-300, 600, 400, Color.DARKSLATEGRAY);

        // ====== THREE BALANCING PLATFORMS ======

        // Platform 1 - Ball pivot (most unstable)
        createBallPivotPlatform(-180, Color.CORAL);

        // Platform 2 - Single edge pivot
        createEdgePivotPlatform(0, Color.STEELBLUE);

        // Platform 3 - Wide hinge (most stable)
        createHingePlatform(180, Color.FORESTGREEN);

        // Labels (static boxes as markers)
        createStaticBox(-180, -280, 120, 60, 10, 10, Color.CORAL);
        createStaticBox(0, -280, 120, 60, 10, 10, Color.STEELBLUE);
        createStaticBox(180, -280, 120, 60, 10, 10, Color.FORESTGREEN);
    }

    private void createBallPivotPlatform(double x, Color color) {
        // Tiny fulcrum point (sphere)
        PhysicsBodyHandle pivot = createStaticBox(x, -220, 0, 15, 30, 15, Color.DIMGRAY);

        // Platform on ball joint
        PhysicsBodyHandle platform = createBox(x, -190, 0, 120, 12, 80, 3.0, color);
        platforms[0] = platform;

        // Ball joint allows tilting in all directions
        createBallJoint(pivot, platform, new PhysicsVector3(x, -200, 0));
    }

    private void createEdgePivotPlatform(double x, Color color) {
        // Edge pivot support
        PhysicsBodyHandle pivot = createStaticBox(x, -220, 0, 10, 30, 80, Color.DIMGRAY);

        // Platform on hinge (tilts front/back only)
        PhysicsBodyHandle platform = createBox(x, -190, 0, 120, 12, 80, 3.0, color);
        platforms[1] = platform;

        platformHinges[1] = createHingeJoint(pivot, platform,
                new PhysicsVector3(x, -195, 0),
                new PhysicsVector3(0, 0, 1),  // Z-axis tilt
                -0.3, 0.3);
    }

    private void createHingePlatform(double x, Color color) {
        // Wide hinge support
        PhysicsBodyHandle pivot = createStaticBox(x, -230, 0, 60, 20, 80, Color.DIMGRAY);

        // Platform on stiff hinge
        PhysicsBodyHandle platform = createBox(x, -200, 0, 120, 12, 80, 3.0, color);
        platforms[2] = platform;

        platformHinges[2] = createHingeJoint(pivot, platform,
                new PhysicsVector3(x, -205, 0),
                new PhysicsVector3(0, 0, 1),
                -0.15, 0.15);  // Limited tilt
    }

    private void dropOnPlatform(int platformIndex) {
        if (platformIndex < 0 || platformIndex > 2) return;

        double[] xPositions = {-180, 0, 180};
        double x = xPositions[platformIndex] + (Math.random() - 0.5) * 60;
        double z = (Math.random() - 0.5) * 40;
        Color color = Color.hsb(Math.random() * 360, 0.7, 0.8);

        double rand = Math.random();
        if (rand < 0.33) {
            createBox(x, -100, z, 25, 25, 25, 1.0, color);
        } else if (rand < 0.66) {
            createSphere(x, -100, z, 15, 0.8, color);
        } else {
            // Tall object (harder to balance)
            createBox(x, -80, z, 15, 50, 15, 1.5, color);
        }
    }

    private void tiltPlatforms() {
        // Apply impulse to disturb the platforms
        for (PhysicsBodyHandle platform : platforms) {
            if (platform != null) {
                applyImpulse(platform, new PhysicsVector3(
                        (Math.random() - 0.5) * 50,
                        0,
                        (Math.random() - 0.5) * 50));
            }
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case DIGIT1, NUMPAD1 -> {
                selectedPlatform = 0;
                dropOnPlatform(0);
            }
            case DIGIT2, NUMPAD2 -> {
                selectedPlatform = 1;
                dropOnPlatform(1);
            }
            case DIGIT3, NUMPAD3 -> {
                selectedPlatform = 2;
                dropOnPlatform(2);
            }
            case D -> dropOnPlatform(selectedPlatform);
            case T -> tiltPlatforms();
            default -> { }
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button drop1 = new Button("Drop on Ball (1)");
        drop1.setOnAction(e -> dropOnPlatform(0));

        Button drop2 = new Button("Drop on Edge (2)");
        drop2.setOnAction(e -> dropOnPlatform(1));

        Button drop3 = new Button("Drop on Hinge (3)");
        drop3.setOnAction(e -> dropOnPlatform(2));

        Button tiltBtn = new Button("Tilt All (T)");
        tiltBtn.setOnAction(e -> tiltPlatforms());

        panel.getChildren().addAll(
                new Label("Balancing Challenge:"),
                new Label(""),
                drop1,
                drop2,
                drop3,
                new Label(""),
                tiltBtn,
                new Label(""),
                new Label("Ball pivot = hardest"),
                new Label("Hinge = easiest"),
                new Label(""),
                new Label("Stack without"),
                new Label("toppling!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Balance objects on unstable platforms. Compare pivot types!";
    }
}
