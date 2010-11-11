/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml;


/**
 * This is a list of parameters that are used across the code.  To tune performance
 * for a particular system change these values.
 *
 * @author Peter Abeles
 */
public class EjmlParameters {

    /**
     * <p>
     * In modern computers there are high speed memory caches.  It is assumed that a square
     * block with this width can be contained entirely in one of those caches.  Settings this
     * value too large can have a dramatic effect on performance in some situations.  Setting
     * it too low results in a less dramatic performance hit.  The optimal value is dependent
     * on the computer's memory architecture.
     * </p>
     */
    // See design notes
    public static int BLOCK_WIDTH = 60;
    public static int BLOCK_WIDTH_CHOL = 20;

    /**
     * Number of elements in a block.
     */
    public static int BLOCK_SIZE = BLOCK_WIDTH*BLOCK_WIDTH;

    public static int TRANSPOSE_SWITCH = 375;

    /**
     * At what point does it switch from a small matrix multiply to the reorder version.
     */
    public static int MULT_COLUMN_SWITCH = 15;
    public static int MULT_TRANAB_COLUMN_SWITCH = 40;

    /**
     * <p>
     * At which point should it switch to the block cholesky algorithm.
     * </p>
     * <p>
     * In benchmarks  the basic actually performed slightly better at 1000
     * but in JVM 1.6 it some times get stuck in a mode where the basic version was very slow
     * in that case the block performed much better.
     * </p>
     */
    public static int SWITCH_BLOCK_CHOLESKY = 1000;
}
