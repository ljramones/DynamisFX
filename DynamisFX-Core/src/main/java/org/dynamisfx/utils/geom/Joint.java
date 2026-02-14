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

package org.dynamisfx.utils.geom;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Joint -  A Joint is equivalent to a Maya Joint Node
 * <p/>
 * If you are post-multiplying matrices, To transform a point p from object-space to world-space you would need to
 * post-multiply by the worldMatrix. (p' = p * wm) matrix = [S][SO][R][JO][IS][T] where R = [RX][RY][RZ]  (Note: order
 * is determined by rotateOrder)
 * <p/>
 * If you are pre-multiplying matrices, to transform a point p from object-space to world-space you would need to
 * pre-multiply by the worldMatrix. (p' = wm * p) matrix = [T][IS][JO][R][SO][S] where R = [RZ][RY][RX]  (Note: order is
 * determined by rotateOrder) Of these sub-matrices we can set [SO] to identity, so matrix = [T][IS][JO][R][S]
 */
public class Joint extends Group {
    public final Translate t = new Translate();

    public final Rotate jox = new Rotate();

    { jox.setAxis(Rotate.X_AXIS); }

    public final Rotate joy = new Rotate();

    { joy.setAxis(Rotate.Y_AXIS); }

    public final Rotate joz = new Rotate();

    { joz.setAxis(Rotate.Z_AXIS); }

    public final Rotate rx = new Rotate();

    { rx.setAxis(Rotate.X_AXIS); }

    public final Rotate ry = new Rotate();

    { ry.setAxis(Rotate.Y_AXIS); }

    public final Rotate rz = new Rotate();

    { rz.setAxis(Rotate.Z_AXIS); }

    public final Scale s = new Scale();
    public final Scale is = new Scale();
    // should bind "is" to be in the inverse of the parent's "s"

    public Joint() {
        super();
        getTransforms().addAll(t, is, joz, joy, jox, rz, ry, rx, s);
    }
}


