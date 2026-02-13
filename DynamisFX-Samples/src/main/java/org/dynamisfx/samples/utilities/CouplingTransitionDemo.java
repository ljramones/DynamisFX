package org.dynamisfx.samples.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationClock;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.SimulationTransformBridge;
import org.dynamisfx.simulation.TransformStore;
import org.dynamisfx.simulation.coupling.CouplingBodyDefinitionProvider;
import org.dynamisfx.simulation.coupling.CouplingDecisionReason;
import org.dynamisfx.simulation.coupling.CouplingModeTransitionEvent;
import org.dynamisfx.simulation.coupling.CouplingStateReconciler;
import org.dynamisfx.simulation.coupling.CouplingTelemetryEvent;
import org.dynamisfx.simulation.coupling.CouplingTransitionApplier;
import org.dynamisfx.simulation.coupling.DeterministicZoneSelector;
import org.dynamisfx.simulation.coupling.DockingConstraintController;
import org.dynamisfx.simulation.coupling.DockingConstraintEvent;
import org.dynamisfx.simulation.coupling.DefaultCouplingManager;
import org.dynamisfx.simulation.coupling.KinematicCouplingObservationProvider;
import org.dynamisfx.simulation.coupling.MutablePhysicsZone;
import org.dynamisfx.simulation.coupling.MutableCouplingObservationProvider;
import org.dynamisfx.simulation.coupling.SphericalTangentFrame;
import org.dynamisfx.simulation.coupling.SphericalTangentFrameBuilder;
import org.dynamisfx.simulation.coupling.SimulationStateReconcilerFactory;
import org.dynamisfx.simulation.coupling.StateHandoffDiagnostics;
import org.dynamisfx.simulation.coupling.StateHandoffSnapshot;
import org.dynamisfx.simulation.coupling.TerrainPatchSpawner;
import org.dynamisfx.simulation.coupling.TerrainPatchSpawnResult;
import org.dynamisfx.simulation.coupling.TerrainPatchSpec;
import org.dynamisfx.simulation.coupling.ThresholdTransitionPolicy;
import org.dynamisfx.simulation.coupling.ZoneGravityProjection;
import org.dynamisfx.simulation.coupling.ZoneBodyRegistry;
import org.dynamisfx.simulation.coupling.ZoneId;
import org.dynamisfx.simulation.entity.SimulationEntityRegistry;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.orbital.ScriptedOrbitalDynamicsEngine;
import org.dynamisfx.simulation.runtime.SimulationOrchestrator;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * End-to-end sample for Physics Zone promote/demote lifecycle.
 * Set {@code -Ddynamisfx.samples.physics.backend=ode4j|jolt} to choose rigid backend.
 */
public class CouplingTransitionDemo extends ShapeBaseSample<Group> {

    private static final Logger LOG = Logger.getLogger(CouplingTransitionDemo.class.getName());
    private static final String OBJECT_ID = "lander-1";
    private static final int DEFAULT_HANDOFF_HISTORY_LIMIT = 10;
    private static final int MIN_HANDOFF_HISTORY_LIMIT = 1;
    private static final int MAX_HANDOFF_HISTORY_LIMIT = 100;
    private static final double LANDER_WIDTH = 80.0;
    private static final double LANDER_HEIGHT = 40.0;
    private static final double LANDER_DEPTH = 80.0;
    private static final double DOCKING_TARGET_SIZE = 60.0;
    private static final double DEMO_PLANET_RADIUS_METERS = 1_000.0;
    private static final double SURFACE_PROMOTE_ALTITUDE_METERS = 400.0;
    private static final double SURFACE_DEMOTE_ALTITUDE_METERS = 900.0;
    private static final int TIMELINE_LIMIT = 8;
    private static final Preferences PREFS = Preferences.userNodeForPackage(CouplingTransitionDemo.class);
    private static final String PREF_HANDOFF_DIAGNOSTICS = "coupling.handoffDiagnosticsEnabled";
    private static final String PREF_FREEZE_SELECTION = "coupling.handoffFreezeSelection";
    private static final String PREF_HISTORY_LIMIT = "coupling.handoffHistoryLimit";

