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
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 * Simple Looping Service. Useful for games, Simulations, or other items that require a running "Loop".
 *
 * @author Jason Pollastrini aka jdub1581
 */
public abstract class AbstractLoopService extends ScheduledService<Void>{
    private static final Logger LOG = Logger.getLogger(AbstractLoopService.class.getName());

    private final long ONE_NANO = 1_000_000_000L;
    private final double ONE_NANO_INV = 1f / 1_000_000_000L;

    private long startTime, previousTime;
    private double frameRate, deltaTime;
        
    private final LoopThreadFactory tf = new LoopThreadFactory();    
    private final ExecutorService cachedExecutor = Executors.newCachedThreadPool(tf);

    
    protected AbstractLoopService() {
        this.setPeriod(Duration.millis(16.667)); // eqiv to 60 fps
        this.setExecutor(cachedExecutor);
    }


    protected final double getTimeElapsed() {
        return getCurrentTime() * ONE_NANO_INV;
    }

    protected final long getCurrentTime() {
        return System.nanoTime() - startTime;
    }

    protected final double getFrameRate() {
        return frameRate;
    }

    protected final double getDeltaTime() {
        return deltaTime;
    }

    private void updateTimer() {
        deltaTime = (getCurrentTime() - previousTime) * (1.0f / ONE_NANO);
        frameRate = 1.0f / deltaTime;
        previousTime = getCurrentTime();

    }

    @Override
    public void start() {
        super.start();
        if (startTime <= 0) {
            startTime = System.nanoTime();
        }
    }

    @Override
    public void reset() {
        super.reset();
        startTime = System.nanoTime();
        previousTime = getCurrentTime();
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTimer();
                // perform needed background tasks here ..
                runInBackground();
                
                return null;
            }
        };
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        // Setup to handle Actions for UI here     
        runOnFXThread();
    }

    @Override
    protected void failed() {
        Throwable ex = getException();
        if (ex != null) {
            LOG.log(Level.SEVERE, "Loop service failed", ex);
        }
    }

    @Override
    public String toString() {
        return "ElapsedTime: " + getCurrentTime() + "\nTime in seconds: " + getTimeElapsed()
                + "\nFrame Rate: " + getFrameRate()
                + "\nDeltaTime: " + getDeltaTime();
    }
    
    /*==========================================================================
     *      Methods for access
     */
    
    protected abstract void runOnFXThread();
    protected abstract void runInBackground();

    /*==========================================================================
    
     */
    private final class LoopThreadFactory implements ThreadFactory {

        public LoopThreadFactory() {
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "NanoTimerThread");
            t.setPriority(Thread.NORM_PRIORITY + 1);
            t.setDaemon(true);
            return t;
        }

    }
    
}
