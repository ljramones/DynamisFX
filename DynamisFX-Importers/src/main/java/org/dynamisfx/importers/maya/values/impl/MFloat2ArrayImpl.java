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
import org.dynamisfx.importers.maya.types.MFloat2ArrayType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MFloat2Array;

public class MFloat2ArrayImpl extends MDataImpl implements MFloat2Array {

    private float[] data;

    static class Parser {
        private final MFloat2Array array;

        Parser(MFloat2Array array) {
            this.array = array;
        }

        public void parse(Iterator<String> elements) {
            int i = 0;
            while (elements.hasNext()) {
                array.set(
                        i++,
                        Float.parseFloat(elements.next()),
                        Float.parseFloat(elements.next()));
            }
        }
    }

    static class MFloat2ArraySlice extends MDataImpl implements MFloat2Array {
        private final MFloat2Array array;
        private final int base;
        private final int length;

        MFloat2ArraySlice(
                MFloat2Array array,
                int base,
                int length) {
            super((MFloat2ArrayType) array.getType());
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
        public void set(int index, float x, float y) {
            if (index >= length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            array.set(base + index, x, y);
        }

        @Override
        public float[] get() {
            // FIXME
            throw new RuntimeException("Probably shouldn't fetch the data behind a slice");
        }

        @Override
        public void parse(Iterator<String> elements) {
            new Parser(this).parse(elements);
        }
    }

    public MFloat2ArrayImpl(MFloat2ArrayType type) {
        super(type);
    }

    @Override
    public void setSize(int size) {
        if (data == null || 2 * size > data.length) {
            float[] newdata = new float[2 * size];
            if (data != null) {
                System.arraycopy(data, 0, newdata, 0, data.length);
            }
            data = newdata;
        }
    }

    @Override
    public void set(int index, float x, float y) {
        data[2 * index + 0] = x;
        data[2 * index + 1] = y;
    }

    @Override
    public int getSize() {
        return data == null ? 0 : data.length / 2;
    }

    @Override
    public float[] get() {
        return data;
    }

    @Override
    public MData getData(int index) {
        // FIXME: should this return an MFloat2 rather than an MFloat2Array?
        return getData(index, index + 1);
    }

    @Override
    public MData getData(int start, int end) {
        return new MFloat2ArraySlice(this, start, end - start + 1);
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
