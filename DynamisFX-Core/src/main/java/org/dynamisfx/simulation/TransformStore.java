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

package org.dynamisfx.simulation;

/**
 * Double-buffered transform store optimized for simulation-write/render-read usage.
 */
public final class TransformStore {

    private final double[] writePositions;
    private final double[] writeOrientations;
    private final double[] readPositions;
    private final double[] readOrientations;
    private double readSimulationTimeSeconds;
    private double writeSimulationTimeSeconds;

    public TransformStore(int objectCapacity) {
        if (objectCapacity <= 0) {
            throw new IllegalArgumentException("objectCapacity must be > 0");
        }
        this.writePositions = new double[objectCapacity * 3];
        this.writeOrientations = new double[objectCapacity * 4];
        this.readPositions = new double[objectCapacity * 3];
        this.readOrientations = new double[objectCapacity * 4];
    }

    public int capacity() {
        return writePositions.length / 3;
    }

    public synchronized void setTransform(
            int objectIndex,
            double posX,
            double posY,
            double posZ,
            double quatX,
            double quatY,
            double quatZ,
            double quatW) {
        validateIndex(objectIndex);
        validateFinite(posX, "posX");
        validateFinite(posY, "posY");
        validateFinite(posZ, "posZ");
        validateFinite(quatX, "quatX");
        validateFinite(quatY, "quatY");
        validateFinite(quatZ, "quatZ");
        validateFinite(quatW, "quatW");

        int p = objectIndex * 3;
        writePositions[p] = posX;
        writePositions[p + 1] = posY;
        writePositions[p + 2] = posZ;

        int q = objectIndex * 4;
        writeOrientations[q] = quatX;
        writeOrientations[q + 1] = quatY;
        writeOrientations[q + 2] = quatZ;
        writeOrientations[q + 3] = quatW;
    }

    /**
     * Publishes the current write buffer as the latest stable snapshot.
     */
    public synchronized void publish(double simulationTimeSeconds) {
        validateFinite(simulationTimeSeconds, "simulationTimeSeconds");
        System.arraycopy(writePositions, 0, readPositions, 0, writePositions.length);
        System.arraycopy(writeOrientations, 0, readOrientations, 0, writeOrientations.length);
        writeSimulationTimeSeconds = simulationTimeSeconds;
        readSimulationTimeSeconds = simulationTimeSeconds;
    }

    public synchronized TransformSample sample(int objectIndex) {
        validateIndex(objectIndex);
        int p = objectIndex * 3;
        int q = objectIndex * 4;
        return new TransformSample(
                readPositions[p],
                readPositions[p + 1],
                readPositions[p + 2],
                readOrientations[q],
                readOrientations[q + 1],
                readOrientations[q + 2],
                readOrientations[q + 3],
                readSimulationTimeSeconds);
    }

    public synchronized double readSimulationTimeSeconds() {
        return readSimulationTimeSeconds;
    }

    public synchronized double writeSimulationTimeSeconds() {
        return writeSimulationTimeSeconds;
    }

    private void validateIndex(int objectIndex) {
        if (objectIndex < 0 || objectIndex >= capacity()) {
            throw new IllegalArgumentException("objectIndex out of range: " + objectIndex);
        }
    }

    private static void validateFinite(double value, String field) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
    }

    public record TransformSample(
            double posX,
            double posY,
            double posZ,
            double quatX,
            double quatY,
            double quatZ,
            double quatW,
            double simulationTimeSeconds) {
    }
}
