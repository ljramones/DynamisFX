package org.fxyz3d.collision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.scene.Node;
import org.fxyz3d.geometry.Vector3D;

/**
 * Runtime orchestration for broad phase, filtering, narrow phase and event generation.
 */
public final class CollisionWorld3D<T> {

    private final BroadPhase3D<T> broadPhase;
    private final Function<T, Aabb> boundsProvider;
    private final Function<T, CollisionFilter> filterProvider;
    private final BiFunction<T, T, Optional<ContactManifold3D>> narrowPhase;
    private final ManifoldCache3D<T> manifoldCache = new ManifoldCache3D<>();
    private final List<Constraint3D<T>> constraints = new CopyOnWriteArrayList<>();

    private final Map<CollisionPair<T>, FrameCollision> previousFrame = new HashMap<>();
    private long manifoldRetentionFrames = 2;
    private int solverIterations = 1;
    private int constraintIterations = 1;
    private CollisionResponder3D<T> responder;
    private RigidBodyAdapter3D<T> bodyAdapter;
    private Vector3D gravity = new Vector3D(0.0, 0.0, 0.0);

    public CollisionWorld3D(
            BroadPhase3D<T> broadPhase,
            Function<T, Aabb> boundsProvider,
            Function<T, CollisionFilter> filterProvider,
            BiFunction<T, T, Optional<ContactManifold3D>> narrowPhase) {
        if (broadPhase == null || boundsProvider == null || filterProvider == null || narrowPhase == null) {
            throw new IllegalArgumentException("constructor arguments must not be null");
        }
        this.broadPhase = broadPhase;
        this.boundsProvider = boundsProvider;
        this.filterProvider = filterProvider;
        this.narrowPhase = narrowPhase;
    }

    public static CollisionWorld3D<Node> forJavaFxNodes(BroadPhase3D<Node> broadPhase) {
        return new CollisionWorld3D<>(
                broadPhase,
                NodeCollisionAdapter::boundsInParent,
                NodeCollisionAdapter::getFilter,
                (left, right) -> ContactGenerator3D.generate(
                        NodeCollisionAdapter.boundsInParent(left),
                        NodeCollisionAdapter.boundsInParent(right)));
    }

    public static CollisionWorld3D<Node> forJavaFxNodesDefault() {
        return forJavaFxNodes(new SweepAndPrune3D<>());
    }

    public void setManifoldRetentionFrames(long manifoldRetentionFrames) {
        if (manifoldRetentionFrames < 0) {
            throw new IllegalArgumentException("manifoldRetentionFrames must be >= 0");
        }
        this.manifoldRetentionFrames = manifoldRetentionFrames;
    }

    public ManifoldCache3D<T> manifoldCache() {
        return manifoldCache;
    }

    public void setResponder(CollisionResponder3D<T> responder) {
        this.responder = responder;
    }

    public void setSolverIterations(int solverIterations) {
        if (solverIterations < 1) {
            throw new IllegalArgumentException("solverIterations must be >= 1");
        }
        this.solverIterations = solverIterations;
    }

    public void setConstraintIterations(int constraintIterations) {
        if (constraintIterations < 1) {
            throw new IllegalArgumentException("constraintIterations must be >= 1");
        }
        this.constraintIterations = constraintIterations;
    }

    public void setBodyAdapter(RigidBodyAdapter3D<T> bodyAdapter) {
        this.bodyAdapter = bodyAdapter;
    }

    public void setGravity(Vector3D gravity) {
        if (gravity == null) {
            throw new IllegalArgumentException("gravity must not be null");
        }
        this.gravity = gravity;
    }

