package org.dynamisfx.controls;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

final class ScriptEngineSupport {
    private static final Logger LOG = Logger.getLogger(ScriptEngineSupport.class.getName());
    private static final String SCRIPT_ENGINE_ENABLED_PROPERTY = "dynamisfx.samples.enableScriptEngine";
    private static final List<String> ENGINE_NAMES = List.of("graal.js", "js", "JavaScript", "javascript");
    private static final AtomicBoolean NO_ENGINE_LOGGED = new AtomicBoolean(false);
    private static final ScriptEngine SHARED_ENGINE = createEngine();

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
