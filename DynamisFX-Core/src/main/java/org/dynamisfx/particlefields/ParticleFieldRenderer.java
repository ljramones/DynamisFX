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

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.particlefields.noise.NoiseMotionController;
import org.dynamisfx.shapes.primitives.ScatterMesh;
import org.dynamisfx.shapes.primitives.helper.MarkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Renders particle field elements to a JavaFX Group.
 * This class is decoupled from window management, allowing particle fields to be
 * rendered in any 3D scene.
 *
 * <p>Uses ScatterMesh with per-particle attributes for efficient rendering:
 * <ul>
 *   <li>Per-particle color via colorIndex (maps to palette texture)</li>
 *   <li>Per-particle size via scale factor</li>
 *   <li>Per-particle opacity via opacity field</li>
 *   <li>Efficient position updates without full mesh rebuild</li>
 * </ul>
 *
 * <p>For LINEAR particles with finite lifetimes, expired particles are respawned
 * via the generator to maintain constant particle count.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * ParticleFieldRenderer renderer = new ParticleFieldRenderer();
 * renderer.initialize(config, new Random(42));
 * parentGroup.getChildren().add(renderer.getGroup());
 *
 * // In animation loop:
 * renderer.update(timeScale);
 * renderer.updateMeshPositions();
 * }</pre>
 */
