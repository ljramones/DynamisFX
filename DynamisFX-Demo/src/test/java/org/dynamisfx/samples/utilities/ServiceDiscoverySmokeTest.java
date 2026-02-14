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
