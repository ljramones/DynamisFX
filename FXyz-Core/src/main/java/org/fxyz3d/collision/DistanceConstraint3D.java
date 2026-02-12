package org.fxyz3d.collision;

import org.fxyz3d.geometry.Vector3D;

/**
 * Keeps two bodies near a target distance.
 */
public final class DistanceConstraint3D<T> implements Constraint3D<T> {

    private final T bodyA;
    private final T bodyB;
    private final double targetDistance;
    private final double stiffness;

    public DistanceConstraint3D(T bodyA, T bodyB, double targetDistance, double stiffness) {
        if (bodyA == null || bodyB == null) {
            throw new IllegalArgumentException("bodies must not be null");
        }
        if (!Double.isFinite(targetDistance) || targetDistance < 0.0) {
            throw new IllegalArgumentException("targetDistance must be >= 0");
        }
        if (!Double.isFinite(stiffness) || stiffness < 0.0 || stiffness > 1.0) {
            throw new IllegalArgumentException("stiffness must be in [0,1]");
        }
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.targetDistance = targetDistance;
        this.stiffness = stiffness;
    }

    @Override
    public void solve(RigidBodyAdapter3D<T> adapter, double dtSeconds) {
        Vector3D pa = adapter.getPosition(bodyA);
        Vector3D pb = adapter.getPosition(bodyB);
        double dx = pb.getX() - pa.getX();
        double dy = pb.getY() - pa.getY();
        double dz = pb.getZ() - pa.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist <= 1e-9) {
            return;
        }

        double invMassA = Math.max(0.0, adapter.getInverseMass(bodyA));
        double invMassB = Math.max(0.0, adapter.getInverseMass(bodyB));
        double invMassSum = invMassA + invMassB;
        if (invMassSum <= 0.0) {
            return;
        }

        double error = dist - targetDistance;
        if (Math.abs(error) <= 1e-9) {
            return;
        }
        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;
        double correctionMag = error * stiffness;

        Vector3D correction = new Vector3D(nx * correctionMag, ny * correctionMag, nz * correctionMag);
        adapter.setPosition(bodyA, new Vector3D(
                pa.getX() + correction.getX() * (invMassA / invMassSum),
                pa.getY() + correction.getY() * (invMassA / invMassSum),
                pa.getZ() + correction.getZ() * (invMassA / invMassSum)));
        adapter.setPosition(bodyB, new Vector3D(
                pb.getX() - correction.getX() * (invMassB / invMassSum),
                pb.getY() - correction.getY() * (invMassB / invMassSum),
                pb.getZ() - correction.getZ() * (invMassB / invMassSum)));
    }
}