    private final Group worldGroup = new Group();
    private final SimulationStateBuffers stateBuffers = new SimulationStateBuffers();
    private final MutableCouplingObservationProvider manualObservationProvider = new MutableCouplingObservationProvider();
    private final KinematicCouplingObservationProvider observationProvider = new KinematicCouplingObservationProvider(
            stateBuffers.rigid()::get,
            stateBuffers.orbital()::get,
            manualObservationProvider);
    private final DefaultCouplingManager couplingManager =
            new DefaultCouplingManager(
                    new ThresholdTransitionPolicy(
                            observationProvider,
                            1_000.0,
                            1_500.0,
                            1.0,
                            2.0,
                            SURFACE_PROMOTE_ALTITUDE_METERS,
                            SURFACE_DEMOTE_ALTITUDE_METERS),
                    observationProvider);
    private final SimulationClock clock = new SimulationClock(0.0, 1.0, false);
    private final SimulationEntityRegistry<Node> entityRegistry = new SimulationEntityRegistry<>();
    private final TransformStore transformStore = new TransformStore(1);
    private final SimulationTransformBridge transformBridge =
            new SimulationTransformBridge(entityRegistry, transformStore);
    private final ZoneBodyRegistry zoneBodyRegistry = new ZoneBodyRegistry();
    private final ScriptedOrbitalDynamicsEngine orbitalEngine = new ScriptedOrbitalDynamicsEngine();
    private final List<String> interactionEvents = new ArrayList<>();
    private final DockingConstraintController dockingController = new DockingConstraintController(
            zoneBodyRegistry,
            this::onDockingEvent);
    private CouplingTransitionApplier transitionApplier;
    private CouplingStateReconciler stateReconciler;
    private SimulationOrchestrator orchestrator;
    private MutablePhysicsZone demoZone;
    private PhysicsBodyHandle dockingTargetHandle;
    private boolean dockingLatched;

    private final Box lander = new Box(LANDER_WIDTH, LANDER_HEIGHT, LANDER_DEPTH);
    private final Box dockingTarget = new Box(DOCKING_TARGET_SIZE, DOCKING_TARGET_SIZE, DOCKING_TARGET_SIZE);
    private AnimationTimer timer;
    private ObjectSimulationMode lastMode = ObjectSimulationMode.ORBITAL_ONLY;
    private CouplingTelemetryEvent latestTelemetry;
    private StateHandoffSnapshot latestHandoff;
    private final List<StateHandoffSnapshot> handoffHistory = new ArrayList<>();

