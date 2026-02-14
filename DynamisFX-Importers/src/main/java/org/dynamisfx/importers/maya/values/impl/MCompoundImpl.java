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
import org.dynamisfx.importers.maya.types.MCompoundType;
import org.dynamisfx.importers.maya.types.MDataType;
import org.dynamisfx.importers.maya.values.MCompound;
import org.dynamisfx.importers.maya.values.MData;

public class MCompoundImpl extends MDataImpl implements MCompound {
    private final MData[] fieldData;

    public MCompoundImpl(MCompoundType type) {
        super(type);
        fieldData = new MData[type.getNumFields()];
        for (int i = 0; i < fieldData.length; i++) {
            MDataType dt = getCompoundType().getField(i).getType();
            if (dt != null) {
                fieldData[i] = dt.createData();
            } else {
                //                System.out.println("field data type is null: " + getCompoundType().getField(i).getName());
            }
        }
    }

    @Override
    public final MCompoundType getCompoundType() {
        return (MCompoundType) getType();
    }

    @Override
    public MData getFieldData(String fieldName) {
        return getFieldData(getCompoundType().getFieldIndex(fieldName));
    }

    @Override
    public MData getFieldData(int fieldIndex) {
        if (fieldIndex < 0) {
            return null;
        }
        return fieldData[fieldIndex];
    }

    @Override
    public void set(int fieldIndex, MData value) {
        fieldData[fieldIndex] = value;
    }

    @Override
    public void set(String fieldName, MData data) {
        set(getCompoundType().getFieldIndex(fieldName), data);
    }

    @Override
    public void parse(Iterator<String> data) {
        for (int i = 0; i < getCompoundType().getNumFields(); i++) {
            MData fdata = getFieldData(i);
            if (fdata != null) {
                fdata.parse(data);
            }
        }
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < fieldData.length; i++) {
            result += getCompoundType().getField(i).getName() + ":\t" + fieldData[i] + "\n";
        }
        return result;
    }
}
