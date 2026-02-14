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
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Trebuchet/catapult demo that launches projectiles at targets.
 *
 * Controls:
 * SPACE - Fire catapult
 * R - Reset and reload
 * +/- - Adjust power
 */
public class Ode4jCatapultDemo extends Ode4jDemoBase {

    private static final double ARM_LENGTH = 150;
    private static final double ARM_WIDTH = 15;

    private PhysicsConstraintHandle armHinge;
    private PhysicsBodyHandle projectile;
    private PhysicsBodyHandle arm;
    private double launchPower = 30.0;
    private boolean armed = true;
    private int hits = 0;

    private Label powerLabel;
    private Label hitsLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        armed = true;

        // Ground
        createFloor(-250, 800, 600, Color.DARKGREEN);

        // ====== CATAPULT ======
        // Base frame
        createStaticBox(-200, -200, 0, 80, 60, 60, Color.SADDLEBROWN);

        // Pivot post
        PhysicsBodyHandle pivot = createStaticBox(-200, -140, 0, 20, 80, 20, Color.SADDLEBROWN);

        // Throwing arm
        arm = createBox(-200 + ARM_LENGTH / 2 - 30, -100, 0,
                ARM_LENGTH, ARM_WIDTH, ARM_WIDTH, 3.0, Color.BURLYWOOD);

        // Arm hinge
        armHinge = createHingeJoint(pivot, arm,
                new PhysicsVector3(-200, -100, 0),
                new PhysicsVector3(0, 0, 1),  // Z-axis rotation
                -1.5, 0.3);  // Limit rotation

        // Counterweight (heavy box on short end)
        PhysicsBodyHandle counterweight = createBox(-230, -80, 0, 30, 30, 30, 8.0, Color.DIMGRAY);
        createFixedJoint(arm, counterweight);

        // Projectile cup/basket
        PhysicsBodyHandle cup = createBox(-120, -90, 0, 25, 10, 25, 0.5, Color.SIENNA);
        createFixedJoint(arm, cup);

        // Load projectile
        loadProjectile();

        // ====== TARGETS ======
        createTargetTower(150, Color.STEELBLUE);
        createTargetTower(250, Color.CORAL);
        createTargetTower(350, Color.GOLD);
    }

    private void createTargetTower(double x, Color color) {
        // Stack of boxes as target
        for (int i = 0; i < 4; i++) {
            createBox(x, -210 + i * 35, 0, 30, 30, 30, 0.8, color);
        }
    }

    private void loadProjectile() {
        if (projectile != null) {
            // Already have a projectile
            return;
        }
        projectile = createSphere(-120, -70, 0, 15, 2.0, Color.DARKRED);
    }

    private void fire() {
        if (!armed) return;

        // Apply strong motor torque to fling the arm
        enableMotor(armHinge, launchPower, 500.0);
        armed = false;

        // Release projectile after a short delay (the ball joint would break naturally)
        // For now, the projectile will fly when the arm swings
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case SPACE, F -> fire();
            case R -> reload();
            case EQUALS, PLUS -> adjustPower(5);
            case MINUS -> adjustPower(-5);
            default -> { }
        }
    }

    private void reload() {
        resetWorld();
        hits = 0;
        updateHitsLabel();
    }

    private void adjustPower(double delta) {
        launchPower = Math.max(10, Math.min(60, launchPower + delta));
        if (powerLabel != null) {
            powerLabel.setText(String.format("Power: %.0f", launchPower));
        }
    }

    private void updateHitsLabel() {
        if (hitsLabel != null) {
            hitsLabel.setText("Targets hit: " + hits);
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button fireBtn = new Button("FIRE! (SPACE)");
        fireBtn.setOnAction(e -> fire());

        Button reloadBtn = new Button("Reload (R)");
        reloadBtn.setOnAction(e -> reload());

        powerLabel = new Label(String.format("Power: %.0f", launchPower));

        Slider powerSlider = new Slider(10, 60, launchPower);
        powerSlider.setPrefWidth(120);
        powerSlider.valueProperty().addListener((obs, oldV, newV) -> {
            launchPower = newV.doubleValue();
            powerLabel.setText(String.format("Power: %.0f", launchPower));
        });

        hitsLabel = new Label("Targets hit: 0");

        panel.getChildren().addAll(
                new Label("Catapult Controls:"),
                new Label(""),
                fireBtn,
                reloadBtn,
                new Label(""),
                powerLabel,
                powerSlider,
                new Label(""),
                hitsLabel,
                new Label(""),
                new Label("Knock down the"),
                new Label("target towers!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Trebuchet catapult launching projectiles at targets. Press SPACE to fire!";
    }
}
