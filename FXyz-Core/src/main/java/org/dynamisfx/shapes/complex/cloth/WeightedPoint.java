/**
 * WeightedPoint.java
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

package org.dynamisfx.shapes.complex.cloth;

import java.util.HashMap;
import org.dynamisfx.geometry.Point3D;

/**
 * A point with mass used in cloth physics simulation.
 * <p>
 * WeightedPoint represents a single vertex in a cloth mesh that participates in
 * physics simulation. Each point has:
 * <ul>
 *   <li>A position that is updated each simulation step</li>
 *   <li>A mass that affects how forces are applied</li>
 *   <li>Constraints (links) to other points that maintain cloth structure</li>
 *   <li>Optional anchoring to fix the point in place</li>
 * </ul>
 * </p>
 * <p>
 * The physics simulation uses Verlet integration to update positions based on
 * accumulated forces and previous positions.
 * </p>
 *
 * @author Jason Pollastrini aka jdub1581
 * @see Link
 */
public class WeightedPoint {

        /** Default mass for new points */
        private static final double DEFAULT_MASS = 0.1;

        /** Verlet integration coefficient (0.5 for standard Verlet) */
        private static final float VERLET_COEFFICIENT = 0.5f;

        private double mass = 0;
        public Point3D position,
                oldPosition,
                anchorPosition,
                force;

        private final HashMap<WeightedPoint, Constraint> constraints = new HashMap<>();

        private boolean anchored = false,
                forceAffected = true;

        /*==========================================================================
         Constructors
         */

        /**
         * Creates a new weighted point with default mass at the origin.
         */
        public WeightedPoint() {
            this(DEFAULT_MASS, 0, 0, 0, false);
        }

        /**
         * Creates a new weighted point with specified mass at the origin.
         *
         * @param mass the mass of the point (affects force response)
         */
        public WeightedPoint(double mass) {
            this(mass, 0, 0, 0, false);
        }

        /**
         * Creates a new weighted point with specified mass and position.
         *
         * @param mass the mass of the point
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         */
        public WeightedPoint(double mass, double x, double y, double z) {
            this(mass, x, y, z, false);
        }

        /**
         * Creates a new weighted point with full configuration.
         *
         * @param mass the mass of the point (affects force response)
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         * @param anchored whether this point is fixed in place
         */
        public WeightedPoint(double mass, double x, double y, double z, boolean anchored) {
            this.position = new Point3D((float) x, (float) y, (float) z);
            this.oldPosition = new Point3D((float) x, (float) y, (float) z);
            this.anchorPosition = new Point3D(0, 0, 0);
            this.force = new Point3D(0, 0, 0);

            this.mass = mass;
            this.anchored = anchored;
        }
        /*==========================================================================
         Constraints
         */

        /**
         * Attaches this point to another point with a spring-like constraint.
         *
         * @param other the point to attach to
         * @param linkDistance the rest distance of the constraint
         * @param stiffness the stiffness of the constraint (0.0 to 1.0)
         */
        public final void attachTo(WeightedPoint other, double linkDistance, double stiffness) {
            attachTo(this, other, linkDistance, stiffness);
        }

        /**
         * Creates a constraint between two points.
         *
         * @param self the first point (anchor)
         * @param other the second point (attached)
         * @param linkDistance the rest distance of the constraint
         * @param stiffness the stiffness of the constraint (0.0 to 1.0)
         */
        public final void attachTo(WeightedPoint self, WeightedPoint other, double linkDistance, double stiffness) {
            Link pl = new Link(self, other, linkDistance, stiffness);
            addConstraint(other, pl);
        }

        //==========================================================================
        public HashMap<WeightedPoint, Constraint> getConstraints() {
            return constraints;
        }

        public void addConstraint(WeightedPoint other, Constraint constraint) {
            this.constraints.put(other, constraint);
        }

        public void addConstraint(Constraint c){
            this.constraints.put(this, c);
        }

        /**
         * Removes a constraint from this point.
         *
         * @param other the point whose constraint should be removed
         * @return true if a constraint was removed, false otherwise
         */
        public boolean removeConstraint(WeightedPoint other) {
            return constraints.remove(other) != null;
        }

        public void clearConstraints() {
            constraints.clear();
        }
        /*==========================================================================
         Updating
         */

