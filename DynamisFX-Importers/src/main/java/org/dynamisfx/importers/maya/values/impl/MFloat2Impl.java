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
import org.dynamisfx.importers.maya.types.MFloat2Type;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MFloat;
import org.dynamisfx.importers.maya.values.MFloat2;

public class MFloat2Impl extends MDataImpl implements MFloat2 {

    private final float[] data = new float[2];

    class MFloat2Component extends MDataImpl implements MFloat {
        private final int index;

        MFloat2Component(int index) {
            super(MFloat2Impl.this.getEnv().findDataType("float"));
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

    public MFloat2Impl(MFloat2Type type) {
        super(type);
    }

    @Override
    public void set(float x, float y) {
        data[0] = x; data[1] = y;
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
    public float get(int index) {
        return data[index];
    }

    @Override
    public void parse(Iterator<String> elements) {
        for (int i = 0; i < 2; i++) {
            data[i] = Float.parseFloat(elements.next());
        }
    }

    @Override
    public MData getData(int index) {
        return new MFloat2Component(index);
    }

    @Override
    public MData getData(String name) {
        if (name.equals("x")) {
            return getData(0);
        } else if (name.equals("y")) {
            return getData(1);
        }
        return super.getData(name);
    }
}
