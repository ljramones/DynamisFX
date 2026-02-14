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

package org.dynamisfx.importers.maya.values;

import java.util.Iterator;
import java.util.List;
import org.dynamisfx.importers.maya.MEnv;
import org.dynamisfx.importers.maya.types.MDataType;

public interface MData {
    public MEnv getEnv();

    public MDataType getType();

    public void setSize(int size);

    public void parse(String field, List<String> values);

    public void parse(List<String> values);

    public void parse(Iterator<String> iter);

    /** Get the data associated with the given string path. */
    public MData getData(String path);

    /** Field access for those values which support it, such as compound values. */
    public MData getFieldData(String name);

    /** Index access for those values which suport it, such as array values. */
    public MData getData(int index);

    /** Slice access for those values which support it, such as array values. */
    public MData getData(int start, int end);
}