    private Slider distanceSlider;
    private CheckBox contactCheck;
    private CheckBox autoScenarioCheck;
    private CheckBox handoffDiagnosticsCheck;
    private CheckBox freezeHandoffSelectionCheck;
    private Label modeLabel;
    private Label distanceLabel;
    private Label decisionLabel;
    private Label zoneLabel;
    private Label predictionLabel;
    private Label telemetryLabel;
    private Label timelineLabel;
    private Label dockingLabel;
    private Label interactionLabel;
    private Label handoffDirectionLabel;
    private Label handoffZoneLabel;
    private Label handoffGlobalLabel;
    private Label handoffLocalLabel;
    private ComboBox<String> handoffHistoryBox;
    private Spinner<Integer> handoffHistoryLimitSpinner;
    private Button copyHandoffButton;
    private Button copyHandoffJsonButton;
    private Button exportHistoryJsonButton;
    private Button clearHandoffHistoryButton;
    private Button resetHandoffDefaultsButton;
    private boolean handoffDiagnosticsEnabled = PREFS.getBoolean(PREF_HANDOFF_DIAGNOSTICS, true);
    private boolean freezeHandoffSelection = PREFS.getBoolean(PREF_FREEZE_SELECTION, false);
    private int handoffHistoryLimit = clampHistoryLimit(PREFS.getInt(PREF_HISTORY_LIMIT, DEFAULT_HANDOFF_HISTORY_LIMIT));
    private int terrainTileCount;
    private final List<String> transitionTimeline = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-900);

        lander.setMaterial(materialForMode(lastMode));
        dockingTarget.setMaterial(new PhongMaterial(Color.LIGHTGREEN));
        worldGroup.getChildren().addAll(lander, dockingTarget);
        model = worldGroup;
        entityRegistry.register(OBJECT_ID, lander);
        orbitalEngine.setTrajectory(OBJECT_ID, (time, frame) -> new OrbitalState(
                new PhysicsVector3(scenarioOrbitalDistance(time), 0.0, 0.0),
                new PhysicsVector3(scenarioOrbitalVelocityX(time), 0.0, 0.0),
                org.dynamisfx.physics.model.PhysicsQuaternion.IDENTITY,
                frame,
                time));
        stateReconciler = SimulationStateReconcilerFactory.create(
                stateBuffers,
                this::seedOrbitalFromPhysics,
                objectId -> {
                },
                (objectId, zones) -> DeterministicZoneSelector.select(
                        zones,
                        zoneBodyRegistry.bindingForObject(objectId)
                                .map(ZoneBodyRegistry.ZoneBodyBinding::zoneId)
                                .orElse(null),
                        stateBuffers.orbital().get(objectId).map(OrbitalState::position).orElse(null)),
                snapshot -> {
                    if (!handoffDiagnosticsEnabled) {
                        return;
                    }
                    appendHandoffSnapshot(snapshot);
                    StateHandoffDiagnostics.loggingSink(LOG).accept(snapshot);
                });

        transitionApplier = new CouplingTransitionApplier(
                stateBuffers,
                zoneBodyRegistry,
                bodyDefinitionProvider());
        demoZone = new MutablePhysicsZone(
                new ZoneId("demo-zone"),
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                2_000.0,
                RigidBodyBackendSelector.createRigidWorld(new PhysicsWorldConfiguration(
                        ReferenceFrame.WORLD,
                        PhysicsVector3.ZERO,
                        1.0 / 120.0)));
        interactionEvents.add("cap:joints=" + demoZone.world().capabilities().supportsJoints());
        interactionEvents.add("cap:queries=" + demoZone.world().capabilities().supportsQueries());
        if (demoZone.world().capabilities().supportsRigidBodies()) {
            TerrainPatchSpawnResult terrain = TerrainPatchSpawner.spawnTiles(
                    demoZone.world(),
                    ReferenceFrame.WORLD,
                    new TerrainPatchSpec(2_000.0, 200.0, 80.0),
                    (x, y) -> 20.0 * Math.sin(x * 0.002) * Math.cos(y * 0.002),
                    0.0);
            terrainTileCount = terrain.tileCount();
            interactionEvents.add("terrain: tiles=" + terrain.tileCount());
        } else {
            terrainTileCount = 0;
            interactionEvents.add("terrain: skipped-rigid-unsupported");
        }
        dockingTargetHandle = demoZone.world().createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.STATIC,
                0.0,
                new BoxShape(DOCKING_TARGET_SIZE, DOCKING_TARGET_SIZE, DOCKING_TARGET_SIZE),
                new PhysicsBodyState(
                        new PhysicsVector3(120.0, 120.0, 40.0),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)));
        couplingManager.registerZone(demoZone);
        couplingManager.setMode(OBJECT_ID, lastMode);
        couplingManager.addTelemetryListener(this::onTelemetry);
        couplingManager.addTransitionListener(this::onTransitionEvent);
        couplingManager.addTransitionListener(transitionApplier);
        couplingManager.addTransitionListener(stateReconciler);
        orchestrator = new SimulationOrchestrator(
                clock,
                orbitalEngine,
                couplingManager,
                this::stepRigidDemo,
                transformBridge,
                stateBuffers.rigid()::snapshot,
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
                if (demoZone != null && demoZone.world() != null) {
                    demoZone.world().close();
                }
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));

        modeLabel = new Label("Mode: " + lastMode);
        distanceLabel = new Label("Distance: 2000 m");
        decisionLabel = new Label("Decision: waiting");
        zoneLabel = new Label("Zone/Frame: n/a");
        predictionLabel = new Label("Prediction: n/a");
        telemetryLabel = new Label("Telemetry: waiting");
        timelineLabel = new Label("Transitions: none");
        timelineLabel.setWrapText(true);
        dockingLabel = new Label("Docking: unlocked");
        interactionLabel = new Label("Interactions: pending");
        handoffDirectionLabel = new Label("Handoff: waiting");
        handoffZoneLabel = new Label("Zone: n/a");
        handoffGlobalLabel = new Label("Global: n/a");
        handoffLocalLabel = new Label("Local: n/a");
        handoffHistoryBox = new ComboBox<>();
        handoffHistoryBox.setPromptText("Select recent handoff");
        handoffHistoryBox.valueProperty().addListener((obs, oldValue, newValue) -> updateHandoffDebugLabels());
        handoffHistoryLimitSpinner = new Spinner<>(MIN_HANDOFF_HISTORY_LIMIT, MAX_HANDOFF_HISTORY_LIMIT, handoffHistoryLimit);
        handoffHistoryLimitSpinner.setEditable(true);
        handoffHistoryLimitSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            handoffHistoryLimit = clampHistoryLimit(newValue);
            PREFS.putInt(PREF_HISTORY_LIMIT, handoffHistoryLimit);
            trimHandoffHistoryToLimit();
            refreshHandoffHistoryControl();
            updateHandoffDebugLabels();
        });
        copyHandoffButton = new Button("Copy Handoff Line");
        copyHandoffButton.setOnAction(event -> copyLatestHandoffToClipboard());
        copyHandoffButton.setDisable(true);
        copyHandoffJsonButton = new Button("Copy JSON");
        copyHandoffJsonButton.setOnAction(event -> copyLatestHandoffJsonToClipboard());
        copyHandoffJsonButton.setDisable(true);
        exportHistoryJsonButton = new Button("Export History JSON");
        exportHistoryJsonButton.setOnAction(event -> copyHandoffHistoryJsonToClipboard());
        exportHistoryJsonButton.setDisable(true);
        clearHandoffHistoryButton = new Button("Clear History");
        clearHandoffHistoryButton.setOnAction(event -> clearHandoffHistory());
        resetHandoffDefaultsButton = new Button("Reset Defaults");
        resetHandoffDefaultsButton.setOnAction(event -> resetHandoffDebugDefaults());

        distanceSlider = new Slider(0, 3000, 2000);
        distanceSlider.valueProperty().addListener((obs, oldValue, newValue) -> updateDistanceLabel(newValue.doubleValue()));

        contactCheck = new CheckBox("Active Contact");
        autoScenarioCheck = new CheckBox("Auto Scenario");
        autoScenarioCheck.setSelected(true);
        handoffDiagnosticsCheck = new CheckBox("Handoff Diagnostics");
        handoffDiagnosticsCheck.setSelected(handoffDiagnosticsEnabled);
        handoffDiagnosticsCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            handoffDiagnosticsEnabled = newValue;
            PREFS.putBoolean(PREF_HANDOFF_DIAGNOSTICS, newValue);
            if (!newValue) {
                latestHandoff = null;
                handoffHistory.clear();
            }
            refreshHandoffHistoryControl();
            updateHandoffDebugLabels();
        });
        freezeHandoffSelectionCheck = new CheckBox("Freeze Selection");
        freezeHandoffSelectionCheck.setSelected(freezeHandoffSelection);
        freezeHandoffSelectionCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            freezeHandoffSelection = newValue;
            PREFS.putBoolean(PREF_FREEZE_SELECTION, newValue);
        });

        root.getChildren().addAll(
                new Label("Coupling Transition Demo"),
                modeLabel,
                distanceLabel,
                decisionLabel,
                zoneLabel,
                predictionLabel,
                telemetryLabel,
                timelineLabel,
                dockingLabel,
                interactionLabel,
                new Label("Handoff Debug"),
                handoffDirectionLabel,
                handoffZoneLabel,
                handoffGlobalLabel,
                handoffLocalLabel,
                new Label("Max History"),
                handoffHistoryLimitSpinner,
                handoffHistoryBox,
                copyHandoffButton,
                copyHandoffJsonButton,
                exportHistoryJsonButton,
                clearHandoffHistoryButton,
                resetHandoffDefaultsButton,
                new Label("Distance To Zone (m)"),
                distanceSlider,
                contactCheck,
                handoffDiagnosticsCheck,
                freezeHandoffSelectionCheck,
                autoScenarioCheck);
        return root;
    }

    private void applyScenario(double simulationTimeSeconds) {
        OrbitalState state = orbitalEngine.propagateTo(
                List.of(OBJECT_ID),
                simulationTimeSeconds,
                ReferenceFrame.WORLD).get(OBJECT_ID);
        if (state != null) {
            stateBuffers.orbital().put(OBJECT_ID, state);
            if (lastMode != ObjectSimulationMode.PHYSICS_ACTIVE && demoZone != null) {
                SphericalTangentFrame tangentFrame = SphericalTangentFrameBuilder.fromCartesian(
                        state.position(),
                        DEMO_PLANET_RADIUS_METERS);
                demoZone.updateAnchorPose(
                        tangentFrame.anchorPosition(),
                        tangentFrame.anchorOrientation());
            }
            if (demoZone != null && demoZone.world() != null) {
                demoZone.world().setGravity(ZoneGravityProjection.projectSphericalGravity(demoZone, 1.62));
            }
        }
        if (autoScenarioCheck != null && autoScenarioCheck.isSelected()) {
            boolean contact = simulationTimeSeconds >= 6.0 && simulationTimeSeconds < 7.5;
            if (state != null) {
                double altitude = Math.max(0.0, radialDistance(state.position()) - DEMO_PLANET_RADIUS_METERS);
                setObservationState(contact, altitude);
            }
            return;
        }

        if (contactCheck != null) {
            Double altitude = null;
            if (distanceSlider != null) {
                altitude = Math.max(0.0, distanceSlider.getValue() - DEMO_PLANET_RADIUS_METERS);
            }
            setObservationState(contactCheck.isSelected(), altitude);
        }
    }

    private void setObservationState(
            boolean activeContact,
            Double altitudeMetersAboveSurface) {
        manualObservationProvider.setActiveContact(OBJECT_ID, activeContact);
        if (altitudeMetersAboveSurface == null) {
            manualObservationProvider.clearAltitudeMetersAboveSurface(OBJECT_ID);
        } else {
            manualObservationProvider.setAltitudeMetersAboveSurface(OBJECT_ID, altitudeMetersAboveSurface);
        }
        if (contactCheck != null && contactCheck.isSelected() != activeContact) {
            contactCheck.setSelected(activeContact);
        }
    }

    private void stepRigidDemo(double dtSeconds) {
        if (dtSeconds <= 0.0) {
            return;
        }
        if (demoZone == null || demoZone.world() == null) {
            return;
        }
        if (dockingTargetHandle != null) {
            dockingLatched = dockingController.updateLatch(
                    demoZone,
                    OBJECT_ID,
                    dockingTargetHandle,
                    90.0,
                    150.0,
                    lastMode != ObjectSimulationMode.PHYSICS_ACTIVE);
        } else {
            dockingLatched = false;
        }
        demoZone.world().step(dtSeconds);
        zoneBodyRegistry.bindingForObject(OBJECT_ID).ifPresent(binding -> {
            if (!demoZone.zoneId().equals(binding.zoneId())) {
                return;
            }
            stateBuffers.rigid().put(OBJECT_ID, demoZone.world().getBodyState(binding.bodyHandle()));
        });
    }

    private void seedOrbitalFromPhysics(String objectId, OrbitalState seededState) {
        stateBuffers.orbital().put(objectId, seededState);
        orbitalEngine.setTrajectory(objectId, (time, frame) -> new OrbitalState(
                seededState.position(),
                seededState.linearVelocity(),
                seededState.angularVelocity(),
                seededState.orientation(),
                frame,
                time));
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
        if (latestTelemetry != null) {
            if (telemetryLabel != null) {
                telemetryLabel.setText(formatTelemetry(latestTelemetry));
            }
            if (decisionLabel != null) {
                decisionLabel.setText("Decision: " + latestTelemetry.reason());
            }
            if (zoneLabel != null) {
                String zone = latestTelemetry.selectedZoneId().map(ZoneId::value).orElse("none");
                String frame = latestTelemetry.selectedZoneFrame().map(Enum::name).orElse("UNSPECIFIED");
                zoneLabel.setText("Zone/Frame: " + zone + " / " + frame);
            }
            if (predictionLabel != null) {
                String intercept = latestTelemetry.predictedInterceptSeconds().isPresent()
                        ? String.format("%.2f s", latestTelemetry.predictedInterceptSeconds().orElseThrow())
                        : "n/a";
                predictionLabel.setText("Prediction: intercept=" + intercept);
            }
            updateDistanceLabel(latestTelemetry.observedDistanceMeters().orElse(Double.NaN));
        }
        if (dockingLabel != null) {
            dockingLabel.setText("Docking: " + (dockingLatched ? "latched" : "unlocked"));
        }
        if (interactionLabel != null) {
            String latest = interactionEvents.isEmpty() ? "none" : interactionEvents.get(interactionEvents.size() - 1);
            interactionLabel.setText("Interactions: tiles=" + terrainTileCount + " latest=" + latest);
        }
        if (demoZone != null && demoZone.world() != null && dockingTargetHandle != null) {
            PhysicsBodyState dockingState = demoZone.world().getBodyState(dockingTargetHandle);
            dockingTarget.setTranslateX(modeOffset(currentMode));
            dockingTarget.setTranslateY(-dockingState.position().z() * 0.12);
            dockingTarget.setTranslateZ(dockingState.position().x() * 0.15);
            dockingTarget.setMaterial(new PhongMaterial(dockingLatched ? Color.ORANGERED : Color.LIGHTGREEN));
        }
        updateHandoffDebugLabels();
    }

    private void onDockingEvent(DockingConstraintEvent event) {
        interactionEvents.add(String.format(
                "dock:%s reason=%s dist=%s",
                event.type(),
                event.reason(),
                Double.isFinite(event.distanceMeters()) ? String.format("%.2f", event.distanceMeters()) : "n/a"));
        if (interactionEvents.size() > 50) {
            interactionEvents.remove(0);
        }
    }

    private void updateHandoffDebugLabels() {
        if (handoffDirectionLabel == null || handoffZoneLabel == null || handoffGlobalLabel == null || handoffLocalLabel == null) {
            return;
        }
        StateHandoffSnapshot selected = selectedHandoffSnapshot();
        if (selected == null) {
            handoffDirectionLabel.setText("Handoff: waiting");
            handoffZoneLabel.setText("Zone: n/a");
            handoffGlobalLabel.setText("Global: n/a");
            handoffLocalLabel.setText("Local: n/a");
            if (copyHandoffButton != null) {
                copyHandoffButton.setDisable(true);
            }
            if (copyHandoffJsonButton != null) {
                copyHandoffJsonButton.setDisable(true);
            }
            if (exportHistoryJsonButton != null) {
                exportHistoryJsonButton.setDisable(handoffHistory.isEmpty());
            }
            return;
        }
        handoffDirectionLabel.setText(String.format(
                "Handoff: %s @ t=%.2f",
                selected.direction(),
                selected.simulationTimeSeconds()));
        handoffZoneLabel.setText(String.format(
                "Zone: %s anchor=%s",
                selected.zoneId().value(),
                formatVector(selected.zoneAnchorPosition())));
        handoffGlobalLabel.setText(String.format(
                "Global: pos=%s vel=%s",
                formatVector(selected.globalPosition()),
                formatVector(selected.globalVelocity())));
        handoffLocalLabel.setText(String.format(
                "Local: pos=%s vel=%s",
                formatVector(selected.localPosition()),
                formatVector(selected.localVelocity())));
        if (copyHandoffButton != null) {
            copyHandoffButton.setDisable(false);
        }
        if (copyHandoffJsonButton != null) {
            copyHandoffJsonButton.setDisable(false);
        }
        if (exportHistoryJsonButton != null) {
            exportHistoryJsonButton.setDisable(handoffHistory.isEmpty());
        }
    }

    private static String formatVector(PhysicsVector3 v) {
        return String.format("(%.1f, %.1f, %.1f)", v.x(), v.y(), v.z());
    }

    private void copyLatestHandoffToClipboard() {
        StateHandoffSnapshot selected = selectedHandoffSnapshot();
        if (selected == null) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(StateHandoffDiagnostics.format(selected));
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void copyLatestHandoffJsonToClipboard() {
        StateHandoffSnapshot selected = selectedHandoffSnapshot();
        if (selected == null) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(StateHandoffDiagnostics.toJson(selected));
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void copyHandoffHistoryJsonToClipboard() {
        if (handoffHistory.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < handoffHistory.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(StateHandoffDiagnostics.toJson(handoffHistory.get(i)));
        }
        sb.append(']');
        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void appendHandoffSnapshot(StateHandoffSnapshot snapshot) {
        latestHandoff = snapshot;
        handoffHistory.add(0, snapshot);
        trimHandoffHistoryToLimit();
        refreshHandoffHistoryControl();
        updateHandoffDebugLabels();
    }

    private void trimHandoffHistoryToLimit() {
        while (handoffHistory.size() > handoffHistoryLimit) {
            handoffHistory.remove(handoffHistory.size() - 1);
        }
    }

    private void refreshHandoffHistoryControl() {
        if (handoffHistoryBox == null) {
            return;
        }
        int previousSelection = handoffHistoryBox.getSelectionModel().getSelectedIndex();
        List<String> labels = new ArrayList<>(handoffHistory.size());
        for (StateHandoffSnapshot snapshot : handoffHistory) {
            labels.add(String.format(
                    "t=%.2f %s %s",
                    snapshot.simulationTimeSeconds(),
                    snapshot.direction(),
                    snapshot.zoneId().value()));
        }
        handoffHistoryBox.getItems().setAll(labels);
        if (labels.isEmpty()) {
            handoffHistoryBox.getSelectionModel().clearSelection();
        } else if (freezeHandoffSelection) {
            int boundedSelection = Math.min(Math.max(previousSelection, 0), labels.size() - 1);
            handoffHistoryBox.getSelectionModel().select(boundedSelection);
        } else if (handoffHistoryBox.getSelectionModel().getSelectedIndex() < 0) {
            handoffHistoryBox.getSelectionModel().select(0);
        } else {
            handoffHistoryBox.getSelectionModel().select(0);
        }
    }

    private StateHandoffSnapshot selectedHandoffSnapshot() {
        if (handoffHistoryBox != null) {
            int selectedIndex = handoffHistoryBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < handoffHistory.size()) {
                return handoffHistory.get(selectedIndex);
            }
        }
        return latestHandoff;
    }

    private void clearHandoffHistory() {
        latestHandoff = null;
        handoffHistory.clear();
        refreshHandoffHistoryControl();
        updateHandoffDebugLabels();
    }

    private void resetHandoffDebugDefaults() {
        PREFS.remove(PREF_HANDOFF_DIAGNOSTICS);
        PREFS.remove(PREF_FREEZE_SELECTION);
        PREFS.remove(PREF_HISTORY_LIMIT);

        handoffDiagnosticsEnabled = true;
        freezeHandoffSelection = false;
        handoffHistoryLimit = DEFAULT_HANDOFF_HISTORY_LIMIT;

        if (handoffDiagnosticsCheck != null) {
            handoffDiagnosticsCheck.setSelected(handoffDiagnosticsEnabled);
        }
        if (freezeHandoffSelectionCheck != null) {
            freezeHandoffSelectionCheck.setSelected(freezeHandoffSelection);
        }
        if (handoffHistoryLimitSpinner != null) {
            handoffHistoryLimitSpinner.getValueFactory().setValue(handoffHistoryLimit);
        }
        clearHandoffHistory();
    }

    private static int clampHistoryLimit(int value) {
        return Math.max(MIN_HANDOFF_HISTORY_LIMIT, Math.min(MAX_HANDOFF_HISTORY_LIMIT, value));
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
            if (Double.isFinite(distanceMeters)) {
                distanceLabel.setText(String.format("Distance: %.0f m", distanceMeters));
            } else {
                distanceLabel.setText("Distance: n/a");
            }
        }
    }

    private static double radialDistance(PhysicsVector3 position) {
        return Math.sqrt(
                (position.x() * position.x())
                        + (position.y() * position.y())
                        + (position.z() * position.z()));
    }

    private void onTelemetry(CouplingTelemetryEvent event) {
        if (OBJECT_ID.equals(event.objectId())) {
            latestTelemetry = event;
            if (event.transitioned()) {
                LOG.info(() -> formatTelemetry(event));
            }
        }
    }

    private void onTransitionEvent(CouplingModeTransitionEvent event) {
        if (!OBJECT_ID.equals(event.objectId())) {
            return;
        }
        String zone = event.selectedZoneId().map(ZoneId::value).orElse("none");
        String line = String.format(
                "t=%.2f %s->%s %s zone=%s",
                event.simulationTimeSeconds(),
                event.fromMode(),
                event.toMode(),
                event.reason(),
                zone);
        transitionTimeline.add(0, line);
        while (transitionTimeline.size() > TIMELINE_LIMIT) {
            transitionTimeline.remove(transitionTimeline.size() - 1);
        }
        if (timelineLabel != null) {
            timelineLabel.setText("Transitions:\n" + String.join("\n", transitionTimeline));
        }
    }

    private static String formatTelemetry(CouplingTelemetryEvent event) {
        String action = event.transitioned() ? "transition" : "hold";
        CouplingDecisionReason reason = event.reason();
        String zone = event.selectedZoneId().map(ZoneId::value).orElse("none");
        String frame = event.selectedZoneFrame().map(Enum::name).orElse("UNSPECIFIED");
        String distance = event.observedDistanceMeters().isPresent()
                ? String.format("%.1f", event.observedDistanceMeters().orElseThrow())
                : "n/a";
        String intercept = event.predictedInterceptSeconds().isPresent()
                ? String.format("%.2f", event.predictedInterceptSeconds().orElseThrow())
                : "n/a";
        return String.format(
                "Telemetry: t=%.2f %s %s -> %s (%s) zone=%s frame=%s d=%sm ti=%ss",
                event.simulationTimeSeconds(),
                action,
                event.fromMode(),
                event.toMode(),
                reason,
                zone,
                frame,
                distance,
                intercept);
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

    private static double scenarioOrbitalVelocityX(double simulationTimeSeconds) {
        if (simulationTimeSeconds < 3.0) {
            return 0.0;
        }
        if (simulationTimeSeconds < 6.0) {
            return (300.0 - 2000.0) / 3.0;
        }
        if (simulationTimeSeconds < 8.0) {
            return (2200.0 - 300.0) / 2.0;
        }
        return 0.0;
    }

    private static CouplingBodyDefinitionProvider bodyDefinitionProvider() {
        return (objectId, event, zone, seedState) -> new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(LANDER_WIDTH, LANDER_HEIGHT, LANDER_DEPTH),
                seedState);
    }
}
