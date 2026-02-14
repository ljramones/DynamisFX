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

package org.dynamisfx.importers.maya.values.impl;

import java.util.Iterator;
import org.dynamisfx.importers.maya.types.MFloatType;
import org.dynamisfx.importers.maya.values.MFloat;

public class MFloatImpl extends MDataImpl implements MFloat {

    float value;

    public MFloatImpl(MFloatType type) {
        super(type);
    }

    @Override
    public void set(float value) {
        this.value = value;
    }

    @Override
    public float get() {
        return value;
    }

    @Override
    public void parse(Iterator<String> values) {
        String val = values.next().toLowerCase();
        value = Float.parseFloat(val);
    }

    @Override
    public String toString() {
        String result = getType().getName();
        result += " " + value;
        return result;
    }
}
