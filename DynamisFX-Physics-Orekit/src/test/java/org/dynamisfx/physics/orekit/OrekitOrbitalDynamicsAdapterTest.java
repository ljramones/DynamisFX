package org.dynamisfx.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.junit.jupiter.api.Test;

class OrekitOrbitalDynamicsAdapterTest {

    @Test
    void propagatesRegisteredBodiesToTargetTime() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0));
        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.KINEMATIC,
                1.0,
                new SphereShape(1.0),
                new PhysicsBodyState(
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(0.0, 2.0, 0.0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.ICRF,
                        0.0)));
        OrekitOrbitalDynamicsAdapter adapter = new OrekitOrbitalDynamicsAdapter(world);
        adapter.registerBody("sat-1", handle);

        Map<String, OrbitalState> states = adapter.propagateTo(List.of("sat-1"), 3.0, ReferenceFrame.ICRF);

        OrbitalState state = states.get("sat-1");
        assertEquals(6.0, state.position().y(), 1e-9);
        assertEquals(3.0, state.timestampSeconds(), 1e-9);
        assertEquals(ReferenceFrame.ICRF, state.referenceFrame());
    }

    @Test
    void enforcesMonotonicPropagationTime() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0));
        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.KINEMATIC,
                1.0,
                new SphereShape(1.0),
                PhysicsBodyState.IDENTITY));
        OrekitOrbitalDynamicsAdapter adapter = new OrekitOrbitalDynamicsAdapter(world);
        adapter.registerBody("sat-1", handle);
        adapter.propagateTo(List.of("sat-1"), 1.0, ReferenceFrame.ICRF);
        assertThrows(IllegalArgumentException.class, () -> adapter.propagateTo(List.of("sat-1"), 0.5, ReferenceFrame.ICRF));
    }

    @Test
    void skipsUnknownObjectIdsAndSupportsUnregister() {
        OrekitWorld world = new OrekitWorld(new PhysicsWorldConfiguration(
                ReferenceFrame.ICRF,
                PhysicsVector3.ZERO,
                1.0));
        PhysicsBodyHandle handle = world.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.KINEMATIC,
                1.0,
                new SphereShape(1.0),
                PhysicsBodyState.IDENTITY));
        OrekitOrbitalDynamicsAdapter adapter = new OrekitOrbitalDynamicsAdapter(world);
        adapter.registerBody("sat-1", handle);
        assertTrue(adapter.unregisterBody("sat-1"));

        Map<String, OrbitalState> states = adapter.propagateTo(List.of("sat-1", "missing"), 1.0, ReferenceFrame.ICRF);
        assertEquals(0, states.size());
    }
}
