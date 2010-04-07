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

/**
 * <p>
 * RANSAC (RANdom SAmpling Consensus) is a technique for fitting a model to noisy data.  It works
 * by performing the following operations until a solution is found that has the desired performance.
 * 1) Randomly sampling a few data points, 2) fitting a model, 3) selecting points in the whole data
 * set that fit that model, 4) and optimizing again.  There are many variants on this algorithm.
 * </p>
 *
 * <p>
 * This specific implementation fits a 2D line to noisy data.  It is intended to demonstrate how
 * {@link org.ejml.alg.dense.linsol.LinearSolver LinearSolver} is used.  In addition it shows that
 * {@link org.ejml.alg.dense.linsol.AdjustableLinearSolver AdjustableLinearSolver} can
 * improve performance by adjusting a previous solution instead of entirely recomputing it.
 * </p>
 * 
 * @author Peter Abeles
 */
public class RansacLineFit {

    
}
