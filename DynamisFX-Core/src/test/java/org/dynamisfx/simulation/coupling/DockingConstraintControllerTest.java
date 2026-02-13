package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class DockingConstraintControllerTest {

    @Test
    void latchesAndReleasesWithHysteresis() {
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        DockingConstraintController controller = new DockingConstraintController(registry);
        FakeWorld world = new FakeWorld();
        PhysicsZone zone = new StubZone(world);
        PhysicsBodyHandle objectHandle = new PhysicsBodyHandle(1);
        PhysicsBodyHandle targetHandle = new PhysicsBodyHandle(2);
        world.states.put(objectHandle, stateAt(0.5, 0, 0));
        world.states.put(targetHandle, stateAt(0, 0, 0));
        registry.bind("lander-1", zone.zoneId(), objectHandle);

        boolean latched = controller.updateLatch(zone, "lander-1", targetHandle, 1.0, 2.0, false);
        assertTrue(latched);
        assertTrue(controller.isLatched("lander-1"));
        assertTrue(world.createdConstraints == 1);

        world.states.put(objectHandle, stateAt(1.5, 0, 0));
        latched = controller.updateLatch(zone, "lander-1", targetHandle, 1.0, 2.0, false);
        assertTrue(latched);

        world.states.put(objectHandle, stateAt(3.0, 0, 0));
        latched = controller.updateLatch(zone, "lander-1", targetHandle, 1.0, 2.0, false);
        assertFalse(latched);
        assertFalse(controller.isLatched("lander-1"));
        assertTrue(world.removedConstraints == 1);
    }

    @Test
    void releasesWhenBindingDisappears() {
        ZoneBodyRegistry registry = new ZoneBodyRegistry();
        DockingConstraintController controller = new DockingConstraintController(registry);
        FakeWorld world = new FakeWorld();
        PhysicsZone zone = new StubZone(world);
        PhysicsBodyHandle objectHandle = new PhysicsBodyHandle(1);
        PhysicsBodyHandle targetHandle = new PhysicsBodyHandle(2);
        world.states.put(objectHandle, stateAt(0.0, 0.0, 0.0));
        world.states.put(targetHandle, stateAt(0.0, 0.0, 0.0));
        registry.bind("lander-1", zone.zoneId(), objectHandle);
        assertTrue(controller.updateLatch(zone, "lander-1", targetHandle, 1.0, 2.0, false));

        registry.unbind("lander-1");
        assertFalse(controller.updateLatch(zone, "lander-1", targetHandle, 1.0, 2.0, false));
        assertFalse(controller.isLatched("lander-1"));
    }

    private static PhysicsBodyState stateAt(double x, double y, double z) {
        return new PhysicsBodyState(
                new PhysicsVector3(x, y, z),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                0.0);
    }

    private record StubZone(RigidBodyWorld world) implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
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
            return 1000.0;
        }
    }

    private static final class FakeWorld implements RigidBodyWorld {
        private final Map<PhysicsBodyHandle, PhysicsBodyState> states = new LinkedHashMap<>();
        private final Map<PhysicsConstraintHandle, PhysicsConstraintDefinition> constraints = new LinkedHashMap<>();
        private long nextConstraint = 1;
        private int createdConstraints;
        private int removedConstraints;

        @Override
        public PhysicsCapabilities capabilities() {
            return new PhysicsCapabilities(true, false, true, false, false);
        }

        @Override
        public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean removeBody(PhysicsBodyHandle handle) {
            return states.remove(handle) != null;
        }

        @Override
        public Collection<PhysicsBodyHandle> bodies() {
            return new ArrayList<>(states.keySet());
        }

        @Override
        public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
            PhysicsBodyState state = states.get(handle);
            if (state == null) {
                throw new IllegalArgumentException("unknown handle");
            }
            return state;
        }

        @Override
        public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
            states.put(handle, state);
        }

        @Override
        public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
            PhysicsConstraintHandle handle = new PhysicsConstraintHandle(nextConstraint++);
            constraints.put(handle, definition);
            createdConstraints++;
            return handle;
        }

        @Override
        public boolean removeConstraint(PhysicsConstraintHandle handle) {
            boolean removed = constraints.remove(handle) != null;
            if (removed) {
                removedConstraints++;
            }
            return removed;
        }

        @Override
        public Collection<PhysicsConstraintHandle> constraints() {
            return List.copyOf(constraints.keySet());
        }

        @Override
        public PhysicsRuntimeTuning runtimeTuning() {
            return new PhysicsRuntimeTuning(4, 1.0, 0.0, 1e-5, 0.1);
        }

        @Override
        public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        }

        @Override
        public void step(double dtSeconds) {
        }
    }
}
