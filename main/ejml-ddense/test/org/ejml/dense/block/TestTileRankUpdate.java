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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTileRankUpdate extends EjmlStandardJUnit {
    int widthA = 3;
    int heightA = 4;
    int widthC = 5;
    int offsetA = 4;
    int offsetB = 5;
    int offsetC = 6;

    @Test void tileMultMinusTransA_U() {
        double[] dataAB = randomArray(Math.max(heightA*widthA + offsetA, heightA*widthC + offsetB));
        double[] expected = randomArray(heightA*widthC + offsetC);
        double[] found = expected.clone();

        TileRankUpdate_F64.tileMultMinusTransA_U(dataAB, found, heightA, widthA, widthC, offsetA, offsetB, offsetC);
        TileMultiplication_F64.tileMultMinusTransA(dataAB, dataAB, expected, heightA, widthA, widthC, offsetA, offsetB, offsetC);

        // Only compare upper triangle
        for (int i = 0; i < heightA; i++) {
            for (int j = i; j < widthC; j++) {
                double f = found[offsetC + i*widthC + j];
                double e = expected[offsetC + i*widthC + j];
                assertEquals(e, f, UtilEjml.TESTP_F64);
            }
        }
    }

    @Test void tileMultMinusTransB_L() {
        double[] dataAB = randomArray(Math.max(heightA*widthA + offsetA, widthA*widthC + offsetB));
        double[] expected = randomArray(heightA*widthC + offsetC);
        double[] found = expected.clone();

        TileRankUpdate_F64.tileMultMinusTransB_L(dataAB, found, heightA, widthA, widthC, offsetA, offsetB, offsetC);
        TileMultiplication_F64.tileMultMinusTransB(dataAB, dataAB, expected, heightA, widthA, widthC, offsetA, offsetB, offsetC);

        // Only compare lower triangle
        for (int i = 0; i < heightA; i++) {
            for (int j = 0; j <= i; j++) {
                double f = found[offsetC + i*widthC + j];
                double e = expected[offsetC + i*widthC + j];
                assertEquals(e, f, UtilEjml.TESTP_F64);
            }
        }
    }

    private double[] randomArray( int length ) {
        var out = new double[length];
        for (int i = 0; i < out.length; i++) {
            out[i] = rand.nextDouble();
        }
        return out;
    }
}
