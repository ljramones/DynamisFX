package org.dynamisfx.physics.jolt;

/**
 * Loads the Jolt C-shim native library.
 */
final class JoltNativeBridge {

    static final String LIBRARY_NAME = "dynamisfx_jolt_cshim";

    private final boolean available;

    JoltNativeBridge() {
        this(tryLoad());
    }

    JoltNativeBridge(boolean available) {
        this.available = available;
    }

    boolean isAvailable() {
        return available;
    }

    private static boolean tryLoad() {
        try {
            System.loadLibrary(LIBRARY_NAME);
            return true;
        } catch (UnsatisfiedLinkError ignored) {
            return false;
        }
    }
}
