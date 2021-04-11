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

package org.ejml.example;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.masks.DMaskFactory;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Example using masked matrix multiplication to count the triangles in a graph.
 * Triangle counting is used to detect communities in graphs and often used to analyse social graphs.
 *
 * More about the connection between graphs and linear algebra can be found at:
 * https://github.com/GraphBLAS/GraphBLAS-Pointers.
 *
 * @author Florentin Doerre
 */
public class ExampleMaskedTriangleCount {
    public static void main( String[] args ) {
        // For the example we will be using the following graph:
        // (0)--(1)--(2)--(0), (2)--(3)--(4)--(2), (5)
        var adjacencyMatrix = new DMatrixSparseCSC(6, 6, 24);
        adjacencyMatrix.set(0, 1, 1);
        adjacencyMatrix.set(0, 2, 1);
        adjacencyMatrix.set(1, 2, 1);
        adjacencyMatrix.set(2, 3, 1);
        adjacencyMatrix.set(2, 4, 1);
        adjacencyMatrix.set(3, 4, 1);

        // Triangle Count is defined over undirected graphs, therefore we make matrix symmetric (i.e. undirected)
        adjacencyMatrix.copy().createCoordinateIterator().forEachRemaining(v -> adjacencyMatrix.set(v.col, v.row, v.value));

        // In a graph context mxm computes all path of length 2 (a->b->c).
        // But, for triangles we are only interested in the "closed" path which form a triangle (a->b->c->a).
        // To avoid computing irrelevant paths, we can use the adjacency matrix as the mask, which assures (a->c) exists.
        var mask = DMaskFactory.builder(adjacencyMatrix, true).build();
        var triangleMatrix = CommonOpsWithSemiRing_DSCC.mult(adjacencyMatrix, adjacencyMatrix, null, DSemiRings.PLUS_TIMES, mask, null, null);

        // To compute the triangles per vertex we calculate the sum per each row.
        // For the correct count, we need to divide the count by 2 as each triangle was counted twice (a--b--c, and a--c--b)
        var trianglesPerVertex = CommonOps_DSCC.reduceRowWise(triangleMatrix, 0, Double::sum, null);
        CommonOps_DDRM.apply(trianglesPerVertex, v -> v/2);

        System.out.println("Triangles including vertex 0 " + trianglesPerVertex.get(0));
        System.out.println("Triangles including vertex 2 " + trianglesPerVertex.get(2));
        System.out.println("Triangles including vertex 5 " + trianglesPerVertex.get(5));

        // Note: To avoid counting each triangle twice, the lower triangle over the adjacency matrix can be used TRI<A> = A * L
    }
}
