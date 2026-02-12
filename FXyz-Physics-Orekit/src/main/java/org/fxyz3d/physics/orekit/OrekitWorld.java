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
import org.fxyz3d.physics.model.ReferenceFrame;

/**
 * Phase-3 astrodynamics world scaffold aligned to the backend-neutral SPI.
 */
public final class OrekitWorld implements PhysicsWorld {

    static final double UNIVERSAL_GRAVITATION = 6.67430e-11;

    private final PhysicsWorldConfiguration configuration;
    private final Map<PhysicsBodyHandle, BodyEntry> bodies = new LinkedHashMap<>();
    private long nextBodyHandle = 1L;
    private PhysicsRuntimeTuning runtimeTuning;
    private double timeScale = 1.0;
    private boolean closed;

    OrekitWorld(PhysicsWorldConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        this.configuration = configuration;
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
        bodies.put(handle, new BodyEntry(definition, normalized));
        return handle;
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        ensureOpen();
        return bodies.remove(handle) != null;
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

        Map<PhysicsBodyHandle, PhysicsBodyState> nextStates = new LinkedHashMap<>();
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            PhysicsBodyHandle handle = pair.getKey();
            BodyEntry body = pair.getValue();
            PhysicsBodyState current = body.state;

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
        PhysicsVector3 acceleration = sumAcceleration(self, current.position());
        PhysicsVector3 velocity = add(current.linearVelocity(), scale(acceleration, dtSeconds));
        PhysicsVector3 position = add(current.position(), scale(velocity, dtSeconds));
        return new PhysicsBodyState(
                position,
                current.orientation(),
                velocity,
                current.angularVelocity(),
                configuration.referenceFrame(),
                current.timestampSeconds() + dtSeconds);
    }

    private PhysicsBodyState integrateKinematic(PhysicsBodyState current, double dtSeconds) {
        PhysicsVector3 position = add(current.position(), scale(current.linearVelocity(), dtSeconds));
        return new PhysicsBodyState(
                position,
                current.orientation(),
                current.linearVelocity(),
                current.angularVelocity(),
                configuration.referenceFrame(),
                current.timestampSeconds() + dtSeconds);
    }

    private PhysicsBodyState advanceTimestamp(PhysicsBodyState current, double dtSeconds) {
        return new PhysicsBodyState(
                current.position(),
                current.orientation(),
                current.linearVelocity(),
                current.angularVelocity(),
                configuration.referenceFrame(),
                current.timestampSeconds() + dtSeconds);
    }

    private PhysicsVector3 sumAcceleration(PhysicsBodyHandle self, PhysicsVector3 position) {
        PhysicsVector3 gravity = configuration.gravity();
        double ax = gravity.x();
        double ay = gravity.y();
        double az = gravity.z();

        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            if (pair.getKey().equals(self)) {
                continue;
            }
            BodyEntry other = pair.getValue();
            double otherMass = other.definition.massKg();
            if (!(otherMass > 0.0)) {
                continue;
            }
            PhysicsVector3 otherPos = other.state.position();
            double dx = otherPos.x() - position.x();
            double dy = otherPos.y() - position.y();
            double dz = otherPos.z() - position.z();
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared < 1e-6) {
                continue;
            }
            double invDistance = 1.0 / Math.sqrt(distanceSquared);
            double invDistanceCubed = invDistance * invDistance * invDistance;
            double scale = UNIVERSAL_GRAVITATION * otherMass * invDistanceCubed;
            ax += dx * scale;
            ay += dy * scale;
            az += dz * scale;
        }

        return new PhysicsVector3(ax, ay, az);
    }

    private PhysicsBodyState normalizeFrame(PhysicsBodyState state) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        ReferenceFrame configuredFrame = configuration.referenceFrame();
        ReferenceFrame frame = state.referenceFrame() == ReferenceFrame.UNSPECIFIED
                ? configuredFrame
                : state.referenceFrame();
        if (frame != configuredFrame) {
            throw new IllegalArgumentException("state frame does not match world frame: " + frame);
        }
        return new PhysicsBodyState(
                state.position(),
                state.orientation(),
                state.linearVelocity(),
                state.angularVelocity(),
                frame,
                state.timestampSeconds());
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

        private BodyEntry(PhysicsBodyDefinition definition, PhysicsBodyState state) {
            this.definition = definition;
            this.state = state;
        }
    }
}
