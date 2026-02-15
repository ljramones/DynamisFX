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

module org.dynamisfx.core {
    requires transitive javafx.controls;
    requires static javafx.swing;
    requires java.desktop;
    requires java.logging;
    requires static eu.mihosoft.vrl.jcsg;		// resolved as automatic module
    requires static eu.mihosoft.vvecmath;		// resolved as automatic module
    requires static org.orbisgis.poly2tri.core; // resolved as automatic module
    requires org.slf4j;
    requires static fastnoiselitenouveau;	// resolved as automatic module

    exports org.dynamisfx.collision;
    exports org.dynamisfx.geometry;
    exports org.dynamisfx.io;
    exports org.dynamisfx.physics.api;
    exports org.dynamisfx.physics.hybrid;
    exports org.dynamisfx.physics.model;
    exports org.dynamisfx.physics.step;
    exports org.dynamisfx.physics.sync;
    exports org.dynamisfx.scene;
    exports org.dynamisfx.scene.paint;
    exports org.dynamisfx.scene.selection;
    exports org.dynamisfx.simulation;
    exports org.dynamisfx.simulation.coupling;
    exports org.dynamisfx.simulation.entity;
    exports org.dynamisfx.simulation.orbital;
    exports org.dynamisfx.simulation.rigid;
    exports org.dynamisfx.simulation.runtime;
    exports org.dynamisfx.shapes;
    exports org.dynamisfx.shapes.complex.cloth;
    exports org.dynamisfx.shapes.composites;
    exports org.dynamisfx.shapes.containers;
    exports org.dynamisfx.shapes.polygon;
    exports org.dynamisfx.shapes.polygon.symbolic;
    exports org.dynamisfx.shapes.primitives;
    exports org.dynamisfx.shapes.primitives.helper;
    exports org.dynamisfx.tools;
    exports org.dynamisfx.utils;
    exports org.dynamisfx.utils.geom;
    exports org.dynamisfx.particlefields;
    exports org.dynamisfx.particlefields.orbital;
    exports org.dynamisfx.particlefields.linear;
    exports org.dynamisfx.particlefields.vortex;
    exports org.dynamisfx.particlefields.noise;
}
