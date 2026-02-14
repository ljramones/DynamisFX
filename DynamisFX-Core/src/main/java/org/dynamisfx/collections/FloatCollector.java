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

package org.dynamisfx.collections;

import java.util.Arrays;

/**
 * Collector to generate a float[] array from a DoubleStream of floats
 * 
 * General use:
 * .collect(FloatCollector::new, FloatCollector::add, FloatCollector::join)
 * Known initial size:
 * .collect(()- new FloatCollector(size), FloatCollector::add, FloatCollector::join)
 * 
 * Generates a FloatCollector, use FloatCollector.toArray() to get the float[]
 * 
 * @author JosÃ© Pereda Llamas
 * Created on 19-nov-2014 - 18:51:11
 */
public class FloatCollector {

    private float[] curr=new float[64];
    private int size;

    public FloatCollector(){}
    
    public FloatCollector(int initialSize){
        if(curr.length<initialSize){
            curr=Arrays.copyOf(curr, initialSize);
        }
    }
    public void add(double d) {
        if(curr.length==size){
            curr=Arrays.copyOf(curr, size*2);
        }
        curr[size++]=(float)d;
    }

    public void join(FloatCollector other) {
        if(size+other.size > curr.length) {
            curr=Arrays.copyOf(curr, size+other.size);
        }
        System.arraycopy(other.curr, 0, curr, size, other.size);
        size+=other.size;
    }

    public float[] toArray() {
        if(size!=curr.length){
            curr=Arrays.copyOf(curr, size);
        }
        return curr;
    }
}
