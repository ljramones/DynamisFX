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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Demonstrates Newton's Cradle - conservation of momentum and energy.
 * Press 1-5 to pull and release different numbers of balls.
 * Press L/R to pull left/right balls.
 */
public class Ode4jNewtonsCradleDemo extends Ode4jDemoBase {

    private static final double BALL_RADIUS = 30;
    private static final double STRING_LENGTH = 200;
    private static final int NUM_BALLS = 5;
    private static final double BALL_MASS = 1.0;

    private List<PhysicsBodyHandle> balls = new ArrayList<>();
    private Slider bounceSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected PhysicsWorldConfiguration defaultConfiguration() {
        return new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 240.0); // Higher precision for cradle
    }

    @Override
    protected void setupWorld() {
        balls.clear();

        // Set high bounce for elastic collisions
        PhysicsRuntimeTuning tuning = world.runtimeTuning();
        world.setRuntimeTuning(new PhysicsRuntimeTuning(
                50, // High iterations for accuracy
                0.0, // No friction
                0.98, // High bounce (nearly elastic)
                tuning.contactSoftCfm(),
                tuning.contactBounceVelocity()));

        // Create frame (static)
        createStaticBox(0, 150, 0, 400, 20, 40, Color.SADDLEBROWN);
        createStaticBox(-190, 0, 0, 20, 300, 40, Color.SADDLEBROWN);
        createStaticBox(190, 0, 0, 20, 300, 40, Color.SADDLEBROWN);

        // Create the balls in a row, touching each other
        double spacing = BALL_RADIUS * 2;
        double startX = -(NUM_BALLS - 1) * spacing / 2;
        double ballY = 150 - STRING_LENGTH - BALL_RADIUS;

        PhysicsBodyHandle frameHandle = bodyNodes.keySet().iterator().next(); // Get frame

        for (int i = 0; i < NUM_BALLS; i++) {
            double x = startX + i * spacing;
            Color color = Color.SILVER;

            PhysicsBodyHandle ball = createSphere(x, ballY, 0, BALL_RADIUS, BALL_MASS, color);
            balls.add(ball);

            // Connect to frame with ball joint (pendulum constraint)
            // The anchor is at the top where the string attaches
            PhysicsVector3 anchor = new PhysicsVector3(x, 140, 0);
            createBallJoint(frameHandle, ball, anchor);
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case DIGIT1, NUMPAD1 -> pullAndRelease(1, true);
            case DIGIT2, NUMPAD2 -> pullAndRelease(2, true);
            case DIGIT3, NUMPAD3 -> pullAndRelease(3, true);
            case L -> pullAndRelease(1, true);  // Pull left
            case R -> pullAndRelease(1, false); // Pull right
            default -> { }
        }
    }

    private void pullAndRelease(int count, boolean fromLeft) {
        if (balls.isEmpty()) return;

        double impulse = 150;

        if (fromLeft) {
            // Pull leftmost balls
            for (int i = 0; i < Math.min(count, balls.size()); i++) {
                applyImpulse(balls.get(i), new PhysicsVector3(-impulse, 0, 0));
            }
        } else {
            // Pull rightmost balls
            for (int i = 0; i < Math.min(count, balls.size()); i++) {
                int idx = balls.size() - 1 - i;
                applyImpulse(balls.get(idx), new PhysicsVector3(impulse, 0, 0));
            }
        }
    }

    @Override
    protected void resetWorld() {
        if (timer != null) {
            timer.stop();
        }
        if (world != null) {
            world.close();
        }
        bodyNodes.clear();
        if (sceneSync != null) {
            sceneSync.clear();
        }
        worldGroup.getChildren().clear();

        world = backend.createWorld(defaultConfiguration());
        accumulator = new org.dynamisfx.physics.step.FixedStepAccumulator(1.0 / 240.0, 16);

        setupWorld();

        timer.start();
        syncControlsFromWorld();
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        bounceSlider = addSlider(grid, 0, "Elasticity", 0.5, 1.0, 0.98);
        bounceSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (world != null) {
                PhysicsRuntimeTuning tuning = world.runtimeTuning();
                world.setRuntimeTuning(new PhysicsRuntimeTuning(
                        tuning.solverIterations(),
                        tuning.contactFriction(),
                        newV.doubleValue(),
                        tuning.contactSoftCfm(),
                        tuning.contactBounceVelocity()));
            }
        });

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Press L - Pull left ball"),
                new Label("Press R - Pull right ball"),
                new Label("Press 1-3 - Pull multiple balls"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Conservation of momentum:"),
                new Label("Balls transfer energy through"),
                new Label("the chain on impact.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates Newton's Cradle - conservation of momentum. Press L/R to swing.";
    }
}
