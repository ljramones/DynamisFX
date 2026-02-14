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

package org.dynamisfx.importers.maya;

import org.dynamisfx.importers.maya.types.MDataType;

public class MAttribute extends MObject {

    String shortName;
    MDataType dataType;
    MNodeType declaringNodeType;
    int childIndex = -1;

    public MAttribute(
            MEnv env, String name,
            String shortName, MDataType type) {
        super(env, name);
        this.shortName = shortName;
        this.dataType = type;
    }

    public MNodeType getContext() {
        return declaringNodeType;
    }

    @Override
    public void accept(MEnv.Visitor visitor) {
        visitor.visitAttribute(this);
    }

    public String getShortName() {
        return shortName;
    }

    public MDataType getType() {
        return dataType;
    }

    public int addChild() {
        return ++childIndex;
    }
}
