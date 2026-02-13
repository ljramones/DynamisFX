package org.dynamisfx.physics.jolt;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Jolt backend shell. Native functionality is provided through a C shim loaded at runtime.
 */
public final class JoltBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);
    static final String PROVIDER_PROPERTY = "dynamisfx.jolt.provider";
    static final String PROVIDER_AUTO = "auto";
    static final String PROVIDER_JOLT_JNI = "jolt-jni";
    static final String PROVIDER_CSHIM = "cshim";

    private final JoltNativeBridge bridge;
    private final PhysicsBackend delegate;

    public JoltBackend() {
        this(new JoltNativeBridge(), null);
    }

    JoltBackend(JoltNativeBridge bridge) {
        this(bridge, cshimBackend(bridge));
    }

    private JoltBackend(JoltNativeBridge bridge, PhysicsBackend forcedDelegate) {
        this.bridge = bridge;
        this.delegate = forcedDelegate != null ? forcedDelegate : selectDelegate(bridge);
    }

    @Override
    public String id() {
        return "jolt";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return delegate.capabilities();
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        return delegate.createWorld(configuration);
    }

    private PhysicsBackend selectDelegate(JoltNativeBridge bridge) {
        String provider = System.getProperty(PROVIDER_PROPERTY, PROVIDER_AUTO).trim().toLowerCase();
        return switch (provider) {
            case PROVIDER_AUTO -> autoSelect(bridge);
            case PROVIDER_JOLT_JNI -> new JoltJniBackend();
            case PROVIDER_CSHIM -> cshimBackend(bridge);
            default -> throw new IllegalArgumentException(
                    "Unknown jolt provider '" + provider + "', expected one of: "
                            + PROVIDER_AUTO + ", " + PROVIDER_JOLT_JNI + ", " + PROVIDER_CSHIM);
        };
    }

    private PhysicsBackend autoSelect(JoltNativeBridge bridge) {
        JoltJniBackend joltJniBackend = new JoltJniBackend();
        return joltJniBackend.isAvailable() ? joltJniBackend : cshimBackend(bridge);
    }

    private static PhysicsBackend cshimBackend(JoltNativeBridge bridge) {
        return new PhysicsBackend() {
            @Override
            public String id() {
                return "jolt";
            }

            @Override
            public PhysicsCapabilities capabilities() {
                return CAPABILITIES;
            }

            @Override
            public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
                return new JoltWorld(configuration, bridge);
            }
        };
    }
}
