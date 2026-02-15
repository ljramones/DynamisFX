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
package org.dynamisfx.particlefields;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Generates soft radial gradient textures for volumetric particle rendering.
 * <p>
 * The generated "blob" texture has a smooth gaussian-like falloff from
 * opaque center to fully transparent edges. When applied to cross-quad
 * markers, overlapping particles blend together to create fog, clouds,
 * and smoke effects.
 * <p>
 * Two texture variants are generated for the atmospheric material:
 * <ul>
 *   <li><b>Alpha blob</b> (for diffuseMap) - controls transparency falloff at edges</li>
 *   <li><b>Color blob</b> (for selfIlluminationMap) - provides visible color without
 *       directional shading, with RGB fading to black at edges</li>
 * </ul>
 */
public final class SoftParticleTexture {

    private SoftParticleTexture() {}

    /**
     * Creates an alpha blob texture for use as diffuseMap.
     * White RGB with gaussian alpha falloff from center to edges.
     *
     * @param resolution texture size in pixels (e.g. 64 for 64x64)
     * @param sigma      gaussian sigma (0.3 = tight, 0.5 = normal, 0.7 = wide)
     * @return a WritableImage with radial alpha gradient
     */
    public static WritableImage createAlphaBlob(int resolution, double sigma) {
        WritableImage img = new WritableImage(resolution, resolution);
        PixelWriter pw = img.getPixelWriter();

        double center = (resolution - 1) / 2.0;
        double maxDist = center;

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                double dx = (x - center) / maxDist;
                double dy = (y - center) / maxDist;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // Gaussian falloff: exp(-dist^2 / (2*sigma^2))
                double alpha;
                if (dist >= 1.0) {
                    alpha = 0.0;
                } else {
                    alpha = Math.exp(-(dist * dist) / (2.0 * sigma * sigma));
                    // Ensure it reaches 0 at the edge
                    double edgeFade = 1.0 - dist;
                    alpha *= edgeFade;
                }

                pw.setColor(x, y, new Color(1.0, 1.0, 1.0, Math.max(0, alpha)));
            }
        }

        return img;
    }

    /**
     * Creates a colored blob texture for use as selfIlluminationMap.
     * RGB fades from the specified color at center to black at edges.
     * Alpha is 1.0 everywhere (self-illumination ignores alpha).
     *
     * @param resolution texture size in pixels
     * @param color      the visible particle color
     * @param sigma      gaussian sigma for falloff
     * @return a WritableImage with radial RGB gradient
     */
    public static WritableImage createColorBlob(int resolution, Color color, double sigma) {
        WritableImage img = new WritableImage(resolution, resolution);
        PixelWriter pw = img.getPixelWriter();

        double center = (resolution - 1) / 2.0;
        double maxDist = center;

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                double dx = (x - center) / maxDist;
                double dy = (y - center) / maxDist;
                double dist = Math.sqrt(dx * dx + dy * dy);

                double intensity;
                if (dist >= 1.0) {
                    intensity = 0.0;
                } else {
                    intensity = Math.exp(-(dist * dist) / (2.0 * sigma * sigma));
                    double edgeFade = 1.0 - dist;
                    intensity *= edgeFade;
                }

                pw.setColor(x, y, new Color(
                        color.getRed() * intensity,
                        color.getGreen() * intensity,
                        color.getBlue() * intensity,
                        1.0
                ));
            }
        }

        return img;
    }

    /**
     * Creates a soft radial gradient blob texture (legacy API).
     */
    public static WritableImage create(int resolution, Color color, double falloff) {
        WritableImage img = new WritableImage(resolution, resolution);
        PixelWriter pw = img.getPixelWriter();

        double center = (resolution - 1) / 2.0;
        double maxDist = center;
        double maxAlpha = color.getOpacity();

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                double dx = (x - center) / maxDist;
                double dy = (y - center) / maxDist;
                double dist = Math.sqrt(dx * dx + dy * dy);

                double alpha;
                if (dist >= 1.0) {
                    alpha = 0.0;
                } else {
                    alpha = Math.pow(1.0 - dist, falloff) * maxAlpha;
                }

                pw.setColor(x, y, new Color(
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        alpha
                ));
            }
        }

        return img;
    }

    /**
     * Creates a blob texture with default quadratic falloff.
     */
    public static WritableImage create(int resolution, Color color) {
        return create(resolution, color, 2.0);
    }

    /**
     * Creates a white blob texture suitable for tinting via PhongMaterial.diffuseColor.
     */
    public static WritableImage createWhite(int resolution, double falloff) {
        return create(resolution, Color.WHITE, falloff);
    }
}
