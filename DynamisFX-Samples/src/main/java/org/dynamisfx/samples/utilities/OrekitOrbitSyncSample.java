package org.dynamisfx.samples.utilities;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.dynamisfx.physics.orekit.OrekitBackendFactory;
import org.dynamisfx.physics.orekit.OrekitWorld;
import org.dynamisfx.physics.step.FixedStepAccumulator;
import org.dynamisfx.physics.sync.PhysicsSceneSync;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Demonstrates the phase-3 Orekit-oriented backend scaffold with orbital motion.
 */
public class OrekitOrbitSyncSample extends ShapeBaseSample<Group> {

    private final Group worldGroup = new Group();
    private PhysicsBackend backend;
    private PhysicsWorld world;
    private PhysicsSceneSync<Node> sceneSync;
    private FixedStepAccumulator accumulator;
    private AnimationTimer timer;
    private OrekitWorld orekitWorld;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-2000);

        backend = new OrekitBackendFactory().createBackend();
        world = backend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0 / 60.0));
        orekitWorld = (OrekitWorld) world;
        sceneSync = new PhysicsSceneSync<>((node, state) -> {
            node.setTranslateX(state.position().x() / 10_000.0);
            node.setTranslateY(state.position().y() / 10_000.0);
            node.setTranslateZ(state.position().z() / 10_000.0);
        });
        accumulator = new FixedStepAccumulator(1.0 / 120.0, 8);

        createCentralBody();
        createOrbiter();
        model = worldGroup;
    }

    @Override
    protected void addMeshAndListeners() {
        subScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.OPEN_BRACKET) {
                orekitWorld.setTimeScale(Math.max(0.1, orekitWorld.timeScale() * 0.5));
                event.consume();
            } else if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                orekitWorld.setTimeScale(Math.min(100.0, orekitWorld.timeScale() * 2.0));
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
        return null;
    }

    private void createCentralBody() {
        Sphere planet = new Sphere(25);
        planet.setMaterial(new PhongMaterial(Color.CADETBLUE));
        worldGroup.getChildren().add(planet);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                5.972e24,
                new SphereShape(6_371_000.0),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.ICRF,
                        0.0)));
        sceneSync.bind(handle, planet);
    }

    private void createOrbiter() {
        Sphere sat = new Sphere(8);
        sat.setMaterial(new PhongMaterial(Color.ORANGE));
        worldGroup.getChildren().add(sat);

        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1_200.0,
                new SphereShape(2.0),
                new PhysicsBodyState(
                        new PhysicsVector3(7_000_000.0, 0.0, 0.0),
                        PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(0.0, 7_500.0, 0.0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.ICRF,
                        0.0)));
        sceneSync.bind(handle, sat);
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
}
