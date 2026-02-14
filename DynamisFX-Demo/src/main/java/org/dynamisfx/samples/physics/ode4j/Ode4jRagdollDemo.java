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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a ragdoll physics simulation with articulated body parts.
 * The ragdoll uses ball joints for flexible limb connections.
 *
 * Controls:
 * SPACE - Pause/Resume
 * R - Reset ragdoll
 * F - Apply upward force (fling)
 * Click - Drop new ragdoll at random position
 */
public class Ode4jRagdollDemo extends Ode4jDemoBase {

    // Body dimensions
    private static final double HEAD_RADIUS = 20;
    private static final double TORSO_WIDTH = 40;
    private static final double TORSO_HEIGHT = 60;
    private static final double TORSO_DEPTH = 25;
    private static final double UPPER_ARM_LENGTH = 35;
    private static final double LOWER_ARM_LENGTH = 30;
    private static final double UPPER_LEG_LENGTH = 40;
    private static final double LOWER_LEG_LENGTH = 35;
    private static final double LIMB_RADIUS = 8;

    private List<PhysicsBodyHandle> ragdollParts = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        ragdollParts.clear();

        // Floor
        createFloor(-250, 600, 400, Color.DARKSLATEGRAY);

        // Some obstacles for the ragdoll to interact with
        createStaticBox(-150, -180, 0, 60, 100, 60, Color.DIMGRAY);
        createStaticBox(150, -200, 0, 80, 60, 80, Color.DIMGRAY);
        createStaticBox(0, -220, -150, 100, 40, 40, Color.DIMGRAY);

