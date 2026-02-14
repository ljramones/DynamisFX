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

package org.dynamisfx.model;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dynamisfx.DynamisFXSample;

/**
 * Represents a project such as ControlsFX or JFXtras
 */
public class Project {
    private static final Logger LOG = Logger.getLogger(Project.class.getName());
    private static final Map<String, String[]> SAMPLE_CATEGORY_OVERRIDES = Map.ofEntries(
            Map.entry("CollisionDebugWorld", new String[]{"Collision Detection"}),
            Map.entry("RayIntersections", new String[]{"Collision Detection"}),
            Map.entry("RayShooting", new String[]{"Collision Detection"}),
            Map.entry("BroadPhaseComparisonDemo", new String[]{"Collision Detection"}),
            Map.entry("GjkEpaVisualizerDemo", new String[]{"Collision Detection"}),
            Map.entry("Sat2dPolygonPlaygroundDemo", new String[]{"Collision Detection"}),
            Map.entry("RayCastingSceneDemo", new String[]{"Collision Detection"}),
            Map.entry("BoundingVolumeComparisonDemo", new String[]{"Collision Detection"}),
            Map.entry("Ode4jPhysicsSyncSample", new String[]{"Collision Detection Solver"}),
            Map.entry("CcdTunnelingDemo", new String[]{"Collision Detection Solver"}),
            Map.entry("ContactSolverStackingDemo", new String[]{"Collision Detection Solver"}),
            Map.entry("CollisionFilterLayerDemo", new String[]{"Collision Detection Solver"}),
            Map.entry("MixedPrimitiveStressTestDemo", new String[]{"Collision Detection Solver"}),
            Map.entry("ManifoldPersistenceVisualizerDemo", new String[]{"Collision Detection Solver"}),
            Map.entry("HybridPhysicsCoordinatorSample", new String[]{"Pipeline Physics"}),
            Map.entry("CouplingTransitionDemo", new String[]{"Pipeline Physics"}),
            Map.entry("OrekitOrbitSyncSample", new String[]{"Pipeline Physics"}),
            Map.entry("CollisionPipelineMonitorDemo", new String[]{"Pipeline Physics"}),
            // ODE4j Demo ports - Phase 2
            Map.entry("Ode4jBoxstackDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jChainDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jHingeDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jSliderDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jFrictionDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jKinematicDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jCardsDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jGyroscopicDemo", new String[]{"Physics", "ODE4J Demos"}),
            // ODE4j Demo ports - Phase 3
            Map.entry("Ode4jDominoDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jNewtonsCradleDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jWreckingBallDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jBridgeDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jPendulumWaveDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jStackChallengeDemo", new String[]{"Physics", "ODE4J Demos"}),
            // ODE4j Demo ports - Phase 4 (Motor-driven)
            Map.entry("Ode4jMotorDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jCraneDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jBuggyDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jRagdollDemo", new String[]{"Physics", "ODE4J Demos"}),
            // ODE4j Demo ports - Phase 5 (Interactive)
            Map.entry("Ode4jSeesawDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jPinballDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jCatapultDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jWindmillDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jElevatorDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jMarbleRunDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jBalancingDemo", new String[]{"Physics", "ODE4J Demos"}),
            Map.entry("Ode4jConveyorDemo", new String[]{"Physics", "ODE4J Demos"}));
    
    private final String name;
    
    private final String basePackage;

    // A Project has a Tree of samples
    private final SampleTree sampleTree;
    
    // Pojo that holds the welcome tab content and title
    private WelcomePage welcomePage;
    
    public Project(String name, String basePackage) {
        this.name = name;
        this.basePackage = basePackage;
        this.sampleTree = new SampleTree(new EmptySample(name));
    }

    public void addSample(String packagePath, DynamisFXSample sample) {
        String[] overridePath = SAMPLE_CATEGORY_OVERRIDES.get(sample.getClass().getSimpleName());
        if (overridePath != null && (packagePath.startsWith("org.dynamisfx.samples.utilities")
                || packagePath.startsWith("org.dynamisfx.samples.physics"))) {
            sampleTree.addSample(overridePath, sample);
            return;
        }

        // convert something like 'org.controlsfx.samples.actions' to 'samples.actions'
        String packagesWithoutBase = "";
        try {
            if (! basePackage.equals(packagePath)) {
                packagesWithoutBase = packagePath.substring(basePackage.length() + 1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            LOG.log(Level.WARNING, "Failed to derive package path from packagePath={0}, basePackage={1}",
                    new Object[]{packagePath, basePackage});
            return;
        }
        
        // then split up the packages into separate strings
        String[] packages = packagesWithoutBase.isEmpty() ? new String[] { } : packagesWithoutBase.split("\\.");
        
        // then for each package convert to a prettier form
        for (int i = 0; i < packages.length; i++) {
            String packageName = packages[i];
            if (packageName.isEmpty()) continue;
            
            packageName = packageName.substring(0, 1).toUpperCase() + packageName.substring(1);
            packageName = packageName.replace("_", " ");
            packages[i] = packageName;
        }
        
        // now we have the pretty package names, we add this sample into the
        // tree in the appropriate place
        sampleTree.addSample(packages, sample);
    }

    public SampleTree getSampleTree() {
        return sampleTree;
    }
    
    public void setWelcomePage(WelcomePage welcomePage) {
        if(null != welcomePage) {
            this.welcomePage = welcomePage;
        }
    }
    
    public WelcomePage getWelcomePage() {
        return this.welcomePage;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Project [ name: ");
        sb.append(name);
        sb.append(", sample count: ");
        sb.append(sampleTree.size());
        sb.append(", tree: ");
        sb.append(sampleTree);
        sb.append(" ]");
        
        return sb.toString();
    }
}
