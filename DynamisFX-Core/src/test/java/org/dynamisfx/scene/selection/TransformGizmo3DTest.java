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

package org.dynamisfx.scene.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import org.junit.jupiter.api.Test;

class TransformGizmo3DTest {

    @Test
    void attachAndDetachTargetUpdatesVisibility() {
        TransformGizmo3D gizmo = new TransformGizmo3D();
        Box target = new Box(10, 10, 10);
        target.setTranslateX(12.0);
        target.setTranslateY(-5.0);

        gizmo.setTarget(target);
        assertTrue(gizmo.isVisible());
        assertEquals(target, gizmo.getTarget());
        assertEquals(12.0, gizmo.getTranslateX(), 0.0001);

        gizmo.setTarget(null);
        assertNull(gizmo.getTarget());
        assertFalse(gizmo.isVisible());
    }

    @Test
    void bindToSelectionModelTracksPrimarySelection() {
        SelectionModel3D model = new SelectionModel3D();
        TransformGizmo3D gizmo = new TransformGizmo3D();
        Box first = new Box(4, 4, 4);
        Box second = new Box(5, 5, 5);

        gizmo.bindToSelectionModel(model);
        model.select(first);
        assertEquals(first, gizmo.getTarget());

        model.select(second);
        assertEquals(second, gizmo.getTarget());

        model.clearSelection();
        assertNull(gizmo.getTarget());
        assertFalse(gizmo.isVisible());
    }

    @Test
    void modeVisibilityAndHandleMarkersAreExposed() {
        TransformGizmo3D gizmo = new TransformGizmo3D();
        gizmo.setMode(TransformGizmo3D.Mode.ROTATE);
        assertEquals(TransformGizmo3D.Mode.ROTATE, gizmo.getMode());

        Group translate = (Group) gizmo.getChildren().get(0);
        Group rotate = (Group) gizmo.getChildren().get(1);
        Group scale = (Group) gizmo.getChildren().get(2);
        assertFalse(translate.isVisible());
        assertTrue(rotate.isVisible());
        assertFalse(scale.isVisible());

        gizmo.setMode(TransformGizmo3D.Mode.ALL);
        assertTrue(translate.isVisible());
        assertTrue(rotate.isVisible());
        assertTrue(scale.isVisible());

        Node anyHandle = gizmo.getChildren().stream()
                .flatMap(n -> ((javafx.scene.Group) n).getChildren().stream())
                .findFirst()
                .orElse(null);
        assertNotNull(anyHandle);
        assertEquals(Boolean.TRUE, anyHandle.getProperties().get(TransformGizmo3D.HANDLE_PROPERTY_KEY));
    }

    @Test
    void applyTranslateRotateScaleByAxis() throws Exception {
        TransformGizmo3D gizmo = new TransformGizmo3D();
        Box target = new Box(10, 10, 10);
        gizmo.setTarget(target);

        Class<?> axisClass = Class.forName("org.dynamisfx.scene.selection.TransformGizmo3D$Axis");
        Object axisX = Enum.valueOf((Class<Enum>) axisClass, "X");
        Object axisY = Enum.valueOf((Class<Enum>) axisClass, "Y");
        Object axisZ = Enum.valueOf((Class<Enum>) axisClass, "Z");

        setDoubleField(gizmo, "startTranslateX", 0.0);
        setDoubleField(gizmo, "startTranslateY", 0.0);
        setDoubleField(gizmo, "startTranslateZ", 0.0);
        invokeAxisMethod(gizmo, "applyTranslate", axisClass, axisX, 10.0, 0.0, false);
        invokeAxisMethod(gizmo, "applyTranslate", axisClass, axisY, 0.0, 8.0, false);
        invokeAxisMethod(gizmo, "applyTranslate", axisClass, axisZ, 10.0, 2.0, false);
        assertEquals(10.0, target.getTranslateX(), 0.0001);
        assertEquals(8.0, target.getTranslateY(), 0.0001);
        assertEquals(4.0, target.getTranslateZ(), 0.0001);

        setDoubleField(gizmo, "startRotateY", 0.0);
        invokeAxisMethod(gizmo, "applyRotate", axisClass, axisY, 20.0, false);
        assertEquals(7.0, target.getRotate(), 0.0001); // 20 * 0.35

        target.setScaleX(1.0);
        setDoubleField(gizmo, "startScaleX", 1.0);
        invokeAxisMethod(gizmo, "applyScale", axisClass, axisX, 10.0, false);
        assertEquals(1.1, target.getScaleX(), 0.0001); // 1 + (10 * 0.01)

        setDoubleField(gizmo, "startScaleX", 0.005);
        invokeAxisMethod(gizmo, "applyScale", axisClass, axisX, -10.0, false);
        assertEquals(0.01, target.getScaleX(), 0.0001); // floor clamp
    }

    @Test
    @SuppressWarnings("unchecked")
    void snappingRoundsTransformValues() throws Exception {
        TransformGizmo3D gizmo = new TransformGizmo3D();
        Box target = new Box(10, 10, 10);
        gizmo.setTarget(target);
        gizmo.setTranslationSnapIncrement(2.0);
        gizmo.setRotationSnapIncrement(15.0);
        gizmo.setScaleSnapIncrement(0.25);

        Class<?> axisClass = Class.forName("org.dynamisfx.scene.selection.TransformGizmo3D$Axis");
        Object axisX = Enum.valueOf((Class<Enum>) axisClass, "X");
        Object axisY = Enum.valueOf((Class<Enum>) axisClass, "Y");

        setDoubleField(gizmo, "startTranslateX", 0.0);
        invokeAxisMethod(gizmo, "applyTranslate", axisClass, axisX, 3.1, 0.0, true);
        assertEquals(4.0, target.getTranslateX(), 0.0001);

        setDoubleField(gizmo, "startRotateY", 0.0);
        invokeAxisMethod(gizmo, "applyRotate", axisClass, axisY, 43.0, true);
        assertEquals(15.0, target.getRotate(), 0.0001);

        setDoubleField(gizmo, "startScaleX", 1.0);
        invokeAxisMethod(gizmo, "applyScale", axisClass, axisX, 12.0, true); // 1 + 0.12 -> 1.0 (snap 0.25)
        assertEquals(1.0, target.getScaleX(), 0.0001);
    }

    private static void setDoubleField(Object target, String fieldName, double value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setDouble(target, value);
    }

    private static void invokeAxisMethod(Object target, String methodName, Class<?> axisClass,
                                         Object axisValue, Object... args) throws Exception {
        Class<?>[] signature = new Class<?>[args.length + 1];
        Object[] params = new Object[args.length + 1];
        signature[0] = axisClass;
        params[0] = axisValue;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            signature[i + 1] = arg instanceof Double ? double.class
                    : arg instanceof Boolean ? boolean.class
                    : arg.getClass();
            params[i + 1] = arg;
        }
        Method m = target.getClass().getDeclaredMethod(methodName, signature);
        m.setAccessible(true);
        m.invoke(target, params);
    }
}
