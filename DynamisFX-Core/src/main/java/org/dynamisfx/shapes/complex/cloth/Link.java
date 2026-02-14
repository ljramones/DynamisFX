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
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.shapes.complex.cloth;

import java.util.logging.Logger;
import org.dynamisfx.geometry.Point3D;

/**
 * A spring-like constraint that connects two {@link WeightedPoint}s in a cloth simulation.
 * <p>
 * Links maintain a target distance between two points using a stiffness coefficient.
 * When solved, the link adjusts both point positions proportionally based on their
 * inverse masses to satisfy the distance constraint.
 * </p>
 * <p>
 * This implements a position-based dynamics (PBD) distance constraint commonly used
 * in cloth and soft-body physics simulations.
 * </p>
 *
 * @author Jason Pollastrini aka jdub1581
 * @see WeightedPoint
 * @see Constraint
 */
public class Link implements Constraint {
    private static final Logger log = Logger.getLogger(Link.class.getName());

    /** Default damping factor for velocity reduction (0.0 = no damping, 1.0 = full damping) */
    private static final double DEFAULT_DAMPING = 0.75;

    private final double distance;
    private final double stiffness;
    private final double damping;
    private final WeightedPoint p1;
    private final WeightedPoint p2;

    /**
     * Creates a new link constraint between two weighted points.
     *
     * @param p1 the first (anchor) point of the link
     * @param p2 the second (attached) point of the link
     * @param distance the target rest distance between the two points (must be positive)
     * @param stiffness the stiffness coefficient (0.0 to 1.0, where 1.0 is fully rigid)
     * @throws IllegalArgumentException if p1 or p2 is null, distance is not positive,
     *         or stiffness is outside the valid range
     */
    public Link(WeightedPoint p1, WeightedPoint p2, double distance, double stiffness) {
        if (p1 == null) {
            throw new IllegalArgumentException("p1 cannot be null");
        }
        if (p2 == null) {
            throw new IllegalArgumentException("p2 cannot be null");
        }
        if (distance <= 0) {
            throw new IllegalArgumentException("distance must be positive, got: " + distance);
        }
        if (stiffness < 0 || stiffness > 1) {
            throw new IllegalArgumentException("stiffness must be between 0 and 1, got: " + stiffness);
        }
        this.p1 = p1;
        this.p2 = p2;
        this.distance = distance;
        this.stiffness = stiffness;
        this.damping = DEFAULT_DAMPING;
    }

    /* Option 2
        // Pseudo-code to satisfy (C2)
        delta = x2-x1;
        deltalength = sqrt(delta*delta);
        diff = (deltalength-restlength)
              /(deltalength*(invmass1+invmass2));
        x1 -= invmass1*delta*diff;
        x2 += invmass2*delta*diff;
    */
    
    /**
     * Solves this constraint by adjusting the positions of both points
     * to satisfy the target distance.
     * <p>
     * Note: This method is designed to be called sequentially from the
     * physics simulation loop. Parallel execution would require external
     * synchronization on the point positions.
     */
    @Override
    public void solve() {
        // Calculate the distance between the two points
        Point3D diff = new Point3D(
                p1.position.x - p2.position.x,
                p1.position.y - p2.position.y,
                p1.position.z - p2.position.z
        );

        double d = diff.magnitude();
        if (d == 0) {
            return; // Avoid division by zero
        }

        double difference = (distance - d) / d;

        double im1 = 1 / p1.getMass();
        double im2 = 1 / p2.getMass();
        double scalarP1 = (im1 / (im1 + im2)) * stiffness;
        double scalarP2 = stiffness - scalarP1;

        // Update positions - called sequentially so no synchronization needed
        p1.position.x += (float) (diff.x * scalarP1 * difference);
        p1.position.y += (float) (diff.y * scalarP1 * difference);
        p1.position.z += (float) (diff.z * scalarP1 * difference);

        p2.position.x -= (float) (diff.x * scalarP2 * difference);
        p2.position.y -= (float) (diff.y * scalarP2 * difference);
        p2.position.z -= (float) (diff.z * scalarP2 * difference);
    }

    /**
     * Returns the first (anchor) point of this link.
     *
     * @return the anchor point
     */
    public WeightedPoint getAnchorPoint() {
        return p1;
    }

    /**
     * Returns the second (attached) point of this link.
     *
     * @return the attached point
     */
    public WeightedPoint getAttachedPoint() {
        return p2;
    }

    /**
     * Returns the target rest distance for this link.
     *
     * @return the rest distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the stiffness coefficient of this link.
     *
     * @return the stiffness (0.0 to 1.0)
     */
    public double getStiffness() {
        return stiffness;
    }

    /**
     * Returns the damping coefficient of this link.
     *
     * @return the damping factor
     */
    public double getDamping() {
        return damping;
    }

    @Override
    public String toString() {
        return "PointLink{" + "distance=" + distance + ", stiffness=" + stiffness + ", p1=" + p1 + ", p2=" + p2 + '}';
    }

}
