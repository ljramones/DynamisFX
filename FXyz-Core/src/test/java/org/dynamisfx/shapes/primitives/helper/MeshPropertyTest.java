/*
 * F(X)yz
 *
 * Copyright (c) 2013-2019, F(X)yz
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for MeshProperty factory methods.
 */
public class MeshPropertyTest {

    // ==================== Guarded Property Tests ====================

    @Nested
    @DisplayName("Guarded DoubleProperty Tests")
    class GuardedDoubleTests {

        @Test
        @DisplayName("createDouble returns property with initial value")
        void testCreateDoubleInitialValue() {
            DoubleProperty prop = MeshProperty.createDouble(42.5, () -> null, () -> {});

            assertThat(prop.get(), is(42.5));
        }

        @Test
        @DisplayName("createDouble does not call update when mesh is null")
        void testCreateDoubleNoUpdateWhenMeshNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            DoubleProperty prop = MeshProperty.createDouble(1.0, () -> null, callCount::incrementAndGet);

            prop.set(2.0);

            assertThat(callCount.get(), is(0));
        }

        @Test
        @DisplayName("createDouble calls update when mesh is non-null")
        void testCreateDoubleUpdatesWhenMeshNonNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            DoubleProperty prop = MeshProperty.createDouble(1.0, () -> mesh, callCount::incrementAndGet);

            prop.set(2.0);

