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

import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.transform.Transform;
import javafx.util.Callback;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public interface CameraAdapter {
    public Camera getCamera();
    
    /*==========================================================================
     Callbacks    
       | R | Up| F |  | P|
     U |mxx|mxy|mxz|  |tx|
     V |myx|myy|myz|  |ty|
     N |mzx|mzy|mzz|  |tz|
    
     */
    //Forward / look direction    
    Callback<Transform, Point3D> F = (a) -> {
        return new Point3D(a.getMzx(), a.getMzy(), a.getMzz());
    };
    Callback<Transform, Point3D> N = (a) -> {
        return new Point3D(a.getMxz(), a.getMyz(), a.getMzz());
    };
    // up direction
    Callback<Transform, Point3D> UP = (a) -> {
        return new Point3D(a.getMyx(), a.getMyy(), a.getMyz());
    };
    Callback<Transform, Point3D> V = (a) -> {
        return new Point3D(a.getMxy(), a.getMyy(), a.getMzy());
    };
    // right direction
    Callback<Transform, Point3D> R = (a) -> {
        return new Point3D(a.getMxx(), a.getMxy(), a.getMxz());
    };
    Callback<Transform, Point3D> U = (a) -> {
        return new Point3D(a.getMxx(), a.getMyx(), a.getMzx());
    };
    //position
    Callback<Transform, Point3D> P = (a) -> {
        return new Point3D(a.getTx(), a.getTy(), a.getTz());
    };

    default Point3D getCameraForwardVectorColumn() {
        return F.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraForwardVectorRow() {
        return N.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraRightVectorColumn() {
        return R.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraRightVectorRow() {
        return U.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraUpVectorColumn() {
        return UP.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraUpVectorRow() {
        return V.call(getCamera().getLocalToSceneTransform());
    }

    default Point3D getCameraScenePosition() {
        return P.call(getCamera().getLocalToSceneTransform());
    }
}
