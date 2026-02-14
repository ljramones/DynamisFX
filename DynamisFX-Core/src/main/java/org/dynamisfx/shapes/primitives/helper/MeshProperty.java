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
 *
 */

package org.dynamisfx.shapes.primitives.helper;

import java.util.function.Supplier;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Factory class for creating JavaFX properties that automatically trigger mesh updates
 * when their values change.
 * <p>
 * This class reduces boilerplate code in mesh classes by providing factory methods
 * that create properties with built-in invalidation handlers. Two variants are provided:
 * <ul>
 *   <li><b>Guarded methods</b> ({@code createDouble}, {@code createInteger}, {@code createObject}):
 *       For use in TexturedMesh subclasses where the mesh may be null during initialization.
 *       These check if the mesh is non-null before calling the update action.</li>
 *   <li><b>Unguarded methods</b> ({@code createDoubleUnguarded}, etc.):
 *       For use in MeshView subclasses where no null check is needed.</li>
 * </ul>
 * <p>
 * Example usage in a TexturedMesh subclass:
 * <pre>{@code
 * private final DoubleProperty radius = MeshProperty.createDouble(
 *     DEFAULT_RADIUS, () -> mesh, this::updateMesh);
 * }</pre>
 *
 * @author FXyz contributors
 */
public final class MeshProperty {

    private MeshProperty() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a DoubleProperty that triggers an update action when invalidated,
     * but only if the mesh (provided by the supplier) is non-null.
     *
     * @param initialValue the initial value of the property
     * @param meshSupplier supplier that returns the mesh object (used for null check)
     * @param updateAction the action to run when the property changes and mesh is non-null
     * @return a new DoubleProperty with the specified behavior
     */
    public static DoubleProperty createDouble(double initialValue,
            Supplier<Object> meshSupplier, Runnable updateAction) {
        return new SimpleDoubleProperty(initialValue) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    updateAction.run();
                }
            }
        };
    }

    /**
     * Creates an IntegerProperty that triggers an update action when invalidated,
     * but only if the mesh (provided by the supplier) is non-null.
     *
     * @param initialValue the initial value of the property
     * @param meshSupplier supplier that returns the mesh object (used for null check)
     * @param updateAction the action to run when the property changes and mesh is non-null
     * @return a new IntegerProperty with the specified behavior
     */
    public static IntegerProperty createInteger(int initialValue,
            Supplier<Object> meshSupplier, Runnable updateAction) {
        return new SimpleIntegerProperty(initialValue) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    updateAction.run();
                }
            }
        };
    }

    /**
     * Creates a FloatProperty that triggers an update action when invalidated,
     * but only if the mesh (provided by the supplier) is non-null.
     *
     * @param initialValue the initial value of the property
     * @param meshSupplier supplier that returns the mesh object (used for null check)
     * @param updateAction the action to run when the property changes and mesh is non-null
     * @return a new FloatProperty with the specified behavior
     */
    public static FloatProperty createFloat(float initialValue,
            Supplier<Object> meshSupplier, Runnable updateAction) {
        return new SimpleFloatProperty(initialValue) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    updateAction.run();
                }
            }
        };
    }

    /**
     * Creates an ObjectProperty that triggers an update action when invalidated,
     * but only if the mesh (provided by the supplier) is non-null.
     *
     * @param <T> the type of the property value
     * @param initialValue the initial value of the property
     * @param meshSupplier supplier that returns the mesh object (used for null check)
     * @param updateAction the action to run when the property changes and mesh is non-null
     * @return a new ObjectProperty with the specified behavior
     */
    public static <T> ObjectProperty<T> createObject(T initialValue,
            Supplier<Object> meshSupplier, Runnable updateAction) {
        return new SimpleObjectProperty<T>(initialValue) {
            @Override
            protected void invalidated() {
                if (meshSupplier.get() != null) {
                    updateAction.run();
                }
            }
        };
    }

    /**
     * Creates a DoubleProperty that triggers an update action when invalidated,
     * without any null check. For use in MeshView subclasses.
     *
     * @param initialValue the initial value of the property
     * @param updateAction the action to run when the property changes
     * @return a new DoubleProperty with the specified behavior
     */
    public static DoubleProperty createDoubleUnguarded(double initialValue, Runnable updateAction) {
        return new SimpleDoubleProperty(initialValue) {
            @Override
            protected void invalidated() {
                updateAction.run();
            }
        };
    }

    /**
     * Creates an IntegerProperty that triggers an update action when invalidated,
     * without any null check. For use in MeshView subclasses.
     *
     * @param initialValue the initial value of the property
     * @param updateAction the action to run when the property changes
     * @return a new IntegerProperty with the specified behavior
     */
    public static IntegerProperty createIntegerUnguarded(int initialValue, Runnable updateAction) {
        return new SimpleIntegerProperty(initialValue) {
            @Override
            protected void invalidated() {
                updateAction.run();
            }
        };
    }

    /**
     * Creates a FloatProperty that triggers an update action when invalidated,
     * without any null check. For use in MeshView subclasses.
     *
     * @param initialValue the initial value of the property
     * @param updateAction the action to run when the property changes
     * @return a new FloatProperty with the specified behavior
     */
    public static FloatProperty createFloatUnguarded(float initialValue, Runnable updateAction) {
        return new SimpleFloatProperty(initialValue) {
            @Override
            protected void invalidated() {
                updateAction.run();
            }
        };
    }

    /**
     * Creates an ObjectProperty that triggers an update action when invalidated,
     * without any null check. For use in MeshView subclasses.
     *
     * @param <T> the type of the property value
     * @param initialValue the initial value of the property
     * @param updateAction the action to run when the property changes
     * @return a new ObjectProperty with the specified behavior
     */
    public static <T> ObjectProperty<T> createObjectUnguarded(T initialValue, Runnable updateAction) {
        return new SimpleObjectProperty<T>(initialValue) {
            @Override
            protected void invalidated() {
                updateAction.run();
            }
        };
    }
}
