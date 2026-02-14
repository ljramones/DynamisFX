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
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

module org.dynamisfx.samples {
    requires org.dynamisfx.core;
    requires org.dynamisfx.importers;
    requires org.dynamisfx.client;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires java.logging;
    requires org.controlsfx.controls;
    requires org.reactfx;		  // resolved as automatic module
    requires org.fxmisc.easybind; // resolved as automatic module
    requires jfxtras.common;
    requires jfxtras.controls;
    requires jfxtras.fxml;
    requires java.scripting;

    opens org.dynamisfx.controls to javafx.fxml;
    provides org.dynamisfx.DynamisFXSamplerProject with org.dynamisfx.samples.DynamisFXProject;
    
    exports org.dynamisfx.samples.importers to org.dynamisfx.client, org.dynamisfx.importers;
    exports org.dynamisfx.samples.shapes.compound to org.dynamisfx.client;
    exports org.dynamisfx.samples.shapes.texturedmeshes to org.dynamisfx.client;
    exports org.dynamisfx.samples.utilities to org.dynamisfx.client;
    exports org.dynamisfx.samples.particlefields to org.dynamisfx.client;
    exports org.dynamisfx.samples.collision to org.dynamisfx.client;
    exports org.dynamisfx.samples.physics.ode4j to org.dynamisfx.client;
    exports org.dynamisfx.samples;
    
}
