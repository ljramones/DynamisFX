package org.fxyz3d.physics.orekit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.api.PhysicsCapabilities;
import org.fxyz3d.physics.api.PhysicsConstraintDefinition;
import org.fxyz3d.physics.api.PhysicsConstraintHandle;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.PhysicsBodyDefinition;
import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsBodyType;
import org.fxyz3d.physics.model.PhysicsRuntimeTuning;
import org.fxyz3d.physics.model.PhysicsVector3;
import org.fxyz3d.physics.model.PhysicsWorldConfiguration;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/**
 * Phase-3 astrodynamics world scaffold aligned to the backend-neutral SPI.
 */
public final class OrekitWorld implements PhysicsWorld {

    static final double UNIVERSAL_GRAVITATION = 6.67430e-11;

    private final PhysicsWorldConfiguration configuration;
    private final Frame orekitFrame;
    private final Map<PhysicsBodyHandle, BodyEntry> bodies = new LinkedHashMap<>();
    private long nextBodyHandle = 1L;
    private PhysicsRuntimeTuning runtimeTuning;
    private double timeScale = 1.0;
    private double simulationTimeSeconds;
    private AbsoluteDate simulationDate = AbsoluteDate.J2000_EPOCH;
    private boolean closed;

    OrekitWorld(PhysicsWorldConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        this.configuration = configuration;
        this.orekitFrame = OrekitFrameBridge.toOrekitFrame(configuration.referenceFrame());
        this.runtimeTuning = configuration.runtimeTuning();
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return new OrekitBackend().capabilities();
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        ensureOpen();
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        PhysicsBodyHandle handle = new PhysicsBodyHandle(nextBodyHandle++);
        PhysicsBodyState normalized = normalizeFrame(definition.initialState());
        BodyEntry entry = new BodyEntry(definition, normalized);
        bodies.put(handle, entry);
        if (definition.bodyType() == PhysicsBodyType.DYNAMIC) {
            configurePropagator(handle, entry);
        }
        return handle;
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        ensureOpen();
        boolean removed = bodies.remove(handle) != null;
        if (removed) {
            for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
                BodyEntry entry = pair.getValue();
                if (entry.primaryBody != null && entry.primaryBody.equals(handle)) {
                    configurePropagator(pair.getKey(), entry);
                }
            }
        }
        return removed;
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        ensureOpen();
        return new ArrayList<>(bodies.keySet());
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        ensureOpen();
        BodyEntry entry = entryFor(handle);
        return entry.state;
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        ensureOpen();
        BodyEntry entry = entryFor(handle);
        entry.state = normalizeFrame(state);
        if (entry.definition.bodyType() == PhysicsBodyType.DYNAMIC) {
            configurePropagator(handle, entry);
        }
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        ensureOpen();
        throw new UnsupportedOperationException("orekit backend does not support rigid-body constraints");
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        ensureOpen();
        return false;
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        ensureOpen();
        return List.of();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        ensureOpen();
        return runtimeTuning;
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        ensureOpen();
        if (tuning == null) {
            throw new IllegalArgumentException("tuning must not be null");
        }
        runtimeTuning = tuning;
    }

    public double timeScale() {
        ensureOpen();
        return timeScale;
    }

    public void setTimeScale(double value) {
        ensureOpen();
        if (!(value > 0.0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException("timeScale must be > 0 and finite");
        }
        timeScale = value;
    }

    @Override
    public void step(double dtSeconds) {
        ensureOpen();
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }
        double scaledDt = dtSeconds * timeScale;
        if (bodies.isEmpty()) {
            return;
        }
        simulationTimeSeconds += scaledDt;
        simulationDate = simulationDate.shiftedBy(scaledDt);

        Map<PhysicsBodyHandle, PhysicsBodyState> nextStates = new LinkedHashMap<>();
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            PhysicsBodyHandle handle = pair.getKey();
            BodyEntry body = pair.getValue();
            PhysicsBodyState current = body.state;
            if (body.definition.bodyType() == PhysicsBodyType.DYNAMIC) {
                configurePropagator(handle, body);
            }

            PhysicsBodyState next = switch (body.definition.bodyType()) {
                case STATIC -> advanceTimestamp(current, scaledDt);
                case KINEMATIC -> integrateKinematic(current, scaledDt);
                case DYNAMIC -> integrateDynamic(handle, current, scaledDt);
            };
            nextStates.put(handle, next);
        }

        for (Map.Entry<PhysicsBodyHandle, PhysicsBodyState> pair : nextStates.entrySet()) {
            bodies.get(pair.getKey()).state = pair.getValue();
        }
    }

    @Override
    public void close() {
        closed = true;
        bodies.clear();
    }

    private PhysicsBodyState integrateDynamic(
            PhysicsBodyHandle self,
            PhysicsBodyState current,
            double dtSeconds) {
        BodyEntry body = entryFor(self);
        if (body.propagator == null) {
            return integrateKinematic(current, dtSeconds);
        }

        SpacecraftState propagated = body.propagator.propagate(simulationDate);
        PVCoordinates relativePv = propagated.getPVCoordinates(orekitFrame);
        PhysicsBodyState primaryState = body.primaryBody == null
                ? null
                : entryFor(body.primaryBody).state;

        Vector3D absolutePos = relativePv.getPosition();
        Vector3D absoluteVel = relativePv.getVelocity();
        if (primaryState != null) {
            absolutePos = absolutePos.add(OrekitFrameBridge.toVector(primaryState.position()));
            absoluteVel = absoluteVel.add(OrekitFrameBridge.toVector(primaryState.linearVelocity()));
        }

        return new PhysicsBodyState(
                OrekitFrameBridge.toPhysics(absolutePos),
                current.orientation(),
                OrekitFrameBridge.toPhysics(absoluteVel),
                current.angularVelocity(),
                configuration.referenceFrame(),
                simulationTimeSeconds);
    }

    private PhysicsBodyState integrateKinematic(PhysicsBodyState current, double dtSeconds) {
        PhysicsVector3 position = add(current.position(), scale(current.linearVelocity(), dtSeconds));
        return new PhysicsBodyState(
                position,
                current.orientation(),
                current.linearVelocity(),
                current.angularVelocity(),
                configuration.referenceFrame(),
                simulationTimeSeconds);
    }

    private PhysicsBodyState advanceTimestamp(PhysicsBodyState current, double dtSeconds) {
        return new PhysicsBodyState(
                current.position(),
                current.orientation(),
                current.linearVelocity(),
                current.angularVelocity(),
                configuration.referenceFrame(),
                simulationTimeSeconds);
    }

    private PhysicsBodyState normalizeFrame(PhysicsBodyState state) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        return OrekitFrameBridge.transformState(
                state,
                configuration.referenceFrame(),
                simulationDate);
    }

    private BodyEntry entryFor(PhysicsBodyHandle handle) {
        BodyEntry entry = bodies.get(handle);
        if (entry == null) {
            throw new IllegalArgumentException("unknown body handle: " + handle);
        }
        return entry;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("world is closed");
        }
    }

    private static PhysicsVector3 add(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    private static PhysicsVector3 scale(PhysicsVector3 v, double s) {
        return new PhysicsVector3(v.x() * s, v.y() * s, v.z() * s);
    }

    private static final class BodyEntry {
        private final PhysicsBodyDefinition definition;
        private PhysicsBodyState state;
        private PhysicsBodyHandle primaryBody;
        private Propagator propagator;

        private BodyEntry(PhysicsBodyDefinition definition, PhysicsBodyState state) {
            this.definition = definition;
            this.state = state;
        }
    }

    private void configurePropagator(PhysicsBodyHandle self, BodyEntry entry) {
        PrimaryAttractor primary = resolvePrimaryAttractor(self);
        if (primary == null) {
            entry.primaryBody = null;
            entry.propagator = null;
            return;
        }

        PhysicsVector3 primaryPos = primary.state.position();
        PhysicsVector3 primaryVel = primary.state.linearVelocity();
        PhysicsVector3 relPos = new PhysicsVector3(
                entry.state.position().x() - primaryPos.x(),
                entry.state.position().y() - primaryPos.y(),
                entry.state.position().z() - primaryPos.z());
        PhysicsVector3 relVel = new PhysicsVector3(
                entry.state.linearVelocity().x() - primaryVel.x(),
                entry.state.linearVelocity().y() - primaryVel.y(),
                entry.state.linearVelocity().z() - primaryVel.z());

        Orbit orbit = new CartesianOrbit(
                OrekitFrameBridge.toPV(relPos, relVel),
                orekitFrame,
                simulationDate,
                primary.mu);
        entry.primaryBody = primary.handle;
        entry.propagator = new KeplerianPropagator(orbit);
    }

    private PrimaryAttractor resolvePrimaryAttractor(PhysicsBodyHandle self) {
        PhysicsBodyHandle bestHandle = null;
        PhysicsBodyState bestState = null;
        double bestMass = 0.0;
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            if (pair.getKey().equals(self)) {
                continue;
            }
            BodyEntry candidate = pair.getValue();
            if (candidate.definition.bodyType() == PhysicsBodyType.DYNAMIC) {
                continue;
            }
            double mass = candidate.definition.massKg();
            if (mass > bestMass) {
                bestMass = mass;
                bestHandle = pair.getKey();
                bestState = candidate.state;
            }
        }
        if (!(bestMass > 0.0)) {
            return null;
        }
        return new PrimaryAttractor(bestHandle, bestState, UNIVERSAL_GRAVITATION * bestMass);
    }

    private record PrimaryAttractor(PhysicsBodyHandle handle, PhysicsBodyState state, double mu) {
    }
}
