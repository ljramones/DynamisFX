package org.dynamisfx.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.dynamisfx.DynamisFXSample;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void addSampleIgnoresBadPackagePathAndAcceptsValidPath() {
        Project project = new Project("FXyz-Samples", "org.dynamisfx.samples");
        DynamisFXSample sample = new StubSample();

        project.addSample("org", sample);
        assertEquals(0, project.getSampleTree().size());

        project.addSample("org.dynamisfx.samples.geometry", sample);
        assertEquals(1, project.getSampleTree().size());
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
