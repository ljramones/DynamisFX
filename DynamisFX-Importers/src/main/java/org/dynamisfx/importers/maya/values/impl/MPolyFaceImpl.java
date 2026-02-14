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
import org.dynamisfx.importers.maya.types.MPolyFaceType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MPolyFace;


public class MPolyFaceImpl extends MDataImpl implements MPolyFace {
    private List<FaceData> faces;

    public MPolyFaceImpl(MPolyFaceType type) {
        super(type);
    }

    @Override
    public void addFace(FaceData face) {
        if (faces == null) {
            faces = new ArrayList<>();
        }
        faces.add(face);
    }

    @Override
    public MData getData(int start, int end) {
        return this; // hack?
    }

    @Override
    public List<FaceData> getFaces() {
        return faces;
    }

    @Override
    public void parse(Iterator<String> values) {
        // System.out.println("parsing poly faces: " + values);
        new Parser(values).parse();
    }

    class Parser {
        private final Iterator<String> curArgs;

        Parser(Iterator<String> args) {
            curArgs = args;
        }

        public void parse() {
            MPolyFace.FaceData curFace = null;
            while (moreArgs()) {
                String tok = nextArg();
                if (tok.equals("f")) {
                    if (curFace != null) {
                        addFace(curFace);
                    }
                    curFace = new MPolyFace.FaceData();
                    curFace.setFaceEdges(nextIntArray());
                } else if (tok.equals("h")) {
                    if (curFace != null) curFace.setHoleEdges(nextIntArray());
                } else if (tok.equals("mu")) {
                    int uvSet = nextInt();
                    if (curFace != null) curFace.setUVData(uvSet, nextIntArray());
                } else if (tok.equals("fc")) {
                    if (curFace != null) curFace.setFaceColors(nextIntArray());
                }
            }
            if (curFace != null) {
                addFace(curFace);
            }
        }

        private boolean moreArgs() {
            return curArgs.hasNext();
        }

        private String nextArg() {
            return curArgs.next();
        }

        private int nextInt() {
            return Integer.parseInt(nextArg());
        }

        private int[] nextIntArray() {
            int num = nextInt();
            int[] res = new int[num];
            for (int i = 0; i < num; i++) {
                res[i] = nextInt();
            }
            return res;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getType().getName());
        if (faces == null) {
            result.append(" ");
            result.append(faces);
        } else {
            for (FaceData fd : faces) {
                result.append(" ");
                result.append(fd);
            }
        }
        return result.toString();
    }
}
