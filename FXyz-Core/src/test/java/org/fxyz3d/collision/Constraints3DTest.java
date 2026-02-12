package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fxyz3d.geometry.Vector3D;
import org.junit.jupiter.api.Test;

class Constraints3DTest {

    private static final RigidBodyAdapter3D<Body> ADAPTER = new RigidBodyAdapter3D<>() {
        @Override
        public Vector3D getPosition(Body body) {
            return body.position;
        }

        @Override
        public void setPosition(Body body, Vector3D position) {
            body.position = position;
        }

        @Override
        public Vector3D getVelocity(Body body) {
            return body.velocity;
        }

        @Override
        public void setVelocity(Body body, Vector3D velocity) {
            body.velocity = velocity;
        }

        @Override
        public double getInverseMass(Body body) {
            return body.inverseMass;
        }

        @Override
        public double getRestitution(Body body) {
            return 0;
        }

        @Override
        public double getFriction(Body body) {
            return 0;
        }
    };

    @Test
    void distanceConstraintConvergesTowardTarget() {
        Body a = new Body(new Vector3D(0, 0, 0), 1.0);
        Body b = new Body(new Vector3D(4, 0, 0), 1.0);
        DistanceConstraint3D<Body> c = new DistanceConstraint3D<>(a, b, 2.0, 1.0);

        c.solve(ADAPTER, 0.016);
        double dx = b.position.getX() - a.position.getX();
        assertEquals(2.0, Math.abs(dx), 1e-9);
    }

    @Test
    void pointConstraintPullsBodyToAnchor() {
        Body a = new Body(new Vector3D(10, 0, 0), 1.0);
        PointConstraint3D<Body> c = new PointConstraint3D<>(a, new Vector3D(1, 2, 3), 1.0);
        c.solve(ADAPTER, 0.016);

        assertEquals(1.0, a.position.getX(), 1e-9);
        assertEquals(2.0, a.position.getY(), 1e-9);
        assertEquals(3.0, a.position.getZ(), 1e-9);
    }

    @Test
    void worldStepMaintainsDistanceConstraintUnderGravity() {
        Body a = new Body(new Vector3D(0, 2, 0), 1.0);
        Body b = new Body(new Vector3D(0, 4, 0), 1.0);

        CollisionWorld3D<Body> world = new CollisionWorld3D<>(
                new SweepAndPrune3D<>(),
                Body::aabb,
                body -> CollisionFilter.DEFAULT,
                (left, right) -> ContactGenerator3D.generate(left.aabb(), right.aabb()));
        world.setBodyAdapter(ADAPTER);
        world.setGravity(new Vector3D(0, -9.8, 0));
        world.setConstraintIterations(4);
        world.addConstraint(new DistanceConstraint3D<>(a, b, 2.0, 0.8));

        for (int i = 0; i < 30; i++) {
            world.step(java.util.List.of(a, b), 1.0 / 120.0);
        }
        double dx = b.position.getX() - a.position.getX();
        double dy = b.position.getY() - a.position.getY();
        double dz = b.position.getZ() - a.position.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        assertTrue(Math.abs(dist - 2.0) < 0.2);
    }

    private static final class Body {
        private Vector3D position;
        private Vector3D velocity = new Vector3D(0, 0, 0);
        private final double inverseMass;

        private Body(Vector3D position, double inverseMass) {
            this.position = position;
            this.inverseMass = inverseMass;
        }

        private Aabb aabb() {
            return new Aabb(
                    position.getX() - 0.25, position.getY() - 0.25, position.getZ() - 0.25,
                    position.getX() + 0.25, position.getY() + 0.25, position.getZ() + 0.25);
        }
    }
}
