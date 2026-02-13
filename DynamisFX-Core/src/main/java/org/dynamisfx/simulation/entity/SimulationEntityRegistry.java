package org.dynamisfx.simulation.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Shared registry for mapping simulation object identifiers to runtime entities.
 */
public final class SimulationEntityRegistry<T> {

    private final Map<String, T> entitiesById = new LinkedHashMap<>();
    private final Map<String, Integer> indicesById = new LinkedHashMap<>();

    public synchronized void register(String objectId, T entity) {
        validateObjectId(objectId);
        Objects.requireNonNull(entity, "entity must not be null");
        if (!indicesById.containsKey(objectId)) {
            indicesById.put(objectId, indicesById.size());
        }
        entitiesById.put(objectId, entity);
    }

    public synchronized Optional<T> get(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(entitiesById.get(objectId));
    }

    public synchronized boolean remove(String objectId) {
        validateObjectId(objectId);
        boolean removedEntity = entitiesById.remove(objectId) != null;
        indicesById.remove(objectId);
        return removedEntity;
    }

    public synchronized Collection<String> objectIds() {
        return new ArrayList<>(entitiesById.keySet());
    }

    public synchronized int size() {
        return entitiesById.size();
    }

    public synchronized OptionalInt indexOf(String objectId) {
        validateObjectId(objectId);
        Integer index = indicesById.get(objectId);
        return index == null ? OptionalInt.empty() : OptionalInt.of(index);
    }

    public synchronized void clear() {
        entitiesById.clear();
        indicesById.clear();
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
