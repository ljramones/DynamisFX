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

import javafx.animation.Interpolator;

/**
 * MayaAnimationCurveInterpolator
 * <p/>
 * Interpolator is from javafx.animation
 */
class MayaAnimationCurveInterpolator extends Interpolator {

    float p1Delta;
    float p2Delta;
    boolean zeroDuration;
    public String debug; // hack

    //=========================================================================
    // MayaAnimationCurveInterpolator
    //=========================================================================
    public MayaAnimationCurveInterpolator(
            float p1Delta, float p2Delta,
            boolean zeroDuration) {
        this.p1Delta = p1Delta;
        this.p2Delta = p2Delta;
        this.zeroDuration = zeroDuration;
    }

    //=========================================================================
    // MayaAnimationCurveInterpolator.curve
    //=========================================================================
    @Override
    public double curve(double t) { return t; }

    //=========================================================================
    // MayaAnimationCurveInterpolator.interpolate
    //=========================================================================
    // [!] API Change
    public double interpolate2(
            double startValue, double endValue,
            double fraction) {
        if (Double.isNaN(fraction)) {
            return startValue;
        }
        if (zeroDuration) {
            return endValue;
        }
        float t = (float) fraction;
        float oneMinusT = 1.0f - t;
        float tSquared = t * t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float p0 = (float) startValue;
        float p3 = (float) endValue;
        float p1 = p0 + p1Delta;
        float p2 = p3 + p2Delta;
        float ret = ((oneMinusTSquared * oneMinusT * p0) +
                (3 * oneMinusTSquared * t * p1) +
                (3 * oneMinusT * tSquared * p2) +
                (tSquared * t * p3));

        if (debug != null) {
            // if (DEBUG) System.out.println("interpolate: " + debug + ": " + t + " " + startValue + " to " + endValue + ": "+ret);
        }

        return ret;
    }

    //=========================================================================
    // MayaAnimationCurveInterpolator.interpolate
    //=========================================================================
    // [!] API Change
    public int interpolate2(int startValue, int endValue, double fraction) {
        return (int) interpolate(
                (double) startValue,
                (double) endValue,
                fraction);
    }

    //=========================================================================
    // MayaAnimationCurveInterpolator.interpolate
    //=========================================================================
    // [!] API Change
    public Object interpolate2(Object startValue, Object endValue, double fraction) {
        return interpolate(
                ((Number) startValue).doubleValue(),
                ((Number) endValue).doubleValue(),
                fraction);
    }
}
