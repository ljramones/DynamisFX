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
import org.dynamisfx.importers.maya.types.MFloat3Type;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MFloat;
import org.dynamisfx.importers.maya.values.MFloat3;

public class MFloat3Impl extends MDataImpl implements MFloat3 {

    private final float[] data = new float[3];

    class MFloat3Component extends MDataImpl implements MFloat {
        private final int index;

        MFloat3Component(int index) {
            super(MFloat3Impl.this.getEnv().findDataType("float"));
            this.index = index;
        }

        @Override
        public void set(float value) {
            data[index] = value;
        }

        @Override
        public float get() {
            return data[index];
        }

        @Override
        public void parse(Iterator<String> elements) {
            data[index] = Float.parseFloat(elements.next());
        }
    }

    public MFloat3Impl(MFloat3Type type) {
        super(type);
    }

    @Override
    public void set(float x, float y, float z) {
        data[0] = x; data[1] = y; data[2] = z;
    }

    @Override
    public float[] get() {
        return data;
    }

    @Override
    public float getX() {
        return data[0];
    }

    @Override
    public float getY() {
        return data[1];
    }

    @Override
    public float getZ() {
        return data[2];
    }

    @Override
    public float get(int index) {
        return data[index];
    }

    @Override
    public void parse(Iterator<String> elements) {
        for (int i = 0; i < 3; i++) {
            data[i] = Float.parseFloat(elements.next());
        }
    }

    @Override
    public MData getData(int index) {
        return new MFloat3Component(index);
    }

    @Override
    public MData getData(String name) {
        if (name.equals("x")) {
            return getData(0);
        } else if (name.equals("y")) {
            return getData(1);
        } else if (name.equals("z")) {
            return getData(2);
        }
        return super.getData(name);
    }
}
