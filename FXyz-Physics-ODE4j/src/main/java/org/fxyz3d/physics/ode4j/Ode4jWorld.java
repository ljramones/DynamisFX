package org.fxyz3d.physics.ode4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.api.PhysicsCapabilities;
import org.fxyz3d.physics.api.PhysicsConstraintDefinition;
import org.fxyz3d.physics.api.PhysicsConstraintHandle;
import org.fxyz3d.physics.api.PhysicsConstraintType;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.BoxShape;
import org.fxyz3d.physics.model.CapsuleShape;
import org.fxyz3d.physics.model.PhysicsBodyDefinition;
import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsBodyType;
import org.fxyz3d.physics.model.PhysicsQuaternion;
import org.fxyz3d.physics.model.PhysicsRuntimeTuning;
import org.fxyz3d.physics.model.PhysicsShape;
import org.fxyz3d.physics.model.PhysicsVector3;
import org.fxyz3d.physics.model.ReferenceFrame;
import org.fxyz3d.physics.model.SphereShape;
import org.fxyz3d.physics.model.PhysicsWorldConfiguration;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DQuaternionC;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBallJoint;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DFixedJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DHingeJoint;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeConstants;
import org.ode4j.ode.DSliderJoint;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

/**
 * ODE4j world implementation bound to the engine runtime.
 */
