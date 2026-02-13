package org.dynamisfx.samples.utilities;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.ode4j.Ode4jBackendFactory;
import org.dynamisfx.physics.step.FixedStepAccumulator;
import org.dynamisfx.physics.sync.PhysicsSceneSync;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Demonstrates ODE4j-backed physics with SPI scene synchronization.
 */
public class Ode4jPhysicsSyncSample extends ShapeBaseSample<Group> {

    private final Group worldGroup = new Group();
    private PhysicsBackend backend;
    private PhysicsWorld world;
    private PhysicsSceneSync<Node> sceneSync;
    private AnimationTimer timer;
    private FixedStepAccumulator accumulator;
    private boolean paused;
    private Slider iterationsSlider;
    private Slider frictionSlider;
    private Slider bounceSlider;
    private Slider cfmSlider;
    private Slider bounceVelSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1100);

        backend = new Ode4jBackendFactory().createBackend();
        world = backend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 120.0));
        sceneSync = new PhysicsSceneSync<>((node, state) -> {
            node.setTranslateX(state.position().x());
            node.setTranslateY(state.position().y());
            node.setTranslateZ(state.position().z());
        });
        accumulator = new FixedStepAccumulator(1.0 / 120.0, 8);

        createFloor();
        createDynamicBox(0, 170, 0, 70, 70, 70, Color.CORNFLOWERBLUE);
        createDynamicBox(90, 280, 0, 70, 70, 70, Color.DARKORANGE);
        createDynamicBox(-95, 390, 0, 70, 70, 70, Color.FORESTGREEN);

        model = worldGroup;
    }

    @Override
    protected void addMeshAndListeners() {
        syncControlsFromWorld();
        subScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
                event.consume();
            }
        });

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
                accumulator.advance(dt, world::step);
                sceneSync.applyFrame(world::getBodyState);
            }
        };
        timer.start();

        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                teardownPhysics();
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        root.getChildren().add(new Label("ODE4j Runtime Tuning"));

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        iterationsSlider = addSlider(grid, 0, "Iterations", 1, 100, 20);
        frictionSlider = addSlider(grid, 1, "Friction", 0, 10, 1);
        bounceSlider = addSlider(grid, 2, "Bounce", 0, 1, 0.1);
        cfmSlider = addSlider(grid, 3, "Soft CFM", 0, 0.01, 0.00001);
        bounceVelSlider = addSlider(grid, 4, "Bounce Vel", 0, 2, 0.1);

        root.getChildren().add(grid);
        installTuningListeners();
        return root;
    }

    private void createFloor() {
        Box floor = new Box(700, 40, 700);
        floor.setMaterial(new PhongMaterial(Color.DIMGRAY));
        floor.setTranslateY(-220);
        worldGroup.getChildren().add(floor);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                0.0,
                new BoxShape(700, 40, 700),
                new PhysicsBodyState(
                        new PhysicsVector3(0, -220, 0),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, floor);
    }

    private void createDynamicBox(
            double x, double y, double z,
            double sx, double sy, double sz,
            Color color) {
        Box box = new Box(sx, sy, sz);
        box.setMaterial(new PhongMaterial(color));
        worldGroup.getChildren().add(box);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(sx, sy, sz),
                new PhysicsBodyState(
                        new PhysicsVector3(x, y, z),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        sceneSync.bind(handle, box);
    }

    private void teardownPhysics() {
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

    private Slider addSlider(GridPane grid, int row, String name, double min, double max, double value) {
        Label label = new Label(name);
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(220);
        slider.setShowTickMarks(false);
        grid.add(label, 0, row);
        grid.add(slider, 1, row);
        return slider;
    }

    private void installTuningListeners() {
        if (iterationsSlider == null) {
            return;
        }
        Runnable apply = this::applyTuningFromControls;
        iterationsSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        frictionSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        bounceSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        cfmSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
        bounceVelSlider.valueProperty().addListener((obs, oldV, newV) -> apply.run());
    }

    private void syncControlsFromWorld() {
        if (world == null || iterationsSlider == null) {
            return;
        }
        PhysicsRuntimeTuning tuning = world.runtimeTuning();
        iterationsSlider.setValue(tuning.solverIterations());
        frictionSlider.setValue(Math.min(frictionSlider.getMax(), tuning.contactFriction()));
        bounceSlider.setValue(tuning.contactBounce());
        cfmSlider.setValue(tuning.contactSoftCfm());
        bounceVelSlider.setValue(tuning.contactBounceVelocity());
    }

    private void applyTuningFromControls() {
        if (world == null || iterationsSlider == null) {
            return;
        }
        int iterations = (int) Math.round(iterationsSlider.getValue());
        world.setRuntimeTuning(new PhysicsRuntimeTuning(
                Math.max(1, iterations),
                frictionSlider.getValue(),
                bounceSlider.getValue(),
                cfmSlider.getValue(),
                bounceVelSlider.getValue()));
    }
}
