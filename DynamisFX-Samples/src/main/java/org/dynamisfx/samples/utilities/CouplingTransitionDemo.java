package org.dynamisfx.samples.utilities;

import java.util.List;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationClock;
import org.dynamisfx.simulation.SimulationTransformBridge;
import org.dynamisfx.simulation.TransformStore;
import org.dynamisfx.simulation.coupling.CouplingDecisionReason;
import org.dynamisfx.simulation.coupling.CouplingTelemetryEvent;
import org.dynamisfx.simulation.coupling.DefaultCouplingManager;
import org.dynamisfx.simulation.coupling.MutableCouplingObservationProvider;
import org.dynamisfx.simulation.coupling.Phase1CouplingBootstrap;
import org.dynamisfx.simulation.coupling.PhysicsZone;
import org.dynamisfx.simulation.coupling.ZoneId;
import org.dynamisfx.simulation.entity.SimulationEntityRegistry;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.orbital.ScriptedOrbitalDynamicsEngine;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.dynamisfx.simulation.runtime.SimulationOrchestrator;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Phase-1 sample demonstrating coupling mode transitions using threshold policy scaffolding.
 */
public class CouplingTransitionDemo extends ShapeBaseSample<Group> {

    private static final Logger LOG = Logger.getLogger(CouplingTransitionDemo.class.getName());
    private static final String OBJECT_ID = "lander-1";

    private final Group worldGroup = new Group();
    private final MutableCouplingObservationProvider observationProvider = new MutableCouplingObservationProvider();
    private final DefaultCouplingManager couplingManager =
            Phase1CouplingBootstrap.createDefaultManager(observationProvider);
    private final SimulationClock clock = new SimulationClock(0.0, 1.0, false);
    private final SimulationEntityRegistry<Node> entityRegistry = new SimulationEntityRegistry<>();
    private final TransformStore transformStore = new TransformStore(1);
    private final SimulationTransformBridge transformBridge =
            new SimulationTransformBridge(entityRegistry, transformStore);
    private final ScriptedOrbitalDynamicsEngine orbitalEngine = new ScriptedOrbitalDynamicsEngine();
    private SimulationOrchestrator orchestrator;

    private final Box lander = new Box(80, 40, 80);
    private AnimationTimer timer;
    private ObjectSimulationMode lastMode = ObjectSimulationMode.ORBITAL_ONLY;
    private CouplingTelemetryEvent latestTelemetry;

