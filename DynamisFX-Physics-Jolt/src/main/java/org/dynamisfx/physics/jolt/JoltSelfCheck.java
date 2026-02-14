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
 * Lightweight native-loading self-check for Jolt providers.
 */
public final class JoltSelfCheck {

    private JoltSelfCheck() {
    }

    public static void main(String[] args) {
        JoltNativeBridge cshim = new JoltNativeBridge();
        String cshimStatus;
        if (cshim.isAvailable()) {
            cshimStatus = "available api=" + cshim.apiVersion() + " backendMode=" + cshim.backendMode();
        } else {
            cshimStatus = "unavailable load=" + cshim.loadDescription();
        }

        JoltJniNativeLoader.LoadResult joltJni = JoltJniNativeLoader.ensureLoaded();
        String jniStatus = joltJni.available()
                ? "available load=" + joltJni.description()
                : "unavailable load=" + joltJni.description();

        System.out.println("jolt.selfcheck cshim=" + cshimStatus);
        System.out.println("jolt.selfcheck jolt-jni=" + jniStatus);

        if (!cshim.isAvailable() && !joltJni.available()) {
            System.exit(2);
        }
    }
}
