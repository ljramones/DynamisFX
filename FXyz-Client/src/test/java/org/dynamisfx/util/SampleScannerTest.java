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
