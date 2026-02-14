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

package org.dynamisfx.physics.orekit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.orekit.time.AbsoluteDate;

/**
 * Phase-3 astrodynamics world scaffold aligned to the backend-neutral SPI.
 */
public final class OrekitWorld implements PhysicsWorld {

    static final double UNIVERSAL_GRAVITATION = 6.67430e-11;

    private final PhysicsWorldConfiguration configuration;
    private final Map<PhysicsBodyHandle, BodyEntry> bodies = new LinkedHashMap<>();
    private long nextBodyHandle = 1L;
    private PhysicsRuntimeTuning runtimeTuning;
    private PhysicsVector3 gravity;
    private double timeScale = 1.0;
    private double simulationTimeSeconds;
    private AbsoluteDate simulationDate = AbsoluteDate.J2000_EPOCH;
    private boolean closed;

    OrekitWorld(PhysicsWorldConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        this.configuration = configuration;
        this.runtimeTuning = configuration.runtimeTuning();
        this.gravity = configuration.gravity();
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

    @Override
    public PhysicsVector3 gravity() {
        ensureOpen();
        return gravity;
    }

    @Override
    public void setGravity(PhysicsVector3 gravity) {
        ensureOpen();
        if (gravity == null) {
            throw new IllegalArgumentException("gravity must not be null");
        }
        this.gravity = gravity;
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

        Map<PhysicsBodyHandle, PhysicsBodyState> currentStates = new LinkedHashMap<>();
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            currentStates.put(pair.getKey(), pair.getValue().state);
        }

        Map<PhysicsBodyHandle, PhysicsBodyState> nextStates = new LinkedHashMap<>();
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            PhysicsBodyHandle handle = pair.getKey();
            BodyEntry body = pair.getValue();
            PhysicsBodyState current = currentStates.get(handle);

            PhysicsBodyState next = switch (body.definition.bodyType()) {
                case STATIC -> advanceTimestamp(current, scaledDt);
                case KINEMATIC -> integrateKinematic(current, scaledDt);
                case DYNAMIC -> integrateDynamic(handle, current, scaledDt, currentStates);
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
            double dtSeconds,
            Map<PhysicsBodyHandle, PhysicsBodyState> states) {
        PhysicsVector3 p0 = current.position();
        PhysicsVector3 v0 = current.linearVelocity();

        PhysicsVector3 k1p = v0;
        PhysicsVector3 k1v = sumAcceleration(self, p0, states);

        PhysicsVector3 k2p = add(v0, scale(k1v, 0.5 * dtSeconds));
        PhysicsVector3 k2v = sumAcceleration(self, add(p0, scale(k1p, 0.5 * dtSeconds)), states);

        PhysicsVector3 k3p = add(v0, scale(k2v, 0.5 * dtSeconds));
        PhysicsVector3 k3v = sumAcceleration(self, add(p0, scale(k2p, 0.5 * dtSeconds)), states);

        PhysicsVector3 k4p = add(v0, scale(k3v, dtSeconds));
        PhysicsVector3 k4v = sumAcceleration(self, add(p0, scale(k3p, dtSeconds)), states);

        PhysicsVector3 nextPosition = add(p0, scale(
                add(add(k1p, scale(add(k2p, k3p), 2.0)), k4p),
                dtSeconds / 6.0));
        PhysicsVector3 nextVelocity = add(v0, scale(
                add(add(k1v, scale(add(k2v, k3v), 2.0)), k4v),
                dtSeconds / 6.0));

        return new PhysicsBodyState(
                nextPosition,
                current.orientation(),
                nextVelocity,
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

        private BodyEntry(PhysicsBodyDefinition definition, PhysicsBodyState state) {
            this.definition = definition;
            this.state = state;
        }
    }

    private PhysicsVector3 sumAcceleration(
            PhysicsBodyHandle self,
            PhysicsVector3 position,
            Map<PhysicsBodyHandle, PhysicsBodyState> states) {
        double ax = gravity.x();
        double ay = gravity.y();
        double az = gravity.z();
        for (Map.Entry<PhysicsBodyHandle, BodyEntry> pair : bodies.entrySet()) {
            if (pair.getKey().equals(self)) {
                continue;
            }
            double mass = pair.getValue().definition.massKg();
            if (!(mass > 0.0)) {
                continue;
            }
            PhysicsBodyState other = states.get(pair.getKey());
            if (other == null) {
                continue;
            }
            double dx = other.position().x() - position.x();
            double dy = other.position().y() - position.y();
            double dz = other.position().z() - position.z();
            double r2 = dx * dx + dy * dy + dz * dz;
            if (r2 < 1e-6) {
                continue;
            }
            double invR = 1.0 / Math.sqrt(r2);
            double invR3 = invR * invR * invR;
            double scale = UNIVERSAL_GRAVITATION * mass * invR3;
            ax += dx * scale;
            ay += dy * scale;
            az += dz * scale;
        }
        return new PhysicsVector3(ax, ay, az);
    }
}
