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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dynamisfx.importers.maya.MayaImporter;
import org.dynamisfx.importers.maya.types.MArrayType;
import org.dynamisfx.importers.maya.values.MArray;
import org.dynamisfx.importers.maya.values.MData;

public class MArrayImpl extends MDataImpl implements MArray {
    List<MData> data = new ArrayList<>();

    static class Parser {
        private final MArray array;

        Parser(MArray array) {
            this.array = array;
        }

        public void parse(Iterator<String> values) {
            int i = 0;
            while (values.hasNext()) {
                array.setSize(i + 1);
                //            System.out.println("get " + i +" of " + array.getSize());
                array.getData(i).parse(values);
                i++;
            }
        }
    }

    class MArraySlice extends MDataImpl implements MArray {
        private final MArray array;
        private final int base;
        private final int length;

        MArraySlice(
                MArray array,
                int base,
                int length) {
            super((MArrayType) array.getType());
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
        public void set(int index, MData data) {
            if (index >= length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            array.set(base + index, data);
        }

        @Override
        public MData getData(int index) {
            return array.getData(base + index);
        }

        @Override
        public MData getData(int start, int end) {
            return new MArraySlice(this, start, end - start);
        }

        @Override
        public List<MData> get() {
            // FIXME
            throw new RuntimeException("Probably shouldn't fetch the data behind a slice");
        }

        @Override
        public void parse(Iterator<String> values) {
            new Parser(this).parse(values);
        }
    }

    public MArrayImpl(MArrayType type) {
        super(type);
    }

    public MArrayType getArrayType() {
        return (MArrayType) getType();
    }

    @Override
    public List<MData> get() {
        return data;
    }

    @Override
    public MData getData(int index) {
        if (index >= data.size()) {  // TODO huge hack, to prevent out of bounds exception
            int oldIndex = index;
            index = data.size() - 1;
            Logger.getLogger(MayaImporter.class.getName()).log(Level.WARNING, "Changed index from [" + oldIndex + "] to [" + index + "]");
        }
        return data.get(index);
    }

    @Override
    public MData getData(int start, int end) {
        return new MArraySlice(this, start, end - start);
    }

    @Override
    public void set(int index, MData data) {
        this.data.set(index, data);
    }

    @Override
    public void setSize(int size) {
        while (data.size() < size) {
            data.add(getArrayType().getElementType().createData());
        }
        //        System.out.println("SET SIZE: " + size + " data.size="+data.size());
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public void parse(Iterator<String> values) {
        new Parser(this).parse(values);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
