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

package org.dynamisfx.util;

import java.io.IOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dynamisfx.DynamisFXSample;
import org.dynamisfx.DynamisFXSamplerProject;
import org.dynamisfx.model.EmptySample;
import org.dynamisfx.model.Project;

/**
 * All the code related to classpath scanning, etc for samples.
 */
public class SampleScanner {
    private static final Logger LOG = Logger.getLogger(SampleScanner.class.getName());
    
    private static final List<String> ILLEGAL_CLASS_NAMES = new ArrayList<>();
    static {
        ILLEGAL_CLASS_NAMES.add("com/javafx/main/Main.class");
        ILLEGAL_CLASS_NAMES.add("com/javafx/main/NoJavaFXFallback.class");
        ILLEGAL_CLASS_NAMES.add("module-info.class");
    }
    
    private static final Map<String, DynamisFXSamplerProject> packageToProjectMap = new HashMap<>();
    static {
        LOG.fine("Initialising DynamisFX Sampler sample scanner...");
        // find all projects on the classpath that expose a DynamisFXSamplerProject
        // service. These guys are our friends....
        ServiceLoader<DynamisFXSamplerProject> loader = ServiceLoader.load(DynamisFXSamplerProject.class);
        for (DynamisFXSamplerProject project : loader) {
            final String projectName = project.getProjectName();
            final String basePackage = project.getSampleBasePackage();
            packageToProjectMap.put(basePackage, project);
            LOG.log(Level.FINE, "Found project ''{0}'' with sample base package ''{1}''",
                    new Object[] {projectName, basePackage});
        }
        
        if (packageToProjectMap.isEmpty()) {
            LOG.warning("Did not find any DynamisFXSamplerProject services");
        }
    }
    
    private final Map<String, Project> projectsMap = new HashMap<>();
    
    /**
     * Gets the list of sample classes to load
     *
     * @return The classes
     */
    public Map<String, Project> discoverSamples() {
        if (packageToProjectMap.isEmpty()) {
            return projectsMap;
        }
        Class<?>[] results = loadFromPathScanning();
        System.out.println("[DEBUG-SCANNER] Found " + results.length + " classes from path scanning");

        for (Class<?> sampleClass : results) {
            System.out.println("[DEBUG-SCANNER] Checking class: " + sampleClass.getName());
            if (! DynamisFXSample.class.isAssignableFrom(sampleClass)) { System.out.println("[DEBUG-SCANNER]   SKIP: not assignable from DynamisFXSample"); continue; }
            if (sampleClass.isInterface()) { System.out.println("[DEBUG-SCANNER]   SKIP: is interface"); continue; }
            if (Modifier.isAbstract(sampleClass.getModifiers())) { System.out.println("[DEBUG-SCANNER]   SKIP: is abstract"); continue; }
//            if (DynamisFXSample.class.isAssignableFrom(EmptySample.class)) continue;
            if (sampleClass == EmptySample.class) { System.out.println("[DEBUG-SCANNER]   SKIP: is EmptySample"); continue; }

            DynamisFXSample sample = null;
            try {
                sample = (DynamisFXSample)sampleClass.getDeclaredConstructor().newInstance();
                System.out.println("[DEBUG-SCANNER]   Instantiated OK: " + sample.getSampleName());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                System.out.println("[DEBUG-SCANNER]   FAILED to instantiate: " + e);
                if (e.getCause() != null) System.out.println("[DEBUG-SCANNER]     Cause: " + e.getCause());
                LOG.log(Level.FINE, "Failed to instantiate sample class " + sampleClass.getName(), e);
            }
            if (sample == null || ! sample.isVisible()) { System.out.println("[DEBUG-SCANNER]   SKIP: null or not visible"); continue; }

            final String packageName = sampleClass.getPackage().getName();
            
            for (String key : packageToProjectMap.keySet()) {
                if (packageName.contains(key)) {
                    final String prettyProjectName = packageToProjectMap.get(key).getProjectName();
                    
                    Project project;
                    if (! projectsMap.containsKey(prettyProjectName)) {
                        project = new Project(prettyProjectName, key);
                        project.setWelcomePage(packageToProjectMap.get(key).getWelcomePage());
                        projectsMap.put(prettyProjectName, project);
                    } else {
                        project = projectsMap.get(prettyProjectName);
                    }
                    
                    project.addSample(packageName, sample);
                }
            }
        }
        
        return projectsMap;
    } 

