/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pabeles.concurrency;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ForkJoinTask;

/**
 * Performs a parallel for loop with the specified step increment and a workspace for each thread. Each thread is
 * tasked with computing the results for a specified number of iterations and it will be provided the same workspace
 * for all iterations
 *
 * @author Peter Abeles
 */
public class IntObjectTask<T> extends ForkJoinTask<Void> {

    final int idx0; // initial index
    final int idx1; // exclusive final index
    final int step; // index step
    final int maxThreads; // maximum number of allowed threads. Used to compute number iterations per thread
    final int whichThread; // -1 for master task, otherwise it's the index of each child task
    final IntObjectConsumer<T> consumer;
    final GrowArray<T> workspace;
    final @Nullable T data;
    @Nullable IntObjectTask<T> next;

    /**
     * @param step The amount the counter steps each iteration. Must be positive
     * @param maxThreads The number of threads it can spawn
     * @param whichThread If < 0 then it's the master thread otherwise this thread index and specifies which
     * workspace to use
     */
    public IntObjectTask( int idx0, int idx1, int step, int maxThreads, int whichThread,
                          GrowArray<T> workspace,
                          IntObjectConsumer<T> consumer ) {
        this.idx0 = idx0;
        this.idx1 = idx1;
        this.step = step;
        this.maxThreads = maxThreads;
        this.whichThread = whichThread;
        this.consumer = consumer;
        this.workspace = workspace;
        this.data = null;
    }

    @Override
    public Void getRawResult() {return null;}

    @Override
    protected void setRawResult( Void value ) {}

    @Override
    protected boolean exec() {
        if (whichThread == -1) {
            // Compute the number of iterations that will be required
            int numIterations = (idx1 - idx0)/step + ((idx1 - idx0)%step != 0 ? 1 : 0);

            // Number of actual threads it will use
            int numThreads = Math.min(numIterations, maxThreads);

            // Declare all the workspace variables
            workspace.reset();
            workspace.resize(numThreads);

            // this is the first task, spawn all the others
            IntObjectTask<T> root = null;
            IntObjectTask<T> previous = null;

            for (int threadId = 0; threadId < numThreads - 1; threadId++) {
                int segment0 = computeIndex(threadId, numThreads, numIterations);
                int segment1 = computeIndex(threadId + 1, numThreads, numIterations);
                var task = new IntObjectTask<>(segment0, segment1, step, -1, threadId, workspace, consumer);
                if (root == null) {
                    root = previous = task;
                } else {
                    Objects.requireNonNull(previous).next = task;
                    previous = task;
                }
                task.fork();
            }
            // process the last segment in this thread
            for (int index = computeIndex(numThreads - 1, numThreads, numIterations); index < idx1; index += step) {
                consumer.accept(workspace.get(numThreads - 1), index);
            }

            // wait until all the other threads are done
            while (root != null) {
                root.join();
                root = root.next;
            }
        } else {
            T work = workspace.get(whichThread);
            for (int index = idx0; index < idx1; index += step) {
                consumer.accept(work, index);
            }
        }
        return true;
    }

    private int computeIndex( int threadId, int numThreads, int numIterations ) {
        return idx0 + (threadId*numIterations/numThreads)*step;
    }
}