    private Slider distanceSlider;
    private CheckBox contactCheck;
    private CheckBox autoScenarioCheck;
    private Label modeLabel;
    private Label distanceLabel;
    private Label telemetryLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-900);

        lander.setMaterial(materialForMode(lastMode));
        worldGroup.getChildren().add(lander);
        model = worldGroup;
        entityRegistry.register(OBJECT_ID, lander);
        orbitalEngine.setTrajectory(OBJECT_ID, (time, frame) -> new OrbitalState(
                new PhysicsVector3(scenarioOrbitalDistance(time), 0.0, 0.0),
                PhysicsVector3.ZERO,
                org.dynamisfx.physics.model.PhysicsQuaternion.IDENTITY,
                frame,
                time));

        couplingManager.registerZone(new DemoZone());
        couplingManager.setMode(OBJECT_ID, lastMode);
        couplingManager.addTelemetryListener(this::onTelemetry);
        orchestrator = new SimulationOrchestrator(
                clock,
                orbitalEngine,
                couplingManager,
                dt -> {
                    // Phase-1 demo has no active rigid-body backend stepping yet.
                },
                transformBridge,
                () -> List.of(OBJECT_ID),
                ReferenceFrame.WORLD);
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

                double simulationTime = clock.simulationTimeSeconds() + dt;
                applyScenario(simulationTime);
                simulationTime = orchestrator.tick(dt);
                updateVisualState(simulationTime);
            }
        };
        timer.start();

        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && timer != null) {
                timer.stop();
                timer = null;
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));

        modeLabel = new Label("Mode: " + lastMode);
        distanceLabel = new Label("Distance: 2000 m");
        telemetryLabel = new Label("Telemetry: waiting");

        distanceSlider = new Slider(0, 3000, 2000);
        distanceSlider.valueProperty().addListener((obs, oldValue, newValue) -> updateDistanceLabel(newValue.doubleValue()));

        contactCheck = new CheckBox("Active Contact");
        autoScenarioCheck = new CheckBox("Auto Scenario");
        autoScenarioCheck.setSelected(true);

        root.getChildren().addAll(
                new Label("Coupling Transition Demo"),
                modeLabel,
                distanceLabel,
                telemetryLabel,
                new Label("Distance To Zone (m)"),
                distanceSlider,
                contactCheck,
                autoScenarioCheck);
        return root;
    }

    private void applyScenario(double simulationTimeSeconds) {
        if (autoScenarioCheck != null && autoScenarioCheck.isSelected()) {
            OrbitalState state = orbitalEngine.propagateTo(
                    List.of(OBJECT_ID),
                    simulationTimeSeconds,
                    ReferenceFrame.WORLD).get(OBJECT_ID);
            boolean contact = simulationTimeSeconds >= 6.0 && simulationTimeSeconds < 7.5;
            Double predictedIntercept = simulationTimeSeconds < 3.0
                    ? 3.0 - simulationTimeSeconds
                    : null;
            setObservationState(Math.abs(state.position().x()), contact, predictedIntercept);
            return;
        }

        if (distanceSlider != null && contactCheck != null) {
            setObservationState(distanceSlider.getValue(), contactCheck.isSelected(), null);
        }
    }

    private void setObservationState(double distanceMeters, boolean activeContact, Double predictedInterceptSeconds) {
        observationProvider.setDistanceMeters(OBJECT_ID, distanceMeters);
        observationProvider.setActiveContact(OBJECT_ID, activeContact);
        if (predictedInterceptSeconds == null) {
            observationProvider.clearPredictedIntercept(OBJECT_ID);
        } else {
            observationProvider.setPredictedInterceptSeconds(OBJECT_ID, predictedInterceptSeconds);
        }

        if (distanceSlider != null && Math.abs(distanceSlider.getValue() - distanceMeters) > 1e-6) {
            distanceSlider.setValue(distanceMeters);
        }
        if (contactCheck != null && contactCheck.isSelected() != activeContact) {
            contactCheck.setSelected(activeContact);
        }
        updateDistanceLabel(distanceMeters);
    }

    private void updateVisualState(double simulationTimeSeconds) {
        ObjectSimulationMode currentMode = couplingManager.modeFor(OBJECT_ID).orElse(ObjectSimulationMode.ORBITAL_ONLY);
        if (currentMode != lastMode) {
            LOG.info(() -> String.format(
                    "Coupling mode transition at t=%.2fs: %s -> %s",
                    simulationTimeSeconds,
                    lastMode,
                    currentMode));
            lastMode = currentMode;
        }

        lander.setMaterial(materialForMode(currentMode));
        if (modeLabel != null) {
            modeLabel.setText("Mode: " + currentMode);
        }
        entityRegistry.get(OBJECT_ID).ifPresent(entity -> entity.setTranslateX(modeOffset(currentMode)));
        entityRegistry.get(OBJECT_ID).ifPresent(entity ->
                entityRegistry.indexOf(OBJECT_ID).ifPresent(index -> {
                    TransformStore.TransformSample sample = transformStore.sample(index);
                    entity.setTranslateZ(sample.posX() * 0.15);
                }));
        if (telemetryLabel != null && latestTelemetry != null) {
            telemetryLabel.setText(formatTelemetry(latestTelemetry));
        }
    }

    private static PhongMaterial materialForMode(ObjectSimulationMode mode) {
        return switch (mode) {
            case ORBITAL_ONLY -> new PhongMaterial(Color.DODGERBLUE);
            case PHYSICS_ACTIVE -> new PhongMaterial(Color.ORANGE);
            case KINEMATIC_DRIVEN -> new PhongMaterial(Color.MEDIUMSEAGREEN);
        };
    }

    private static double modeOffset(ObjectSimulationMode mode) {
        return switch (mode) {
            case ORBITAL_ONLY -> -180.0;
            case PHYSICS_ACTIVE -> 0.0;
            case KINEMATIC_DRIVEN -> 180.0;
        };
    }

    private void updateDistanceLabel(double distanceMeters) {
        if (distanceLabel != null) {
            distanceLabel.setText(String.format("Distance: %.0f m", distanceMeters));
        }
    }

    private void onTelemetry(CouplingTelemetryEvent event) {
        if (OBJECT_ID.equals(event.objectId())) {
            latestTelemetry = event;
            if (event.transitioned()) {
                LOG.info(() -> formatTelemetry(event));
            }
        }
    }

    private static String formatTelemetry(CouplingTelemetryEvent event) {
        String action = event.transitioned() ? "transition" : "hold";
        CouplingDecisionReason reason = event.reason();
        return String.format(
                "Telemetry: t=%.2f %s %s -> %s (%s)",
                event.simulationTimeSeconds(),
                action,
                event.fromMode(),
                event.toMode(),
                reason);
    }

    private static double scenarioOrbitalDistance(double simulationTimeSeconds) {
        if (simulationTimeSeconds < 3.0) {
            return 2000.0;
        }
        if (simulationTimeSeconds < 6.0) {
            double alpha = (simulationTimeSeconds - 3.0) / 3.0;
            return 2000.0 + (300.0 - 2000.0) * alpha;
        }
        if (simulationTimeSeconds < 8.0) {
            double alpha = (simulationTimeSeconds - 6.0) / 2.0;
            return 300.0 + (2200.0 - 300.0) * alpha;
        }
        return 2200.0;
    }

    private static final class DemoZone implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId("demo-zone");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public PhysicsVector3 anchorPosition() {
            return PhysicsVector3.ZERO;
        }

        @Override
        public double radiusMeters() {
            return 2_000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
