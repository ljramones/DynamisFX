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

package org.dynamisfx.physics.jolt;

import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BodyInterface;
import com.github.stephengold.joltjni.BroadPhaseLayerInterfaceTable;
import com.github.stephengold.joltjni.JobSystemThreadPool;
import com.github.stephengold.joltjni.ObjectLayerPairFilterTable;
import com.github.stephengold.joltjni.ObjectVsBroadPhaseLayerFilterTable;
import com.github.stephengold.joltjni.PhysicsSystem;
import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Shape;
import com.github.stephengold.joltjni.TempAllocatorImpl;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.enumerate.EPhysicsUpdateError;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
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
import org.dynamisfx.physics.model.SphereShape;

/**
 * World backed directly by jolt-jni.
 */
final class JoltJniWorld implements PhysicsWorld {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private static final int LAYER_NON_MOVING = 0;
    private static final int LAYER_MOVING = 1;
    private static final int BROAD_PHASE_NON_MOVING = 0;
    private static final int BROAD_PHASE_MOVING = 1;

    private final PhysicsWorldConfiguration configuration;
    private final String loadDescription;
    private final Set<PhysicsBodyHandle> bodyHandles = new LinkedHashSet<>();

    private final BroadPhaseLayerInterfaceTable broadPhaseLayerInterface;
    private final ObjectLayerPairFilterTable objectLayerPairFilter;
    private final ObjectVsBroadPhaseLayerFilterTable objectVsBroadPhaseLayerFilter;
    private final PhysicsSystem physicsSystem;
    private final BodyInterface bodyInterface;
    private final TempAllocatorImpl tempAllocator;
    private final JobSystemThreadPool jobSystem;

    private double simulationTimeSeconds;
    private boolean closed;

    JoltJniWorld(PhysicsWorldConfiguration configuration, String loadDescription) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.loadDescription = Objects.requireNonNull(loadDescription, "loadDescription must not be null");

        this.broadPhaseLayerInterface = new BroadPhaseLayerInterfaceTable(2, 2)
                .mapObjectToBroadPhaseLayer(LAYER_NON_MOVING, BROAD_PHASE_NON_MOVING)
                .mapObjectToBroadPhaseLayer(LAYER_MOVING, BROAD_PHASE_MOVING);
        this.objectLayerPairFilter = new ObjectLayerPairFilterTable(2)
                .enableCollision(LAYER_NON_MOVING, LAYER_MOVING)
                .enableCollision(LAYER_MOVING, LAYER_MOVING);
        this.objectVsBroadPhaseLayerFilter = new ObjectVsBroadPhaseLayerFilterTable(
                broadPhaseLayerInterface,
                2,
                objectLayerPairFilter,
                2);

        this.physicsSystem = new PhysicsSystem().init(
                65_536,
                0,
                65_536,
                16_384,
                broadPhaseLayerInterface,
                objectVsBroadPhaseLayerFilter,
                objectLayerPairFilter);
        PhysicsVector3 gravity = configuration.gravity();
        this.physicsSystem.setGravity((float) gravity.x(), (float) gravity.y(), (float) gravity.z());
        this.bodyInterface = physicsSystem.getBodyInterface();
        this.tempAllocator = new TempAllocatorImpl(10 * 1024 * 1024);
        this.jobSystem = new JobSystemThreadPool(2048, 8);
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        ensureOpen();

        Shape shape = mapShape(definition);
        PhysicsBodyState initial = definition.initialState();
        BodyCreationSettings settings = new BodyCreationSettings(
                shape,
                toRVec3(initial.position()),
                toQuat(initial.orientation()),
                toMotionType(definition.bodyType()),
                toObjectLayer(definition.bodyType()));
        settings.setLinearVelocity((float) initial.linearVelocity().x(), (float) initial.linearVelocity().y(), (float) initial.linearVelocity().z());
        settings.setAngularVelocity((float) initial.angularVelocity().x(), (float) initial.angularVelocity().y(), (float) initial.angularVelocity().z());

        Body body = bodyInterface.createBody(settings);
        if (body == null) {
            throw unavailable("createBody");
        }

        EActivation activation = definition.bodyType() == PhysicsBodyType.STATIC
                ? EActivation.DontActivate
                : EActivation.Activate;
        bodyInterface.addBody(body, activation);

        int nativeId = body.getId();
        PhysicsBodyHandle handle = new PhysicsBodyHandle(Integer.toUnsignedLong(nativeId));
        bodyHandles.add(handle);
        return handle;
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        ensureOpen();
        if (!bodyHandles.contains(handle)) {
            return false;
        }

        int bodyId = toBodyId(handle);
        bodyInterface.removeBody(bodyId);
        bodyInterface.destroyBody(bodyId);
        return bodyHandles.remove(handle);
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        ensureOpen();
        return List.copyOf(bodyHandles);
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        ensureOpen();

