package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class TerrainPatchSpawnerTest {

    @Test
    void spawnsExpectedTileGrid() {
        CapturingWorld world = new CapturingWorld();
        TerrainPatchSpec spec = new TerrainPatchSpec(10.0, 10.0, 2.0);
        TerrainPatchSpawnResult result = TerrainPatchSpawner.spawnTiles(
                world,
                ReferenceFrame.WORLD,
                spec,
                (x, y) -> x + y,
                1.5);

        assertEquals(4, result.tileCount());
        List<PhysicsBodyHandle> handles = result.tileHandles();
        assertEquals(4, world.created.size());
        PhysicsBodyState first = world.created.get(0).initialState();
        assertEquals(-5.0, first.position().x(), 1e-9);
        assertEquals(-5.0, first.position().y(), 1e-9);
        assertEquals(-11.0, first.position().z(), 1e-9); // (-5 + -5) - thickness/2
        assertEquals(1.5, first.timestampSeconds(), 1e-9);
    }

    @Test
    void validatesSamplerOutput() {
        CapturingWorld world = new CapturingWorld();
        assertThrows(IllegalArgumentException.class, () -> TerrainPatchSpawner.spawnTiles(
                world,
                ReferenceFrame.WORLD,
                new TerrainPatchSpec(10.0, 10.0, 1.0),
                (x, y) -> Double.NaN,
                0.0));
    }

    private static final class CapturingWorld implements RigidBodyWorld {
        private final List<PhysicsBodyDefinition> created = new ArrayList<>();
        private long next = 1L;

        @Override
        public PhysicsCapabilities capabilities() {
            return new PhysicsCapabilities(false, false, false, false, false);
        }

        @Override
        public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
            created.add(definition);
            return new PhysicsBodyHandle(next++);
        }

        @Override
        public boolean removeBody(PhysicsBodyHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsBodyHandle> bodies() {
            return List.of();
        }

        @Override
        public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean removeConstraint(PhysicsConstraintHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsConstraintHandle> constraints() {
            return List.of();
        }

        @Override
        public PhysicsRuntimeTuning runtimeTuning() {
            return new PhysicsRuntimeTuning(4, 1.0, 0.0, 1e-5, 0.1);
        }

        @Override
        public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        }

        @Override
        public PhysicsVector3 gravity() {
            return PhysicsVector3.ZERO;
        }

        @Override
        public void setGravity(PhysicsVector3 gravity) {
        }

        @Override
        public void step(double dtSeconds) {
        }
    }
}