    /**
     * Scans all classes.
     *
     * @return The classes
     * @throws IOException
     */
    private Class<?>[] loadFromPathScanning() {

        final Set<Class<?>> classes = new LinkedHashSet<>();
        final Set<String> allowedClassPathPrefixes = toClassPathPrefixes(packageToProjectMap.keySet());
        // scan the module-path
        ModuleLayer.boot().configuration().modules().stream()
                .map(ResolvedModule::reference)
                .filter(rm -> !isSystemModule(rm.descriptor().name()))
                .forEach(mref -> {
                    final String moduleName = mref.descriptor().name();
                    final ClassLoader moduleClassLoader = ModuleLayer.boot().findLoader(moduleName);
                    try (ModuleReader reader = mref.open()) {
                        reader.list()
                            .filter(c -> isScannableClass(c, allowedClassPathPrefixes))
                            .forEach(c -> {
                                final Class<?> clazz = processClassName(c, moduleClassLoader);
                                if (clazz != null) {
                                    classes.add(clazz);
                                }
                            });
                    } catch (IOException ioe) {
                        LOG.log(Level.FINE, "Failed to scan module " + moduleName, ioe);
                    }
                });

        // Most sampler runs in this repo use classpath mode (useModulePath=false),
        // where ModuleLayer scanning won't see sample classes.
        if (classes.isEmpty()) {
            loadFromClassPathScanning(classes, allowedClassPathPrefixes);
        }
        
        return classes.toArray(new Class[classes.size()]);
    }

    private void loadFromClassPathScanning(Set<Class<?>> classes, Set<String> allowedPrefixes) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String prefix : allowedPrefixes) {
            try {
                final Enumeration<URL> resources = classLoader.getResources(prefix);
                while (resources.hasMoreElements()) {
                    final URL resource = resources.nextElement();
                    final String protocol = resource.getProtocol();
                    if ("file".equals(protocol)) {
                        scanDirectoryResource(classes, allowedPrefixes, classLoader, prefix, resource);
                    } else if ("jar".equals(protocol)) {
                        scanJarResource(classes, allowedPrefixes, classLoader, resource);
                    }
                }
            } catch (IOException ioe) {
                LOG.log(Level.FINE, "Failed to scan classpath for prefix " + prefix, ioe);
            }
        }
    }

    private void scanDirectoryResource(Set<Class<?>> classes,
                                       Set<String> allowedPrefixes,
                                       ClassLoader classLoader,
                                       String prefix,
                                       URL resource) {
        try {
            final Path root = Paths.get(resource.toURI());
            if (!Files.exists(root)) {
                return;
            }
            Files.walk(root)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        final String relative = root.relativize(path).toString().replace('\\', '/');
                        final String classPath = prefix + relative;
                        if (isScannableClass(classPath, allowedPrefixes)) {
                            final Class<?> clazz = processClassName(classPath, classLoader);
                            if (clazz != null) {
                                classes.add(clazz);
                            }
                        }
                    });
        } catch (IOException | URISyntaxException ex) {
            LOG.log(Level.FINE, "Failed to scan directory classpath resource " + resource, ex);
        }
    }

    private void scanJarResource(Set<Class<?>> classes,
                                 Set<String> allowedPrefixes,
                                 ClassLoader classLoader,
                                 URL resource) {
        try {
            final JarURLConnection connection = (JarURLConnection) resource.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                jarFile.stream()
                        .map(JarEntry::getName)
                        .filter(name -> isScannableClass(name, allowedPrefixes))
                        .forEach(name -> {
                            final Class<?> clazz = processClassName(name, classLoader);
                            if (clazz != null) {
                                classes.add(clazz);
                            }
                        });
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Failed to scan jar classpath resource " + resource, ex);
        }
    }

    private boolean isScannableClass(final String classPath, Set<String> allowedPrefixes) {
        if (!classPath.endsWith(".class")) {
            return false;
        }
        final String normalized = normalizeClassPath(classPath);
        if (ILLEGAL_CLASS_NAMES.contains(normalized)) {
            return false;
        }
        return allowedPrefixes.stream().anyMatch(normalized::startsWith);
    }

    private Set<String> toClassPathPrefixes(Collection<String> packageNames) {
        final Set<String> prefixes = new LinkedHashSet<>();
        for (String packageName : packageNames) {
            prefixes.add(packageName.replace('.', '/') + "/");
        }
        return prefixes;
    }

    private String normalizeClassPath(final String classPath) {
        return classPath.startsWith("/") ? classPath.substring(1) : classPath;
    }

    private Class<?> processClassName(final String name, ClassLoader classLoader) {
        String className = name.replace("\\", ".");
        className = className.replace("/", ".");
        
        // some cleanup code
        if (className.contains("$")) {
            // we don't care about samples as inner classes, so 
            // we jump out
            return null;
        }
        if (className.contains(".bin")) {
            className = className.substring(className.indexOf(".bin") + 4);
            className = className.replace(".bin", "");
        }
        if (className.startsWith(".")) {
            className = className.substring(1);
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            LOG.log(Level.FINE, "Failed to load class {0} from module path entry {1}",
                    new Object[] {className, name});
        }
        return clazz;
    }

    /**
     * Return true if the given module name is a system module. There can be
     * system modules in layers above the boot layer.
     */
    private static boolean isSystemModule(final String moduleName) {
        return moduleName.startsWith("java.")
                || moduleName.startsWith("javax.")
                || moduleName.startsWith("javafx.")
                || moduleName.startsWith("jdk.")
                || moduleName.startsWith("oracle.");
    }
}
