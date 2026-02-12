package org.fxyz3d.physics.model;

/**
 * World-level simulation configuration.
 */
public record PhysicsWorldConfiguration(
        ReferenceFrame referenceFrame,
        PhysicsVector3 gravity,
        double fixedStepSeconds,
        PhysicsRuntimeTuning runtimeTuning) {

    public static final int DEFAULT_SOLVER_ITERATIONS = 20;
    public static final double DEFAULT_CONTACT_FRICTION = Double.POSITIVE_INFINITY;
    public static final double DEFAULT_CONTACT_BOUNCE = 0.1;
    public static final double DEFAULT_CONTACT_SOFT_CFM = 1e-5;
    public static final double DEFAULT_CONTACT_BOUNCE_VELOCITY = 0.1;

    public PhysicsWorldConfiguration(
            ReferenceFrame referenceFrame,
            PhysicsVector3 gravity,
            double fixedStepSeconds) {
        this(
                referenceFrame,
                gravity,
                fixedStepSeconds,
                new PhysicsRuntimeTuning(
                        DEFAULT_SOLVER_ITERATIONS,
                        DEFAULT_CONTACT_FRICTION,
                        DEFAULT_CONTACT_BOUNCE,
                        DEFAULT_CONTACT_SOFT_CFM,
                        DEFAULT_CONTACT_BOUNCE_VELOCITY));
    }

    public PhysicsWorldConfiguration(
            ReferenceFrame referenceFrame,
            PhysicsVector3 gravity,
            double fixedStepSeconds,
            int solverIterations,
            double contactFriction,
            double contactBounce,
            double contactSoftCfm,
            double contactBounceVelocity) {
        this(
                referenceFrame,
                gravity,
                fixedStepSeconds,
                new PhysicsRuntimeTuning(
                        solverIterations,
                        contactFriction,
                        contactBounce,
                        contactSoftCfm,
                        contactBounceVelocity));
    }

    public PhysicsWorldConfiguration {
        if (referenceFrame == null || gravity == null) {
            throw new IllegalArgumentException("referenceFrame and gravity must not be null");
        }
        if (!(fixedStepSeconds > 0.0) || !Double.isFinite(fixedStepSeconds)) {
            throw new IllegalArgumentException("fixedStepSeconds must be > 0 and finite");
        }
        if (runtimeTuning == null) {
            throw new IllegalArgumentException("runtimeTuning must not be null");
        }
    }
}
