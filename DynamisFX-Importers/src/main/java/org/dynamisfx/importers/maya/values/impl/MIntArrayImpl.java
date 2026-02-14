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
import org.dynamisfx.importers.maya.types.MIntArrayType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MIntArray;

public class MIntArrayImpl extends MDataImpl implements MIntArray {

    private int[] data;

    static class Parser {
        private final MIntArray array;

        Parser(MIntArray array) {
            this.array = array;
        }

        public void parse(Iterator<String> elements) {
            int i = 0;
            while (elements.hasNext()) {
                array.set(
                        i++,
                        Integer.parseInt(elements.next()));
            }
        }
    }

    static class MIntArraySlice extends MDataImpl implements MIntArray {
        private final MIntArray array;
        private final int base;
        private final int length;

        MIntArraySlice(
                MIntArray array,
                int base,
                int length) {
            super((MIntArrayType) array.getType());
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
        public void set(int index, int x) {
            if (index >= length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            array.set(base + index, x);
        }

        @Override
        public int[] get() {
            // FIXME
            throw new RuntimeException("Probably shouldn't fetch the data behind a slice");
        }

        @Override
        public int get(int index) {
            return array.get(base + index);
        }

        @Override
        public void parse(Iterator<String> elements) {
            new Parser(this).parse(elements);
        }
    }

    public MIntArrayImpl(MIntArrayType type) {
        super(type);
    }

    @Override
    public void setSize(int size) {
        if (data == null || size > data.length) {
            int[] newdata = new int[size];
            if (data != null) {
                System.arraycopy(data, 0, newdata, 0, data.length);
            }
            data = newdata;
        }
    }

    @Override
    public void set(int index, int x) {
        setSize(index + 1);
        data[index] = x;
    }

    @Override
    public int[] get() {
        return data;
    }

    @Override
    public int get(int index) {
        return data[index];
    }

    @Override
    public MData getData(int index) {
        return getData(index, index + 1);
    }

    @Override
    public int getSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    public MData getData(int start, int end) {
        return new MIntArraySlice(this, start, end - start + 1);
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
            for (int f : data) {
                result += sep;
                result += f;
            }
        }
        return result;
    }
}
