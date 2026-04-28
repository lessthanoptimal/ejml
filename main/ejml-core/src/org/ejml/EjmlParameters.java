/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml;

/// Parameters that are change EJML's behavior across the entire codebase. It can be used to tune performance
/// for specific platforms or catastrophically break the library, depending on how they are used.
public class EjmlParameters {

    public static final float TOL32 = 1e-4f;
    public static final double TOL64 = 1e-8;

    /// If true then threaded code will produce the exact same output as single threaded code and always
    /// produce the exact same result. If false then it's free to do optimizations than can improve performance
    /// but because the order of operations has changed results will only be withing floating point tol and
    /// could potentially change on every run.
    ///
    /// Note: This is added as a final boolean for future use and make determinism officially part of the
    /// EJML contract.
    public static final boolean DETERMINISTIC_THREADS = true;

    /// Used to adjust which algorithms are used. Often there is a trade-off between memory usage
    /// and speed.
    public static MemoryUsage MEMORY = MemoryUsage.FASTER;

    /// In modern computers there are high speed memory caches. It is assumed that a square
    /// block with this width can be contained entirely in one of those caches. Settings this
    /// value too large can have a dramatic effect on performance in some situations. Setting
    /// it too low results in a less dramatic performance hit. The optimal value is dependent
    /// on the computer's memory architecture.
    ///
    public static int BLOCK_WIDTH = 60;
    public static int BLOCK_WIDTH_CHOL = 20;

    public static int TRANSPOSE_SWITCH = 375;

    /// At what point does it switch from a small matrix multiply to the reorder version.
    public static int MULT_COLUMN_SWITCH = 15;
    public static int MULT_TRANAB_COLUMN_SWITCH = 40;
    public static int MULT_INNER_SWITCH = 100;

    public static int CMULT_COLUMN_SWITCH = 7;
    public static int CMULT_TRANAB_COLUMN_SWITCH = 20;

    /// At which point should it switch to the block cholesky algorithm.
    ///
    /// In benchmarks  the basic actually performed slightly better at 1000
    /// but in JVM 1.6 it sometimes gets stuck in a mode where the basic version was very slow
    /// in that case the block performed much better.
    ///
    public static int SWITCH_BLOCK64_CHOLESKY = 1000;

    public static int SWITCH_BLOCK64_QR = 1500;

    public enum MemoryUsage {
        /// Use lower memory algorithm while not totally sacrificing speed.
        LOW_MEMORY,
        /// Always favor faster algorithms even if they use more memory.
        FASTER
    }
}
