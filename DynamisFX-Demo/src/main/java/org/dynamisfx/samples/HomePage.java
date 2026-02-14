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

package org.dynamisfx.samples;

import javafx.scene.Node;
import org.dynamisfx.model.WelcomePage;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class HomePage extends WelcomePage{
    
    private static final String dynamisFxDemoTitle = "DynamisFX Demo";
    private static final Node homePageContent = buildHomePageContent();
    
    public HomePage() {
        this(dynamisFxDemoTitle, homePageContent);
    }

    private HomePage(String title, Node content) {
        super(title, content);
    }

    private static Node buildHomePageContent() {
        return null;
    }
    
}
