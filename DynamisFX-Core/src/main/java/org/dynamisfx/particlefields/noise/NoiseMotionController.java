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
package org.dynamisfx.particlefields.noise;

import com.cognitivedynamics.noisegen.FastNoiseLite;
import com.cognitivedynamics.noisegen.spatial.TurbulenceNoise;
import org.dynamisfx.particlefields.ParticleFieldElement;

import java.util.List;

/**
 * Applies curl-noise displacement and density modulation to particle fields
 * using fastnoiselitenouveau's {@link TurbulenceNoise} for curl FBm and
 * {@link FastNoiseLite} for 4D density noise.
 *
 * <p>The controller supports frame-skipping via {@link NoiseMotionConfig#updateInterval()}
 * with linear interpolation on intermediate frames for smooth motion at reduced cost.
 */
public class NoiseMotionController {

    private final NoiseMotionConfig config;
    private final TurbulenceNoise turbulenceNoise;
    private final FastNoiseLite densityNoise;

    private double noiseTime = 0;
    private int frameCounter = 0;

    // Previous/current displacement arrays for interpolation
    private double[] prevDx, prevDy, prevDz;
    private double[] currDx, currDy, currDz;
    private boolean hasDisplacements = false;

    public NoiseMotionController(NoiseMotionConfig config) {
        this.config = config;

        // Curl noise: OpenSimplex2 base with TurbulenceNoise wrapper for curlFBm3D
        FastNoiseLite curlBase = new FastNoiseLite(config.seed());
        curlBase.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.turbulenceNoise = new TurbulenceNoise(curlBase, (float) config.curlFrequency());

        // Density noise: separate FastNoiseLite with FBm fractal (different seed)
        this.densityNoise = new FastNoiseLite(config.seed() + 1000);
        densityNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
        densityNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        densityNoise.SetFractalOctaves(config.densityOctaves());
        densityNoise.SetFrequency((float) config.densityFrequency());
    }

    /**
     * Applies noise-driven motion and density modulation to the given elements.
     *
     * @param elements the particle elements to modify
     * @param timeScale frame time multiplier (1.0 = normal speed)
     */
    public void applyNoiseMotion(List<ParticleFieldElement> elements, double timeScale) {
        noiseTime += timeScale * config.timeSpeed();
        frameCounter++;

        int count = elements.size();
        boolean fullUpdate = (frameCounter % config.updateInterval() == 0) || !hasDisplacements;

        if (fullUpdate) {
            ensureArrays(count);

            // Shift current -> previous
            if (hasDisplacements) {
                System.arraycopy(currDx, 0, prevDx, 0, count);
                System.arraycopy(currDy, 0, prevDy, 0, count);
                System.arraycopy(currDz, 0, prevDz, 0, count);
            }

            // Compute new curl displacements using TurbulenceNoise.curlFBm3D
            float timeOffset = (float) noiseTime;
            for (int i = 0; i < count; i++) {
                ParticleFieldElement e = elements.get(i);
                float x = (float) e.getX();
                float y = (float) e.getY();
                float z = (float) e.getZ() + timeOffset;

                float[] curl = turbulenceNoise.curlFBm3D(x, y, z, config.curlOctaves());
                currDx[i] = curl[0] * config.curlStrength();
                currDy[i] = curl[1] * config.curlStrength();
                currDz[i] = curl[2] * config.curlStrength();
            }

            if (!hasDisplacements) {
                // First frame: copy current to previous
                System.arraycopy(currDx, 0, prevDx, 0, count);
                System.arraycopy(currDy, 0, prevDy, 0, count);
                System.arraycopy(currDz, 0, prevDz, 0, count);
                hasDisplacements = true;
            }
        }

        // Interpolation factor for intermediate frames
        double t = fullUpdate ? 1.0 :
                (double) (frameCounter % config.updateInterval()) / config.updateInterval();

        // Apply displacement and density modulation
        for (int i = 0; i < count; i++) {
            ParticleFieldElement e = elements.get(i);

            // Interpolated displacement
            double dx = prevDx[i] + t * (currDx[i] - prevDx[i]);
            double dy = prevDy[i] + t * (currDy[i] - prevDy[i]);
            double dz = prevDz[i] + t * (currDz[i] - prevDz[i]);
            e.addVelocity(dx * timeScale, dy * timeScale, dz * timeScale);

            // Density modulation using true 4D noise (x, y, z, time)
            if (config.densityOpacityEnabled() || config.densityScaleEnabled()) {
                float nx = (float) e.getX();
                float ny = (float) e.getY();
                float nz = (float) e.getZ();
                float nw = (float) noiseTime;
                // GetNoise returns values in [-1, 1], remap to [0, 1]
                double density = (densityNoise.GetNoise(nx, ny, nz, nw) + 1.0) * 0.5;

                if (config.densityOpacityEnabled()) {
                    double opacity = config.densityMinOpacity() +
                            density * (config.densityMaxOpacity() - config.densityMinOpacity());
                    e.setDynamicOpacity(opacity);
                }

                if (config.densityScaleEnabled()) {
                    double scale = config.densityMinScale() +
                            density * (config.densityMaxScale() - config.densityMinScale());
                    e.setDynamicScale(scale);
                }
            }
        }
    }

    private void ensureArrays(int count) {
        if (currDx == null || currDx.length < count) {
            prevDx = new double[count];
            prevDy = new double[count];
            prevDz = new double[count];
            currDx = new double[count];
            currDy = new double[count];
            currDz = new double[count];
            hasDisplacements = false;
        }
    }

    /**
     * Resets the noise time and displacement history.
     */
    public void reset() {
        noiseTime = 0;
        frameCounter = 0;
        hasDisplacements = false;
    }

    public NoiseMotionConfig getConfig() {
        return config;
    }

    public double getNoiseTime() {
        return noiseTime;
    }
}
