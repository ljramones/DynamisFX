package org.dynamisfx.physics.jolt.panama;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Minimal Panama probe against the C shim.
 *
 * <p>This class is only compiled when building with {@code -Dpanama=true} (JDK 22+).</p>
 */
public final class JoltPanamaVersionProbe {

    private JoltPanamaVersionProbe() {
    }

    public static int readApiVersion() {
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        MethodHandle handle = lookup.find("dfx_jolt_api_version")
                .map(symbol -> Linker.nativeLinker().downcallHandle(
                        symbol,
                        FunctionDescriptor.of(ValueLayout.JAVA_INT)))
                .orElseThrow(() -> new IllegalStateException("dfx_jolt_api_version symbol not found"));
        try {
            return (int) handle.invokeExact();
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to call dfx_jolt_api_version", throwable);
        }
    }
}
