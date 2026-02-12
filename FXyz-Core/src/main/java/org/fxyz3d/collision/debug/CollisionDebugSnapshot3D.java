package org.fxyz3d.collision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Builds a frame snapshot that can be used by debug overlays and diagnostics.
 */
public record CollisionDebugSnapshot3D<T>(
        List<ItemBounds<T>> items,
        List<Contact<T>> contacts) {

    public CollisionDebugSnapshot3D {
        if (items == null || contacts == null) {
            throw new IllegalArgumentException("items and contacts must not be null");
        }
        items = List.copyOf(items);
        contacts = List.copyOf(contacts);
    }

    public static <T> CollisionDebugSnapshot3D<T> from(
            Collection<T> items,
            Function<T, Aabb> boundsProvider,
            Collection<CollisionEvent<T>> events) {
        if (items == null || boundsProvider == null || events == null) {
            throw new IllegalArgumentException("items, boundsProvider and events must not be null");
        }

        List<ItemBounds<T>> itemBounds = new ArrayList<>(items.size());
        for (T item : items) {
            itemBounds.add(new ItemBounds<>(item, boundsProvider.apply(item)));
        }

        List<Contact<T>> contacts = new ArrayList<>();
        for (CollisionEvent<T> event : events) {
            if (event == null || event.manifold() == null) {
                continue;
            }
            for (ContactPoint3D point : event.manifold().contacts()) {
                contacts.add(new Contact<>(
                        event.pair(),
                        event.type(),
                        event.responseEnabled(),
                        event.manifold().manifold(),
                        point));
            }
        }

        return new CollisionDebugSnapshot3D<>(itemBounds, contacts);
    }

    public record ItemBounds<T>(T item, Aabb bounds) {

        public ItemBounds {
            if (item == null || bounds == null) {
                throw new IllegalArgumentException("item and bounds must not be null");
            }
        }
    }

    public record Contact<T>(
            CollisionPair<T> pair,
            CollisionEventType type,
            boolean responseEnabled,
            CollisionManifold3D manifold,
            ContactPoint3D point) {

        public Contact {
            if (pair == null || type == null || manifold == null || point == null) {
                throw new IllegalArgumentException("pair, type, manifold and point must not be null");
            }
        }
    }
}
