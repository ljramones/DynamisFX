package org.dynamisfx.physics.api;

/**
 * Optional continuous-collision-detection controls.
 */
public interface CcdCapability {

    boolean isCcdEnabled();

    void setCcdEnabled(boolean enabled);
}
