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
import org.dynamisfx.importers.maya.types.MFloatArrayType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MFloatArray;

public class MFloatArrayImpl extends MDataImpl implements MFloatArray {

    private float[] data;

    static class Parser {
        private final MFloatArray array;

        Parser(MFloatArray array) {
            this.array = array;
        }

        public void parse(Iterator<String> elements) {
            int i = 0;
            //        System.out.println("PARSING FLOAT ARRAY");
            while (elements.hasNext()) {
                String str = elements.next();
                if ("nan".equals(str)) {
                    str = "0";
                }
                array.set(
                        i++,
                        Float.parseFloat(str));
            }
        }
    }

    static class MFloatArraySlice extends MDataImpl implements MFloatArray {
        private final MFloatArray array;
        private final int base;
        private final int length;

        MFloatArraySlice(
                MFloatArray array,
                int base,
                int length) {
            super((MFloatArrayType) array.getType());
            this.array = array;
            this.base = base;
            this.length = length;
        }

        @Override
        public void setSize(int size) {
            array.setSize(base + size);
        }

        @Override
        public int getSize() {
            return length;
        }

        @Override
        public void set(int index, float x) {
            if (index >= length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            array.set(base + index, x);
        }

        @Override
        public float[] get() {
            // FIXME
            throw new RuntimeException("Probably shouldn't fetch the data behind a slice");
        }

        @Override
        public float get(int index) {
            return array.get(base + index);
        }

        @Override
        public void parse(Iterator<String> elements) {
            new Parser(this).parse(elements);
        }
    }

    public MFloatArrayImpl(MFloatArrayType type) {
        super(type);
    }

    @Override
    public void setSize(int size) {
        if (data == null || size > data.length) {
            float[] newdata = new float[size];
            if (data != null) {
                System.arraycopy(data, 0, newdata, 0, data.length);
            }
            data = newdata;
        }
    }

    @Override
    public int getSize() {
        return data == null ? 0 : data.length;
    }


    @Override
    public void set(int index, float x) {
        setSize(index + 1);
        data[index] = x;
    }

    @Override
    public float[] get() {
        return data;
    }

    @Override
    public float get(int index) {
        return data[index];
    }

    @Override
    public MData getData(int index) {
        return getData(index, index + 1);
    }

    @Override
    public MData getData(int start, int end) {
        return new MFloatArraySlice(this, start, end - start + 1);
    }

    @Override
    public void parse(Iterator<String> elements) {
        new Parser(this).parse(elements);
    }

    @Override
    public String toString() {
        String result = getType().getName();
        String sep = " ";
        if (data != null) {
            for (float f : data) {
                result += sep;
                result += f;
            }
        }
        return result;
    }
}
