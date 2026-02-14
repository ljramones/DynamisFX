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

package org.dynamisfx.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.TextField;

/**
 * Define a text field that accept only numbers.
 *
 */
public abstract class NumberField extends TextField {
    // Constants allowed for this editor
    List<String> constants = new ArrayList<>();
    
    public NumberField() {
    }

    public void setConstants(List<String> constants) {
        this.constants.clear();
        this.constants.addAll(constants);
    }
    
    public String getNewText(int start, int end, String text) {
        String oldText = getText();
        String toReplace = oldText.substring(start, end);
        String newText;
        if (toReplace.isEmpty()) {
            // start/end is outside oldText ==> add
            newText = oldText + text;
        } else {
            String headerStr = oldText.substring(0, start);
            String trailerStr = "";
            if (end < oldText.length()) {
                trailerStr = oldText.substring(end, oldText.length());
            }
            newText = headerStr + text + trailerStr;
        }
        return newText;
     }

    protected boolean partOfConstants(String text) {
        // Check if the text is a part of a constant
        text = text.toLowerCase(Locale.ROOT);
        for (String constant : constants) {
            if (constant.toLowerCase(Locale.ROOT).startsWith(text)) {
                return true;
            }
        }
        return false;
    }
}
