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
 * Demonstrates a wrecking ball demolishing a structure.
 * Press W to swing the wrecking ball.
 * Press B to build a new tower.
 */
public class Ode4jWreckingBallDemo extends Ode4jDemoBase {

    private static final double BALL_RADIUS = 50;
    private static final double BALL_MASS = 20.0;
    private static final double CHAIN_LINK_RADIUS = 10;
    private static final int CHAIN_LINKS = 6;
    private static final double CHAIN_SPACING = 25;

    private static final double BRICK_WIDTH = 50;
    private static final double BRICK_HEIGHT = 25;
    private static final double BRICK_DEPTH = 30;

    private PhysicsBodyHandle wreckingBall;
    private List<PhysicsBodyHandle> chainLinks = new ArrayList<>();
    private List<PhysicsBodyHandle> bricks = new ArrayList<>();

    private Slider towerHeightSlider;
    private Slider ballMassSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 1200, 800, Color.DARKGRAY);

        // Build the wrecking ball crane
        buildWreckingBall();

        // Build the tower to demolish
        int height = towerHeightSlider != null ? (int) towerHeightSlider.getValue() : 6;
        buildTower(height);
    }

    private void buildWreckingBall() {
        chainLinks.clear();

        // Crane arm (static)
        PhysicsBodyHandle craneArm = createStaticBox(-300, 250, 0, 400, 30, 30, Color.DARKRED);

        // Chain links
        PhysicsBodyHandle prevBody = craneArm;
        double y = 220;
        double x = -100;

        for (int i = 0; i < CHAIN_LINKS; i++) {
            y -= CHAIN_SPACING;
            PhysicsBodyHandle link = createSphere(x, y, 0, CHAIN_LINK_RADIUS, 0.5, Color.DIMGRAY);
            chainLinks.add(link);

            // Connect with ball joint
            PhysicsVector3 anchor = new PhysicsVector3(x, y + CHAIN_SPACING / 2, 0);
            createBallJoint(prevBody, link, anchor);

            prevBody = link;
        }

        // Wrecking ball at the end
        y -= BALL_RADIUS + CHAIN_LINK_RADIUS;
        double mass = ballMassSlider != null ? ballMassSlider.getValue() : BALL_MASS;
        wreckingBall = createSphere(x, y, 0, BALL_RADIUS, mass, Color.DARKSLATEGRAY);

        // Connect ball to last chain link
        PhysicsVector3 ballAnchor = new PhysicsVector3(x, y + BALL_RADIUS, 0);
        createBallJoint(prevBody, wreckingBall, ballAnchor);
    }

    private void buildTower(int height) {
        bricks.clear();

        double towerX = 200;
        double baseY = -230 + BRICK_HEIGHT / 2;

        Color[] brickColors = {
                Color.FIREBRICK, Color.INDIANRED, Color.SALMON,
                Color.LIGHTSALMON, Color.CORAL, Color.TOMATO
        };

        // Build tower with alternating brick pattern
        for (int level = 0; level < height; level++) {
            double y = baseY + level * BRICK_HEIGHT;
            boolean offset = (level % 2 == 1);

            // Three bricks per level
            for (int i = 0; i < 3; i++) {
                double brickX = towerX + (i - 1) * BRICK_WIDTH;
                if (offset) {
                    brickX += BRICK_WIDTH / 2;
                }

                Color color = brickColors[level % brickColors.length];
                PhysicsBodyHandle brick = createBox(brickX, y, 0,
                        BRICK_WIDTH - 2, BRICK_HEIGHT - 2, BRICK_DEPTH,
                        0.8, color);
                bricks.add(brick);
            }
        }

        // Add a pyramid top
        for (int level = 0; level < 2; level++) {
            double y = baseY + height * BRICK_HEIGHT + level * BRICK_HEIGHT;
            int bricksInLevel = 2 - level;
            double startX = towerX - (bricksInLevel - 1) * BRICK_WIDTH / 2;

            for (int i = 0; i < bricksInLevel; i++) {
                double brickX = startX + i * BRICK_WIDTH;
                PhysicsBodyHandle brick = createBox(brickX, y, 0,
                        BRICK_WIDTH - 2, BRICK_HEIGHT - 2, BRICK_DEPTH,
                        0.8, Color.GOLD);
                bricks.add(brick);
            }
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case W -> swingWreckingBall();
            case B -> rebuildTower();
            default -> { }
        }
    }

    private void swingWreckingBall() {
        if (wreckingBall != null) {
            // Give the ball a strong push toward the tower
            applyImpulse(wreckingBall, new PhysicsVector3(800, 0, 0));
        }
    }

    private void rebuildTower() {
        // Remove old bricks
        for (PhysicsBodyHandle brick : bricks) {
            world.removeBody(brick);
            Node node = bodyNodes.remove(brick);
            if (node != null) {
                worldGroup.getChildren().remove(node);
            }
            sceneSync.unbindHandle(brick);
        }
        bricks.clear();

        // Build new tower
        int height = towerHeightSlider != null ? (int) towerHeightSlider.getValue() : 6;
        buildTower(height);
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
        chainLinks.clear();
        bricks.clear();

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

        towerHeightSlider = addSlider(grid, 0, "Tower Height", 3, 10, 6);
        towerHeightSlider.setMajorTickUnit(1);
        towerHeightSlider.setSnapToTicks(true);

        ballMassSlider = addSlider(grid, 1, "Ball Mass", 5, 50, BALL_MASS);

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Press W - Swing wrecking ball"),
                new Label("Press B - Rebuild tower"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Watch the momentum transfer"),
                new Label("as the ball hits the tower!")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates a wrecking ball demolishing a brick tower. Press W to swing!";
    }
}
