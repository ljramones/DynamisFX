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
import org.dynamisfx.importers.maya.types.MInt3ArrayType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MInt3Array;

public class MInt3ArrayImpl extends MDataImpl implements MInt3Array {

    private int[] data;

    static class Parser {
        private final MInt3Array array;

        Parser(MInt3Array array) {
            this.array = array;
        }

        public void parse(Iterator<String> elements) {
            int i = 0;
            while (elements.hasNext()) {
                array.set(
                        i++,
                        Integer.parseInt(elements.next()),
                        Integer.parseInt(elements.next()),
                        Integer.parseInt(elements.next()));
            }
        }
    }

    static class MInt3ArraySlice extends MDataImpl implements MInt3Array {
        private final MInt3Array array;
        private final int base;
        private final int length;

        MInt3ArraySlice(
                MInt3Array array,
                int base,
                int length) {
            super((MInt3ArrayType) array.getType());
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
        public void set(int index, int x, int y, int z) {
            if (index >= length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            array.set(base + index, x, y, z);
        }

        @Override
        public int[] get() {
            // FIXME
            throw new RuntimeException("Probably shouldn't fetch the data behind a slice");
        }

        @Override
        public void parse(Iterator<String> elements) {
            new Parser(this).parse(elements);
        }
    }

    public MInt3ArrayImpl(MInt3ArrayType type) {
        super(type);
    }

    @Override
    public void setSize(int size) {
        if (data == null || 3 * size > data.length) {
            int[] newdata = new int[3 * size];
            if (data != null) {
                System.arraycopy(data, 0, newdata, 0, data.length);
            }
            data = newdata;
        }
    }

    @Override
    public void set(int index, int x, int y, int z) {
        data[3 * index + 0] = x;
        data[3 * index + 1] = y;
        data[3 * index + 2] = z;
    }

    @Override
    public int getSize() {
        return data == null ? 0 : data.length / 3;
    }

    @Override
    public int[] get() {
        return data;
    }

    @Override
    public MData getData(int index) {
        // FIXME: should we introduce MInt3 and have this return one instead of an MInt3Array?
        return getData(index, index + 1);
    }

    @Override
    public MData getData(int start, int end) {
        return new MInt3ArraySlice(this, start, end - start + 1);
    }

    @Override
    public void parse(Iterator<String> elements) {
        new Parser(this).parse(elements);
    }
}
