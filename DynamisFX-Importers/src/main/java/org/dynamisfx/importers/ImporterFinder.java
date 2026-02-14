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

package org.dynamisfx.importers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImporterFinder {

    public URLClassLoader addUrlToClassPath() {
        try {
            final List<URL> urls = loadFromPathScanning();
            return new URLClassLoader((URL[]) urls.toArray(new URL[0]), this.getClass().getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Scans all classes.
     *
     * @return a list of URLs
     * @throws IOException
     */
    private List<URL> loadFromPathScanning() throws IOException {

        final List<URL> urlList = new ArrayList<>();
        
        ModuleLayer.boot().configuration().modules().stream()
                .map(ResolvedModule::reference)
                .filter(rm -> !isSystemModule(rm.descriptor().name()))
                .forEach(mref -> {
                    try (ModuleReader reader = mref.open()) {
                        reader.list()
                                .filter(c -> c.endsWith(".class"))
                                .map(this::processClassName)
                                .filter(Objects::nonNull)
                                .forEach(urlList::add);
                    } catch (IOException ioe) {
                        throw new UncheckedIOException(ioe);
                    }
                });
        
        return urlList;
    }
    
    private URL processClassName(final String name) {
        String className = name.replace("\\", ".");
        className = className.replace("/", ".");
        
        if (className.contains("$")) {
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
            clazz = Class.forName(className);
        } catch (Throwable e) {
            // ignored
        }
        if (clazz != null) {
            return clazz.getProtectionDomain().getCodeSource().getLocation();
        }
        return null;
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
