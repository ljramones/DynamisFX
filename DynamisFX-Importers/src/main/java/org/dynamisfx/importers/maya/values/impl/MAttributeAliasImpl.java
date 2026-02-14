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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.dynamisfx.importers.maya.types.MAttributeAliasType;
import org.dynamisfx.importers.maya.values.MAttributeAlias;

public class MAttributeAliasImpl extends MDataImpl implements MAttributeAlias {

    Map<String, String> map = new TreeMap<>();

    public MAttributeAliasImpl(MAttributeAliasType type) {
        super(type);
    }

    @Override
    public Map<String, String> getMapping() {
        return map;
    }

    @Override
    public void parse(Iterator<String> values) {
        int count = 0;
        List<String> list = new ArrayList<>();
        while (values.hasNext()) {
            String str = values.next();
            int start = str.indexOf("\"");
            if (start < 0) {
                System.out.println("parse error at: " + str);
                continue;
            }
            str = str.substring(start);
            StringTokenizer izer = new StringTokenizer(str, ",");
            while (izer.hasMoreTokens()) {
                String tok = izer.nextToken();
                tok = tok.substring(1, tok.length() - 1);
                list.add(tok);
            }
        }
        for (int i = 0; i < list.size(); i += 2) {
            map.put(list.get(i), list.get(i + 1));
        }
        System.out.println("parsed aal: " + map);
    }

}
