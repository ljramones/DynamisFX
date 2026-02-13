package org.dynamisfx.simulation.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared registry for mapping simulation object identifiers to runtime entities.
 */
public final class SimulationEntityRegistry<T> {

    private final Map<String, T> entitiesById = new LinkedHashMap<>();

    public synchronized void register(String objectId, T entity) {
        validateObjectId(objectId);
        Objects.requireNonNull(entity, "entity must not be null");
        entitiesById.put(objectId, entity);
    }

    public synchronized Optional<T> get(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(entitiesById.get(objectId));
    }

    public synchronized boolean remove(String objectId) {
        validateObjectId(objectId);
        return entitiesById.remove(objectId) != null;
    }

    public synchronized Collection<String> objectIds() {
        return new ArrayList<>(entitiesById.keySet());
    }

    public synchronized int size() {
        return entitiesById.size();
    }

    public synchronized void clear() {
        entitiesById.clear();
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
