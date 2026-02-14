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

package org.dynamisfx.controls;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

final class ScriptEngineSupport {
    private static final Logger LOG = Logger.getLogger(ScriptEngineSupport.class.getName());
    private static final String SCRIPT_ENGINE_ENABLED_PROPERTY = "dynamisfx.samples.enableScriptEngine";
    static final String DIAG_SCRIPT_ENABLED_PROPERTY = "dynamisfx.samples.scriptEngine.enabled";
    static final String DIAG_SCRIPT_AVAILABLE_PROPERTY = "dynamisfx.samples.scriptEngine.available";
    private static final List<String> ENGINE_NAMES = List.of("graal.js", "js", "JavaScript", "javascript");
    private static final AtomicBoolean NO_ENGINE_LOGGED = new AtomicBoolean(false);
    private static final ScriptEngine SHARED_ENGINE = createEngine();

    static {
        boolean enabled = Boolean.getBoolean(SCRIPT_ENGINE_ENABLED_PROPERTY);
        System.setProperty(DIAG_SCRIPT_ENABLED_PROPERTY, Boolean.toString(enabled));
        System.setProperty(DIAG_SCRIPT_AVAILABLE_PROPERTY, Boolean.toString(SHARED_ENGINE != null));
    }

    private ScriptEngineSupport() {
    }

    static ScriptEngine sharedEngine() {
        return SHARED_ENGINE;
    }

    static void logNoEngineOnce() {
        if (NO_ENGINE_LOGGED.compareAndSet(false, true)) {
            LOG.warning("No JavaScript ScriptEngine is available. Script function controls are disabled.");
        }
    }

    private static ScriptEngine createEngine() {
        if (!Boolean.getBoolean(SCRIPT_ENGINE_ENABLED_PROPERTY)) {
            return null;
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        for (String name : ENGINE_NAMES) {
            try {
                ScriptEngine candidate = manager.getEngineByName(name);
                if (candidate != null) {
                    return candidate;
                }
            } catch (RuntimeException ex) {
                // Try the next provider.
            }
        }
        return null;
    }
}
