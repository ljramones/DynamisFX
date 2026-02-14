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

package org.dynamisfx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.dynamisfx.DynamisFXSample;
import org.junit.jupiter.api.Test;

class DynamisFXClientResourceTest {

    @Test
    void getResourceReturnsEmptyStringForNullStream() throws Exception {
        DynamisFXClient client = new DynamisFXClient();
        Method method = DynamisFXClient.class.getDeclaredMethod("getResource", InputStream.class);
        method.setAccessible(true);

        String result = (String) method.invoke(client, new Object[] {null});
        assertEquals("", result);
    }

    @Test
    void formatMethodsResolveBundledTemplates() throws Exception {
        DynamisFXClient client = new DynamisFXClient();
        DynamisFXSample sample = new StubSample();

        Method formatSource = DynamisFXClient.class.getDeclaredMethod("formatSourceCode", org.dynamisfx.DynamisFXSample.class);
        formatSource.setAccessible(true);
        String sourceHtml = (String) formatSource.invoke(client, sample);

        Method formatCss = DynamisFXClient.class.getDeclaredMethod("formatCss", org.dynamisfx.DynamisFXSample.class);
        formatCss.setAccessible(true);
        String cssHtml = (String) formatCss.invoke(client, sample);

        assertNotNull(sourceHtml);
        assertNotNull(cssHtml);
        assertTrue(sourceHtml.contains("No sample source available"));
        assertTrue(cssHtml.contains("No CSS source available"));
        assertTrue(sourceHtml.contains("<pre class=\"brush: java\">"));
        assertTrue(cssHtml.contains("<pre class=\"brush: css\">"));
    }

    private static final class StubSample implements DynamisFXSample {
        @Override public String getSampleName() { return "Stub"; }
        @Override public String getSampleDescription() { return ""; }
        @Override public String getProjectName() { return "Test"; }
        @Override public String getProjectVersion() { return "1"; }
        @Override public Node getPanel(Stage stage) { return null; }
        @Override public Node getControlPanel() { return null; }
        @Override public String getJavaDocURL() { return null; }
        @Override public String getControlStylesheetURL() { return null; }
        @Override public String getSampleSourceURL() { return null; }
        @Override public boolean isVisible() { return true; }
    }
}
