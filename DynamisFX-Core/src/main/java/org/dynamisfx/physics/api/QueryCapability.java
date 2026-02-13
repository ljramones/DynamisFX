package org.dynamisfx.physics.api;

import java.util.List;
import java.util.Optional;

/**
 * Optional world query capability surface.
 */
public interface QueryCapability {

    Optional<RaycastHit> raycast(RaycastRequest request);

    List<PhysicsBodyHandle> overlapSphere(OverlapSphereQuery query);
}
