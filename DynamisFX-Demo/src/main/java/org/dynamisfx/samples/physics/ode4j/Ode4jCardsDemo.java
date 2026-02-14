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
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a card house that can be collapsed.
 * Press C to trigger collapse by pushing the base cards.
 * Based on ODE4j DemoCards.
 */
public class Ode4jCardsDemo extends Ode4jDemoBase {

    // Card dimensions (thin boxes)
    private static final double CARD_WIDTH = 60;
    private static final double CARD_HEIGHT = 90;
    private static final double CARD_DEPTH = 3;
    private static final double CARD_MASS = 0.2;

    private static final int DEFAULT_LEVELS = 3;

    private List<PhysicsBodyHandle> cards = new ArrayList<>();
    private Slider levelsSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 800, 800, Color.DARKGREEN.darker());

        int levels = levelsSlider != null ? (int) levelsSlider.getValue() : DEFAULT_LEVELS;
        buildCardHouse(levels);
    }

    private void buildCardHouse(int levels) {
        cards.clear();
        double floorY = -230;
        double levelHeight = CARD_HEIGHT * 0.85;
        double cardSpacing = CARD_WIDTH * 1.2;

        Color[] cardColors = {
                Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN,
                Color.ORANGE, Color.PURPLE, Color.PINK, Color.CYAN
        };

        for (int level = 0; level < levels; level++) {
            int cardsInLevel = levels - level;
            double levelY = floorY + CARD_HEIGHT / 2 + level * levelHeight;
            double levelStartX = -(cardsInLevel - 1) * cardSpacing / 2;

            for (int i = 0; i < cardsInLevel; i++) {
                double x = levelStartX + i * cardSpacing;
                Color color = cardColors[(level + i) % cardColors.length];

                // Create two leaning cards forming an inverted V (A-frame)
                // Left card leans right
                PhysicsQuaternion leftLean = quaternionFromAxisAngle(0, 0, 1, 15);
                PhysicsBodyHandle leftCard = createCard(
                        x - CARD_WIDTH / 4, levelY, 0,
                        color.darker(), leftLean);
                cards.add(leftCard);

                // Right card leans left
                PhysicsQuaternion rightLean = quaternionFromAxisAngle(0, 0, 1, -15);
                PhysicsBodyHandle rightCard = createCard(
                        x + CARD_WIDTH / 4, levelY, 0,
                        color, rightLean);
                cards.add(rightCard);
            }

            // Add horizontal cards on top of each pair (except top level)
            if (level < levels - 1) {
                int horizontalCards = cardsInLevel - 1;
                double hLevelY = levelY + CARD_HEIGHT / 2 + CARD_DEPTH;

                for (int i = 0; i < horizontalCards; i++) {
                    double x = levelStartX + cardSpacing / 2 + i * cardSpacing;
                    Color color = cardColors[(level + i + 2) % cardColors.length];

                    // Horizontal card (rotated 90 degrees)
                    PhysicsQuaternion horizontal = quaternionFromAxisAngle(0, 0, 1, 90);
                    PhysicsBodyHandle hCard = createCard(x, hLevelY, 0, color.brighter(), horizontal);
                    cards.add(hCard);
                }
            }
        }
    }

    private PhysicsBodyHandle createCard(double x, double y, double z,
                                         Color color, PhysicsQuaternion orientation) {
        return createBox(x, y, z, CARD_DEPTH, CARD_HEIGHT, CARD_WIDTH, CARD_MASS, color, orientation);
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.C) {
            collapseCards();
        }
    }

    private void collapseCards() {
        // Push the bottom cards to trigger collapse
        if (!cards.isEmpty()) {
            int bottomCards = Math.min(4, cards.size());
            for (int i = 0; i < bottomCards; i++) {
                PhysicsBodyHandle card = cards.get(i);
                // Random push direction
                double px = (Math.random() - 0.5) * 100;
                double pz = (Math.random() - 0.5) * 50;
                applyImpulse(card, new PhysicsVector3(px, 20, pz));
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

        levelsSlider = addSlider(grid, 0, "Levels", 1, 6, DEFAULT_LEVELS);
        levelsSlider.setMajorTickUnit(1);
        levelsSlider.setSnapToTicks(true);

        panel.getChildren().addAll(
                grid,
                new Label("Press C - Collapse cards"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Adjust levels and Reset"),
                new Label("to rebuild card house.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates a card house collapse. Press C to knock it down.";
    }
}
