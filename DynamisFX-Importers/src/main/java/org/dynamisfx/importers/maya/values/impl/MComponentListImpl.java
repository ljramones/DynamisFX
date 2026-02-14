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
import org.dynamisfx.importers.maya.types.MComponentListType;
import org.dynamisfx.importers.maya.values.MComponentList;

public class MComponentListImpl extends MDataImpl implements MComponentList {

    private List<Component> components = new ArrayList<>();

    public MComponentListImpl(MComponentListType type) {
        super(type);
    }

    @Override
    public void set(List<Component> value) {
        components = value;
    }

    @Override
    public List<Component> get() {
        return components;
    }

    @Override
    public void parse(Iterator<String> values) {
        try {
            int num = Integer.parseInt(values.next());
            for (int i = 0; i < num; i++) {
                components.add(Component.parse(values.next()));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(getType().getName());
        for (Component c : components) {
            res.append(" ");
            res.append(c);
        }
        return res.toString();
    }
}
