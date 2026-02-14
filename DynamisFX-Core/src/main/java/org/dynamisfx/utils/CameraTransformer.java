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
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.utils;
// 
// Xform.java is a class that extends the Group class. It is used in the 
// MoleculeSampleApp application that is built using the Getting Started with JavaFX
// 3D Graphics tutorial. The method allows you to add your own transforms and rotation.
// 
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class CameraTransformer extends Group {

    public enum RotateOrder {
        XYZ, XZY, YXZ, YZX, ZXY, ZYX
    }

    public Translate t  = new Translate(); 
    public Translate p  = new Translate(); 
    public Translate ip = new Translate(); 
    public Rotate rx = new Rotate();
    { rx.setAxis(Rotate.X_AXIS); }
    public Rotate ry = new Rotate();
    { ry.setAxis(Rotate.Y_AXIS); }
    public Rotate rz = new Rotate();
    { rz.setAxis(Rotate.Z_AXIS); }
    public Scale s = new Scale();

    public CameraTransformer() { 
        super(); 
        getTransforms().addAll(t, rz, ry, rx, s); 
    }

    public CameraTransformer(CameraTransformer.RotateOrder rotateOrder) { 
        super(); 
        // choose the order of rotations based on the rotateOrder
        switch (rotateOrder) {
        case XYZ:
            getTransforms().addAll(t, p, rz, ry, rx, s, ip); 
            break;
        case XZY:
            getTransforms().addAll(t, p, ry, rz, rx, s, ip); 
            break;
        case YXZ:
            getTransforms().addAll(t, p, rz, rx, ry, s, ip); 
            break;
        case YZX:
            getTransforms().addAll(t, p, rx, rz, ry, s, ip);  // For Camera
            break;
        case ZXY:
            getTransforms().addAll(t, p, ry, rx, rz, s, ip); 
            break;
        case ZYX:
            getTransforms().addAll(t, p, rx, ry, rz, s, ip); 
            break;
        }
    }

    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }

    public void setTx(double x) { t.setX(x); }
    public void setTy(double y) { t.setY(y); }
    public void setTz(double z) { t.setZ(z); }

    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    public void setRotateX(double x) { rx.setAngle(x); }
    public void setRotateY(double y) { ry.setAngle(y); }
    public void setRotateZ(double z) { rz.setAngle(z); }
    public void setRx(double x) { rx.setAngle(x); }
    public void setRy(double y) { ry.setAngle(y); }
    public void setRz(double z) { rz.setAngle(z); }

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    public void setSx(double x) { s.setX(x); }
    public void setSy(double y) { s.setY(y); }
    public void setSz(double z) { s.setZ(z); }

    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
        ip.setX(-x);
        ip.setY(-y);
        ip.setZ(-z);
    }

    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }

    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }
}