        // Create the ragdoll
        createRagdoll(0, 100, 0);
    }

    private void createRagdoll(double x, double y, double z) {
        // ====== HEAD ======
        PhysicsBodyHandle head = createSphere(x, y + 90, z, HEAD_RADIUS, 1.0, Color.PEACHPUFF);
        ragdollParts.add(head);

        // ====== TORSO ======
        PhysicsBodyHandle torso = createBox(x, y + 30, z, TORSO_WIDTH, TORSO_HEIGHT, TORSO_DEPTH, 3.0, Color.STEELBLUE);
        ragdollParts.add(torso);

        // Neck joint (ball joint connecting head to torso)
        createBallJoint(torso, head, new PhysicsVector3(x, y + 60, z));

        // ====== LEFT ARM ======
        // Upper left arm
        PhysicsBodyHandle upperLeftArm = createCapsule(
                x - TORSO_WIDTH / 2 - UPPER_ARM_LENGTH / 2, y + 50, z,
                LIMB_RADIUS, UPPER_ARM_LENGTH, 0.5, Color.PEACHPUFF);
        ragdollParts.add(upperLeftArm);
        createBallJoint(torso, upperLeftArm, new PhysicsVector3(x - TORSO_WIDTH / 2, y + 50, z));

        // Lower left arm
        PhysicsBodyHandle lowerLeftArm = createCapsule(
                x - TORSO_WIDTH / 2 - UPPER_ARM_LENGTH - LOWER_ARM_LENGTH / 2, y + 50, z,
                LIMB_RADIUS - 1, LOWER_ARM_LENGTH, 0.3, Color.PEACHPUFF);
        ragdollParts.add(lowerLeftArm);
        createBallJoint(upperLeftArm, lowerLeftArm,
                new PhysicsVector3(x - TORSO_WIDTH / 2 - UPPER_ARM_LENGTH, y + 50, z));

        // ====== RIGHT ARM ======
        // Upper right arm
        PhysicsBodyHandle upperRightArm = createCapsule(
                x + TORSO_WIDTH / 2 + UPPER_ARM_LENGTH / 2, y + 50, z,
                LIMB_RADIUS, UPPER_ARM_LENGTH, 0.5, Color.PEACHPUFF);
        ragdollParts.add(upperRightArm);
        createBallJoint(torso, upperRightArm, new PhysicsVector3(x + TORSO_WIDTH / 2, y + 50, z));

        // Lower right arm
        PhysicsBodyHandle lowerRightArm = createCapsule(
                x + TORSO_WIDTH / 2 + UPPER_ARM_LENGTH + LOWER_ARM_LENGTH / 2, y + 50, z,
                LIMB_RADIUS - 1, LOWER_ARM_LENGTH, 0.3, Color.PEACHPUFF);
        ragdollParts.add(lowerRightArm);
        createBallJoint(upperRightArm, lowerRightArm,
                new PhysicsVector3(x + TORSO_WIDTH / 2 + UPPER_ARM_LENGTH, y + 50, z));

        // ====== LEFT LEG ======
        // Upper left leg
        PhysicsBodyHandle upperLeftLeg = createCapsule(
                x - TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH / 2, z,
                LIMB_RADIUS + 2, UPPER_LEG_LENGTH, 0.8, Color.DARKBLUE);
        ragdollParts.add(upperLeftLeg);
        createBallJoint(torso, upperLeftLeg, new PhysicsVector3(x - TORSO_WIDTH / 4, y, z));

        // Lower left leg
        PhysicsBodyHandle lowerLeftLeg = createCapsule(
                x - TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH - LOWER_LEG_LENGTH / 2, z,
                LIMB_RADIUS, LOWER_LEG_LENGTH, 0.5, Color.DARKBLUE);
        ragdollParts.add(lowerLeftLeg);
        createBallJoint(upperLeftLeg, lowerLeftLeg,
                new PhysicsVector3(x - TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH, z));

        // ====== RIGHT LEG ======
        // Upper right leg
        PhysicsBodyHandle upperRightLeg = createCapsule(
                x + TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH / 2, z,
                LIMB_RADIUS + 2, UPPER_LEG_LENGTH, 0.8, Color.DARKBLUE);
        ragdollParts.add(upperRightLeg);
        createBallJoint(torso, upperRightLeg, new PhysicsVector3(x + TORSO_WIDTH / 4, y, z));

        // Lower right leg
        PhysicsBodyHandle lowerRightLeg = createCapsule(
                x + TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH - LOWER_LEG_LENGTH / 2, z,
                LIMB_RADIUS, LOWER_LEG_LENGTH, 0.5, Color.DARKBLUE);
        ragdollParts.add(lowerRightLeg);
        createBallJoint(upperRightLeg, lowerRightLeg,
                new PhysicsVector3(x + TORSO_WIDTH / 4, y - UPPER_LEG_LENGTH, z));
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case R -> resetWorld();
            case F -> flingRagdoll();
            case N -> dropNewRagdoll();
            default -> { }
        }
    }

    private void flingRagdoll() {
        // Apply upward impulse to all parts
        for (PhysicsBodyHandle part : ragdollParts) {
            applyImpulse(part, new PhysicsVector3(
                    (Math.random() - 0.5) * 100,
                    200 + Math.random() * 100,
                    (Math.random() - 0.5) * 100));
        }
    }

    private void dropNewRagdoll() {
        double x = (Math.random() - 0.5) * 200;
        double z = (Math.random() - 0.5) * 200;
        createRagdoll(x, 200, z);
    }

    @Override
    protected void resetWorld() {
        super.resetWorld();
        ragdollParts.clear();
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button flingButton = new Button("Fling (F)");
        flingButton.setOnAction(e -> flingRagdoll());

        Button newButton = new Button("New Ragdoll (N)");
        newButton.setOnAction(e -> dropNewRagdoll());

        panel.getChildren().addAll(
                new Label("Ragdoll Controls:"),
                new Label(""),
                flingButton,
                newButton,
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label("R - Reset"),
                new Label(""),
                new Label("Watch the ragdoll"),
                new Label("interact with the"),
                new Label("environment!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Articulated ragdoll with ball joints. Press F to fling, N for new ragdoll.";
    }
}
