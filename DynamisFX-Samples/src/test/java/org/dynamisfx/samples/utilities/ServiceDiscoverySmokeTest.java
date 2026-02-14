package org.dynamisfx.samples.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.dynamisfx.DynamisFXSamplerProject;

class ServiceDiscoverySmokeTest {

    @Test
    void samplerProjectProviderIsDiscoverable() {
        ServiceLoader<DynamisFXSamplerProject> loader = ServiceLoader.load(DynamisFXSamplerProject.class);
        List<DynamisFXSamplerProject> providers = new ArrayList<>();
        loader.forEach(providers::add);

        assertThat("Expected at least one sampler project provider", providers.size(), greaterThan(0));
    }
}