    public void addConstraint(Constraint3D<T> constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException("constraint must not be null");
        }
        constraints.add(constraint);
    }

    public void clearConstraints() {
        constraints.clear();
    }

    public List<Constraint3D<T>> constraints() {
        return List.copyOf(constraints);
    }

    public List<CollisionEvent<T>> update(Collection<T> items) {
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }

        manifoldCache.nextFrame();

        Set<CollisionPair<T>> candidates = broadPhase.findPotentialPairs(items, boundsProvider);
        Set<FilteredCollisionPair<T>> filteredPairs = CollisionFiltering.filterPairs(candidates, filterProvider);

        Map<CollisionPair<T>, FrameCollision> currentFrame = new HashMap<>();
        for (FilteredCollisionPair<T> filtered : filteredPairs) {
            CollisionPair<T> pair = filtered.pair();
            Optional<ContactManifold3D> contact = narrowPhase.apply(pair.first(), pair.second());
            if (contact.isEmpty()) {
                continue;
            }
            ContactManifold3D manifold = contact.get();
            currentFrame.put(pair, new FrameCollision(filtered.responseEnabled(), manifold));
            manifoldCache.put(pair, manifold);
        }

        List<CollisionEvent<T>> events = new ArrayList<>();

        List<CollisionEvent<T>> responseEvents = new ArrayList<>();
        for (Map.Entry<CollisionPair<T>, FrameCollision> entry : currentFrame.entrySet()) {
            CollisionPair<T> pair = entry.getKey();
            FrameCollision current = entry.getValue();
            CollisionEventType type = previousFrame.containsKey(pair) ? CollisionEventType.STAY : CollisionEventType.ENTER;
            CollisionEvent<T> event = new CollisionEvent<>(pair, type, current.responseEnabled(), current.manifold());
            events.add(event);
            if (event.responseEnabled()) {
                responseEvents.add(event);
            }
        }

        for (Map.Entry<CollisionPair<T>, FrameCollision> entry : previousFrame.entrySet()) {
            if (!currentFrame.containsKey(entry.getKey())) {
                FrameCollision prior = entry.getValue();
                events.add(new CollisionEvent<>(
                        entry.getKey(),
                        CollisionEventType.EXIT,
                        prior.responseEnabled(),
                        prior.manifold()));
            }
        }

        previousFrame.clear();
        previousFrame.putAll(currentFrame);
        manifoldCache.pruneStale(manifoldRetentionFrames);

        applyResponses(responseEvents);

        return events;
    }

    public List<CollisionEvent<T>> step(Collection<T> items, double dtSeconds) {
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        if (!Double.isFinite(dtSeconds) || dtSeconds <= 0.0) {
            throw new IllegalArgumentException("dtSeconds must be > 0");
        }
        if (bodyAdapter == null) {
            throw new IllegalStateException("bodyAdapter must be set to use step()");
        }

        // 1) integrate velocities (external acceleration only)
        for (T body : items) {
            double invMass = Math.max(0.0, bodyAdapter.getInverseMass(body));
            if (invMass <= 0.0) {
                continue;
            }
            Vector3D v = bodyAdapter.getVelocity(body);
            bodyAdapter.setVelocity(body, new Vector3D(
                    v.getX() + gravity.getX() * dtSeconds,
                    v.getY() + gravity.getY() * dtSeconds,
                    v.getZ() + gravity.getZ() * dtSeconds));
        }

        // 2) solve constraints
        for (int i = 0; i < constraintIterations; i++) {
            for (Constraint3D<T> constraint : constraints) {
                constraint.solve(bodyAdapter, dtSeconds);
            }
        }

        // 3) solve collisions at current predicted state
        List<CollisionEvent<T>> events = update(items);

        // 4) integrate positions
        for (T body : items) {
            double invMass = Math.max(0.0, bodyAdapter.getInverseMass(body));
            if (invMass <= 0.0) {
                continue;
            }
            Vector3D p = bodyAdapter.getPosition(body);
            Vector3D v = bodyAdapter.getVelocity(body);
            bodyAdapter.setPosition(body, new Vector3D(
                    p.getX() + v.getX() * dtSeconds,
                    p.getY() + v.getY() * dtSeconds,
                    p.getZ() + v.getZ() * dtSeconds));
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private void applyResponses(List<CollisionEvent<T>> responseEvents) {
        if (responder == null || responseEvents.isEmpty()) {
            return;
        }

        responseEvents.sort(Comparator.comparing(event ->
                String.valueOf(event.pair().first()) + "|" + String.valueOf(event.pair().second())));

        if (responder instanceof ContactSolver3D<?> anySolver) {
            ContactSolver3D<T> solver = (ContactSolver3D<T>) anySolver;
            for (int i = 0; i < solverIterations; i++) {
                for (CollisionEvent<T> event : responseEvents) {
                    solver.solvePosition(event);
                }
            }
            for (int i = 0; i < solverIterations; i++) {
                for (CollisionEvent<T> event : responseEvents) {
                    WarmStartImpulse warmStart = i == 0
                            ? manifoldCache.getWarmStart(event.pair()).orElse(WarmStartImpulse.ZERO)
                            : WarmStartImpulse.ZERO;
                    WarmStartImpulse solved = solver.solveVelocity(event, warmStart);
                    if (i == solverIterations - 1) {
                        manifoldCache.setWarmStart(event.pair(), solved);
                    }
                }
            }
            return;
        }

        for (CollisionEvent<T> event : responseEvents) {
            responder.resolve(event);
        }
    }

    private record FrameCollision(boolean responseEnabled, ContactManifold3D manifold) {
    }
}