public class ParticleFieldRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(ParticleFieldRenderer.class);

    /**
     * Rendering mode for particle clouds.
     */
    public enum RenderingMode {
        SCATTER_MESH_AUTO,
        SCATTER_MESH_SINGLE,
        SCATTER_MESH_CHUNKED,
        INDIVIDUAL_SPHERES
    }

    private static final int PALETTE_SIZE = 256;
    private static final int MAX_PARTICLES_PER_CHUNK = 2000;
    private static final int MAX_INDIVIDUAL_SPHERES = 10000;

    private final Group group = new Group();
    private ParticleFieldConfiguration config;
    private List<ParticleFieldElement> elements = new ArrayList<>();

    private ScatterMesh mesh;
    private List<ScatterMesh> chunkedMeshes = new ArrayList<>();
    private RenderingMode renderingMode = RenderingMode.SCATTER_MESH_AUTO;
    private boolean useChunkedRendering = true;
    private boolean usedSpatialBinning = false;
    private boolean useIndividualSpheres = false;
    private String currentLodLevel = "FULL";

    private List<Sphere> individualSpheres = new ArrayList<>();
    private List<Color> colorPalette;
    private double baseSize;
    private List<Point3D> cachedPoints;
    private boolean initialized = false;
    private boolean useOpacity = false;

    private double worldOffsetX = 0, worldOffsetY = 0, worldOffsetZ = 0;
    private boolean useWorldCoordinates = false;
    private double centroidX = 0, centroidY = 0, centroidZ = 0;
    private boolean useCentroidRebasing = true;

    /** Marker shape for ScatterMesh particles (null = ScatterMesh default TETRAHEDRA) */
    private MarkerFactory.Marker markerType;

    /** Atmospheric/volumetric rendering using soft blob textures */
    private boolean atmosphericRendering = false;
    private Color atmosphericColor;
    private WritableImage alphaBlob;
    private WritableImage selfIlluminationBlob;

    /** Noise-driven motion controller (optional) */
    private NoiseMotionController noiseController;

    /** Random instance for respawning expired linear particles */
    private Random respawnRandom;

    /** Generator reference for respawning */
    private ParticleFieldGenerator generator;

    public ParticleFieldRenderer() {
    }

    public ParticleFieldRenderer(ParticleFieldConfiguration config, Random random) {
        initialize(config, random);
    }

    public static ParticleFieldRenderer fromPreset(String presetName) {
        return fromPreset(presetName, new Random(42));
    }

    public static ParticleFieldRenderer fromPreset(String presetName, Random random) {
        ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(presetName);
        return new ParticleFieldRenderer(config, random);
    }

    /**
     * Initializes or reinitializes the renderer with a new configuration.
     */
    public void initialize(ParticleFieldConfiguration config, Random random) {
        this.config = config;
        this.respawnRandom = random;

        group.getChildren().clear();
        mesh = null;
        cachedPoints = null;

        // Get and store the generator for respawning
        generator = ParticleFieldFactory.getGenerator(config.type());

        // Generate elements
        elements = ParticleFieldFactory.generateElements(config, random);

        baseSize = (config.minSize() + config.maxSize()) / 2.0;
        colorPalette = buildColorPalette(config);

        useOpacity = elements.stream()
                .anyMatch(e -> e.getColor().getOpacity() < 0.99);

        initialized = true;
        buildMesh();
        applyGlowEffect();

        LOG.debug("ParticleFieldRenderer initialized: {} with {} elements, mode={}, opacity={}, glow={}",
                config.name(), elements.size(), renderingMode, useOpacity,
                config.glowEnabled() ? config.glowIntensity() : "disabled");

        if (elements.size() > 10000) {
            logRenderingStats();
        }
    }

    private void applyGlowEffect() {
        if (config.glowEnabled() && config.glowIntensity() > 0) {
            group.setEffect(new Glow(config.glowIntensity()));
        } else {
            group.setEffect(null);
        }
    }

    public void setGlowIntensity(double intensity) {
        if (intensity > 0) {
            group.setEffect(new Glow(Math.min(1.0, intensity)));
        } else {
            group.setEffect(null);
        }
    }

    private List<Color> buildColorPalette(ParticleFieldConfiguration config) {
        Color primary = config.primaryColor();
        Color secondary = config.secondaryColor();
        Color tertiary = config.tertiaryColor() != null ? config.tertiaryColor() :
                primary.interpolate(secondary, 0.5);
        ColorGradientMode mode = config.colorGradientMode() != null ?
                config.colorGradientMode() : ColorGradientMode.LINEAR;

        List<Color> palette = new ArrayList<>(PALETTE_SIZE);

        switch (mode) {
            case LINEAR:
            case RADIAL:
            case NOISE_BASED:
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    palette.add(primary.interpolate(secondary, t));
                }
                break;

            case TEMPERATURE:
                Color hot = Color.color(1.0, 0.9, 0.7);
                Color warm = primary;
                Color cool = secondary;
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    if (t < 0.3) {
                        palette.add(hot.interpolate(warm, t / 0.3));
                    } else if (t < 0.7) {
                        palette.add(warm.interpolate(cool, (t - 0.3) / 0.4));
                    } else {
                        palette.add(cool.interpolate(cool.darker(), (t - 0.7) / 0.3));
                    }
                }
                break;

            case MULTI_ZONE:
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    if (i <= 85) {
                        double t = i / 85.0;
                        palette.add(primary.interpolate(tertiary, t));
                    } else if (i <= 170) {
                        double t = (i - 86) / 84.0;
                        palette.add(tertiary.interpolate(secondary, t));
                    } else {
                        double t = (i - 171) / 84.0;
                        palette.add(secondary.interpolate(secondary.darker(), t * 0.3));
                    }
                }
                break;

            default:
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    palette.add(primary.interpolate(secondary, t));
                }
        }

        return palette;
    }

    private int colorToIndex(Color color, Color primary, Color secondary) {
        double primaryDist = colorDistance(color, primary);
        double secondaryDist = colorDistance(color, secondary);
        double totalDist = primaryDist + secondaryDist;

        if (totalDist < 0.001) {
            return 0;
        }

        double t = primaryDist / totalDist;
        return Math.min(255, Math.max(0, (int) (t * 255)));
    }

    private double colorDistance(Color c1, Color c2) {
        double dr = c1.getRed() - c2.getRed();
        double dg = c1.getGreen() - c2.getGreen();
        double db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private void buildMesh() {
        if (elements.isEmpty()) return;

        if (useIndividualSpheres) {
            buildIndividualSpheres();
            return;
        }

        if (useChunkedRendering && elements.size() > MAX_PARTICLES_PER_CHUNK) {
            group.setTranslateX(0);
            group.setTranslateY(0);
            group.setTranslateZ(0);
            buildChunkedMeshes();
            return;
        }

        if (useCentroidRebasing) {
            computeCentroid();
        }

        cachedPoints = new ArrayList<>(elements.size());

        for (ParticleFieldElement element : elements) {
            int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());
            float scale = (float) (element.getSize() / baseSize);
            float opacity = (float) element.getColor().getOpacity();

            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;

            float px, py, pz;
            if (useCentroidRebasing) {
                px = (float) (wx - centroidX);
                py = (float) (wy - centroidY);
                pz = (float) (wz - centroidZ);
            } else {
                px = (float) wx;
                py = (float) wy;
                pz = (float) wz;
            }

            Point3D point = new Point3D(px, py, pz, colorIndex, scale, opacity);
            cachedPoints.add(point);
        }

        if (useCentroidRebasing) {
            group.setTranslateX(centroidX);
            group.setTranslateY(centroidY);
            group.setTranslateZ(centroidZ);
        }

        buildSingleMesh();
    }

    private void computeCentroid() {
        if (elements.isEmpty()) {
            centroidX = centroidY = centroidZ = 0;
            return;
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        for (ParticleFieldElement element : elements) {
            sumX += element.getX() + worldOffsetX;
            sumY += element.getY() + worldOffsetY;
            sumZ += element.getZ() + worldOffsetZ;
        }

        centroidX = sumX / elements.size();
        centroidY = sumY / elements.size();
        centroidZ = sumZ / elements.size();
    }

    private void buildIndividualSpheres() {
        individualSpheres.clear();

        List<ParticleFieldElement> particlesToRender;
        if (elements.size() > MAX_INDIVIDUAL_SPHERES) {
            particlesToRender = sampleParticles(elements, MAX_INDIVIDUAL_SPHERES);
        } else {
            particlesToRender = elements;
        }

        Map<Integer, PhongMaterial> materialCache = new HashMap<>();

        for (ParticleFieldElement element : particlesToRender) {
            double radius = element.getSize();
            Sphere sphere = new Sphere(radius, 4);

            double px = element.getX() + (useWorldCoordinates ? worldOffsetX : 0);
            double py = element.getY() + (useWorldCoordinates ? worldOffsetY : 0);
            double pz = element.getZ() + (useWorldCoordinates ? worldOffsetZ : 0);
            sphere.setTranslateX(px);
            sphere.setTranslateY(py);
            sphere.setTranslateZ(pz);

            int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());
            PhongMaterial material = materialCache.computeIfAbsent(colorIndex, idx -> {
                PhongMaterial mat = new PhongMaterial();
                Color color = colorPalette.get(idx);
                mat.setDiffuseColor(color);
                mat.setSpecularColor(color.brighter());
                return mat;
            });
            sphere.setMaterial(material);

            individualSpheres.add(sphere);
            group.getChildren().add(sphere);
        }
    }

    private List<ParticleFieldElement> sampleParticles(List<ParticleFieldElement> particles, int targetCount) {
        List<ParticleFieldElement> sampled = new ArrayList<>(targetCount);
        double step = (double) particles.size() / targetCount;
        for (int i = 0; i < targetCount; i++) {
            int idx = (int) (i * step);
            sampled.add(particles.get(idx));
        }
        return sampled;
    }

    private void buildChunkedMeshes() {
        chunkedMeshes.clear();

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (ParticleFieldElement element : elements) {
            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;
            minX = Math.min(minX, wx); maxX = Math.max(maxX, wx);
            minY = Math.min(minY, wy); maxY = Math.max(maxY, wy);
            minZ = Math.min(minZ, wz); maxZ = Math.max(maxZ, wz);
        }

        int divisions = 3;
        double cellSizeX = (maxX - minX) / divisions + 0.001;
        double cellSizeY = (maxY - minY) / divisions + 0.001;
        double cellSizeZ = (maxZ - minZ) / divisions + 0.001;

        Map<String, List<ParticleFieldElement>> bins = new HashMap<>();

        for (ParticleFieldElement element : elements) {
            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;

            int ix = Math.min(divisions - 1, (int) ((wx - minX) / cellSizeX));
            int iy = Math.min(divisions - 1, (int) ((wy - minY) / cellSizeY));
            int iz = Math.min(divisions - 1, (int) ((wz - minZ) / cellSizeZ));
            String key = ix + "_" + iy + "_" + iz;
            bins.computeIfAbsent(key, k -> new ArrayList<>()).add(element);
        }

        for (Map.Entry<String, List<ParticleFieldElement>> entry : bins.entrySet()) {
            List<ParticleFieldElement> binElements = entry.getValue();
            if (binElements.isEmpty()) continue;

            double localCentroidX = 0, localCentroidY = 0, localCentroidZ = 0;
            for (ParticleFieldElement e : binElements) {
                localCentroidX += e.getX() + worldOffsetX;
                localCentroidY += e.getY() + worldOffsetY;
                localCentroidZ += e.getZ() + worldOffsetZ;
            }
            localCentroidX /= binElements.size();
            localCentroidY /= binElements.size();
            localCentroidZ /= binElements.size();

            List<Point3D> localPoints = new ArrayList<>(binElements.size());
            for (ParticleFieldElement element : binElements) {
                double wx = element.getX() + worldOffsetX;
                double wy = element.getY() + worldOffsetY;
                double wz = element.getZ() + worldOffsetZ;

                float px = (float) (wx - localCentroidX);
                float py = (float) (wy - localCentroidY);
                float pz = (float) (wz - localCentroidZ);

                int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());
                float scale = (float) (element.getSize() / baseSize);
                float opacity = (float) element.getColor().getOpacity();

                localPoints.add(new Point3D(px, py, pz, colorIndex, scale, opacity));
            }

            ScatterMesh chunkMesh = new ScatterMesh(localPoints, true, baseSize, 0);
            if (markerType != null) {
                chunkMesh.setMarker(markerType);
            }

            if (atmosphericRendering) {
                chunkMesh.setPerParticleScale(true);
                applyAtmosphericMaterial(chunkMesh);
            } else if (useOpacity) {
                chunkMesh.enableAllPerParticleAttributes(colorPalette);
            } else {
                chunkMesh.enablePerParticleAttributes(colorPalette);
            }
            chunkMesh.setCullFace(CullFace.NONE);

            Group binGroup = new Group(chunkMesh);
            binGroup.setTranslateX(localCentroidX);
            binGroup.setTranslateY(localCentroidY);
            binGroup.setTranslateZ(localCentroidZ);

            chunkedMeshes.add(chunkMesh);
            group.getChildren().add(binGroup);
        }

        usedSpatialBinning = true;
        mesh = chunkedMeshes.isEmpty() ? null : chunkedMeshes.get(0);
    }

    private void buildSingleMesh() {
        usedSpatialBinning = false;
        mesh = new ScatterMesh(cachedPoints, true, baseSize, 0);
        if (markerType != null) {
            mesh.setMarker(markerType);
        }

        if (atmosphericRendering) {
            // Atmospheric mode: per-particle scale only (preserves quad UVs for blob texture)
            mesh.setPerParticleScale(true);
            applyAtmosphericMaterial(mesh);
        } else if (useOpacity) {
            mesh.enableAllPerParticleAttributes(colorPalette);
        } else {
            mesh.enablePerParticleAttributes(colorPalette);
        }

        mesh.setCullFace(CullFace.NONE);
        group.getChildren().add(mesh);
    }

    /**
     * Applies the soft blob material for atmospheric/volumetric rendering.
     * <p>
     * Uses a diffuse alpha-blob for soft edge falloff plus a low-intensity
     * self-illumination blob to reduce harsh directional-light contrast without
     * over-brightening the volume.
     */
    private void applyAtmosphericMaterial(ScatterMesh targetMesh) {
        if (alphaBlob == null || atmosphericColor == null) return;

        PhongMaterial mat = new PhongMaterial();
        // Alpha blob controls soft edge transparency.
        mat.setDiffuseMap(alphaBlob);
        // Keep diffuse color atmospheric; using black here causes dark blotches when many
        // transparent layers overlap with depth test disabled.
        mat.setDiffuseColor(atmosphericColor);
        // A low-intensity self-illumination map softens directional lighting contrast
        // without over-brightening the fog volume.
        if (selfIlluminationBlob != null) {
            mat.setSelfIlluminationMap(selfIlluminationBlob);
        }
        mat.setSpecularColor(Color.TRANSPARENT);

        // Apply to all meshes in the ScatterMesh
        targetMesh.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.shape.MeshView mv) {
                mv.setMaterial(mat);
                // Disable depth test so overlapping transparent triangles ACCUMULATE
                // rather than only showing the frontmost one. This is critical for
                // volumetric effects where many low-alpha particles must blend together.
                mv.setDepthTest(javafx.scene.DepthTest.DISABLE);
            }
        });
    }

    /**
     * Sets the rendering mode for this renderer.
     */
    public void setRenderingMode(RenderingMode mode) {
        this.renderingMode = mode;
        switch (mode) {
            case SCATTER_MESH_AUTO:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = true;
                break;
            case SCATTER_MESH_SINGLE:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = false;
                break;
            case SCATTER_MESH_CHUNKED:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = true;
                break;
            case INDIVIDUAL_SPHERES:
                this.useIndividualSpheres = true;
                this.useChunkedRendering = false;
                break;
        }
    }

    /**
     * Sets the marker shape used for each particle in ScatterMesh mode.
     * If called after initialization, triggers a mesh rebuild.
     */
    public void setMarkerType(MarkerFactory.Marker marker) {
        this.markerType = marker;
        if (initialized && !useIndividualSpheres) {
            group.getChildren().clear();
            chunkedMeshes.clear();
            mesh = null;
            buildMesh();
        }
    }

    public MarkerFactory.Marker getMarkerType() {
        return markerType;
    }

    /**
     * Enables atmospheric/volumetric rendering mode.
     * <p>
     * Uses CROSS_QUAD markers with an alpha-blob diffuseMap and the atmospheric
     * color as diffuseColor. The blob texture provides gaussian alpha falloff
     * so particle edges blend smoothly. With low per-particle alpha (0.01-0.03)
     * and DepthTest.DISABLE, overlapping particles accumulate to create
     * continuous volumetric haze.
     *
     * @param color the atmospheric color (e.g. fog gray, cloud white).
     *              Alpha controls overall particle transparency.
     * @param sigma gaussian sigma for blob softness (0.3=tight, 0.5=normal, 0.7=wide/soft)
     */
    public void setAtmosphericRendering(Color color, double sigma) {
        this.atmosphericRendering = true;
        this.atmosphericColor = color;
        this.markerType = MarkerFactory.Marker.CROSS_QUAD;
        this.useChunkedRendering = false;  // Force single mesh for atmospheric rendering
        this.alphaBlob = SoftParticleTexture.createAlphaBlob(64, sigma);
        // Scale emissive contribution by atmospheric alpha to avoid white blowout.
        double emissiveScale = Math.min(0.2, Math.max(0.03, color.getOpacity() * 2.0));
        Color emissiveColor = new Color(
                color.getRed() * emissiveScale,
                color.getGreen() * emissiveScale,
                color.getBlue() * emissiveScale,
                1.0
        );
        this.selfIlluminationBlob = SoftParticleTexture.createColorBlob(64, emissiveColor, sigma);
        if (initialized) {
            group.getChildren().clear();
            chunkedMeshes.clear();
            mesh = null;
            buildMesh();
        }
    }

    /**
     * Enables atmospheric rendering with default sigma.
     */
    public void setAtmosphericRendering(Color color) {
        setAtmosphericRendering(color, 0.5);
    }

    public boolean isAtmosphericRendering() {
        return atmosphericRendering;
    }

    /**
     * Sets the noise motion controller for curl-noise driven particle movement.
     */
    public void setNoiseMotionController(NoiseMotionController controller) {
        this.noiseController = controller;
        // Enable opacity support if density modulation is enabled
        if (controller != null && controller.getConfig().densityOpacityEnabled()) {
            this.useOpacity = true;
        }
    }

    /**
     * Gets the current noise motion controller, or null if none is set.
     */
    public NoiseMotionController getNoiseMotionController() {
        return noiseController;
    }

    public void setCurrentLodLevel(String lodLevel) {
        this.currentLodLevel = lodLevel;
    }

    public void logRenderingStats() {
        String mode = renderingMode.name();
        int elementCount = elements.size();
        int meshCount = getMeshCount();
        String rendering = useIndividualSpheres ? "Individual Spheres" :
                (usedSpatialBinning ? "Chunked ScatterMesh" : "Single ScatterMesh");

        LOG.info("PERF [{}]: mode={}, elements={}, meshes={}, rendering={}, lod={}",
                config != null ? config.name() : "unknown",
                mode, elementCount, meshCount, rendering, currentLodLevel);
    }

    public void setUseCentroidRebasing(boolean useRebasing) {
        this.useCentroidRebasing = useRebasing;
    }

    public void setConfiguration(ParticleFieldConfiguration config) {
        initialize(config, new Random(42));
    }

    public void switchPreset(String presetName) {
        ParticleFieldConfiguration newConfig = ParticleFieldFactory.getPreset(presetName);
        initialize(newConfig, new Random(42));
    }

    /**
     * Updates all element positions based on time scale.
     * For LINEAR particles, respawns expired particles via the generator.
     *
     * @param timeScale multiplier for movement (1.0 = normal speed at 60fps)
     */
    public void update(double timeScale) {
        if (!initialized) return;

        for (int i = 0; i < elements.size(); i++) {
            ParticleFieldElement element = elements.get(i);
            element.advance(timeScale);

            // Respawn expired non-ORBITAL particles (LINEAR and VORTEX)
            if (element.getMotionModel() != MotionModel.ORBITAL && !element.isAlive()) {
                ParticleFieldElement replacement = generator.generateOne(config, respawnRandom);
                if (replacement != null) {
                    elements.set(i, replacement);
                }
            }
        }

        // Apply noise-driven motion after the base advance
        if (noiseController != null) {
            noiseController.applyNoiseMotion(elements, timeScale);
        }
    }

    /**
     * Updates the mesh positions efficiently without full rebuild.
     *
     * @return true if efficient update was used, false if full rebuild was needed
     */
    public boolean updateMeshPositions() {
        if (!initialized || elements.isEmpty()) {
            return false;
        }

        if (useIndividualSpheres && !individualSpheres.isEmpty()) {
            for (int i = 0; i < elements.size() && i < individualSpheres.size(); i++) {
                ParticleFieldElement element = elements.get(i);
                Sphere sphere = individualSpheres.get(i);
                sphere.setTranslateX(element.getX() + (useWorldCoordinates ? worldOffsetX : 0));
                sphere.setTranslateY(element.getY() + (useWorldCoordinates ? worldOffsetY : 0));
                sphere.setTranslateZ(element.getZ() + (useWorldCoordinates ? worldOffsetZ : 0));
            }
            return true;
        }

        if (usedSpatialBinning) {
            group.getChildren().clear();
            chunkedMeshes.clear();
            mesh = null;
            buildMesh();
            return false;
        }

        if (cachedPoints == null) {
            return false;
        }

        boolean noiseActive = noiseController != null;

        for (int i = 0; i < elements.size(); i++) {
            ParticleFieldElement element = elements.get(i);
            Point3D point = cachedPoints.get(i);
            point.x = (float) element.getX();
            point.y = (float) element.getY();
            point.z = (float) element.getZ();

            if (noiseActive && !atmosphericRendering) {
                point.opacity = (float) element.getDynamicOpacity();
                point.scale = (float) (element.getDynamicScale() * element.getSize() / baseSize);
            }
        }

        if (mesh == null) {
            return false;
        }

        if (noiseActive && useOpacity && !atmosphericRendering) {
            return mesh.updatePositionsAndAttributes(cachedPoints);
        }
        return mesh.updatePositions(cachedPoints);
    }

    @Deprecated
    public void refreshMeshes() {
        if (!updateMeshPositions()) {
            group.getChildren().clear();
            mesh = null;
            buildMesh();
        }
    }

    public void setPosition(double x, double y, double z) {
        worldOffsetX = x;
        worldOffsetY = y;
        worldOffsetZ = z;

        if (useCentroidRebasing) {
            if (initialized) {
                group.getChildren().clear();
                chunkedMeshes.clear();
                mesh = null;
                buildMesh();
            }
        } else if (useWorldCoordinates) {
            group.setTranslateX(0);
            group.setTranslateY(0);
            group.setTranslateZ(0);
            if (initialized) {
                group.getChildren().clear();
                mesh = null;
                buildMesh();
            }
        } else {
            group.setTranslateX(x);
            group.setTranslateY(y);
            group.setTranslateZ(z);
        }
    }

    public void setUseWorldCoordinates(boolean useWorldCoordinates) {
        this.useWorldCoordinates = useWorldCoordinates;
    }

    public void setScale(double scale) {
        group.setScaleX(scale);
        group.setScaleY(scale);
        group.setScaleZ(scale);
    }

    public void setScale(double scaleX, double scaleY, double scaleZ) {
        group.setScaleX(scaleX);
        group.setScaleY(scaleY);
        group.setScaleZ(scaleZ);
    }

    public void setVisible(boolean visible) {
        group.setVisible(visible);
    }

    // ==================== Getters ====================

    public Group getGroup() { return group; }
    public ParticleFieldConfiguration getConfig() { return config; }
    public List<ParticleFieldElement> getElements() { return elements; }
    public RenderingMode getRenderingMode() { return renderingMode; }
    public String getCurrentLodLevel() { return currentLodLevel; }
    public boolean isInitialized() { return initialized; }
    public boolean isDebugMode() { return debugMode; }

    public int getElementCount() {
        return elements.size();
    }

    public int getMeshCount() {
        if (!individualSpheres.isEmpty()) {
            return individualSpheres.size();
        }
        if (!chunkedMeshes.isEmpty()) {
            return chunkedMeshes.size();
        }
        return mesh != null ? 1 : 0;
    }

    public void clear() {
        elements.clear();
        group.getChildren().clear();
        mesh = null;
        chunkedMeshes.clear();
        individualSpheres.clear();
        cachedPoints = null;
        colorPalette = null;
        initialized = false;
        usedSpatialBinning = false;
        centroidX = centroidY = centroidZ = 0;
        group.setTranslateX(0);
        group.setTranslateY(0);
        group.setTranslateZ(0);
    }

    public void dispose() {
        clear();
        config = null;
    }

    // ==================== Diagnostic Methods ====================

    private Box debugBoundingBox;
    private boolean debugMode = false;

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        if (enabled) {
            updateDebugBoundingBox();
            logDiagnostics();
        } else {
            removeDebugBoundingBox();
        }
    }

    public void logDiagnostics() {
        if (mesh == null) {
            LOG.info("DIAG [{}]: Mesh is null", config != null ? config.name() : "unknown");
            return;
        }

        Bounds localBounds = mesh.getBoundsInLocal();
        Bounds groupParentBounds = group.getBoundsInParent();

        LOG.info("DIAG [{}]: Element count={}", config.name(), elements.size());
        LOG.info("DIAG [{}]: World offset=({}, {}, {}), useWorldCoords={}",
                config.name(), worldOffsetX, worldOffsetY, worldOffsetZ, useWorldCoordinates);
        LOG.info("DIAG [{}]: Group translate=({}, {}, {})",
                config.name(), group.getTranslateX(), group.getTranslateY(), group.getTranslateZ());
        LOG.info("DIAG [{}]: Mesh boundsInLocal: size=({}, {}, {})",
                config.name(),
                localBounds.getWidth(), localBounds.getHeight(), localBounds.getDepth());
        LOG.info("DIAG [{}]: Group boundsInParent: min=({}, {}, {}), max=({}, {}, {})",
                config.name(),
                groupParentBounds.getMinX(), groupParentBounds.getMinY(), groupParentBounds.getMinZ(),
                groupParentBounds.getMaxX(), groupParentBounds.getMaxY(), groupParentBounds.getMaxZ());
    }

    private void updateDebugBoundingBox() {
        if (mesh == null) return;

        Bounds bounds = mesh.getBoundsInLocal();

        if (debugBoundingBox == null) {
            debugBoundingBox = new Box(bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
            PhongMaterial material = new PhongMaterial(Color.TRANSPARENT);
            material.setSpecularColor(Color.YELLOW);
            debugBoundingBox.setMaterial(material);
            debugBoundingBox.setDrawMode(DrawMode.LINE);
            debugBoundingBox.setCullFace(CullFace.NONE);
        } else {
            debugBoundingBox.setWidth(bounds.getWidth());
            debugBoundingBox.setHeight(bounds.getHeight());
            debugBoundingBox.setDepth(bounds.getDepth());
        }

        debugBoundingBox.setTranslateX(bounds.getCenterX());
        debugBoundingBox.setTranslateY(bounds.getCenterY());
        debugBoundingBox.setTranslateZ(bounds.getCenterZ());

        if (!group.getChildren().contains(debugBoundingBox)) {
            group.getChildren().add(debugBoundingBox);
        }
    }

    private void removeDebugBoundingBox() {
        if (debugBoundingBox != null) {
            group.getChildren().remove(debugBoundingBox);
            debugBoundingBox = null;
        }
    }

    public String getDiagnosticSummary() {
        if (mesh == null) return "Mesh is null";

        Bounds bounds = mesh.getBoundsInParent();
        return String.format("Elements=%d, BoundsInParent=[%.1f,%.1f,%.1f to %.1f,%.1f,%.1f], GroupTranslate=[%.1f,%.1f,%.1f]",
                elements.size(),
                bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ(),
                group.getTranslateX(), group.getTranslateY(), group.getTranslateZ());
    }
}
