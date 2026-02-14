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

package org.dynamisfx.importers.maya.values.impl;

import java.util.Iterator;
import org.dynamisfx.importers.maya.types.MNurbsCurveType;
import org.dynamisfx.importers.maya.values.MData;
import org.dynamisfx.importers.maya.values.MNurbsCurve;

public class MNurbsCurveImpl extends MDataImpl implements MNurbsCurve {
    int degree;
    int spans;
    int form;
    boolean rational;
    int dimension;
    int numKnots;
    float[] knots;
    int numCvs;
    float[] cvs;

    public MNurbsCurveImpl(MNurbsCurveType type) {
        super(type);
    }

    @Override
    public MData getData(int start, int end) {
        return this; // hack?
    }

    @Override
    public int getDegree() {
        return degree;
    }

    @Override
    public int getSpans() {
        return spans;
    }

    @Override
    public int getForm() {
        return form;
    }

    @Override
    public boolean isRational() {
        return rational;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int getNumKnots() {
        return numKnots;
    }

    @Override
    public float[] getKnots() {
        return knots;
    }

    @Override
    public int getNumCVs() {
        return numCvs;
    }

    @Override
    public float[] getCVs() {
        return cvs;
    }


    @Override
    public void parse(Iterator<String> values) {
        degree = Integer.parseInt(values.next());
        //        System.out.println("degree="+degree);
        spans = Integer.parseInt(values.next());
        //        System.out.println("spans="+spans);
        form = Integer.parseInt(values.next());
        //        System.out.println("form="+form);
        String tok = values.next();
        //        rational = tok.equals("yes");
        //        System.out.println("rational="+rational);
        dimension = Integer.parseInt(values.next());
        //        System.out.println("dimension="+dimension);
        numKnots = Integer.parseInt(values.next());
        //        System.out.println("numKnots="+numKnots);
        knots = new float[numKnots];
        for (int i = 0; i < numKnots; i++) {
            knots[i] = Float.parseFloat(values.next());
            //            System.out.println("knot="+knots[i]);
        }
        numCvs = Integer.parseInt(values.next());
        cvs = new float[numCvs * dimension];
        for (int i = 0; i < cvs.length; i++) {
            cvs[i] = Float.parseFloat(values.next());
        }
    }

}
