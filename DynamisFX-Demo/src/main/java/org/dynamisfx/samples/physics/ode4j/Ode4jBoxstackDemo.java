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

import java.util.Random;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Demonstrates stacking physics objects.
 * Press B to spawn a box, S to spawn a sphere, C to spawn a capsule.
 * Based on ODE4j DemoBoxstack.
 */
public class Ode4jBoxstackDemo extends Ode4jDemoBase {

    private static final double BOX_SIZE = 50;
    private static final double SPHERE_RADIUS = 30;
    private static final double CAPSULE_RADIUS = 20;
    private static final double CAPSULE_LENGTH = 40;
    private static final double SPAWN_HEIGHT = 400;
    private static final double MASS = 1.0;

    private final Random random = new Random();
    private int objectCount = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 800, 800, Color.DIMGRAY);

        // Create initial stack of boxes
        for (int i = 0; i < 5; i++) {
            double y = -200 + i * (BOX_SIZE + 5);
            createBox(0, y, 0, BOX_SIZE, BOX_SIZE, BOX_SIZE, MASS, randomColor());
            objectCount++;
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        double x = (random.nextDouble() - 0.5) * 100;
        double z = (random.nextDouble() - 0.5) * 100;

        switch (code) {
            case B -> {
                createBox(x, SPAWN_HEIGHT, z, BOX_SIZE, BOX_SIZE, BOX_SIZE, MASS, randomColor());
                objectCount++;
            }
            case S -> {
                createSphere(x, SPAWN_HEIGHT, z, SPHERE_RADIUS, MASS, randomColor());
                objectCount++;
            }
            case C -> {
                createCapsule(x, SPAWN_HEIGHT, z, CAPSULE_RADIUS, CAPSULE_LENGTH, MASS, randomColor());
                objectCount++;
            }
            default -> { }
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);
        panel.getChildren().addAll(
                new Label("Press B - Spawn Box"),
                new Label("Press S - Spawn Sphere"),
                new Label("Press C - Spawn Capsule"),
                new Label("Press SPACE - Pause/Resume")
        );
        return panel;
    }

    private Color randomColor() {
        return Color.hsb(random.nextDouble() * 360, 0.7, 0.8);
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates stacking physics objects. Press B/S/C to spawn boxes, spheres, or capsules.";
    }
}