public final class Ode4jWorld implements PhysicsWorld {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            false,
            false);
    private static final int MAX_CONTACTS = 8;

    private final PhysicsWorldConfiguration configuration;
    private final Map<PhysicsBodyHandle, BodyRecord> bodies = new LinkedHashMap<>();
    private final Map<PhysicsConstraintHandle, ConstraintRecord> constraints = new LinkedHashMap<>();
    private final DWorld world;
    private final DSpace space;
    private final DJointGroup contactGroup;
    private PhysicsRuntimeTuning runtimeTuning;
    private long nextHandleValue;
    private long nextConstraintHandleValue;
    private double timeSeconds;
    private boolean closed;

    public Ode4jWorld(PhysicsWorldConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        world = OdeHelper.createWorld();
        world.setGravity(
                configuration.gravity().x(),
                configuration.gravity().y(),
                configuration.gravity().z());
        runtimeTuning = configuration.runtimeTuning();
        world.setQuickStepNumIterations(runtimeTuning.solverIterations());
        space = OdeHelper.createHashSpace();
        contactGroup = OdeHelper.createJointGroup();
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        ensureOpen();
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        DBody body = OdeHelper.createBody(world);
        DGeom geom = createGeom(definition.shape());
        geom.setBody(body);

        applyMass(body, definition);
        configureBodyType(body, definition.bodyType());
        applyBodyState(body, definition.initialState());

        PhysicsBodyHandle handle = new PhysicsBodyHandle(nextHandleValue++);
        bodies.put(handle, new BodyRecord(definition, body, geom, definition.initialState()));
        return handle;
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        ensureOpen();
        if (handle == null) {
            return false;
        }
        BodyRecord removed = bodies.remove(handle);
        if (removed == null) {
            return false;
        }
        removed.geom().destroy();
        removed.body().destroy();
        return true;
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        ensureOpen();
        return Set.copyOf(bodies.keySet());
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        ensureOpen();
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        BodyRecord a = requireRecord(definition.bodyA());
        BodyRecord b = requireRecord(definition.bodyB());
        DJoint joint;
        if (definition.type() == PhysicsConstraintType.FIXED) {
            DFixedJoint fixed = OdeHelper.createFixedJoint(world);
            fixed.attach(a.body(), b.body());
            fixed.setFixed();
            joint = fixed;
        } else if (definition.type() == PhysicsConstraintType.BALL) {
            DBallJoint ball = OdeHelper.createBallJoint(world);
            ball.attach(a.body(), b.body());
            if (definition.anchorWorld() != null) {
                ball.setAnchor(
                        definition.anchorWorld().x(),
                        definition.anchorWorld().y(),
                        definition.anchorWorld().z());
            }
            joint = ball;
        } else if (definition.type() == PhysicsConstraintType.HINGE) {
            DHingeJoint hinge = OdeHelper.createHingeJoint(world);
            hinge.attach(a.body(), b.body());
            if (definition.anchorWorld() != null) {
                hinge.setAnchor(
                        definition.anchorWorld().x(),
                        definition.anchorWorld().y(),
                        definition.anchorWorld().z());
            }
            hinge.setAxis(
                    definition.axisWorld().x(),
                    definition.axisWorld().y(),
                    definition.axisWorld().z());
            if (definition.lowerLimit() != null) {
                hinge.setParamLoStop(definition.lowerLimit());
            }
            if (definition.upperLimit() != null) {
                hinge.setParamHiStop(definition.upperLimit());
            }
            joint = hinge;
        } else if (definition.type() == PhysicsConstraintType.SLIDER) {
            DSliderJoint slider = OdeHelper.createSliderJoint(world);
            slider.attach(a.body(), b.body());
            slider.setAxis(
                    definition.axisWorld().x(),
                    definition.axisWorld().y(),
                    definition.axisWorld().z());
            if (definition.lowerLimit() != null) {
                slider.setParamLoStop(definition.lowerLimit());
            }
            if (definition.upperLimit() != null) {
                slider.setParamHiStop(definition.upperLimit());
            }
            joint = slider;
        } else {
            throw new IllegalArgumentException("unsupported constraint type: " + definition.type());
        }
        PhysicsConstraintHandle handle = new PhysicsConstraintHandle(nextConstraintHandleValue++);
        constraints.put(handle, new ConstraintRecord(definition, joint));
        return handle;
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        ensureOpen();
        if (handle == null) {
            return false;
        }
        ConstraintRecord removed = constraints.remove(handle);
        if (removed == null) {
            return false;
        }
        removed.joint().destroy();
        return true;
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        ensureOpen();
        return Set.copyOf(constraints.keySet());
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
        world.setQuickStepNumIterations(tuning.solverIterations());
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        ensureOpen();
        BodyRecord record = requireRecord(handle);
        PhysicsBodyState state = readBodyState(record.body(), record.state().referenceFrame(), record.state().timestampSeconds());
        record.state = state;
        return state;
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        ensureOpen();
        BodyRecord record = requireRecord(handle);
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        applyBodyState(record.body(), state);
        record.state = state;
    }

    @Override
    public void step(double dtSeconds) {
        ensureOpen();
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }
        space.collide(null, this::nearCallback);
        world.quickStep(dtSeconds);
        contactGroup.empty();
        timeSeconds += dtSeconds;
        for (BodyRecord record : bodies.values()) {
            record.state = readBodyState(record.body(), record.state().referenceFrame(), timeSeconds);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        for (BodyRecord record : bodies.values()) {
            record.geom().destroy();
            record.body().destroy();
        }
        bodies.clear();
        for (ConstraintRecord constraint : constraints.values()) {
            constraint.joint().destroy();
        }
        constraints.clear();
        contactGroup.destroy();
        space.destroy();
        world.destroy();
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("world is closed");
        }
    }

    private BodyRecord requireRecord(PhysicsBodyHandle handle) {
        if (handle == null) {
            throw new IllegalArgumentException("handle must not be null");
        }
        BodyRecord record = bodies.get(handle);
        if (record == null) {
            throw new IllegalArgumentException("unknown handle: " + handle.value());
        }
        return record;
    }

    private void nearCallback(Object data, DGeom g1, DGeom g2) {
        DBody b1 = g1.getBody();
        DBody b2 = g2.getBody();
        if (b1 != null && b2 != null && OdeHelper.areConnectedExcluding(b1, b2, org.ode4j.ode.DContactJoint.class)) {
            return;
        }

        DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);
        int count = OdeHelper.collide(g1, g2, MAX_CONTACTS, contacts.getGeomBuffer());
        for (int i = 0; i < count; i++) {
            DContact contact = contacts.get(i);
            contact.surface.mode = OdeConstants.dContactBounce | OdeConstants.dContactSoftCFM;
            contact.surface.mu = runtimeTuning.contactFriction();
            contact.surface.bounce = runtimeTuning.contactBounce();
            contact.surface.bounce_vel = runtimeTuning.contactBounceVelocity();
            contact.surface.soft_cfm = runtimeTuning.contactSoftCfm();
            DJoint joint = OdeHelper.createContactJoint(world, contactGroup, contact);
            joint.attach(b1, b2);
        }
    }

    private DGeom createGeom(PhysicsShape shape) {
        if (shape instanceof BoxShape box) {
            return OdeHelper.createBox(space, box.width(), box.height(), box.depth());
        }
        if (shape instanceof SphereShape sphere) {
            return OdeHelper.createSphere(space, sphere.radius());
        }
        if (shape instanceof CapsuleShape capsule) {
            return OdeHelper.createCapsule(space, capsule.radius(), capsule.length());
        }
        throw new IllegalArgumentException("unsupported shape: " + shape.getClass().getName());
    }

    private static void applyMass(DBody body, PhysicsBodyDefinition definition) {
        DMass mass = OdeHelper.createMass();
        PhysicsShape shape = definition.shape();
        double massKg = definition.massKg();
        if (!(massKg > 0.0)) {
            massKg = 1.0;
        }
        if (shape instanceof BoxShape box) {
            mass.setBoxTotal(massKg, box.width(), box.height(), box.depth());
        } else if (shape instanceof SphereShape sphere) {
            mass.setSphereTotal(massKg, sphere.radius());
        } else if (shape instanceof CapsuleShape capsule) {
            mass.setCapsuleTotal(massKg, 3, capsule.radius(), capsule.length());
        } else {
            throw new IllegalArgumentException("unsupported shape: " + shape.getClass().getName());
        }
        body.setMass(mass);
    }

    private static void configureBodyType(DBody body, PhysicsBodyType bodyType) {
        switch (bodyType) {
            case STATIC -> {
                body.setKinematic();
                body.setGravityMode(false);
            }
            case KINEMATIC -> {
                body.setKinematic();
                body.setGravityMode(false);
            }
            case DYNAMIC -> {
                body.setDynamic();
                body.setGravityMode(true);
            }
        }
    }

    private static void applyBodyState(DBody body, PhysicsBodyState state) {
        body.setPosition(
                state.position().x(),
                state.position().y(),
                state.position().z());
        body.setLinearVel(
                state.linearVelocity().x(),
                state.linearVelocity().y(),
                state.linearVelocity().z());
        body.setAngularVel(
                state.angularVelocity().x(),
                state.angularVelocity().y(),
                state.angularVelocity().z());
        // ODE quaternion is (w, x, y, z)
        DQuaternion q = new DQuaternion(
                state.orientation().w(),
                state.orientation().x(),
                state.orientation().y(),
                state.orientation().z());
        body.setQuaternion(q);
    }

    private static PhysicsBodyState readBodyState(DBody body, ReferenceFrame frame, double timestampSeconds) {
        DVector3C position = body.getPosition();
        DVector3C linearVelocity = body.getLinearVel();
        DVector3C angularVelocity = body.getAngularVel();
        DQuaternionC q = body.getQuaternion();
        return new PhysicsBodyState(
                new PhysicsVector3(position.get0(), position.get1(), position.get2()),
                new PhysicsQuaternion(q.get1(), q.get2(), q.get3(), q.get0()),
                new PhysicsVector3(
                        linearVelocity.get0(),
                        linearVelocity.get1(),
                        linearVelocity.get2()),
                new PhysicsVector3(
                        angularVelocity.get0(),
                        angularVelocity.get1(),
                        angularVelocity.get2()),
                frame,
                timestampSeconds);
    }

    private static final class BodyRecord {
        private final PhysicsBodyDefinition definition;
        private final DBody body;
        private final DGeom geom;
        private PhysicsBodyState state;

        private BodyRecord(PhysicsBodyDefinition definition, DBody body, DGeom geom, PhysicsBodyState state) {
            this.definition = definition;
            this.body = body;
            this.geom = geom;
            this.state = state;
        }

        private PhysicsBodyDefinition definition() {
            return definition;
        }

        private PhysicsBodyState state() {
            return state;
        }

        private DBody body() {
            return body;
        }

        private DGeom geom() {
            return geom;
        }
    }

    private record ConstraintRecord(PhysicsConstraintDefinition definition, DJoint joint) {
    }
}
