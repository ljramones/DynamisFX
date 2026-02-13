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
    provides org.dynamisfx.FXyzSamplerProject with org.dynamisfx.samples.FXyzProject;
    
    exports org.dynamisfx.samples.importers to org.dynamisfx.client, org.dynamisfx.importers;
    exports org.dynamisfx.samples.shapes.compound to org.dynamisfx.client;
    exports org.dynamisfx.samples.shapes.texturedmeshes to org.dynamisfx.client;
    exports org.dynamisfx.samples.utilities to org.dynamisfx.client;
    exports org.dynamisfx.samples;
    
}
