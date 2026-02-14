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

package org.dynamisfx.collision;

import org.dynamisfx.geometry.Vector3D;

/**
 * Basic position/velocity response solver for collision contacts.
 */
public final class ContactSolver3D<T> implements CollisionResponder3D<T> {

    private final RigidBodyAdapter3D<T> adapter;
    private double positionCorrectionPercent = 0.8;
    private double positionCorrectionSlop = 0.001;

    public ContactSolver3D(RigidBodyAdapter3D<T> adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter must not be null");
        }
        this.adapter = adapter;
    }

    public void setPositionCorrectionPercent(double value) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("positionCorrectionPercent must be in [0,1]");
        }
        this.positionCorrectionPercent = value;
    }

    public void setPositionCorrectionSlop(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException("positionCorrectionSlop must be >= 0");
        }
        this.positionCorrectionSlop = value;
    }

    @Override
    public void resolve(CollisionEvent<T> event) {
        if (event == null || !event.responseEnabled() || event.type() == CollisionEventType.EXIT || event.manifold() == null) {
            return;
        }
        solvePosition(event.pair(), event.manifold());
        solveVelocity(event.pair(), event.manifold(), WarmStartImpulse.ZERO);
    }

    public void solve(CollisionPair<T> pair, ContactManifold3D contact) {
        solvePosition(pair, contact);
        solveVelocity(pair, contact, WarmStartImpulse.ZERO);
    }

    public void solvePosition(CollisionEvent<T> event) {
        if (event == null || !event.responseEnabled() || event.type() == CollisionEventType.EXIT || event.manifold() == null) {
            return;
        }
        solvePosition(event.pair(), event.manifold());
    }

    public WarmStartImpulse solveVelocity(CollisionEvent<T> event, WarmStartImpulse warmStart) {
        if (event == null || !event.responseEnabled() || event.type() == CollisionEventType.EXIT || event.manifold() == null) {
            return WarmStartImpulse.ZERO;
        }
        return solveVelocity(event.pair(), event.manifold(), warmStart == null ? WarmStartImpulse.ZERO : warmStart);
    }

    public void solvePosition(CollisionPair<T> pair, ContactManifold3D contact) {
        if (pair == null || contact == null) {
            throw new IllegalArgumentException("pair and contact must not be null");
        }
        T bodyA = pair.first();
        T bodyB = pair.second();
        CollisionManifold3D manifold = contact.manifold();

        double invMassA = Math.max(0.0, adapter.getInverseMass(bodyA));
        double invMassB = Math.max(0.0, adapter.getInverseMass(bodyB));
        double invMassSum = invMassA + invMassB;
        if (invMassSum <= 0.0) {
            return;
        }

        Vector3D normal = new Vector3D(manifold.normalX(), manifold.normalY(), manifold.normalZ());
        solvePosition(bodyA, bodyB, normal, manifold.penetrationDepth(), invMassA, invMassB, invMassSum);
    }

    public WarmStartImpulse solveVelocity(CollisionPair<T> pair, ContactManifold3D contact, WarmStartImpulse warmStart) {
        if (pair == null || contact == null) {
            throw new IllegalArgumentException("pair and contact must not be null");
        }
        T bodyA = pair.first();
        T bodyB = pair.second();
        CollisionManifold3D manifold = contact.manifold();

        double invMassA = Math.max(0.0, adapter.getInverseMass(bodyA));
        double invMassB = Math.max(0.0, adapter.getInverseMass(bodyB));
        double invMassSum = invMassA + invMassB;
        if (invMassSum <= 0.0) {
            return WarmStartImpulse.ZERO;
        }

        Vector3D normal = new Vector3D(manifold.normalX(), manifold.normalY(), manifold.normalZ());
        return solveVelocity(bodyA, bodyB, normal, invMassA, invMassB, invMassSum,
                warmStart == null ? WarmStartImpulse.ZERO : warmStart);
    }

    private void solvePosition(
            T bodyA,
            T bodyB,
            Vector3D normal,
            double penetrationDepth,
            double invMassA,
            double invMassB,
            double invMassSum) {

        double correctionMagnitude = Math.max(0.0, penetrationDepth - positionCorrectionSlop)
                * positionCorrectionPercent / invMassSum;
        if (correctionMagnitude <= 0.0) {
            return;
        }
        Vector3D correction = scale(normal, correctionMagnitude);

        Vector3D positionA = adapter.getPosition(bodyA);
        Vector3D positionB = adapter.getPosition(bodyB);
        adapter.setPosition(bodyA, sub(positionA, scale(correction, invMassA)));
        adapter.setPosition(bodyB, add(positionB, scale(correction, invMassB)));
    }

    private WarmStartImpulse solveVelocity(
            T bodyA,
            T bodyB,
            Vector3D normal,
            double invMassA,
            double invMassB,
            double invMassSum,
            WarmStartImpulse warmStart) {

        Vector3D velocityA = adapter.getVelocity(bodyA);
        Vector3D velocityB = adapter.getVelocity(bodyB);

        Vector3D tangentDir = tangentDirection(sub(velocityB, velocityA), normal);
        if (Math.abs(warmStart.normalImpulse()) > 0.0 || Math.abs(warmStart.tangentImpulse()) > 0.0) {
            Vector3D warmImpulse = add(
                    scale(normal, warmStart.normalImpulse()),
                    scale(tangentDir, warmStart.tangentImpulse()));
            velocityA = sub(velocityA, scale(warmImpulse, invMassA));
            velocityB = add(velocityB, scale(warmImpulse, invMassB));
        }

        Vector3D relativeVelocity = sub(velocityB, velocityA);
        double velocityAlongNormal = dot(relativeVelocity, normal);
        double accumulatedNormal = warmStart.normalImpulse();
        double accumulatedTangent = warmStart.tangentImpulse();
        if (velocityAlongNormal > 0.0) {
            adapter.setVelocity(bodyA, velocityA);
            adapter.setVelocity(bodyB, velocityB);
            return new WarmStartImpulse(accumulatedNormal, accumulatedTangent);
        }

        double restitution = Math.min(
                clamp01(adapter.getRestitution(bodyA)),
                clamp01(adapter.getRestitution(bodyB)));

        double impulseScalar = -(1.0 + restitution) * velocityAlongNormal / invMassSum;
        accumulatedNormal = Math.max(0.0, accumulatedNormal + impulseScalar);
        Vector3D impulse = scale(normal, impulseScalar);
        velocityA = sub(velocityA, scale(impulse, invMassA));
        velocityB = add(velocityB, scale(impulse, invMassB));

        Vector3D rvAfterNormal = sub(velocityB, velocityA);
        Vector3D tangent = tangentDirection(rvAfterNormal, normal);
        double jt = -dot(rvAfterNormal, tangent) / invMassSum;
        double friction = Math.sqrt(
                Math.max(0.0, adapter.getFriction(bodyA))
                        * Math.max(0.0, adapter.getFriction(bodyB)));
        double maxFriction = accumulatedNormal * friction;
        double desiredTangent = accumulatedTangent + jt;
        double clampedTangent = clamp(desiredTangent, -maxFriction, maxFriction);
        double tangentDelta = clampedTangent - accumulatedTangent;
        accumulatedTangent = clampedTangent;
        if (Math.abs(tangentDelta) > 1e-12) {
            Vector3D frictionImpulse = scale(tangent, tangentDelta);
            velocityA = sub(velocityA, scale(frictionImpulse, invMassA));
            velocityB = add(velocityB, scale(frictionImpulse, invMassB));
        }

        adapter.setVelocity(bodyA, velocityA);
        adapter.setVelocity(bodyB, velocityB);
        return new WarmStartImpulse(accumulatedNormal, accumulatedTangent);
    }

    private static Vector3D tangentDirection(Vector3D relativeVelocity, Vector3D normal) {
        double tangentX = relativeVelocity.getX() - normal.getX() * dot(relativeVelocity, normal);
        double tangentY = relativeVelocity.getY() - normal.getY() * dot(relativeVelocity, normal);
        double tangentZ = relativeVelocity.getZ() - normal.getZ() * dot(relativeVelocity, normal);
        double tangentLen = Math.sqrt(tangentX * tangentX + tangentY * tangentY + tangentZ * tangentZ);
        if (tangentLen > 1e-9) {
            return new Vector3D(tangentX / tangentLen, tangentY / tangentLen, tangentZ / tangentLen);
        }
        return anyPerpendicular(normal);
    }

    private static Vector3D anyPerpendicular(Vector3D normal) {
        Vector3D axis = Math.abs(normal.getX()) < 0.9 ? new Vector3D(1, 0, 0) : new Vector3D(0, 1, 0);
        Vector3D tangent = cross(normal, axis);
        double len = Math.sqrt(dot(tangent, tangent));
        if (len <= 1e-9) {
            return new Vector3D(0, 0, 1);
        }
        return scale(tangent, 1.0 / len);
    }

    private static Vector3D cross(Vector3D a, Vector3D b) {
        return new Vector3D(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        return Math.min(1.0, value);
    }

    private static double dot(Vector3D a, Vector3D b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }

    private static Vector3D add(Vector3D a, Vector3D b) {
        return new Vector3D(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
    }

    private static Vector3D sub(Vector3D a, Vector3D b) {
        return new Vector3D(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
    }

    private static Vector3D scale(Vector3D v, double scale) {
        return new Vector3D(v.getX() * scale, v.getY() * scale, v.getZ() * scale);
    }
}
