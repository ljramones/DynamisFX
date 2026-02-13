/**
 * F(X)yz
 *
 * Copyright (c) 2013-2025, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    exports org.dynamisfx.simulation.orbital;
    exports org.dynamisfx.simulation.rigid;
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
}
