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
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Conveyor belt system using kinematic platforms.
 * Objects are pushed along by moving belt segments.
 *
 * Controls:
 * D - Drop object
 * +/- - Adjust belt speed
 * R - Reverse belt direction
 */
public class Ode4jConveyorDemo extends Ode4jDemoBase {

    private static final int BELT_SEGMENTS = 8;
    private static final double SEGMENT_WIDTH = 50;
    private static final double SEGMENT_HEIGHT = 15;
    private static final double SEGMENT_DEPTH = 80;
    private static final double BELT_LENGTH = BELT_SEGMENTS * SEGMENT_WIDTH;

    private List<PhysicsBodyHandle> beltSegments = new ArrayList<>();
    private double beltSpeed = 30.0;  // Units per second
    private double beltOffset = 0;
    private boolean beltRunning = true;

    private Slider speedSlider;
    private AnimationTimer conveyorTimer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        beltSegments.clear();
        beltOffset = 0;

        // Ground (below conveyor)
        createFloor(-300, 600, 400, Color.DARKSLATEGRAY);

        // ====== CONVEYOR BELT ======
        // Create belt segments (kinematic bodies that we move manually)
        for (int i = 0; i < BELT_SEGMENTS; i++) {
            double x = -BELT_LENGTH / 2 + SEGMENT_WIDTH / 2 + i * SEGMENT_WIDTH;
            Color color = (i % 2 == 0) ? Color.DIMGRAY : Color.SLATEGRAY;
            PhysicsBodyHandle segment = createKinematicBox(x, -150, 0,
                    SEGMENT_WIDTH - 2, SEGMENT_HEIGHT, SEGMENT_DEPTH, color);
            beltSegments.add(segment);
        }

        // Side rails
        createStaticBox(-BELT_LENGTH / 2 - 20, -130, 0, 20, 50, SEGMENT_DEPTH, Color.SADDLEBROWN);
        createStaticBox(BELT_LENGTH / 2 + 20, -130, 0, 20, 50, SEGMENT_DEPTH, Color.SADDLEBROWN);

        // Pusher rollers (kinematic cylinders approximated with boxes)
        createStaticBox(-BELT_LENGTH / 2 - 10, -120, -SEGMENT_DEPTH / 2 - 10, 30, 30, 20, Color.DARKGRAY);
        createStaticBox(-BELT_LENGTH / 2 - 10, -120, SEGMENT_DEPTH / 2 + 10, 30, 30, 20, Color.DARKGRAY);
        createStaticBox(BELT_LENGTH / 2 + 10, -120, -SEGMENT_DEPTH / 2 - 10, 30, 30, 20, Color.DARKGRAY);
        createStaticBox(BELT_LENGTH / 2 + 10, -120, SEGMENT_DEPTH / 2 + 10, 30, 30, 20, Color.DARKGRAY);

        // Collection bin at end
        createStaticBox(BELT_LENGTH / 2 + 80, -200, 0, 80, 20, 100, Color.FORESTGREEN);
        createStaticBox(BELT_LENGTH / 2 + 80, -170, -55, 80, 60, 10, Color.FORESTGREEN);
        createStaticBox(BELT_LENGTH / 2 + 80, -170, 55, 80, 60, 10, Color.FORESTGREEN);
        createStaticBox(BELT_LENGTH / 2 + 125, -170, 0, 10, 60, 100, Color.FORESTGREEN);

        // Start conveyor animation
        startConveyorAnimation();

        // Drop initial objects
        dropObject();
        dropObject();
    }

    private void startConveyorAnimation() {
        // Note: This is in addition to the physics timer
        // We update kinematic belt positions each frame
    }

    @Override
    protected void addMeshAndListeners() {
        super.addMeshAndListeners();

        // Override the timer to also update conveyor belt
        AnimationTimer originalTimer = timer;
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

                // Update physics
                if (world != null && accumulator != null && sceneSync != null) {
                    accumulator.advance(dt, world::step);
                    sceneSync.applyFrame(world::getBodyState);
                }

                // Update conveyor belt
                if (beltRunning) {
                    updateConveyorBelt(dt);
                }
            }
        };
        timer.start();
    }

    private void updateConveyorBelt(double dt) {
        beltOffset += beltSpeed * dt;

        // Wrap offset
        if (Math.abs(beltOffset) > SEGMENT_WIDTH) {
            beltOffset = beltOffset % SEGMENT_WIDTH;
        }

        // Update each segment position
        for (int i = 0; i < beltSegments.size(); i++) {
            PhysicsBodyHandle segment = beltSegments.get(i);
            double baseX = -BELT_LENGTH / 2 + SEGMENT_WIDTH / 2 + i * SEGMENT_WIDTH;
            double newX = baseX + beltOffset;

            // Wrap segments that go off the end
            if (newX > BELT_LENGTH / 2 + SEGMENT_WIDTH / 2) {
                newX -= BELT_LENGTH;
            } else if (newX < -BELT_LENGTH / 2 - SEGMENT_WIDTH / 2) {
                newX += BELT_LENGTH;
            }

            setKinematicPosition(segment, new PhysicsVector3(newX, -150, 0));
        }
    }

    private void dropObject() {
        double x = -BELT_LENGTH / 2 + 50 + (Math.random() - 0.5) * 60;
        double z = (Math.random() - 0.5) * 40;
        Color color = Color.hsb(Math.random() * 360, 0.7, 0.8);

        double rand = Math.random();
        if (rand < 0.4) {
            createBox(x, -50, z, 25, 25, 25, 1.0, color);
        } else if (rand < 0.7) {
            createSphere(x, -50, z, 15, 0.8, color);
        } else {
            createBox(x, -50, z, 35, 20, 20, 1.2, color);
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case D -> dropObject();
            case R -> reverseBelt();
            case EQUALS, PLUS -> adjustSpeed(10);
            case MINUS -> adjustSpeed(-10);
            case S -> toggleBelt();
            default -> { }
        }
    }

    private void reverseBelt() {
        beltSpeed = -beltSpeed;
        if (speedSlider != null) {
            speedSlider.setValue(beltSpeed);
        }
    }

    private void adjustSpeed(double delta) {
        beltSpeed = Math.max(-100, Math.min(100, beltSpeed + delta));
        if (speedSlider != null) {
            speedSlider.setValue(beltSpeed);
        }
    }

    private void toggleBelt() {
        beltRunning = !beltRunning;
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button dropBtn = new Button("Drop Object (D)");
        dropBtn.setOnAction(e -> dropObject());

        Button reverseBtn = new Button("Reverse (R)");
        reverseBtn.setOnAction(e -> reverseBelt());

        Button toggleBtn = new Button("Stop/Start (S)");
        toggleBtn.setOnAction(e -> toggleBelt());

        Label speedLabel = new Label("Belt Speed:");
        speedSlider = new Slider(-100, 100, beltSpeed);
        speedSlider.setPrefWidth(120);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(50);
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            beltSpeed = newV.doubleValue();
        });

        panel.getChildren().addAll(
                new Label("Conveyor Controls:"),
                new Label(""),
                dropBtn,
                reverseBtn,
                toggleBtn,
                new Label(""),
                speedLabel,
                speedSlider,
                new Label(""),
                new Label("Objects move along"),
                new Label("the conveyor belt"),
                new Label("into the bin!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Conveyor belt system moving objects. Adjust speed with slider.";
    }
}
