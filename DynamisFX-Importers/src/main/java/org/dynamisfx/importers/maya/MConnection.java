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

package org.dynamisfx.importers.maya;

import java.util.Comparator;

public class MConnection {
    private final MPath sourcePath;
    private final MPath targetPath;

    public MConnection(MPath sourcePath, MPath targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    public MPath getSourcePath() {
        return sourcePath;
    }

    public MPath getTargetPath() {
        return targetPath;
    }

    @Override
    public boolean equals(Object arg) {
        if (!(arg instanceof MConnection)) {
            return false;
        }
        MConnection other = (MConnection) arg;
        return (sourcePath.equals(other.sourcePath) &&
                targetPath.equals(other.targetPath));
    }

    @Override
    public int hashCode() {
        return sourcePath.hashCode() ^ targetPath.hashCode();
    }

    public static final Comparator<? super MConnection> SOURCE_PATH_COMPARATOR = (o1, o2) -> {
        return o1.getSourcePath().compareTo(o2.getSourcePath());
    };

    public static final Comparator<? super MConnection> TARGET_PATH_COMPARATOR = (o1, o2) -> {
        return o1.getTargetPath().compareTo(o2.getTargetPath());
    };
}
