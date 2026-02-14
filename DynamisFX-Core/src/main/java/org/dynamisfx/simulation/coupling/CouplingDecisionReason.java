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

/**
 * Reason emitted by coupling policy evaluation.
 */
public enum CouplingDecisionReason {
    NO_CHANGE,
    PROMOTE_DISTANCE_THRESHOLD,
    PROMOTE_ALTITUDE_THRESHOLD,
    PROMOTE_PREDICTED_INTERCEPT,
    DEMOTE_DISTANCE_THRESHOLD,
    DEMOTE_ALTITUDE_THRESHOLD,
    DEMOTE_PREDICTED_EXIT,
    BLOCKED_BY_CONTACT,
    BLOCKED_BY_COOLDOWN,
    MISSING_DISTANCE_OBSERVATION,
    UNSUPPORTED_MODE
}
