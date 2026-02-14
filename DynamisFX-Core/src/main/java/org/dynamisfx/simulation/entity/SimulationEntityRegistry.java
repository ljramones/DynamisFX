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
