package org.dynamisfx.physics.ode4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class Ode4jRigidBodyWorldAdapterTest {

    @Test
    void supportsBodyLifecycleAndBulkRead() {
        Ode4jRigidBodyWorldAdapter adapter = new Ode4jRigidBodyWorldAdapter(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 60.0));

        PhysicsBodyHandle handle = adapter.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));
        adapter.step(0.25);

        Map<PhysicsBodyHandle, Ode4jRigidBodyWorldAdapter.TransformSnapshot> snapshots =
                adapter.readTransforms(List.of(handle));

        assertEquals(1, snapshots.size());
        assertTrue(snapshots.containsKey(handle));
        assertTrue(snapshots.get(handle).timestampSeconds() > 0.0);
        assertTrue(adapter.removeBody(handle));
        assertFalse(adapter.removeBody(handle));
        adapter.close();
    }
}
