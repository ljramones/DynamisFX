package org.dynamisfx.physics.ode4j;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.dynamisfx.physics.api.OverlapSphereQuery;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.QueryCapability;
import org.dynamisfx.physics.api.RaycastHit;
import org.dynamisfx.physics.api.RaycastRequest;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsConstraintType;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.CapsuleShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsShape;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
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
            true);
    private static final int MAX_CONTACTS = 8;

    private final PhysicsWorldConfiguration configuration;
    private final Map<PhysicsBodyHandle, BodyRecord> bodies = new LinkedHashMap<>();
    private final Map<PhysicsConstraintHandle, ConstraintRecord> constraints = new LinkedHashMap<>();
    private final DWorld world;
    private final DSpace space;
    private final DJointGroup contactGroup;
    private final QueryCapability queryCapability = new Ode4jQueryCapability();
    private PhysicsRuntimeTuning runtimeTuning;
    private long nextHandleValue;
    private long nextConstraintHandleValue;
    private double timeSeconds;
    private PhysicsVector3 gravity;
    private boolean closed;

    public Ode4jWorld(PhysicsWorldConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        world = OdeHelper.createWorld();
        gravity = configuration.gravity();
        world.setGravity(
                gravity.x(),
                gravity.y(),
                gravity.z());
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
        world.setGravity(gravity.x(), gravity.y(), gravity.z());
    }

    @Override
    public Optional<QueryCapability> queryCapability() {
        ensureOpen();
        return Optional.of(queryCapability);
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

    private final class Ode4jQueryCapability implements QueryCapability {

        @Override
        public Optional<RaycastHit> raycast(RaycastRequest request) {
            Objects.requireNonNull(request, "request must not be null");
            PhysicsVector3 direction = normalize(request.direction());
            List<RaycastHit> hits = bodies.entrySet().stream()
                    .map(entry -> raycastAgainstBody(entry.getKey(), entry.getValue(), request.origin(), direction, request.maxDistanceMeters()))
                    .filter(Objects::nonNull)
                    .sorted(Comparator
                            .comparingDouble(RaycastHit::distanceMeters)
                            .thenComparingLong(hit -> hit.bodyHandle().value()))
                    .toList();
            return hits.isEmpty() ? Optional.empty() : Optional.of(hits.get(0));
        }

        @Override
        public List<PhysicsBodyHandle> overlapSphere(OverlapSphereQuery query) {
            Objects.requireNonNull(query, "query must not be null");
            Set<PhysicsBodyHandle> hits = new LinkedHashSet<>();
            for (Map.Entry<PhysicsBodyHandle, BodyRecord> entry : bodies.entrySet()) {
                if (intersectsSphere(entry.getValue(), query.center(), query.radiusMeters())) {
                    hits.add(entry.getKey());
                    if (hits.size() >= query.maxResults()) {
                        break;
                    }
                }
            }
            return hits.stream()
                    .sorted(Comparator.comparingLong(PhysicsBodyHandle::value))
                    .toList();
        }
    }

    private static RaycastHit raycastAgainstBody(
            PhysicsBodyHandle handle,
            BodyRecord record,
            PhysicsVector3 origin,
            PhysicsVector3 directionNormalized,
            double maxDistanceMeters) {
        PhysicsBodyState state = record.state();
        PhysicsVector3 localOrigin = toLocal(state, origin);
        PhysicsVector3 localDirection = rotate(inverseNormalized(state.orientation()), directionNormalized);

        PhysicsShape shape = record.definition().shape();
        Double localDistance = null;
        PhysicsVector3 localNormal = null;
        if (shape instanceof SphereShape sphere) {
            RaycastResult result = intersectSphereLocal(localOrigin, localDirection, sphere.radius());
            if (result != null) {
                localDistance = result.distance();
                localNormal = result.normal();
            }
        } else if (shape instanceof BoxShape box) {
            RaycastResult result = intersectBoxLocal(localOrigin, localDirection, box.width(), box.height(), box.depth());
            if (result != null) {
                localDistance = result.distance();
                localNormal = result.normal();
            }
        } else if (shape instanceof CapsuleShape capsule) {
            // Conservative approximation to keep query semantics stable.
            double approxRadius = capsule.radius() + (capsule.length() * 0.5);
            RaycastResult result = intersectSphereLocal(localOrigin, localDirection, approxRadius);
            if (result != null) {
                localDistance = result.distance();
                localNormal = result.normal();
            }
        }
        if (localDistance == null || localDistance < 0.0 || localDistance > maxDistanceMeters) {
            return null;
        }
        PhysicsVector3 worldPoint = add(origin, scale(directionNormalized, localDistance));
        PhysicsVector3 worldNormal = rotate(normalized(state.orientation()), localNormal);
        return new RaycastHit(handle, worldPoint, worldNormal, localDistance);
    }

    private static boolean intersectsSphere(BodyRecord record, PhysicsVector3 center, double radius) {
        PhysicsBodyState state = record.state();
        PhysicsVector3 localCenter = toLocal(state, center);
        PhysicsShape shape = record.definition().shape();
        if (shape instanceof SphereShape sphere) {
            return normSquared(localCenter) <= square(radius + sphere.radius());
        }
        if (shape instanceof BoxShape box) {
            double hx = box.width() * 0.5;
            double hy = box.height() * 0.5;
            double hz = box.depth() * 0.5;
            double dx = Math.max(Math.abs(localCenter.x()) - hx, 0.0);
            double dy = Math.max(Math.abs(localCenter.y()) - hy, 0.0);
            double dz = Math.max(Math.abs(localCenter.z()) - hz, 0.0);
            return (dx * dx + dy * dy + dz * dz) <= square(radius);
        }
        if (shape instanceof CapsuleShape capsule) {
            double approxRadius = capsule.radius() + (capsule.length() * 0.5);
            return normSquared(localCenter) <= square(radius + approxRadius);
        }
        return false;
    }

    private static RaycastResult intersectSphereLocal(PhysicsVector3 origin, PhysicsVector3 dir, double radius) {
        double b = 2.0 * dot(origin, dir);
        double c = dot(origin, origin) - (radius * radius);
        double disc = (b * b) - (4.0 * c);
        if (disc < 0.0) {
            return null;
        }
        double sqrtDisc = Math.sqrt(disc);
        double t0 = (-b - sqrtDisc) * 0.5;
        double t1 = (-b + sqrtDisc) * 0.5;
        double t = t0 >= 0.0 ? t0 : t1;
        if (t < 0.0) {
            return null;
        }
        PhysicsVector3 hit = add(origin, scale(dir, t));
        return new RaycastResult(t, normalize(hit));
    }

    private static RaycastResult intersectBoxLocal(PhysicsVector3 origin, PhysicsVector3 dir, double w, double h, double d) {
        double[] min = {-w * 0.5, -h * 0.5, -d * 0.5};
        double[] max = {w * 0.5, h * 0.5, d * 0.5};
        double[] o = {origin.x(), origin.y(), origin.z()};
        double[] v = {dir.x(), dir.y(), dir.z()};
        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;
        int hitAxis = -1;
        double hitSign = 1.0;
        for (int axis = 0; axis < 3; axis++) {
            double vd = v[axis];
            if (Math.abs(vd) < 1e-12) {
                if (o[axis] < min[axis] || o[axis] > max[axis]) {
                    return null;
                }
                continue;
            }
            double t1 = (min[axis] - o[axis]) / vd;
            double t2 = (max[axis] - o[axis]) / vd;
            double near = Math.min(t1, t2);
            double far = Math.max(t1, t2);
            if (near > tMin) {
                tMin = near;
                hitAxis = axis;
                hitSign = t1 > t2 ? 1.0 : -1.0;
            }
            tMax = Math.min(tMax, far);
            if (tMin > tMax) {
                return null;
            }
        }
        if (tMin < 0.0) {
            return null;
        }
        PhysicsVector3 normal = switch (hitAxis) {
            case 0 -> new PhysicsVector3(hitSign, 0.0, 0.0);
            case 1 -> new PhysicsVector3(0.0, hitSign, 0.0);
            case 2 -> new PhysicsVector3(0.0, 0.0, hitSign);
            default -> PhysicsVector3.ZERO;
        };
        return new RaycastResult(tMin, normal);
    }

    private static PhysicsVector3 toLocal(PhysicsBodyState state, PhysicsVector3 worldPoint) {
        PhysicsVector3 delta = subtract(worldPoint, state.position());
        return rotate(inverseNormalized(state.orientation()), delta);
    }

    private static PhysicsVector3 normalize(PhysicsVector3 v) {
        double n2 = normSquared(v);
        if (!(n2 > 0.0)) {
            return PhysicsVector3.ZERO;
        }
        double inv = 1.0 / Math.sqrt(n2);
        return new PhysicsVector3(v.x() * inv, v.y() * inv, v.z() * inv);
    }

    private static PhysicsQuaternion normalized(PhysicsQuaternion q) {
        double n2 = (q.x() * q.x()) + (q.y() * q.y()) + (q.z() * q.z()) + (q.w() * q.w());
        if (!(n2 > 0.0)) {
            return PhysicsQuaternion.IDENTITY;
        }
        double inv = 1.0 / Math.sqrt(n2);
        return new PhysicsQuaternion(q.x() * inv, q.y() * inv, q.z() * inv, q.w() * inv);
    }

    private static PhysicsQuaternion inverseNormalized(PhysicsQuaternion q) {
        PhysicsQuaternion n = normalized(q);
        return new PhysicsQuaternion(-n.x(), -n.y(), -n.z(), n.w());
    }

    private static PhysicsVector3 rotate(PhysicsQuaternion rotation, PhysicsVector3 vector) {
        PhysicsQuaternion v = new PhysicsQuaternion(vector.x(), vector.y(), vector.z(), 0.0);
        PhysicsQuaternion result = multiply(multiply(rotation, v), conjugate(rotation));
        return new PhysicsVector3(result.x(), result.y(), result.z());
    }

    private static PhysicsQuaternion multiply(PhysicsQuaternion a, PhysicsQuaternion b) {
        return new PhysicsQuaternion(
                (a.w() * b.x()) + (a.x() * b.w()) + (a.y() * b.z()) - (a.z() * b.y()),
                (a.w() * b.y()) - (a.x() * b.z()) + (a.y() * b.w()) + (a.z() * b.x()),
                (a.w() * b.z()) + (a.x() * b.y()) - (a.y() * b.x()) + (a.z() * b.w()),
                (a.w() * b.w()) - (a.x() * b.x()) - (a.y() * b.y()) - (a.z() * b.z()));
    }

    private static PhysicsQuaternion conjugate(PhysicsQuaternion q) {
        return new PhysicsQuaternion(-q.x(), -q.y(), -q.z(), q.w());
    }

    private static PhysicsVector3 add(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    private static PhysicsVector3 subtract(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    private static PhysicsVector3 scale(PhysicsVector3 v, double s) {
        return new PhysicsVector3(v.x() * s, v.y() * s, v.z() * s);
    }

    private static double dot(PhysicsVector3 a, PhysicsVector3 b) {
        return (a.x() * b.x()) + (a.y() * b.y()) + (a.z() * b.z());
    }

    private static double normSquared(PhysicsVector3 v) {
        return dot(v, v);
    }

    private static double square(double v) {
        return v * v;
    }

    private record RaycastResult(double distance, PhysicsVector3 normal) {
    }
}
