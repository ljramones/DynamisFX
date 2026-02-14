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

import java.util.OptionalDouble;
import java.util.Objects;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Distance-threshold transition policy with cooldown and hysteresis support.
 */
public final class ThresholdTransitionPolicy implements CouplingTransitionPolicy {

    private final CouplingObservationProvider observationProvider;
    private final double promoteDistanceMeters;
    private final double demoteDistanceMeters;
    private final double cooldownSeconds;
    private final double preemptInterceptWindowSeconds;
    private final double promoteAltitudeMeters;
    private final double demoteAltitudeMeters;

    public ThresholdTransitionPolicy(
            CouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds) {
        this(
                observationProvider,
                promoteDistanceMeters,
                demoteDistanceMeters,
                cooldownSeconds,
                0.0,
                Double.NaN,
                Double.NaN);
    }

    public ThresholdTransitionPolicy(
            CouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds,
            double preemptInterceptWindowSeconds) {
        this.observationProvider = Objects.requireNonNull(observationProvider, "observationProvider must not be null");
        if (!Double.isFinite(promoteDistanceMeters) || promoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("promoteDistanceMeters must be finite and > 0");
        }
        if (!Double.isFinite(demoteDistanceMeters) || demoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("demoteDistanceMeters must be finite and > 0");
        }
        if (demoteDistanceMeters < promoteDistanceMeters) {
            throw new IllegalArgumentException("demoteDistanceMeters must be >= promoteDistanceMeters");
        }
        if (!Double.isFinite(cooldownSeconds) || cooldownSeconds < 0.0) {
            throw new IllegalArgumentException("cooldownSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(preemptInterceptWindowSeconds) || preemptInterceptWindowSeconds < 0.0) {
            throw new IllegalArgumentException("preemptInterceptWindowSeconds must be finite and >= 0");
        }
        this.promoteAltitudeMeters = Double.NaN;
        this.demoteAltitudeMeters = Double.NaN;
        this.promoteDistanceMeters = promoteDistanceMeters;
        this.demoteDistanceMeters = demoteDistanceMeters;
        this.cooldownSeconds = cooldownSeconds;
        this.preemptInterceptWindowSeconds = preemptInterceptWindowSeconds;
    }

    public ThresholdTransitionPolicy(
            CouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds,
            double preemptInterceptWindowSeconds,
            double promoteAltitudeMeters,
            double demoteAltitudeMeters) {
        this.observationProvider = Objects.requireNonNull(observationProvider, "observationProvider must not be null");
        if (!Double.isFinite(promoteDistanceMeters) || promoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("promoteDistanceMeters must be finite and > 0");
        }
        if (!Double.isFinite(demoteDistanceMeters) || demoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("demoteDistanceMeters must be finite and > 0");
        }
        if (demoteDistanceMeters < promoteDistanceMeters) {
            throw new IllegalArgumentException("demoteDistanceMeters must be >= promoteDistanceMeters");
        }
        if (!Double.isFinite(cooldownSeconds) || cooldownSeconds < 0.0) {
            throw new IllegalArgumentException("cooldownSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(preemptInterceptWindowSeconds) || preemptInterceptWindowSeconds < 0.0) {
            throw new IllegalArgumentException("preemptInterceptWindowSeconds must be finite and >= 0");
        }
        if (Double.isFinite(promoteAltitudeMeters) && promoteAltitudeMeters < 0.0) {
            throw new IllegalArgumentException("promoteAltitudeMeters must be >= 0 when configured");
        }
        if (Double.isFinite(demoteAltitudeMeters) && demoteAltitudeMeters < 0.0) {
            throw new IllegalArgumentException("demoteAltitudeMeters must be >= 0 when configured");
        }
        if (Double.isFinite(promoteAltitudeMeters)
                && Double.isFinite(demoteAltitudeMeters)
                && demoteAltitudeMeters < promoteAltitudeMeters) {
            throw new IllegalArgumentException("demoteAltitudeMeters must be >= promoteAltitudeMeters");
        }
        this.promoteDistanceMeters = promoteDistanceMeters;
        this.demoteDistanceMeters = demoteDistanceMeters;
        this.cooldownSeconds = cooldownSeconds;
        this.preemptInterceptWindowSeconds = preemptInterceptWindowSeconds;
        this.promoteAltitudeMeters = promoteAltitudeMeters;
        this.demoteAltitudeMeters = demoteAltitudeMeters;
    }

    @Override
    public CouplingTransitionDecision evaluate(CouplingTransitionContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (isInCooldown(context)) {
            return CouplingTransitionDecision.noChange(CouplingDecisionReason.BLOCKED_BY_COOLDOWN);
        }

        OptionalDouble distanceMeters = observationProvider.distanceMetersToNearestZone(context.objectId(), context.zones());
        if (distanceMeters.isEmpty()) {
            return CouplingTransitionDecision.noChange(CouplingDecisionReason.MISSING_DISTANCE_OBSERVATION);
        }
        double distance = distanceMeters.orElseThrow();
        if (!Double.isFinite(distance) || distance < 0.0) {
            throw new IllegalArgumentException("distance observation must be finite and >= 0");
        }

        ObjectSimulationMode mode = context.currentMode();
        if (mode == ObjectSimulationMode.ORBITAL_ONLY) {
            if (Double.isFinite(promoteAltitudeMeters)) {
                OptionalDouble altitudeMeters = observationProvider.altitudeMetersAboveSurface(context.objectId(), context.zones());
                if (altitudeMeters.isPresent()) {
                    double altitude = altitudeMeters.orElseThrow();
                    if (!Double.isFinite(altitude) || altitude < 0.0) {
                        throw new IllegalArgumentException("altitude observation must be finite and >= 0");
                    }
                    if (altitude <= promoteAltitudeMeters) {
                        return CouplingTransitionDecision.transitionTo(
                                ObjectSimulationMode.PHYSICS_ACTIVE,
                                CouplingDecisionReason.PROMOTE_ALTITUDE_THRESHOLD);
                    }
                }
            }
            if (preemptInterceptWindowSeconds > 0.0) {
                double predictedIntercept = context.predictedInterceptSeconds().orElse(Double.POSITIVE_INFINITY);
                if (predictedIntercept <= preemptInterceptWindowSeconds) {
                    return CouplingTransitionDecision.transitionTo(
                            ObjectSimulationMode.PHYSICS_ACTIVE,
                            CouplingDecisionReason.PROMOTE_PREDICTED_INTERCEPT);
                }
            }
            if (distance <= promoteDistanceMeters) {
                return CouplingTransitionDecision.transitionTo(
                        ObjectSimulationMode.PHYSICS_ACTIVE,
                        CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD);
            }
            return CouplingTransitionDecision.noChange(CouplingDecisionReason.NO_CHANGE);
        }

        if (mode == ObjectSimulationMode.PHYSICS_ACTIVE) {
            if (preemptInterceptWindowSeconds > 0.0) {
                double predictedIntercept = context.predictedInterceptSeconds().orElse(Double.POSITIVE_INFINITY);
                if (predictedIntercept <= preemptInterceptWindowSeconds) {
                    if (observationProvider.hasActiveContact(context.objectId())) {
                        return CouplingTransitionDecision.noChange(CouplingDecisionReason.BLOCKED_BY_CONTACT);
                    }
                    return CouplingTransitionDecision.transitionTo(
                            ObjectSimulationMode.ORBITAL_ONLY,
                            CouplingDecisionReason.DEMOTE_PREDICTED_EXIT);
                }
            }
            if (Double.isFinite(demoteAltitudeMeters)) {
                OptionalDouble altitudeMeters = observationProvider.altitudeMetersAboveSurface(context.objectId(), context.zones());
                if (altitudeMeters.isPresent()) {
                    double altitude = altitudeMeters.orElseThrow();
                    if (!Double.isFinite(altitude) || altitude < 0.0) {
                        throw new IllegalArgumentException("altitude observation must be finite and >= 0");
                    }
                    if (altitude >= demoteAltitudeMeters) {
                        if (observationProvider.hasActiveContact(context.objectId())) {
                            return CouplingTransitionDecision.noChange(CouplingDecisionReason.BLOCKED_BY_CONTACT);
                        }
                        return CouplingTransitionDecision.transitionTo(
                                ObjectSimulationMode.ORBITAL_ONLY,
                                CouplingDecisionReason.DEMOTE_ALTITUDE_THRESHOLD);
                    }
                }
            }
            if (distance >= demoteDistanceMeters) {
                if (observationProvider.hasActiveContact(context.objectId())) {
                    return CouplingTransitionDecision.noChange(CouplingDecisionReason.BLOCKED_BY_CONTACT);
                }
                return CouplingTransitionDecision.transitionTo(
                        ObjectSimulationMode.ORBITAL_ONLY,
                        CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD);
            }
            return CouplingTransitionDecision.noChange(CouplingDecisionReason.NO_CHANGE);
        }

        return CouplingTransitionDecision.noChange(CouplingDecisionReason.UNSUPPORTED_MODE);
    }

    private boolean isInCooldown(CouplingTransitionContext context) {
        if (context.lastTransitionTimeSeconds() < 0.0) {
            return false;
        }
        return context.simulationTimeSeconds() < (context.lastTransitionTimeSeconds() + cooldownSeconds);
    }
}
