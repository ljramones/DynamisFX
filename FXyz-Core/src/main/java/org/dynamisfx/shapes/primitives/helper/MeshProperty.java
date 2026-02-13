/**
 * MeshProperty.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
