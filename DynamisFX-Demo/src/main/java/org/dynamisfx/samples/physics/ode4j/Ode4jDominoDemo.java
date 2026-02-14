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
 * Demonstrates a domino cascade effect.
 * Press D to push the first domino and watch them all fall.
 * Press R to reset after they've fallen.
 */
public class Ode4jDominoDemo extends Ode4jDemoBase {

    // Domino dimensions
    private static final double DOMINO_WIDTH = 8;
    private static final double DOMINO_HEIGHT = 60;
    private static final double DOMINO_DEPTH = 30;
    private static final double DOMINO_MASS = 0.5;

    // Layout
    private static final int DEFAULT_DOMINO_COUNT = 20;
    private static final double DEFAULT_SPACING = 25;

    private List<PhysicsBodyHandle> dominos = new ArrayList<>();
    private Slider dominoCountSlider;
    private Slider spacingSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 1000, 600, Color.DARKSLATEGRAY);

        int count = dominoCountSlider != null ? (int) dominoCountSlider.getValue() : DEFAULT_DOMINO_COUNT;
        double spacing = spacingSlider != null ? spacingSlider.getValue() : DEFAULT_SPACING;

        buildDominoLine(count, spacing);
    }

    private void buildDominoLine(int count, double spacing) {
        dominos.clear();

        // Start position
        double startX = -count * spacing / 2;
        double y = -220 + DOMINO_HEIGHT / 2;

        // Create a line of dominos
        for (int i = 0; i < count; i++) {
            double x = startX + i * spacing;
            Color color = Color.hsb(i * 360.0 / count, 0.8, 0.9);
            PhysicsBodyHandle domino = createBox(x, y, 0,
                    DOMINO_WIDTH, DOMINO_HEIGHT, DOMINO_DEPTH,
                    DOMINO_MASS, color);
            dominos.add(domino);
        }

        // Create a curved section
        int curveCount = count / 2;
        double curveRadius = 150;
        double lastX = startX + (count - 1) * spacing;
        double centerZ = curveRadius;

        for (int i = 0; i < curveCount; i++) {
            double angle = Math.PI * i / (curveCount - 1);
            double x = lastX + curveRadius * Math.sin(angle);
            double z = centerZ - curveRadius * Math.cos(angle);

            Color color = Color.hsb((count + i) * 360.0 / (count + curveCount), 0.7, 0.85);

            // Create rotated domino
            PhysicsBodyHandle domino = createBox(x, y, z,
                    DOMINO_WIDTH, DOMINO_HEIGHT, DOMINO_DEPTH,
                    DOMINO_MASS, color,
                    quaternionFromAxisAngle(0, 1, 0, -Math.toDegrees(angle)));
            dominos.add(domino);
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case D -> pushFirstDomino();
            case R -> resetWorld();
            default -> { }
        }
    }

    private void pushFirstDomino() {
        if (!dominos.isEmpty()) {
            PhysicsBodyHandle first = dominos.get(0);
            // Push forward (positive X direction)
            applyImpulse(first, new PhysicsVector3(15, 0, 0));
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

        dominoCountSlider = addSlider(grid, 0, "Domino Count", 5, 40, DEFAULT_DOMINO_COUNT);
        dominoCountSlider.setMajorTickUnit(5);
        dominoCountSlider.setSnapToTicks(true);

        spacingSlider = addSlider(grid, 1, "Spacing", 15, 40, DEFAULT_SPACING);

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Press D - Push first domino"),
                new Label("Press R - Reset"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Adjust count/spacing and"),
                new Label("press Reset to rebuild.")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates a domino cascade effect. Press D to push the first domino.";
    }
}
