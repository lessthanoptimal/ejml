package org.ejml.simple;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSimpleSVD extends EjmlStandardJUnit {
    @Test
    public void rank_case0() {
        for (int dimen = 1; dimen < 10; dimen++) {
            assertEquals(dimen, SimpleMatrix.identity(dimen).svd().rank());
            assertEquals(dimen, SimpleMatrix.identity(dimen).svd(true).rank());
        }
    }

    @Test
    public void rank_case1() {
        double[] values = new double[]{10.0, 3.0, 20.0, 6.0, 30.0, 9.0};
        assertEquals(1, new SimpleMatrix(3, 2, true, values).svd().rank());
        assertEquals(1, new SimpleMatrix(3, 2, true, values).svd(true).rank());
    }
}