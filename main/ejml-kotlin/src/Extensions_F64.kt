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

package org.ejml.kotlin

import org.ejml.data.DMatrixRMaj
import org.ejml.data.DMatrixSparseCSC
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.NormOps_DDRM
import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.ops.MatrixIO
import org.ejml.sparse.csc.CommonOps_DSCC
import org.ejml.sparse.csc.NormOps_DSCC

//----------------------- DDRM ------------------------------------------------------

operator fun DMatrixRMaj.times(a : DMatrixRMaj) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.mult(this,a, out)
    return out
}

operator fun DMatrixRMaj.plus(a : DMatrixRMaj) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.add(this,a, out)
    return out
}

operator fun DMatrixRMaj.plusAssign(a : DMatrixRMaj) {CommonOps_DDRM.add(this,a, this)}

operator fun DMatrixRMaj.plus(a : Double) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.add(this,a, out)
    return out
}
operator fun DMatrixRMaj.plusAssign(a : Double) {CommonOps_DDRM.add(this,a, this)}

operator fun DMatrixRMaj.minus(a : DMatrixRMaj) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.subtract(this,a, out)
    return out
}

operator fun DMatrixRMaj.minusAssign(a : DMatrixRMaj) {CommonOps_DDRM.subtract(this,a, this)}

operator fun DMatrixRMaj.unaryMinus() : DMatrixRMaj {
    val output = DMatrixRMaj(1,1)
    CommonOps_DDRM.changeSign(this,output)
    return output
}

operator fun DMatrixRMaj.minus(a : Double) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.subtract(this,a, out)
    return out
}

operator fun DMatrixRMaj.minusAssign(a : Double) {CommonOps_DDRM.subtract(this,a, this)}

operator fun DMatrixRMaj.rem(a : DMatrixRMaj) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.solve(this,a, out)
    return out
}

fun DMatrixRMaj.transpose() : DMatrixRMaj = CommonOps_DDRM.transpose(this,DMatrixRMaj(1,1))

fun DMatrixRMaj.diag() : DMatrixRMaj {
    val output = DMatrixRMaj(1,1)
    CommonOps_DDRM.extractDiag(this,output)
    return output
}
fun DMatrixRMaj.trace() : Double = CommonOps_DDRM.trace(this)

fun DMatrixRMaj.svd( compact : Boolean = false ) : Triple<DMatrixRMaj,DMatrixRMaj,DMatrixRMaj> {
    val decomposition = DecompositionFactory_DDRM.svd(true,true,compact)
    return Triple(
            decomposition.getU(null,false),
            decomposition.getW(null),
            decomposition.getV(null,false))
}

fun DMatrixRMaj.solve(B : DMatrixRMaj) : DMatrixRMaj {
    val X = DMatrixRMaj(1,1)
    if( !CommonOps_DDRM.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun DMatrixRMaj.solveSPD(B : DMatrixRMaj) : DMatrixRMaj {
    val X = DMatrixRMaj(1,1)
    if( !CommonOps_DDRM.solveSPD(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun DMatrixRMaj.normF() : Double = NormOps_DDRM.normF(this)
fun DMatrixRMaj.normP( p : Double ) : Double = NormOps_DDRM.normP(this,p)
fun DMatrixRMaj.normP1() : Double = NormOps_DDRM.normP1(this)
fun DMatrixRMaj.normP2() : Double = NormOps_DDRM.normP2(this)
fun DMatrixRMaj.normPInf() : Double = NormOps_DDRM.normPInf(this)

fun String.toDDRM() : DMatrixRMaj = MatrixIO.matlabToDDRM(this)

fun DMatrixRMaj.toDSCC() : DMatrixSparseCSC {
    val output = DMatrixSparseCSC(this.numRows,this.numCols)
    ConvertDMatrixStruct.convert(this,output)
    return output
}

//----------------------- DSCC ------------------------------------------------------

operator fun DMatrixSparseCSC.times(a : DMatrixSparseCSC) : DMatrixSparseCSC {
    val out = DMatrixSparseCSC(1,1)
    CommonOps_DSCC.mult(this,a, out)
    return out
}

operator fun DMatrixSparseCSC.times(a : DMatrixRMaj) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1)
    CommonOps_DSCC.mult(this,a, out)
    return out
}

operator fun DMatrixSparseCSC.plus(a : DMatrixSparseCSC) : DMatrixSparseCSC {
    val out = DMatrixSparseCSC(1,1)
    CommonOps_DSCC.add(1.0,this,1.0,a, out,null,null)
    return out
}

operator fun DMatrixSparseCSC.minus(a : DMatrixSparseCSC) : DMatrixSparseCSC {
    val out = DMatrixSparseCSC(1,1)
    CommonOps_DSCC.add(1.0,this,-1.0,a, out,null,null)
    return out
}

fun DMatrixSparseCSC.solve(B : DMatrixSparseCSC) : DMatrixSparseCSC {
    val X = DMatrixSparseCSC(1,1)
    if( !CommonOps_DSCC.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun DMatrixSparseCSC.solve(B : DMatrixRMaj) : DMatrixRMaj {
    val X = DMatrixRMaj(1,1)
    if( !CommonOps_DSCC.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun DMatrixSparseCSC.transpose() : DMatrixSparseCSC {
    return CommonOps_DSCC.transpose(this,DMatrixSparseCSC(1,1),null)
}
fun DMatrixSparseCSC.diag() : DMatrixSparseCSC {
    val output = DMatrixSparseCSC(1,1)
    CommonOps_DSCC.extractDiag(this,output)
    return output
}
fun DMatrixSparseCSC.trace() : Double {return CommonOps_DSCC.trace(this)}

fun DMatrixSparseCSC.normF() : Double {return NormOps_DSCC.normF(this)}

fun DMatrixSparseCSC.toFDRM() : DMatrixRMaj {
    val output = DMatrixRMaj(this.numRows,this.numCols)
    ConvertDMatrixStruct.convert(this,output)
    return output
}


