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

package org.dynamisfx.importers.maya;

import javafx.util.Duration;

public class Frame extends Duration {

    static final double FPS = 24.0;

    Frame(double frames) {
        super(frames / FPS * 1000.0);
    }

    Frame(int frames) {
        super((double) frames / FPS * 1000.0);
    }

    Frame(int frames, int fps) {
        super(((double) frames) / ((double) fps) * 1000.0);
    }

    public static Duration valueOf(double frames) {
        return Duration.seconds(frames / FPS * 1000.0);
    }

}

