package org.dynamisfx.physics.jolt;

/**
 * Lightweight native-loading self-check for Jolt providers.
 */
public final class JoltSelfCheck {

    private JoltSelfCheck() {
    }

    public static void main(String[] args) {
        JoltNativeBridge cshim = new JoltNativeBridge();
        String cshimStatus;
        if (cshim.isAvailable()) {
            cshimStatus = "available api=" + cshim.apiVersion() + " backendMode=" + cshim.backendMode();
        } else {
            cshimStatus = "unavailable load=" + cshim.loadDescription();
        }

        JoltJniNativeLoader.LoadResult joltJni = JoltJniNativeLoader.ensureLoaded();
        String jniStatus = joltJni.available()
                ? "available load=" + joltJni.description()
                : "unavailable load=" + joltJni.description();

        System.out.println("jolt.selfcheck cshim=" + cshimStatus);
        System.out.println("jolt.selfcheck jolt-jni=" + jniStatus);

        if (!cshim.isAvailable() && !joltJni.available()) {
            System.exit(2);
        }
    }
}
