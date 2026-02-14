package org.dynamisfx.physics.jolt;

/**
 * Produces runtime diagnostics for Jolt native loading without side effects like process exit.
 */
public final class JoltRuntimeDiagnostics {

    private JoltRuntimeDiagnostics() {
    }

    public static String report() {
        String os = System.getProperty("os.name", "unknown");
        String arch = System.getProperty("os.arch", "unknown");
        String javaVersion = System.getProperty("java.version", "unknown");

        JoltNativeBridge cshim = new JoltNativeBridge();
        String cshimStatus;
        if (cshim.isAvailable()) {
            cshimStatus = "available api=" + cshim.apiVersion() + " mode=" + cshim.backendMode();
        } else {
            cshimStatus = "unavailable load=" + cshim.loadDescription();
        }

        JoltJniNativeLoader.LoadResult jni = JoltJniNativeLoader.ensureLoaded();
        String jniStatus = jni.available()
                ? "available load=" + jni.description()
                : "unavailable load=" + jni.description();

        return "os=" + os
                + ",arch=" + arch
                + ",java=" + javaVersion
                + ",cshim=" + cshimStatus
                + ",jolt-jni=" + jniStatus;
    }
}