            assertThat(callCount.get(), is(1));
        }

        @Test
        @DisplayName("createDouble calls update multiple times for multiple changes when observed")
        void testCreateDoubleMultipleUpdates() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            DoubleProperty prop = MeshProperty.createDouble(1.0, () -> mesh, callCount::incrementAndGet);

            // Add listener to ensure property is observed (triggers invalidation on each change)
            prop.addListener((obs, oldVal, newVal) -> {});

            prop.set(2.0);
            prop.set(3.0);
            prop.set(4.0);

            assertThat(callCount.get(), is(3));
        }

        @Test
        @DisplayName("createDouble respects dynamic mesh state")
        void testCreateDoubleDynamicMeshState() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object[] meshHolder = new Object[] { null };
            DoubleProperty prop = MeshProperty.createDouble(1.0, () -> meshHolder[0], callCount::incrementAndGet);

            // Add listener to ensure property is observed (triggers invalidation on each change)
            prop.addListener((obs, oldVal, newVal) -> {});

            prop.set(2.0);
            assertThat(callCount.get(), is(0));

            meshHolder[0] = new Object();
            prop.set(3.0);
            assertThat(callCount.get(), is(1));

            meshHolder[0] = null;
            prop.set(4.0);
            assertThat(callCount.get(), is(1));
        }
    }

    @Nested
    @DisplayName("Guarded IntegerProperty Tests")
    class GuardedIntegerTests {

        @Test
        @DisplayName("createInteger returns property with initial value")
        void testCreateIntegerInitialValue() {
            IntegerProperty prop = MeshProperty.createInteger(42, () -> null, () -> {});

            assertThat(prop.get(), is(42));
        }

        @Test
        @DisplayName("createInteger does not call update when mesh is null")
        void testCreateIntegerNoUpdateWhenMeshNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            IntegerProperty prop = MeshProperty.createInteger(1, () -> null, callCount::incrementAndGet);

            prop.set(2);

            assertThat(callCount.get(), is(0));
        }

        @Test
        @DisplayName("createInteger calls update when mesh is non-null")
        void testCreateIntegerUpdatesWhenMeshNonNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            IntegerProperty prop = MeshProperty.createInteger(1, () -> mesh, callCount::incrementAndGet);

            prop.set(2);

            assertThat(callCount.get(), is(1));
        }
    }

    @Nested
    @DisplayName("Guarded ObjectProperty Tests")
    class GuardedObjectTests {

        @Test
        @DisplayName("createObject returns property with initial value")
        void testCreateObjectInitialValue() {
            ObjectProperty<String> prop = MeshProperty.createObject("hello", () -> null, () -> {});

            assertThat(prop.get(), is("hello"));
        }

        @Test
        @DisplayName("createObject handles null initial value")
        void testCreateObjectNullInitialValue() {
            ObjectProperty<String> prop = MeshProperty.createObject(null, () -> null, () -> {});

            assertThat(prop.get(), is(nullValue()));
        }

        @Test
        @DisplayName("createObject does not call update when mesh is null")
        void testCreateObjectNoUpdateWhenMeshNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            ObjectProperty<String> prop = MeshProperty.createObject("a", () -> null, callCount::incrementAndGet);

            prop.set("b");

            assertThat(callCount.get(), is(0));
        }

        @Test
        @DisplayName("createObject calls update when mesh is non-null")
        void testCreateObjectUpdatesWhenMeshNonNull() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            ObjectProperty<String> prop = MeshProperty.createObject("a", () -> mesh, callCount::incrementAndGet);

            prop.set("b");

            assertThat(callCount.get(), is(1));
        }

        @Test
        @DisplayName("createObject works with custom types")
        void testCreateObjectCustomType() {
            record Point(int x, int y) {}
            Point initial = new Point(1, 2);
            Point updated = new Point(3, 4);
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();

            ObjectProperty<Point> prop = MeshProperty.createObject(initial, () -> mesh, callCount::incrementAndGet);
            prop.set(updated);

            assertThat(prop.get(), is(updated));
            assertThat(callCount.get(), is(1));
        }
    }

    // ==================== Unguarded Property Tests ====================

    @Nested
    @DisplayName("Unguarded DoubleProperty Tests")
    class UnguardedDoubleTests {

        @Test
        @DisplayName("createDoubleUnguarded returns property with initial value")
        void testCreateDoubleUnguardedInitialValue() {
            DoubleProperty prop = MeshProperty.createDoubleUnguarded(42.5, () -> {});

            assertThat(prop.get(), is(42.5));
        }

        @Test
        @DisplayName("createDoubleUnguarded always calls update on change when observed")
        void testCreateDoubleUnguardedAlwaysUpdates() {
            AtomicInteger callCount = new AtomicInteger(0);
            DoubleProperty prop = MeshProperty.createDoubleUnguarded(1.0, callCount::incrementAndGet);

            // Add listener to ensure property is observed (triggers invalidation on each change)
            prop.addListener((obs, oldVal, newVal) -> {});

            prop.set(2.0);
            prop.set(3.0);

            assertThat(callCount.get(), is(2));
        }
    }

    @Nested
    @DisplayName("Unguarded IntegerProperty Tests")
    class UnguardedIntegerTests {

        @Test
        @DisplayName("createIntegerUnguarded returns property with initial value")
        void testCreateIntegerUnguardedInitialValue() {
            IntegerProperty prop = MeshProperty.createIntegerUnguarded(42, () -> {});

            assertThat(prop.get(), is(42));
        }

        @Test
        @DisplayName("createIntegerUnguarded always calls update on change when observed")
        void testCreateIntegerUnguardedAlwaysUpdates() {
            AtomicInteger callCount = new AtomicInteger(0);
            IntegerProperty prop = MeshProperty.createIntegerUnguarded(1, callCount::incrementAndGet);

            // Add listener to ensure property is observed (triggers invalidation on each change)
            prop.addListener((obs, oldVal, newVal) -> {});

            prop.set(2);
            prop.set(3);

            assertThat(callCount.get(), is(2));
        }
    }

    @Nested
    @DisplayName("Unguarded ObjectProperty Tests")
    class UnguardedObjectTests {

        @Test
        @DisplayName("createObjectUnguarded returns property with initial value")
        void testCreateObjectUnguardedInitialValue() {
            ObjectProperty<String> prop = MeshProperty.createObjectUnguarded("hello", () -> {});

            assertThat(prop.get(), is("hello"));
        }

        @Test
        @DisplayName("createObjectUnguarded always calls update on change when observed")
        void testCreateObjectUnguardedAlwaysUpdates() {
            AtomicInteger callCount = new AtomicInteger(0);
            ObjectProperty<String> prop = MeshProperty.createObjectUnguarded("a", callCount::incrementAndGet);

            // Add listener to ensure property is observed (triggers invalidation on each change)
            prop.addListener((obs, oldVal, newVal) -> {});

            prop.set("b");
            prop.set("c");

            assertThat(callCount.get(), is(2));
        }
    }

    // ==================== Property Behavior Tests ====================

    @Nested
    @DisplayName("Property Behavior Tests")
    class BehaviorTests {

        @Test
        @DisplayName("Setting same value does not trigger invalidation in JavaFX")
        void testSameValueNoInvalidation() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            DoubleProperty prop = MeshProperty.createDouble(1.0, () -> mesh, callCount::incrementAndGet);

            prop.set(1.0);

            // JavaFX properties don't invalidate when set to the same value
            assertThat(callCount.get(), is(0));
        }

        @Test
        @DisplayName("Properties can be bound to other properties")
        void testPropertyBinding() {
            AtomicInteger callCount = new AtomicInteger(0);
            Object mesh = new Object();
            DoubleProperty source = MeshProperty.createDoubleUnguarded(1.0, () -> {});
            DoubleProperty target = MeshProperty.createDouble(0.0, () -> mesh, callCount::incrementAndGet);

            target.bind(source);
            source.set(2.0);

            assertThat(target.get(), is(2.0));
            assertThat(callCount.get(), is(greaterThan(0)));
        }
    }
}
