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

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates kinematic bodies - bodies that are moved programmatically
 * and push dynamic bodies around without being affected by physics themselves.
 * Based on ODE4j DemoKinematic.
 */
public class Ode4jKinematicDemo extends Ode4jDemoBase {

    private static final double PLATFORM_WIDTH = 200;
    private static final double PLATFORM_HEIGHT = 20;
    private static final double PLATFORM_DEPTH = 200;
    private static final double BOX_SIZE = 40;
    private static final double AMPLITUDE = 150;

    private PhysicsBodyHandle platform1;
    private PhysicsBodyHandle platform2;
    private double time = 0;
    private Slider speedSlider;
    private Slider amplitudeSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-300, 800, 800, Color.DIMGRAY);

        // Create kinematic platform 1 (moves horizontally)
        platform1 = createKinematicBox(-150, -100, 0,
                PLATFORM_WIDTH, PLATFORM_HEIGHT, PLATFORM_DEPTH,
                Color.STEELBLUE);

        // Create kinematic platform 2 (moves vertically)
        platform2 = createKinematicBox(150, -100, 0,
                PLATFORM_WIDTH, PLATFORM_HEIGHT, PLATFORM_DEPTH,
                Color.DARKSEAGREEN);

        // Create dynamic boxes on the platforms
        createBox(-150, -50, 0, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.CORAL);
        createBox(-180, -50, 30, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.GOLD);
        createBox(-120, -50, -30, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.ORCHID);

        createBox(150, -50, 0, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.TOMATO);
        createBox(180, -50, 30, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.LIME);
        createBox(120, -50, -30, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.CYAN);

        time = 0;
    }

    @Override
    protected void addMeshAndListeners() {
        super.addMeshAndListeners();

        // Override the timer to also update kinematic bodies
        if (timer != null) {
            timer.stop();
        }

        timer = new AnimationTimer() {
            private long lastNanos;

            @Override
            public void handle(long now) {
                if (lastNanos == 0L) {
                    lastNanos = now;
                    return;
                }
                if (paused) {
                    lastNanos = now;
                    return;
                }
                double dt = Math.min((now - lastNanos) * 1.0e-9, 0.1);
                lastNanos = now;

                if (world == null || accumulator == null || sceneSync == null) {
                    return;
                }

                // Update kinematic bodies before physics step
                updateKinematicBodies(dt);

                accumulator.advance(dt, world::step);
                sceneSync.applyFrame(world::getBodyState);
            }
        };
        timer.start();
    }

    private void updateKinematicBodies(double dt) {
        double speed = speedSlider != null ? speedSlider.getValue() : 2.0;
        double amp = amplitudeSlider != null ? amplitudeSlider.getValue() : AMPLITUDE;

        time += dt * speed;

        // Platform 1: Horizontal sine wave motion
        if (platform1 != null) {
            double x = -150 + amp * Math.sin(time);
            setKinematicPosition(platform1, new PhysicsVector3(x, -100, 0));
        }

        // Platform 2: Vertical sine wave motion (phase shifted)
        if (platform2 != null) {
            double y = -100 + amp * 0.5 * Math.sin(time + Math.PI / 2);
            setKinematicPosition(platform2, new PhysicsVector3(150, y, 0));
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        speedSlider = addSlider(grid, 0, "Speed", 0.5, 5.0, 2.0);
        amplitudeSlider = addSlider(grid, 1, "Amplitude", 50, 250, AMPLITUDE);

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Blue platform: Horizontal motion"),
                new Label("Green platform: Vertical motion"),
                new Label(""),
                new Label("Kinematic bodies move objects"),
                new Label("without being affected by physics.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates kinematic bodies that move programmatically and push dynamic bodies.";
    }
}
