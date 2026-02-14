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

package org.dynamisfx.importers.maya.types;

import org.dynamisfx.importers.maya.MEnv;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MFloatArray;
import org.dynamisfx.importers.maya.values.impl.MFloatArrayImpl;

public class MMatrixType extends MFloatArrayType {

    public static final String NAME = "matrix";

    public MMatrixType(MEnv env) {
        super(env, NAME);
    }

    @Override
    public MData createData() {
        MFloatArray array = new MFloatArrayImpl(this);
        array.setSize(16);
        // Make the default value the identity matrix
        array.set(0, 1);
        array.set(5, 1);
        array.set(10, 1);
        array.set(15, 1);
        return array;
    }
}
