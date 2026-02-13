package org.dynamisfx.samples.utilities;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.hybrid.HybridBodyLink;
import org.dynamisfx.physics.hybrid.HybridOwnership;
import org.dynamisfx.physics.hybrid.HybridPhysicsCoordinator;
import org.dynamisfx.physics.hybrid.HybridSnapshot;
import org.dynamisfx.physics.hybrid.StateHandoffMode;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.dynamisfx.physics.ode4j.Ode4jBackendFactory;
import org.dynamisfx.physics.orekit.OrekitBackendFactory;
import org.dynamisfx.physics.orekit.OrekitWorld;
import org.dynamisfx.physics.step.FixedStepAccumulator;
import org.dynamisfx.physics.step.FixedStepResult;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Demonstrates a shared-timeline hybrid run with ODE4j (general) and Orekit (orbital).
 */
public class HybridPhysicsCoordinatorSample extends ShapeBaseSample<Group> {

    private final Group worldGroup = new Group();
    private PhysicsBackend generalBackend;
    private PhysicsBackend orbitalBackend;
    private PhysicsWorld generalWorld;
    private OrekitWorld orbitalWorld;
    private HybridPhysicsCoordinator coordinator;
    private FixedStepAccumulator accumulator;
    private AnimationTimer timer;
    private PhysicsBodyHandle generalBody;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1800);

        generalBackend = new Ode4jBackendFactory().createBackend();
        generalWorld = generalBackend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 120.0));

        orbitalBackend = new OrekitBackendFactory().createBackend();
        orbitalWorld = (OrekitWorld) orbitalBackend.createWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0 / 120.0));
        orbitalWorld.setTimeScale(5.0);

        coordinator = new HybridPhysicsCoordinator(generalWorld, orbitalWorld);
        accumulator = new FixedStepAccumulator(1.0 / 120.0, 8);

        createVisualsAndBodies();
        model = worldGroup;
    }

    @Override
    protected void addMeshAndListeners() {
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
                FixedStepResult stepResult = accumulator.advance(dt, coordinator::step);
                coordinator.updateRenderMetadata(
                        stepResult.interpolationAlpha(),
                        stepResult.accumulatedRemainderSeconds());

                HybridSnapshot snapshot = coordinator.latestSnapshot();
                if (snapshot != null) {
                    PhysicsBodyState state = snapshot.generalStates().get(generalBody);
                    if (state != null) {
                        satNode.setTranslateX(state.position().x() / 10_000.0);
                        satNode.setTranslateY(state.position().y() / 10_000.0);
                        satNode.setTranslateZ(state.position().z() / 10_000.0);
                    }
                }
            }
        };
        timer.start();

        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                teardown();
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        return null;
    }

    private Node satNode;

    private void createVisualsAndBodies() {
        Sphere central = new Sphere(30);
        central.setMaterial(new PhongMaterial(Color.CADETBLUE));
        worldGroup.getChildren().add(central);

        satNode = new Box(16, 16, 16);
        ((Box) satNode).setMaterial(new PhongMaterial(Color.ORANGE));
        worldGroup.getChildren().add(satNode);

        PhysicsBodyHandle generalAnchor = generalWorld.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                5.972e24,
                new BoxShape(1, 1, 1),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        generalBody = generalWorld.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1_200.0,
                new BoxShape(16, 16, 16),
                new PhysicsBodyState(
                        new PhysicsVector3(7_000_000.0, 0.0, 0.0),
                        PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(0.0, 7_500.0, 0.0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));

        PhysicsBodyHandle orbitalAnchor = orbitalWorld.createBody(new PhysicsBodyDefinition(
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
        PhysicsBodyHandle orbitalBody = orbitalWorld.createBody(new PhysicsBodyDefinition(
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

        coordinator.registerLink(new HybridBodyLink(
                generalBody,
                orbitalBody,
                HybridOwnership.ORBITAL,
                StateHandoffMode.POSITION_VELOCITY_ONLY));

        // Keep anchors alive and clearly intentional in both worlds.
        if (generalAnchor == null || orbitalAnchor == null) {
            throw new IllegalStateException("anchor creation failed");
        }
    }

    private void teardown() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        if (orbitalWorld != null) {
            orbitalWorld.close();
            orbitalWorld = null;
        }
        if (generalWorld != null) {
            generalWorld.close();
            generalWorld = null;
        }
        if (generalBackend != null) {
            generalBackend.close();
            generalBackend = null;
        }
        if (orbitalBackend != null) {
            orbitalBackend.close();
            orbitalBackend = null;
        }
    }
}
