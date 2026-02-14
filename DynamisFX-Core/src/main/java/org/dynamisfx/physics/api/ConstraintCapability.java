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

package org.dynamisfx.physics.api;

/**
 * Optional advanced constraint controls.
 */
public interface ConstraintCapability {

    boolean supportsConstraintMotors();

    boolean supportsConstraintLimitUpdates();

    /**
     * Sets the motor target velocity for a constraint.
     * For hinge joints, this is in radians/second.
     * For slider joints, this is in meters/second.
     *
     * @param handle the constraint handle
     * @param velocity the target motor velocity
     * @throws IllegalArgumentException if the constraint doesn't support motors
     */
    void setMotorVelocity(PhysicsConstraintHandle handle, double velocity);

    /**
     * Sets the maximum motor force/torque for a constraint.
     * For hinge joints, this is maximum torque in Newton-meters.
     * For slider joints, this is maximum force in Newtons.
     *
     * @param handle the constraint handle
     * @param maxForce the maximum motor force/torque
     * @throws IllegalArgumentException if the constraint doesn't support motors
     */
    void setMotorMaxForce(PhysicsConstraintHandle handle, double maxForce);

    /**
     * Gets the current motor target velocity for a constraint.
     *
     * @param handle the constraint handle
     * @return the motor velocity, or 0 if not set
     */
    double getMotorVelocity(PhysicsConstraintHandle handle);

    /**
     * Gets the current motor maximum force for a constraint.
     *
     * @param handle the constraint handle
     * @return the motor max force, or 0 if not set
     */
    double getMotorMaxForce(PhysicsConstraintHandle handle);

    /**
     * Enables or disables the motor for a constraint.
     * When disabled, motor force is set to 0.
     *
     * @param handle the constraint handle
     * @param enabled true to enable motor, false to disable
     */
    default void setMotorEnabled(PhysicsConstraintHandle handle, boolean enabled) {
        if (!enabled) {
            setMotorMaxForce(handle, 0);
        }
    }
}
