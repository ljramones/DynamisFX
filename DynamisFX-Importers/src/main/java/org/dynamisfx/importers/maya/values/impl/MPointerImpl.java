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
import org.dynamisfx.importers.maya.MNode;
import org.dynamisfx.importers.maya.MPath;
import org.dynamisfx.importers.maya.types.MPointerType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MPointer;

public class MPointerImpl extends MDataImpl implements MPointer {

    private MPath target;

    public MPointerImpl(MPointerType type) {
        super(type);
    }

    @Override
    public void setTarget(MPath path) {
        target = path;
    }

    @Override
    public MPath getTarget() {
        return target;
    }

    public void set(MData data) {
        //targetNode.setAttr(targetAttribute, data);
    }

    public MData get() {
        return target.apply();
    }

    @Override
    public void parse(Iterator<String> iter) {
        // Nothing
    }

    @Override
    public String toString() {
        if (target != null) {
            return target.toString();
        } else {
            return "Null Pointer";
        }
    }

    @Override
    public MNode getTargetNode() {
        return target.getTargetNode();
    }
}
