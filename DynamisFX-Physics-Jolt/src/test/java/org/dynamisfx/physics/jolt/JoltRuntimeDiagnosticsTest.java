package org.dynamisfx.physics.jolt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

class JoltRuntimeDiagnosticsTest {

    @Test
    void reportContainsCoreFields() {
        String report = JoltRuntimeDiagnostics.report();
        assertThat(report, notNullValue());
        assertThat(report, containsString("os="));
        assertThat(report, containsString("arch="));
        assertThat(report, containsString("java="));
        assertThat(report, containsString("cshim="));
        assertThat(report, containsString("jolt-jni="));
    }
}
