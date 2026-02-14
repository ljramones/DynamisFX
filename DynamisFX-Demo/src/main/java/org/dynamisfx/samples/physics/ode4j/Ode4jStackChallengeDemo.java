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
import java.util.Random;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Interactive tower stacking challenge.
 * Use arrow keys to position blocks, press DROP to release.
 * Try to build the tallest stable tower!
 */
public class Ode4jStackChallengeDemo extends Ode4jDemoBase {

    private static final double BLOCK_WIDTH = 80;
    private static final double BLOCK_HEIGHT = 30;
    private static final double BLOCK_DEPTH = 80;
    private static final double BLOCK_MASS = 1.0;
    private static final double DROP_HEIGHT = 300;
    private static final double MOVE_SPEED = 5;

    private List<PhysicsBodyHandle> stackedBlocks = new ArrayList<>();
    private PhysicsBodyHandle currentBlock;
    private double currentX = 0;
    private double currentZ = 0;
    private int blockCount = 0;
    private int bestScore = 0;
    private Label scoreLabel;
    private Label bestLabel;

    private final Random random = new Random();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        stackedBlocks.clear();
        blockCount = 0;
        currentX = 0;
        currentZ = 0;

        // Create floor/base
        createFloor(-250, 400, 400, Color.DARKSLATEGRAY);

        // Create foundation block (static)
        createStaticBox(0, -210, 0, BLOCK_WIDTH + 20, BLOCK_HEIGHT, BLOCK_DEPTH + 20, Color.DIMGRAY);

        // Spawn first block to drop
        spawnNewBlock();

        updateScoreLabels();
    }

    private void spawnNewBlock() {
        // Random starting position
        currentX = (random.nextDouble() - 0.5) * 100;
        currentZ = (random.nextDouble() - 0.5) * 100;

        Color color = Color.hsb(blockCount * 30 % 360, 0.7, 0.85);
        currentBlock = createBox(currentX, DROP_HEIGHT, currentZ,
                BLOCK_WIDTH, BLOCK_HEIGHT, BLOCK_DEPTH,
                BLOCK_MASS, color);

        // Make it kinematic while positioning
        // (We'll convert to dynamic when dropped)
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        if (currentBlock == null) return;

        switch (code) {
            case LEFT -> moveBlock(-MOVE_SPEED, 0);
            case RIGHT -> moveBlock(MOVE_SPEED, 0);
            case UP -> moveBlock(0, -MOVE_SPEED);
            case DOWN -> moveBlock(0, MOVE_SPEED);
            case ENTER, D -> dropBlock();
            case N -> spawnNewBlock();
            default -> { }
        }
    }

    private void moveBlock(double dx, double dz) {
        if (currentBlock == null) return;

        currentX += dx;
        currentZ += dz;

        // Clamp to reasonable bounds
        currentX = Math.max(-150, Math.min(150, currentX));
        currentZ = Math.max(-150, Math.min(150, currentZ));

        // Update block position
        PhysicsBodyState state = world.getBodyState(currentBlock);
        if (state != null) {
            world.setBodyState(currentBlock, new PhysicsBodyState(
                    new PhysicsVector3(currentX, DROP_HEIGHT, currentZ),
                    state.orientation(),
                    PhysicsVector3.ZERO,
                    PhysicsVector3.ZERO,
                    state.referenceFrame(),
                    state.timestampSeconds()));
        }
    }

    private void dropBlock() {
        if (currentBlock == null) return;

        stackedBlocks.add(currentBlock);
        blockCount++;

        // Let physics take over - the block is already dynamic
        // Just remove position override by letting it fall naturally

        currentBlock = null;

        // Update score
        updateScoreLabels();

        // Spawn next block after a delay (using the animation timer would be better)
        // For simplicity, spawn immediately
        spawnNewBlock();
    }

    private void updateScoreLabels() {
        if (scoreLabel != null) {
            scoreLabel.setText("Blocks: " + blockCount);
        }
        if (blockCount > bestScore) {
            bestScore = blockCount;
        }
        if (bestLabel != null) {
            bestLabel.setText("Best: " + bestScore);
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
        stackedBlocks.clear();
        currentBlock = null;

        world = backend.createWorld(defaultConfiguration());
        accumulator = new org.dynamisfx.physics.step.FixedStepAccumulator(1.0 / 120.0, 8);

        setupWorld();

        timer.start();
        syncControlsFromWorld();
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        scoreLabel = new Label("Blocks: 0");
        scoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        bestLabel = new Label("Best: 0");
        bestLabel.setStyle("-fx-font-size: 14;");

        panel.getChildren().addAll(
                scoreLabel,
                bestLabel,
                new Label(""),
                new Label("Arrow keys - Move block"),
                new Label("ENTER/D - Drop block"),
                new Label("N - New block"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Stack blocks as high"),
                new Label("as you can without"),
                new Label("the tower falling!"),
                new Label(""),
                new Label("Tip: Center blocks"),
                new Label("for stability.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Interactive stacking challenge. Use arrows to position, ENTER to drop!";
    }
}
