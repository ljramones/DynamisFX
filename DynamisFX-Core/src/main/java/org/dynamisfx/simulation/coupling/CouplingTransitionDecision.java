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

package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import java.util.Optional;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Encapsulates policy decision and reason for telemetry and control flow.
 */
public record CouplingTransitionDecision(Optional<ObjectSimulationMode> nextMode, CouplingDecisionReason reason) {

    public CouplingTransitionDecision {
        Objects.requireNonNull(nextMode, "nextMode must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }

    public static CouplingTransitionDecision noChange(CouplingDecisionReason reason) {
        return new CouplingTransitionDecision(Optional.empty(), reason);
    }

    public static CouplingTransitionDecision transitionTo(ObjectSimulationMode nextMode, CouplingDecisionReason reason) {
        return new CouplingTransitionDecision(Optional.of(nextMode), reason);
    }
}
