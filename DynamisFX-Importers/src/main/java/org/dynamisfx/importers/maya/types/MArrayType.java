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
import org.dynamisfx.importers.maya.values.impl.MArrayImpl;

public class MArrayType extends MDataType {

    MDataType elementType;

    public MArrayType(MEnv env, MDataType elementType) {
        super(env, elementType.getName() + "[]");
        this.elementType = elementType;
    }

    public MDataType getElementType() {
        return elementType;
    }

    @Override
    public MData createData() {
        return new MArrayImpl(this);
    }

}
