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
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a pendulum wave - multiple pendulums of different lengths
 * that create mesmerizing wave patterns when released together.
 * Press S to start/sync all pendulums.
 */
public class Ode4jPendulumWaveDemo extends Ode4jDemoBase {

    private static final double BALL_RADIUS = 15;
    private static final double BALL_MASS = 1.0;
    private static final int DEFAULT_PENDULUM_COUNT = 15;
    private static final double BASE_LENGTH = 150;
    private static final double LENGTH_INCREMENT = 8;

    private List<PhysicsBodyHandle> pendulums = new ArrayList<>();
    private Slider pendulumCountSlider;
    private Slider amplitudeSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        pendulums.clear();

        int count = pendulumCountSlider != null ? (int) pendulumCountSlider.getValue() : DEFAULT_PENDULUM_COUNT;

        // Create support beam
        double beamWidth = count * (BALL_RADIUS * 2 + 10) + 50;
        PhysicsBodyHandle beam = createStaticBox(0, 200, 0, beamWidth, 20, 30, Color.SADDLEBROWN);

        // Create pendulums with varying lengths
        double spacing = BALL_RADIUS * 2 + 10;
        double startX = -(count - 1) * spacing / 2;

        for (int i = 0; i < count; i++) {
            double x = startX + i * spacing;

            // Each pendulum has a slightly different length
            // This creates the wave effect as they have different periods
            double length = BASE_LENGTH + i * LENGTH_INCREMENT;
            double ballY = 200 - length - BALL_RADIUS;

            // Color gradient from red to violet
            Color color = Color.hsb(i * 270.0 / (count - 1), 0.9, 0.9);

            PhysicsBodyHandle ball = createSphere(x, ballY, 0, BALL_RADIUS, BALL_MASS, color);
            pendulums.add(ball);

            // Connect to beam with ball joint
            PhysicsVector3 anchor = new PhysicsVector3(x, 190, 0);
            createBallJoint(beam, ball, anchor);
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case S -> syncPendulums();
            case P -> pushRandom();
            default -> { }
        }
    }

    private void syncPendulums() {
        // Pull all pendulums to one side and release
        double amplitude = amplitudeSlider != null ? amplitudeSlider.getValue() : 80;

        for (PhysicsBodyHandle pendulum : pendulums) {
            // Apply impulse to swing all pendulums in the same direction
            applyImpulse(pendulum, new PhysicsVector3(amplitude, 0, 0));
        }
    }

    private void pushRandom() {
        // Give random pendulums a push
        for (int i = 0; i < pendulums.size(); i += 3) {
            double impulse = (Math.random() - 0.5) * 100;
            applyImpulse(pendulums.get(i), new PhysicsVector3(impulse, 0, 0));
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
        pendulums.clear();

        world = backend.createWorld(defaultConfiguration());
        accumulator = new org.dynamisfx.physics.step.FixedStepAccumulator(1.0 / 120.0, 8);

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

        pendulumCountSlider = addSlider(grid, 0, "Pendulums", 5, 25, DEFAULT_PENDULUM_COUNT);
        pendulumCountSlider.setMajorTickUnit(5);
        pendulumCountSlider.setSnapToTicks(true);

        amplitudeSlider = addSlider(grid, 1, "Amplitude", 20, 150, 80);

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Press S - Sync & release"),
                new Label("Press P - Random push"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Each pendulum has a"),
                new Label("slightly different length,"),
                new Label("creating wave patterns!"),
                new Label(""),
                new Label("Adjust count and Reset")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates pendulum waves. Press S to sync all pendulums and watch the patterns!";
    }
}
