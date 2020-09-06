/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;

/**
 * Example including one iteration of the graph traversal algorithm breath-first-search (BFS), using different semirings.
 * So following the outgoing relationships for a set of starting nodes.
 *
 * more about the connection between graphs and linear algebra can be found at: https://github.com/GraphBLAS/GraphBLAS-Pointers.
 *
 * @author Florentin Doerre
 */
public class ExampleGraphPaths {
    private static final int NODE_COUNT = 4;

    public static void main(String[] args) {
        DMatrixSparseCSC adjacencyMatrix = new DMatrixSparseCSC(NODE_COUNT, 4);

        /*
            For the example we will be using the following graph:

            (3)<-[cost: 0.2]-(0)<-[cost: 0.1]->(2)<-[cost: 0.3]-(1)
         */
        adjacencyMatrix.set(0, 2, 0.1);
        adjacencyMatrix.set(0, 3, 0.2);
        adjacencyMatrix.set(2, 0, 0.1);
        adjacencyMatrix.set(3, 2, 0.3);

        // Semirings are used to redefine + and * f.i. with OR for + and AND for *
        DSemiRing lor_land = DSemiRings.OR_AND;
        DSemiRing min_times = DSemiRings.MIN_TIMES;
        DSemiRing plus_land = new DSemiRing(DMonoids.PLUS, DMonoids.AND);

        // sparse Vector (Matrix with one column)
        DMatrixSparseCSC startNodes = new DMatrixSparseCSC(1, NODE_COUNT);
        // setting the node 0 as the start-node
        startNodes.set(0, 0, 1);

        DMatrixSparseCSC outputVector = startNodes.createLike();

        // Compute which nodes can be reached from the node 0 (disregarding the costs of the relationship)
        CommonOpsWithSemiRing_DSCC.mult(startNodes, adjacencyMatrix, outputVector, lor_land, null, null);

        System.out.println("Node 3 can be reached from node 0: " + (outputVector.get(0, 3) == 1));
        System.out.println("Node 1 can be reached from node 0: " + (outputVector.get(0, 1) == 1));

        // Add node 3 to the start nodes
        startNodes.set(0, 3, 1);

        // Find the number of path the nodes can be reached with
        CommonOpsWithSemiRing_DSCC.mult(startNodes, adjacencyMatrix, outputVector, plus_land, null, null);
        System.out.println("The number of start-nodes leading to node 2 is " + (int) outputVector.get(0, 2));

        // Find the path with the minimal cost (direct connection from one of the specified starting nodes)
        // the calculated cost equals the cost specified in the relationship (as both startNodes have a weight of 1)
        // as an alternative you could use the MIN_PLUS semiring to consider the existing cost specified in the startNodes vector
        CommonOpsWithSemiRing_DSCC.mult(startNodes, adjacencyMatrix, outputVector, min_times, null, null);
        System.out.println("The minimal cost to reach the node 2 is " + outputVector.get(0, 2));
    }
}
