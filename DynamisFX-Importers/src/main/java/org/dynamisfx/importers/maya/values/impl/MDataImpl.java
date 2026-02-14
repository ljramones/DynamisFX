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
import java.util.List;
import org.dynamisfx.importers.maya.MEnv;
import org.dynamisfx.importers.maya.types.MDataType;
import org.dynamisfx.importers.maya.values.MData;

public abstract class MDataImpl implements MData {

    private final MDataType dataType;

    @Override
    public MEnv getEnv() {
        return getType().getEnv();
    }

    public MDataImpl(MDataType type) {
        dataType = type;
    }

    @Override
    public MDataType getType() {
        return dataType;
    }

    @Override
    public void setSize(int size) {
        // nothing
    }

    @Override
    public void parse(String field, List<String> values) {
        MData value = doGet(field, 0);
        if (value == null) {
            //            System.out.println("field value is null: " +field + " in " + getType().getName());
            throw new RuntimeException("field value is null: " + field);
        }
        value.parse(values);
    }

    @Override
    public void parse(List<String> values) {
        parse(values.iterator());
    }

    @Override
    public abstract void parse(Iterator<String> iter);

    // Get the data associated with the given string path
    @Override
    public MData getData(String path) {
        //        System.out.println("get: "+ path);
        return doGet(path, 0);
    }

    // Field access for those values which support it, such as compound values
    @Override
    public MData getFieldData(String name) {
        if (name.length() == 0) {
            return this;
        }
        return null;
    }

    // Index access for those values which suport it, such as array values
    @Override
    public MData getData(int index) {
        if (index == 0) {
            return this;
        }
        return null;
    }

    // Slice access for those values which support it, such as array values
    @Override
    public MData getData(int start, int end) {
        if (start == 0 && end == 0) {
            return this;
        }
        return null;
    }

    // Dereference from this MData down the path, starting parsing at the current point
    protected MData doGet(String path, int start) {
        if (start == path.length())
            return this;
        int dot = path.indexOf('.', start);
        int bracket = path.indexOf('[', start);
        if (dot == start) {
            return doGet(path, start + 1);
        } else if (bracket == start) {
            int endBracket = path.indexOf(']', start);
            int sliceStart = 0;
            int sliceEnd = 0;
            int i = start + 1;
            for (; i < endBracket; i++) {
                if (path.charAt(i) == ':')
                    break;
                sliceStart *= 10;
                sliceStart += path.charAt(i) - '0';
            }
            if (path.charAt(i) == ':') {
                i++;
                for (; i < endBracket; i++) {
                    sliceEnd *= 10;
                    sliceEnd += path.charAt(i) - '0';
                }
                // FIXME: downcast undesirable
                return ((MDataImpl) getData(sliceStart, sliceEnd)).doGet(path, endBracket + 1);
            } else {
                // FIXME: downcast undesirable
                return ((MDataImpl) getData(sliceStart)).doGet(path, endBracket + 1);
            }
        } else {
            int endIdx;
            if (dot < 0 && bracket < 0) {
                endIdx = path.length();
            } else {
                if (dot < 0) {
                    endIdx = bracket;
                } else if (bracket < 0) {
                    endIdx = dot;
                } else {
                    endIdx = Math.min(dot, bracket);
                }
            }
            String field = path.substring(start, endIdx);
            MData data = getFieldData(field);
            if (data == null) {
                //                System.err.println("WARNING: field data not found: "+field + " in "+ getType().getName());
                return null;
            } else {
                // FIXME: downcast undesirable
                return ((MDataImpl) data).doGet(path, endIdx);
            }
        }
    }
}
