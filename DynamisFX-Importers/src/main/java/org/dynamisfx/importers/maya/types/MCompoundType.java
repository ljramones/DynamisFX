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

package org.dynamisfx.importers.maya.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dynamisfx.importers.maya.MEnv;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.impl.MCompoundImpl;

public class MCompoundType extends MDataType {

    Map<String, Field> fields = new HashMap<>();
    List<Field> fieldArray = new ArrayList<>();

    public MCompoundType(MEnv env, String name) {
        super(env, name);
    }

    public final Map<String, Field> getFields() {
        return fields;
    }

    public int getNumFields() {
        return fieldArray.size();
    }

    public int getFieldIndex(String name) {
        Field field = getField(name);
        if (field == null) {
            //            System.out.println("No such field in type " + getName() + ": " + name);
            return -1;
        }
        return getField(name).getIndex();
    }

    public final Field getField(String name) {
        return fields.get(name);
    }

    public final Field getField(int index) {
        return fieldArray.get(index);
    }

    public final Field addField(String name, MDataType type, MData defaultValue) {
        Field field;
        fields.put(name, field = new Field(name, type, defaultValue, fieldArray.size()));
        fieldArray.add(field);
        return field;
    }

    public static class Field {
        String name;
        MDataType type;
        MData defaultValue;
        int index;

        public Field(String name, MDataType type, MData defaultValue, int index) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public MDataType getType() {
            return type;
        }

        public MData getDefault() {
            //return defaultValue;
            return type.createData();
        }

        public int getIndex() {
            return index;
        }
    }

    @Override
    public MData createData() {
        return new MCompoundImpl(this);
    }
}
