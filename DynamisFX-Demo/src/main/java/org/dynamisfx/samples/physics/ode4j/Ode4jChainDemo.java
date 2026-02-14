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
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a swinging pendulum chain using ball joints.
 * Click or press D to disturb the chain.
 * Based on ODE4j DemoChain.
 */
public class Ode4jChainDemo extends Ode4jDemoBase {

    private static final double SPHERE_RADIUS = 25;
    private static final double LINK_SPACING = 60;
    private static final double MASS = 1.0;
    private static final int DEFAULT_LINKS = 8;

    private PhysicsBodyHandle[] chainLinks;
    private PhysicsBodyHandle anchorBody;
    private Slider linkCountSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        int linkCount = linkCountSlider != null ? (int) linkCountSlider.getValue() : DEFAULT_LINKS;
        buildChain(linkCount);
    }

    private void buildChain(int linkCount) {
        // Create a static anchor point at the top
        anchorBody = createStaticBox(0, 200, 0, 30, 30, 30, Color.DARKGRAY);

        chainLinks = new PhysicsBodyHandle[linkCount];
        PhysicsBodyHandle prevBody = anchorBody;
        double prevY = 200;

        // Create chain links with ball joints
        for (int i = 0; i < linkCount; i++) {
            double y = prevY - LINK_SPACING;
            Color color = Color.hsb(i * 360.0 / linkCount, 0.8, 0.9);
            chainLinks[i] = createSphere(0, y, 0, SPHERE_RADIUS, MASS, color);

            // Connect to previous body with ball joint
            PhysicsVector3 anchor = new PhysicsVector3(0, (prevY + y) / 2, 0);
            createBallJoint(prevBody, chainLinks[i], anchor);

            prevBody = chainLinks[i];
            prevY = y;
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.D) {
            disturbChain();
        }
    }

    private void disturbChain() {
        if (chainLinks != null && chainLinks.length > 0) {
            // Apply impulse to the last link
            PhysicsBodyHandle lastLink = chainLinks[chainLinks.length - 1];
            applyImpulse(lastLink, new PhysicsVector3(200, 0, 100));
        }
    }

    @Override
    protected void resetWorld() {
        // Clear and rebuild chain with current link count
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

        linkCountSlider = addSlider(grid, 0, "Links", 3, 20, DEFAULT_LINKS);
        linkCountSlider.setMajorTickUnit(1);
        linkCountSlider.setSnapToTicks(true);

        panel.getChildren().addAll(
                grid,
                new Label("Press D - Disturb chain"),
                new Label("Press SPACE - Pause/Resume"),
                new Label("Adjust links and Reset to rebuild")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates a swinging pendulum chain using ball joints. Press D to disturb.";
    }
}
