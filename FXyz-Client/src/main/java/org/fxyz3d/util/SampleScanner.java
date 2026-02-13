/**
 * SampleScanner.java
 *
 * Copyright (c) 2013-2018, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.fxyz3d.util;

import java.io.IOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fxyz3d.FXyzSample;
import org.fxyz3d.FXyzSamplerProject;
import org.fxyz3d.model.EmptySample;
import org.fxyz3d.model.Project;

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
    
    private static final Map<String, FXyzSamplerProject> packageToProjectMap = new HashMap<>();
    static {
        LOG.fine("Initialising DynamisFX Sampler sample scanner...");
        // find all projects on the classpath that expose a FXyzSamplerProject
        // service. These guys are our friends....
        ServiceLoader<FXyzSamplerProject> loader = ServiceLoader.load(FXyzSamplerProject.class);
        for (FXyzSamplerProject project : loader) {
            final String projectName = project.getProjectName();
            final String basePackage = project.getSampleBasePackage();
            packageToProjectMap.put(basePackage, project);
            LOG.log(Level.FINE, "Found project ''{0}'' with sample base package ''{1}''",
                    new Object[] {projectName, basePackage});
        }
        
        if (packageToProjectMap.isEmpty()) {
            LOG.warning("Did not find any FXyzSamplerProject services");
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
        
        for (Class<?> sampleClass : results) {
            if (! FXyzSample.class.isAssignableFrom(sampleClass)) continue;
            if (sampleClass.isInterface()) continue;
            if (Modifier.isAbstract(sampleClass.getModifiers())) continue;
//            if (FXyzSample.class.isAssignableFrom(EmptySample.class)) continue;
            if (sampleClass == EmptySample.class) continue;
            
            FXyzSample sample = null;
            try {
                sample = (FXyzSample)sampleClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOG.log(Level.FINE, "Failed to instantiate sample class " + sampleClass.getName(), e);
            }
            if (sample == null || ! sample.isVisible()) continue;

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
        
        return classes.toArray(new Class[classes.size()]);
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