        int bodyId = toBodyId(handle);
        RVec3 position = bodyInterface.getPosition(bodyId);
        Quat rotation = bodyInterface.getRotation(bodyId);
        Vec3 linearVelocity = bodyInterface.getLinearVelocity(bodyId);
        Vec3 angularVelocity = bodyInterface.getAngularVelocity(bodyId);

        return new PhysicsBodyState(
                new PhysicsVector3(position.xx(), position.yy(), position.zz()),
                new PhysicsQuaternion(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW()),
                new PhysicsVector3(linearVelocity.getX(), linearVelocity.getY(), linearVelocity.getZ()),
                new PhysicsVector3(angularVelocity.getX(), angularVelocity.getY(), angularVelocity.getZ()),
                configuration.referenceFrame(),
                simulationTimeSeconds);
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        Objects.requireNonNull(handle, "handle must not be null");
        Objects.requireNonNull(state, "state must not be null");
        ensureOpen();

        int bodyId = toBodyId(handle);
        bodyInterface.setPositionAndRotation(
                bodyId,
                toRVec3(state.position()),
                toQuat(state.orientation()),
                EActivation.DontActivate);
        bodyInterface.setLinearVelocity(bodyId, new Vec3(
                (float) state.linearVelocity().x(),
                (float) state.linearVelocity().y(),
                (float) state.linearVelocity().z()));
        bodyInterface.setAngularVelocity(bodyId, new Vec3(
                (float) state.angularVelocity().x(),
                (float) state.angularVelocity().y(),
                (float) state.angularVelocity().z()));
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        throw unavailable("createConstraint");
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        throw unavailable("removeConstraint");
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        return List.of();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        return configuration.runtimeTuning();
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        throw unavailable("setRuntimeTuning");
    }

    @Override
    public PhysicsVector3 gravity() {
        return configuration.gravity();
    }

    @Override
    public void setGravity(PhysicsVector3 gravity) {
        Objects.requireNonNull(gravity, "gravity must not be null");
        ensureOpen();
        physicsSystem.setGravity((float) gravity.x(), (float) gravity.y(), (float) gravity.z());
    }

    @Override
    public void step(double dtSeconds) {
        ensureOpen();
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }
        int status = physicsSystem.update(
                (float) dtSeconds,
                Math.max(1, configuration.runtimeTuning().solverIterations()),
                tempAllocator,
                jobSystem);
        if (status != EPhysicsUpdateError.None) {
            throw unavailable("step");
        }
        simulationTimeSeconds += dtSeconds;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        bodyHandles.clear();
        try {
            physicsSystem.removeAllBodies();
            physicsSystem.destroyAllBodies();
        } catch (RuntimeException ignored) {
            // best-effort cleanup
        }

        closeQuietly(jobSystem);
        closeQuietly(tempAllocator);
        closeQuietly(physicsSystem);
        closeQuietly(objectVsBroadPhaseLayerFilter);
        closeQuietly(objectLayerPairFilter);
        closeQuietly(broadPhaseLayerInterface);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Jolt world is closed");
        }
    }

    private IllegalStateException unavailable(String operation) {
        return new IllegalStateException(
                "jolt-jni operation failed: "
                        + operation
                        + "; load-status="
                        + loadDescription);
    }

    private static int toBodyId(PhysicsBodyHandle handle) {
        return (int) handle.value();
    }

    private static EMotionType toMotionType(PhysicsBodyType bodyType) {
        return switch (bodyType) {
            case STATIC -> EMotionType.Static;
            case KINEMATIC -> EMotionType.Kinematic;
            case DYNAMIC -> EMotionType.Dynamic;
        };
    }

    private static int toObjectLayer(PhysicsBodyType bodyType) {
        return bodyType == PhysicsBodyType.STATIC ? LAYER_NON_MOVING : LAYER_MOVING;
    }

    private static Shape mapShape(PhysicsBodyDefinition definition) {
        if (definition.shape() instanceof SphereShape sphere) {
            return new com.github.stephengold.joltjni.SphereShape((float) sphere.radius());
        }
        if (definition.shape() instanceof BoxShape box) {
            return new com.github.stephengold.joltjni.BoxShape(
                    (float) (box.width() * 0.5),
                    (float) (box.height() * 0.5),
                    (float) (box.depth() * 0.5));
        }
        if (definition.shape() instanceof CapsuleShape capsule) {
            return new com.github.stephengold.joltjni.CapsuleShape((float) (capsule.length() * 0.5), (float) capsule.radius());
        }
        throw new IllegalArgumentException("Unsupported shape: " + definition.shape().getClass().getName());
    }

    private static RVec3 toRVec3(PhysicsVector3 vector) {
        return new RVec3(vector.x(), vector.y(), vector.z());
    }

    private static Quat toQuat(PhysicsQuaternion quaternion) {
        return new Quat((float) quaternion.x(), (float) quaternion.y(), (float) quaternion.z(), (float) quaternion.w());
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }
}
