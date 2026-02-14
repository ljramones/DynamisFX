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

module org.dynamisfx.importers {
    requires org.dynamisfx.core;
    requires transitive javafx.graphics;
    requires static javafx.fxml;
    requires java.logging;

    opens org.dynamisfx.importers to javafx.fxml;

    uses org.dynamisfx.importers.Importer;

    exports org.dynamisfx.importers;
    exports org.dynamisfx.importers.cad;
    exports org.dynamisfx.importers.dae;
    exports org.dynamisfx.importers.dxf;
    exports org.dynamisfx.importers.fxml;
    exports org.dynamisfx.importers.gltf;
    exports org.dynamisfx.importers.maya;
    exports org.dynamisfx.importers.obj;
    exports org.dynamisfx.importers.off;
    exports org.dynamisfx.importers.ply;
    exports org.dynamisfx.importers.stl;
    exports org.dynamisfx.importers.tds;
    exports org.dynamisfx.importers.threemf;
    exports org.dynamisfx.importers.usd;
    exports org.dynamisfx.importers.vrml;
    exports org.dynamisfx.importers.x3d;
}
