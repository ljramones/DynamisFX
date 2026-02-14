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

package org.dynamisfx.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.dynamisfx.DynamisFXSample;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void addSampleIgnoresBadPackagePathAndAcceptsValidPath() {
        Project project = new Project("DynamisFX-Demo", "org.dynamisfx.samples");
        DynamisFXSample sample = new StubSample();

        project.addSample("org", sample);
        assertEquals(0, project.getSampleTree().size());

        project.addSample("org.dynamisfx.samples.geometry", sample);
        assertEquals(1, project.getSampleTree().size());
    }

    @Test
    void collisionSamplesAreRoutedIntoDedicatedDivisions() {
        Project project = new Project("DynamisFX-Demo", "org.dynamisfx.samples");

        project.addSample("org.dynamisfx.samples.utilities", new CollisionDebugWorld());
        project.addSample("org.dynamisfx.samples.utilities", new BroadPhaseComparisonDemo());
        project.addSample("org.dynamisfx.samples.utilities", new Ode4jPhysicsSyncSample());
        project.addSample("org.dynamisfx.samples.utilities", new CcdTunnelingDemo());
        project.addSample("org.dynamisfx.samples.utilities", new CouplingTransitionDemo());

        SampleTree.TreeNode root = project.getSampleTree().getRoot();
        assertTrue(root.containsChild("Collision Detection"));
        assertTrue(root.containsChild("Collision Detection Solver"));
        assertTrue(root.containsChild("Pipeline Physics"));
    }

    private static class StubSample implements DynamisFXSample {
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

    private static final class CollisionDebugWorld extends StubSample {
    }

    private static final class Ode4jPhysicsSyncSample extends StubSample {
    }

    private static final class BroadPhaseComparisonDemo extends StubSample {
    }

    private static final class CcdTunnelingDemo extends StubSample {
    }

    private static final class CouplingTransitionDemo extends StubSample {
    }
}
