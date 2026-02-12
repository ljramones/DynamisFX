/**
 * Link.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.fxyz3d.shapes.complex.cloth;

import java.util.logging.Logger;
import org.fxyz3d.geometry.Point3D;

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
