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

/**
 *  Utility class providing vantage points.
 *  For primary use with (upcoming) CameraControl
 * @author Jason Pollastrini aka jdub1581
 */
public enum CameraPositions {
    //6 centers
    FRONT_CENTER,     
    BACK_CENTER,    
    LEFT_CENTER,
    RIGHT_CENTER,
    TOP_CENTER,
    BOTTOM_CENTER,
    
    //12 edges
    LEFT_LEFT,
    LEFT_RIGHT, 
    LEFT_TOP,
    LEFT_BOTTOM,
    
    RIGHT_LEFT, 
    RIGHT_RIGHT,
    RIGHT_TOP,
    RIGHT_BOTTOM,
    
    TOP_LEFT, 
    TOP_RIGHT, 
    TOP_FRONT,
    TOP_BACK,
    
    BOTTOM_LEFT, 
    BOTTOM_RIGHT, 
    BOTTOM_FRONT,
    BOTTOM_BACK,
    
    // 8 corners
    TOP_LEFT_FRONT_CORNER,
    TOP_LEFT_BACK_CORNER,
    TOP_RIGHT_FRONT_CORNER,
    TOP_RIGHT_BACK_CORNER,
    
    BOTTOM_LEFT_FRONT_CORNER,
    BOTTOM_LEFT_BACK_CORNER,
    BOTTOM_RIGHT_FRONT_CORNER,
    BOTTOM_RIGHT_BACK_CORNER;
    
    private CameraPositions(){}        
}
