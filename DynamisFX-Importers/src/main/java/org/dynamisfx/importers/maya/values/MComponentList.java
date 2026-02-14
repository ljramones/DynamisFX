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

import java.util.List;

public interface MComponentList extends MData {
    public static class Component {
        // Ideally we would have an enum of these, but we don't know all of the mappings yet.
        // The possible values are listed in MFn::Type (MFn.h), but not the names.
        // Here are some, derived by using the Maya selection tool and
        // watching the script editor output:
        //   "f[i]"          -> faces
        //   "vtx[i]"        -> vertices
        //   "e[i]"          -> edges
        //   "map[i]"        -> uvs
        //   "vtxFace[i][j]" -> vertices within faces
        private final String name;
        private final int startIndex; // Or -1 if "all"
        private final int endIndex;   // Inclusive

        public String name() { return name; }

        public int startIndex() { return startIndex; }

        public int endIndex() { return endIndex; }

        public Component(String name, int startIndex, int endIndex) {
            this.name = name;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public static Component parse(String str) {
            String name;
            int startIndex = 0;
            int endIndex = 0;

            int bracket = str.indexOf("[");
            int endBracket = str.indexOf("]");
            if (bracket < 0) {
                name = str;
                startIndex = -1;
            } else {
                name = str.substring(0, bracket);
                if (str.charAt(bracket + 1) == '*') {
                    startIndex = -1;
                    endIndex = -1;
                } else {
                    int i = bracket + 1;
                    for (; i < endBracket; i++) {
                        if (str.charAt(i) == ':')
                            break;
                        startIndex *= 10;
                        startIndex += str.charAt(i) - '0';
                    }
                    if (str.charAt(i) == ':') {
                        i++;
                        for (; i < endBracket; i++) {
                            endIndex *= 10;
                            endIndex += str.charAt(i) - '0';
                        }
                    } else {
                        endIndex = startIndex;
                    }
                }
            }

            return new Component(name, startIndex, endIndex);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(name);
            buf.append("[");
            if (startIndex < 0) {
                buf.append("*");
            } else {
                buf.append(startIndex);
                if (endIndex > startIndex) {
                    buf.append(":");
                    buf.append(endIndex);
                }
            }
            buf.append("]");
            return buf.toString();
        }
    }

    public void set(List<Component> value);

    public List<Component> get();
}
