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
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a seesaw/teeter-totter with falling objects.
 * Drop objects on either side to see the balance shift.
 *
 * Controls:
 * L - Drop object on left side
 * R - Drop object on right side
 * B - Drop heavy ball in center
 */
public class Ode4jSeesawDemo extends Ode4jDemoBase {

    private static final double PLANK_LENGTH = 300;
    private static final double PLANK_WIDTH = 60;
    private static final double PLANK_HEIGHT = 15;
    private static final double FULCRUM_HEIGHT = 50;

    private PhysicsBodyHandle plank;
    private int dropCount = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        dropCount = 0;

        // Floor
        createFloor(-250, 500, 400, Color.DARKSLATEGRAY);

        // Fulcrum (triangular base approximated with a box)
        PhysicsBodyHandle fulcrum = createStaticBox(0, -200, 0, 30, FULCRUM_HEIGHT, PLANK_WIDTH, Color.DIMGRAY);

        // Seesaw plank
        plank = createBox(0, -170, 0, PLANK_LENGTH, PLANK_HEIGHT, PLANK_WIDTH, 5.0, Color.SADDLEBROWN);

        // Hinge joint at the fulcrum point
        createHingeJoint(fulcrum, plank,
                new PhysicsVector3(0, -175, 0),
                new PhysicsVector3(0, 0, 1),  // Z-axis rotation
                -Math.PI / 4, Math.PI / 4);  // Limit rotation to 45 degrees

        // Initial weights to show balance
        createBox(-100, -100, 0, 30, 30, 30, 2.0, Color.STEELBLUE);
        createBox(100, -100, 0, 30, 30, 30, 2.0, Color.CORAL);
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case L -> dropOnLeft();
            case R -> dropOnRight();
            case B -> dropHeavyBall();
            default -> { }
        }
    }

    private void dropOnLeft() {
        double x = -120 + (Math.random() - 0.5) * 40;
        double z = (Math.random() - 0.5) * 30;
        Color color = Color.hsb(dropCount * 40 % 360, 0.7, 0.8);

        if (dropCount % 2 == 0) {
            createBox(x, 150, z, 25, 25, 25, 1.5, color);
        } else {
            createSphere(x, 150, z, 15, 1.0, color);
        }
        dropCount++;
    }

    private void dropOnRight() {
        double x = 120 + (Math.random() - 0.5) * 40;
        double z = (Math.random() - 0.5) * 30;
        Color color = Color.hsb(dropCount * 40 % 360, 0.7, 0.8);

        if (dropCount % 2 == 0) {
            createBox(x, 150, z, 25, 25, 25, 1.5, color);
        } else {
            createSphere(x, 150, z, 15, 1.0, color);
        }
        dropCount++;
    }

    private void dropHeavyBall() {
        createSphere(0, 200, 0, 30, 5.0, Color.GOLD);
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button leftBtn = new Button("Drop Left (L)");
        leftBtn.setOnAction(e -> dropOnLeft());

        Button rightBtn = new Button("Drop Right (R)");
        rightBtn.setOnAction(e -> dropOnRight());

        Button heavyBtn = new Button("Heavy Ball (B)");
        heavyBtn.setOnAction(e -> dropHeavyBall());

        panel.getChildren().addAll(
                new Label("Seesaw Controls:"),
                new Label(""),
                leftBtn,
                rightBtn,
                heavyBtn,
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Try to balance the"),
                new Label("seesaw!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Seesaw balance demo. Drop objects on each side with L/R keys.";
    }
}