        /**
         * Solves all constraints attached to this point.
         * Uses sequential iteration for thread safety since constraints
         * modify shared position state.
         */
        public void solveConstraints() {
            for (Constraint c : constraints.values()) {
                c.solve();
            }
        }

        /**
         * Updates the point's position using Verlet integration.
         * <p>
         * The Verlet integration formula calculates the next position based on:
         * <ul>
         *   <li>Current position</li>
         *   <li>Velocity (derived from position - oldPosition)</li>
         *   <li>Acceleration from accumulated forces</li>
         * </ul>
         * </p>
         *
         * @param dt the time delta for this physics step
         * @param t the current simulation time (unused, reserved for future use)
         */
        public void updatePhysics(double dt, double t) {
            synchronized (this) {
                if (isAnchored()) {
                    setPosition(getAnchorPosition());
                    return;
                }
                Point3D vel = new Point3D(
                        (position.x - oldPosition.x),
                        (position.y - oldPosition.y),
                        (position.z - oldPosition.z)
                );
                float dtSq = (float) (dt * dt);

                // calculate the next position using Verlet Integration
                // Formula: x_new = x + velocity + (acceleration * 0.5 * dt²)
                Point3D next = new Point3D(
                        position.x + (vel.x + (((force.x / (float) mass) * VERLET_COEFFICIENT) * dtSq)),
                        position.y + (vel.y + (((force.y / (float) mass) * VERLET_COEFFICIENT) * dtSq)),
                        position.z + (vel.z + (((force.z / (float) mass) * VERLET_COEFFICIENT) * dtSq))
                );

                // reset variables
                setOldPosition(position);
                setPosition(next);
                clearForces();
                //log.log(Level.INFO, "\n Velocity: {0}", vel);
            }
        }
        /*==========================================================================
         Variable's Getters / setters
         */

        public double getMass() {
            return mass;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        /*==========================================================================
         Positions
         */
        public final Point3D getPosition() {
            return new Point3D(position.x, position.y, position.z);
        }

        public void setPosition(Point3D pos) {
            position.x = pos.x;
            position.y = pos.y;
            position.z = pos.z;
        }

        public void setPosition(double x, double y, double z) {
            position.x = (float) x;
            position.y = (float) y;
            position.z = (float) z;
        }

        //==========================================================================
        public Point3D getOldPosition() {
            return oldPosition;
        }

        public void setOldPosition(Point3D oldPosition) {
            this.oldPosition.x = oldPosition.x;
            this.oldPosition.y = oldPosition.y;
            this.oldPosition.z = oldPosition.z;
        }

        public void setOldPosition(float x, float y, float z) {
            this.oldPosition.x = x;
            this.oldPosition.y = y;
            this.oldPosition.z = z;
        }

        public void setOldPosition(double x, double y, double z) {
            this.oldPosition.x = (float) x;
            this.oldPosition.y = (float) y;
            this.oldPosition.z = (float) z;
        }

        //==========================================================================
        public boolean isAnchored() {
            return anchored;
        }

        public void setAnchored(boolean anchored) {
            this.anchored = anchored;
            if (anchored) {
                setAnchorPosition(new Point3D(position.x, position.y, position.z));
            } else {
                setAnchorPosition(null);
            }
        }

        public Point3D getAnchorPosition() {
            return anchorPosition;
        }

        public void setAnchorPosition(Point3D anchorPosition) {
            this.anchorPosition = anchorPosition;
        }
        /*==========================================================================
         Forces
         */

        public Point3D getForce() {
            return force;
        }

        private void setForce(Point3D p) {
            this.force = p;
        }

        public void applyForce(Point3D force) {
            if (isForceAffected()) {
                this.force.x += force.x;
                this.force.y += force.y;
                this.force.z += force.z;
            }
        }

        public void clearForces() {
            setForce(new Point3D(0, 0, 0));
        }

        public boolean isForceAffected() {
            return forceAffected;
        }

        public void setForceAffected(boolean forceAffected) {
            this.forceAffected = forceAffected;
        }

        /*==========================================================================
    
         */
        @Override
        public String toString() {
            return "WeightedPoint: ".concat(position.toString());
        }

    }//End WeightedPoint========================================================
