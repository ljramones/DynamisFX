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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import org.dynamisfx.physics.api.ConstraintCapability;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsConstraintType;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.CapsuleShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.dynamisfx.physics.step.FixedStepAccumulator;
import org.dynamisfx.physics.sync.PhysicsSceneSync;
import org.dynamisfx.samples.shapes.ShapeBaseSample;
import org.dynamisfx.samples.utilities.RigidBodyBackendSelector;

/**
 * Abstract base class for ODE4j physics demos.
 * Provides common physics setup, animation timer, and helper methods for creating bodies and joints.
 */
public abstract class Ode4jDemoBase extends ShapeBaseSample<Group> {

    protected Group worldGroup;
    protected PhysicsBackend backend;
    protected PhysicsWorld world;
    protected PhysicsSceneSync<Node> sceneSync;
    protected FixedStepAccumulator accumulator;
    protected AnimationTimer timer;
    protected boolean paused;
    protected Map<PhysicsBodyHandle, Node> bodyNodes = new LinkedHashMap<>();

    // Control panel sliders
    protected Slider iterationsSlider;
    protected Slider gravitySlider;
    protected Slider frictionSlider;
    protected Slider bounceSlider;
    protected CheckBox pauseCheckBox;

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1200);
        worldGroup = new Group();

        backend = RigidBodyBackendSelector.createBackend();
        world = backend.createWorld(defaultConfiguration());
        sceneSync = new PhysicsSceneSync<>(this::applyState);
        accumulator = new FixedStepAccumulator(1.0 / 120.0, 8);

        setupWorld();
        model = worldGroup;
    }

    /**
     * Returns the default physics world configuration.
     * Subclasses can override to customize gravity or timestep.
     */
    /**
     * Returns the default physics world configuration.
     * Uses standard physics convention: -Y is down (gravity pulls negative Y).
     * The applyState method handles the JavaFX Y-axis flip.
     */
    protected PhysicsWorldConfiguration defaultConfiguration() {
        return new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),  // Standard physics: -Y is down
                1.0 / 120.0);
    }

    /**
     * Hook for subclasses to set up their world (bodies, joints, etc.).
     */
    protected abstract void setupWorld();

    @Override
    protected void addMeshAndListeners() {
        syncControlsFromWorld();
        setupKeyboardControls();

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
                PhysicsWorld currentWorld = world;
                FixedStepAccumulator currentAccumulator = accumulator;
                PhysicsSceneSync<Node> currentSceneSync = sceneSync;
                if (currentWorld == null || currentAccumulator == null || currentSceneSync == null) {
                    return;
                }
                currentAccumulator.advance(dt, currentWorld::step);
                currentSceneSync.applyFrame(currentWorld::getBodyState);
            }
        };
        timer.start();

        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                teardown();
            }
        });
    }

    protected void setupKeyboardControls() {
        subScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
                if (pauseCheckBox != null) {
                    pauseCheckBox.setSelected(paused);
                }
                event.consume();
            } else {
                handleKeyPressed(event.getCode());
            }
        });
    }

    /**
     * Hook for subclasses to handle additional key presses.
     */
    protected void handleKeyPressed(KeyCode code) {
        // Default: no additional handling
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        String backendId = backend != null ? backend.id() : "unavailable";
        root.getChildren().add(new Label("Physics Controls (" + backendId + ")"));

        RigidBodyBackendSelector.BackendSelection selection = RigidBodyBackendSelector.selectionSnapshot();
        if (selection.fallbackUsed()) {
            root.getChildren().add(new Label("Backend: " + selection.requested()
                    + " -> " + selection.resolved()
                    + " (fallback: " + selection.fallbackReason() + ")"));
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        iterationsSlider = addSlider(grid, 0, "Solver Iterations", 1, 50, 20);
        gravitySlider = addSlider(grid, 1, "Gravity", -20, 0, -9.81);
        frictionSlider = addSlider(grid, 2, "Friction", 0, 2, 1);
        bounceSlider = addSlider(grid, 3, "Bounce", 0, 1, 0.1);

        root.getChildren().add(grid);

        pauseCheckBox = new CheckBox("Paused (SPACE)");
        pauseCheckBox.setSelected(paused);
        pauseCheckBox.selectedProperty().addListener((obs, oldV, newV) -> paused = newV);
        root.getChildren().add(pauseCheckBox);

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetWorld());
        root.getChildren().add(resetButton);

        // Add demo-specific controls
        Node demoControls = buildDemoControlPanel();
        if (demoControls != null) {
            root.getChildren().add(demoControls);
        }

        installTuningListeners();
        return root;
    }

    /**
     * Hook for subclasses to add demo-specific controls.
     */
    protected Node buildDemoControlPanel() {
        return null;
    }

    /**
     * Resets the world by tearing down and recreating.
     */
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
        accumulator = new FixedStepAccumulator(1.0 / 120.0, 8);

        setupWorld();

        timer.start();
        syncControlsFromWorld();
    }

    protected void teardown() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        if (world != null) {
            world.close();
            world = null;
        }
        if (backend != null) {
            backend.close();
            backend = null;
        }
    }

    // --- Helper methods for creating bodies ---

    /**
     * Creates a dynamic box body with visual representation.
     */
    protected PhysicsBodyHandle createBox(double x, double y, double z,
                                          double w, double h, double d,
                                          double mass, Color color) {
        Box box = new Box(w, h, d);
        box.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                mass,
                new BoxShape(w, h, d),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
        return handle;
    }

    /**
     * Creates a dynamic box body with initial rotation.
     */
    protected PhysicsBodyHandle createBox(double x, double y, double z,
                                          double w, double h, double d,
                                          double mass, Color color,
                                          PhysicsQuaternion orientation) {
        Box box = new Box(w, h, d);
        box.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                mass,
                new BoxShape(w, h, d),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        orientation,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
        return handle;
    }

    /**
     * Creates a kinematic box body.
     */
    protected PhysicsBodyHandle createKinematicBox(double x, double y, double z,
                                                   double w, double h, double d,
                                                   Color color) {
        Box box = new Box(w, h, d);
        box.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.KINEMATIC,
                0.0,
                new BoxShape(w, h, d),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
        return handle;
    }

    /**
     * Creates a dynamic sphere body with visual representation.
     */
    protected PhysicsBodyHandle createSphere(double x, double y, double z,
                                             double radius, double mass, Color color) {
        Sphere sphere = new Sphere(radius);
        sphere.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(sphere);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                mass,
                new SphereShape(radius),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, sphere);
        bodyNodes.put(handle, sphere);
        return handle;
    }

    /**
     * Creates a dynamic capsule body with visual representation.
     * Note: JavaFX doesn't have a native capsule shape, so we use a Cylinder.
     */
    protected PhysicsBodyHandle createCapsule(double x, double y, double z,
                                              double radius, double length,
                                              double mass, Color color) {
        // Approximate capsule with cylinder (spheres would need to be added for caps)
        Cylinder capsule = new Cylinder(radius, length + 2 * radius);
        capsule.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(capsule);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                mass,
                new CapsuleShape(radius, length),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, capsule);
        bodyNodes.put(handle, capsule);
        return handle;
    }

    /**
     * Creates a static floor.
     */
    protected PhysicsBodyHandle createFloor(double y, double width, double depth, Color color) {
        Box floor = new Box(width, 40, depth);
        floor.setMaterial(new PhongMaterial(color));
        floor.setTranslateY(y);
        worldGroup.getChildren().add(floor);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                0.0,
                new BoxShape(width, 40, depth),
                new PhysicsBodyState(
                        new PhysicsVector3(0, y, 0),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, floor);
        bodyNodes.put(handle, floor);
        return handle;
    }

    /**
     * Creates a static box (obstacle, wall, etc.).
     */
    protected PhysicsBodyHandle createStaticBox(double x, double y, double z,
                                                double w, double h, double d,
                                                Color color) {
        Box box = new Box(w, h, d);
        box.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                0.0,
                new BoxShape(w, h, d),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
        return handle;
    }

    // --- Helper methods for creating joints ---

    /**
     * Creates a ball joint between two bodies.
     */
    protected PhysicsConstraintHandle createBallJoint(PhysicsBodyHandle bodyA,
                                                      PhysicsBodyHandle bodyB,
                                                      PhysicsVector3 anchorWorld) {
        return world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.BALL,
                bodyA,
                bodyB,
                anchorWorld));
    }

    /**
     * Creates a hinge joint between two bodies.
     */
    protected PhysicsConstraintHandle createHingeJoint(PhysicsBodyHandle bodyA,
                                                       PhysicsBodyHandle bodyB,
                                                       PhysicsVector3 anchor,
                                                       PhysicsVector3 axis,
                                                       Double loStop,
                                                       Double hiStop) {
        return world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.HINGE,
                bodyA,
                bodyB,
                anchor,
                axis,
                loStop,
                hiStop));
    }

    /**
     * Creates a slider joint between two bodies.
     */
    protected PhysicsConstraintHandle createSliderJoint(PhysicsBodyHandle bodyA,
                                                        PhysicsBodyHandle bodyB,
                                                        PhysicsVector3 axis,
                                                        Double loStop,
                                                        Double hiStop) {
        return world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.SLIDER,
                bodyA,
                bodyB,
                PhysicsVector3.ZERO,
                axis,
                loStop,
                hiStop));
    }

    /**
     * Creates a fixed joint between two bodies.
     */
    protected PhysicsConstraintHandle createFixedJoint(PhysicsBodyHandle bodyA,
                                                       PhysicsBodyHandle bodyB) {
        return world.createConstraint(new PhysicsConstraintDefinition(
                PhysicsConstraintType.FIXED,
                bodyA,
                bodyB,
                PhysicsVector3.ZERO));
    }

    // --- Body state manipulation ---

    /**
     * Applies an impulse to a body by modifying its linear velocity.
     */
    protected void applyImpulse(PhysicsBodyHandle handle, PhysicsVector3 impulse) {
        PhysicsBodyState state = world.getBodyState(handle);
        if (state != null) {
            PhysicsVector3 newVel = new PhysicsVector3(
                    state.linearVelocity().x() + impulse.x(),
                    state.linearVelocity().y() + impulse.y(),
                    state.linearVelocity().z() + impulse.z());
            world.setBodyState(handle, new PhysicsBodyState(
                    state.position(),
                    state.orientation(),
                    newVel,
                    state.angularVelocity(),
                    state.referenceFrame(),
                    state.timestampSeconds()));
        }
    }

    /**
     * Sets the angular velocity of a body.
     */
    protected void setAngularVelocity(PhysicsBodyHandle handle, PhysicsVector3 angularVel) {
        PhysicsBodyState state = world.getBodyState(handle);
        if (state != null) {
            world.setBodyState(handle, new PhysicsBodyState(
                    state.position(),
                    state.orientation(),
                    state.linearVelocity(),
                    angularVel,
                    state.referenceFrame(),
                    state.timestampSeconds()));
        }
    }

    /**
     * Updates the position of a kinematic body.
     */
    protected void setKinematicPosition(PhysicsBodyHandle handle, PhysicsVector3 position) {
        PhysicsBodyState state = world.getBodyState(handle);
        if (state != null) {
            world.setBodyState(handle, new PhysicsBodyState(
                    position,
                    state.orientation(),
                    state.linearVelocity(),
                    state.angularVelocity(),
                    state.referenceFrame(),
                    state.timestampSeconds()));
        }
    }

    // --- Motor control for joints ---

    /**
     * Sets the motor velocity for a hinge or slider joint.
     * For hinge joints, velocity is in radians/second.
     * For slider joints, velocity is in meters/second.
     */
    protected void setMotorVelocity(PhysicsConstraintHandle handle, double velocity) {
        Optional<ConstraintCapability> capability = world.constraintCapability();
        if (capability.isPresent()) {
            capability.get().setMotorVelocity(handle, velocity);
        }
    }

    /**
     * Sets the maximum motor force/torque for a hinge or slider joint.
     * For hinge joints, this is maximum torque in Newton-meters.
     * For slider joints, this is maximum force in Newtons.
     */
    protected void setMotorMaxForce(PhysicsConstraintHandle handle, double maxForce) {
        Optional<ConstraintCapability> capability = world.constraintCapability();
        if (capability.isPresent()) {
            capability.get().setMotorMaxForce(handle, maxForce);
        }
    }

    /**
     * Enables a motor on a joint with specified velocity and max force.
     */
    protected void enableMotor(PhysicsConstraintHandle handle, double velocity, double maxForce) {
        setMotorVelocity(handle, velocity);
        setMotorMaxForce(handle, maxForce);
    }

    /**
     * Disables the motor on a joint by setting max force to 0.
     */
    protected void disableMotor(PhysicsConstraintHandle handle) {
        setMotorMaxForce(handle, 0);
    }

    /**
     * Creates a hinge joint with a motor already configured.
     */
    protected PhysicsConstraintHandle createMotorizedHinge(PhysicsBodyHandle bodyA,
                                                           PhysicsBodyHandle bodyB,
                                                           PhysicsVector3 anchor,
                                                           PhysicsVector3 axis,
                                                           double motorVelocity,
                                                           double motorMaxForce) {
        PhysicsConstraintHandle handle = createHingeJoint(bodyA, bodyB, anchor, axis, null, null);
        enableMotor(handle, motorVelocity, motorMaxForce);
        return handle;
    }

    // --- State application to JavaFX nodes ---

    /**
     * Applies physics body state to a JavaFX node.
     * Note: Y is negated to convert from physics coords (Y-up) to JavaFX (Y-down).
     */
    protected void applyState(Node node, PhysicsBodyState state) {
        node.setTranslateX(state.position().x());
        node.setTranslateY(-state.position().y());  // Negate Y for JavaFX
        node.setTranslateZ(state.position().z());

        // Apply quaternion rotation (also flip Y component for correct orientation)
        PhysicsQuaternion q = state.orientation();
        if (q.x() != 0 || q.y() != 0 || q.z() != 0 || q.w() != 1) {
            double angle = 2.0 * Math.acos(q.w());
            double s = Math.sqrt(1.0 - q.w() * q.w());
            double ax, ay, az;
            if (s < 0.001) {
                ax = q.x();
                ay = -q.y();  // Negate Y axis
                az = q.z();
            } else {
                ax = q.x() / s;
                ay = -q.y() / s;  // Negate Y axis
                az = q.z() / s;
            }
            node.setRotationAxis(new Point3D(ax, ay, az));
            node.setRotate(Math.toDegrees(angle));
        }
    }

    // --- Control panel utilities ---

    protected Slider addSlider(GridPane grid, int row, String name, double min, double max, double value) {
        Label label = new Label(name);
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(180);
        slider.setShowTickMarks(false);
        grid.add(label, 0, row);
        grid.add(slider, 1, row);
        return slider;
    }

    protected void installTuningListeners() {
        if (iterationsSlider == null) {
            return;
        }
        Runnable apply = this::applyTuningFromControls;
        iterationsSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        gravitySlider.valueProperty().addListener((obs, oldV, newV) -> applyGravity());
        frictionSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        bounceSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
    }

    protected void syncControlsFromWorld() {
        if (world == null || iterationsSlider == null) {
            return;
        }
        PhysicsRuntimeTuning tuning = world.runtimeTuning();
        iterationsSlider.setValue(tuning.solverIterations());
        frictionSlider.setValue(Math.min(frictionSlider.getMax(), tuning.contactFriction()));
        bounceSlider.setValue(tuning.contactBounce());
        PhysicsVector3 g = world.gravity();
        gravitySlider.setValue(g.y());
    }

    protected void applyTuningFromControls() {
        if (world == null || iterationsSlider == null) {
            return;
        }
        PhysicsRuntimeTuning current = world.runtimeTuning();
        int iterations = (int) Math.round(iterationsSlider.getValue());
        world.setRuntimeTuning(new PhysicsRuntimeTuning(
                Math.max(1, iterations),
                frictionSlider.getValue(),
                bounceSlider.getValue(),
                current.contactSoftCfm(),
                current.contactBounceVelocity()));
    }

    protected void applyGravity() {
        if (world == null || gravitySlider == null) {
            return;
        }
        world.setGravity(new PhysicsVector3(0, gravitySlider.getValue(), 0));
    }

    // --- Utility methods ---

    /**
     * Creates a quaternion from axis-angle representation.
     */
    protected PhysicsQuaternion quaternionFromAxisAngle(double ax, double ay, double az, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg) / 2.0;
        double s = Math.sin(angleRad);
        double c = Math.cos(angleRad);
        double len = Math.sqrt(ax * ax + ay * ay + az * az);
        if (len > 0) {
            ax /= len;
            ay /= len;
            az /= len;
        }
        return new PhysicsQuaternion(ax * s, ay * s, az * s, c);
    }
}
