/**
 * F(X)yz
 *
 * Copyright (c) 2013-2018, F(X)yz
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
