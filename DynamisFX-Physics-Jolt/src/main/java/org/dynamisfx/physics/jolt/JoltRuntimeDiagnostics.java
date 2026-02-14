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

package org.dynamisfx.physics.jolt;

/**
 * Produces runtime diagnostics for Jolt native loading without side effects like process exit.
 */
public final class JoltRuntimeDiagnostics {

    private JoltRuntimeDiagnostics() {
    }

    public static String report() {
        String os = System.getProperty("os.name", "unknown");
        String arch = System.getProperty("os.arch", "unknown");
        String javaVersion = System.getProperty("java.version", "unknown");

        JoltNativeBridge cshim = new JoltNativeBridge();
        String cshimStatus;
        if (cshim.isAvailable()) {
            cshimStatus = "available api=" + cshim.apiVersion() + " mode=" + cshim.backendMode();
        } else {
            cshimStatus = "unavailable load=" + cshim.loadDescription();
        }

        JoltJniNativeLoader.LoadResult jni = JoltJniNativeLoader.ensureLoaded();
        String jniStatus = jni.available()
                ? "available load=" + jni.description()
                : "unavailable load=" + jni.description();

        return "os=" + os
                + ",arch=" + arch
                + ",java=" + javaVersion
                + ",cshim=" + cshimStatus
                + ",jolt-jni=" + jniStatus;
    }
}
