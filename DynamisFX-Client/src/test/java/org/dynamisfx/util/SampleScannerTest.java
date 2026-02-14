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

package org.dynamisfx.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SampleScannerTest {

    @Test
    void normalizeClassPathStripsLeadingSlash() throws Exception {
        SampleScanner scanner = new SampleScanner();
        Method method = SampleScanner.class.getDeclaredMethod("normalizeClassPath", String.class);
        method.setAccessible(true);

        String normalized = (String) method.invoke(scanner, "/com/javafx/main/Main.class");
        assertEquals("com/javafx/main/Main.class", normalized);
    }

    @Test
    @SuppressWarnings("unchecked")
    void scannableClassChecksRespectAllowedPrefixesAndIllegalEntries() throws Exception {
        SampleScanner scanner = new SampleScanner();

        Method toPrefixes = SampleScanner.class.getDeclaredMethod("toClassPathPrefixes", java.util.Collection.class);
        toPrefixes.setAccessible(true);
        Set<String> prefixes = (Set<String>) toPrefixes.invoke(scanner, List.of("org.dynamisfx.samples"));

        Method isScannable = SampleScanner.class.getDeclaredMethod("isScannableClass", String.class, Set.class);
        isScannable.setAccessible(true);

        boolean allowed = (boolean) isScannable.invoke(scanner, "org/dynamisfx/samples/Demo.class", prefixes);
        boolean illegal = (boolean) isScannable.invoke(scanner, "module-info.class", prefixes);
        boolean wrongPackage = (boolean) isScannable.invoke(scanner, "org/example/Other.class", prefixes);

        assertTrue(allowed);
        assertFalse(illegal);
        assertFalse(wrongPackage);
    }
}
