package org.dynamisfx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.dynamisfx.FXyzSample;
import org.junit.jupiter.api.Test;

class FXyzClientResourceTest {

    @Test
    void getResourceReturnsEmptyStringForNullStream() throws Exception {
        FXyzClient client = new FXyzClient();
        Method method = FXyzClient.class.getDeclaredMethod("getResource", InputStream.class);
        method.setAccessible(true);

        String result = (String) method.invoke(client, new Object[] {null});
        assertEquals("", result);
    }

    @Test
    void formatMethodsResolveBundledTemplates() throws Exception {
        FXyzClient client = new FXyzClient();
        FXyzSample sample = new StubSample();

        Method formatSource = FXyzClient.class.getDeclaredMethod("formatSourceCode", org.dynamisfx.FXyzSample.class);
        formatSource.setAccessible(true);
        String sourceHtml = (String) formatSource.invoke(client, sample);

        Method formatCss = FXyzClient.class.getDeclaredMethod("formatCss", org.dynamisfx.FXyzSample.class);
        formatCss.setAccessible(true);
        String cssHtml = (String) formatCss.invoke(client, sample);

        assertNotNull(sourceHtml);
        assertNotNull(cssHtml);
        assertTrue(sourceHtml.contains("No sample source available"));
        assertTrue(cssHtml.contains("No CSS source available"));
        assertTrue(sourceHtml.contains("<pre class=\"brush: java\">"));
        assertTrue(cssHtml.contains("<pre class=\"brush: css\">"));
    }

    private static final class StubSample implements FXyzSample {
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
