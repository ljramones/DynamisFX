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
import org.dynamisfx.importers.maya.types.MCharacterMappingType;
import org.dynamisfx.importers.maya.values.MCharacterMapping;

public class MCharacterMappingImpl extends MDataImpl implements MCharacterMapping {

    class EntryImpl implements Entry {
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public int getSourceIndex() {
            return sourceIndex;
        }

        @Override
        public int getTargetIndex() {
            return targetIndex;
        }

        String key;
        int sourceIndex;
        int targetIndex;

        public EntryImpl(String key, int sourceIndex, int targetIndex) {
            this.key = key; this.sourceIndex = sourceIndex; this.targetIndex = targetIndex;
        }
    }

    Entry[] entries;

    public MCharacterMappingImpl(MCharacterMappingType type) {
        super(type);
    }

    @Override
    public Entry[] getMapping() {
        return entries;
    }

    @Override
    public void parse(Iterator<String> values) {
        int count = Integer.parseInt(values.next());
        entries = new Entry[count];
        for (int i = 0; i < count; i++) {
            String k = values.next();
            int i1 = Integer.parseInt(values.next());
            int i2 = Integer.parseInt(values.next());
            entries[i] = new EntryImpl(k, i1, i2);
        }
    }

}
