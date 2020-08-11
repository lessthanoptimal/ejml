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

import org.ejml.data.FMatrixRMaj
import org.ejml.data.FMatrixSparseCSC
import org.ejml.dense.row.CommonOps_FDRM
import org.ejml.dense.row.NormOps_FDRM
import org.ejml.dense.row.factory.DecompositionFactory_FDRM
import org.ejml.ops.ConvertFMatrixStruct
import org.ejml.ops.MatrixIO
import org.ejml.sparse.csc.CommonOps_FSCC
import org.ejml.sparse.csc.NormOps_FSCC

//----------------------- FDRM ------------------------------------------------------

operator fun FMatrixRMaj.times(a : FMatrixRMaj) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.mult(this,a, out)
    return out
}

operator fun FMatrixRMaj.plus(a : FMatrixRMaj) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.add(this,a, out)
    return out
}

operator fun FMatrixRMaj.plusAssign(a : FMatrixRMaj) {CommonOps_FDRM.add(this,a, this)}

operator fun FMatrixRMaj.plus(a : Float) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.add(this,a, out)
    return out
}
operator fun FMatrixRMaj.plusAssign(a : Float) {CommonOps_FDRM.add(this,a, this)}

operator fun FMatrixRMaj.minus(a : FMatrixRMaj) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.subtract(this,a, out)
    return out
}

operator fun FMatrixRMaj.minusAssign(a : FMatrixRMaj) {CommonOps_FDRM.subtract(this,a, this)}

operator fun FMatrixRMaj.unaryMinus() : FMatrixRMaj {
    val output = FMatrixRMaj(1,1)
    CommonOps_FDRM.changeSign(this,output)
    return output
}

operator fun FMatrixRMaj.minus(a : Float) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.subtract(this,a, out)
    return out
}

operator fun FMatrixRMaj.minusAssign(a : Float) {CommonOps_FDRM.subtract(this,a, this)}

operator fun FMatrixRMaj.rem(a : FMatrixRMaj) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1);
    CommonOps_FDRM.solve(this,a, out)
    return out
}

fun FMatrixRMaj.transpose() : FMatrixRMaj = CommonOps_FDRM.transpose(this,FMatrixRMaj(1,1))

fun FMatrixRMaj.diag() : FMatrixRMaj {
    val output = FMatrixRMaj(1,1)
    CommonOps_FDRM.extractDiag(this,output)
    return output
}
fun FMatrixRMaj.trace() : Float = CommonOps_FDRM.trace(this)

fun FMatrixRMaj.svd( compact : Boolean = false ) : Triple<FMatrixRMaj,FMatrixRMaj,FMatrixRMaj> {
    val decomposition = DecompositionFactory_FDRM.svd(true,true,compact)
    return Triple(
            decomposition.getU(null,false),
            decomposition.getW(null),
            decomposition.getV(null,false))
}

fun FMatrixRMaj.solve(B : FMatrixRMaj) : FMatrixRMaj {
    val X = FMatrixRMaj(1,1)
    if( !CommonOps_FDRM.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun FMatrixRMaj.solveSPD(B : FMatrixRMaj) : FMatrixRMaj {
    val X = FMatrixRMaj(1,1)
    if( !CommonOps_FDRM.solveSPD(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun FMatrixRMaj.normF() : Float = NormOps_FDRM.normF(this)
fun FMatrixRMaj.normP( p : Float ) : Float = NormOps_FDRM.normP(this,p)
fun FMatrixRMaj.normP1() : Float = NormOps_FDRM.normP1(this)
fun FMatrixRMaj.normP2() : Float = NormOps_FDRM.normP2(this)
fun FMatrixRMaj.normPInf() : Float = NormOps_FDRM.normPInf(this)

fun String.toFDRM() : FMatrixRMaj = MatrixIO.matlabToFDRM(this)

fun FMatrixRMaj.toDSCC() : FMatrixSparseCSC {
    val output = FMatrixSparseCSC(this.numRows,this.numCols)
    ConvertFMatrixStruct.convert(this,output)
    return output
}

//----------------------- DSCC ------------------------------------------------------

operator fun FMatrixSparseCSC.times(a : FMatrixSparseCSC) : FMatrixSparseCSC {
    val out = FMatrixSparseCSC(1,1)
    CommonOps_FSCC.mult(this,a, out)
    return out
}

operator fun FMatrixSparseCSC.times(a : FMatrixRMaj) : FMatrixRMaj {
    val out = FMatrixRMaj(1,1)
    CommonOps_FSCC.mult(this,a, out)
    return out
}

operator fun FMatrixSparseCSC.plus(a : FMatrixSparseCSC) : FMatrixSparseCSC {
    val out = FMatrixSparseCSC(1,1)
    CommonOps_FSCC.add(1.0f,this,1.0f,a, out,null,null)
    return out
}

operator fun FMatrixSparseCSC.minus(a : FMatrixSparseCSC) : FMatrixSparseCSC {
    val out = FMatrixSparseCSC(1,1)
    CommonOps_FSCC.add(1.0f,this,-1.0f,a, out,null,null)
    return out
}

fun FMatrixSparseCSC.solve(B : FMatrixSparseCSC) : FMatrixSparseCSC {
    val X = FMatrixSparseCSC(1,1)
    if( !CommonOps_FSCC.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun FMatrixSparseCSC.solve(B : FMatrixRMaj) : FMatrixRMaj {
    val X = FMatrixRMaj(1,1)
    if( !CommonOps_FSCC.solve(this,B,X) ) {
        throw RuntimeException("Failed to solve")
    }
    return X
}

fun FMatrixSparseCSC.transpose() : FMatrixSparseCSC {
    return CommonOps_FSCC.transpose(this,FMatrixSparseCSC(1,1),null)
}
fun FMatrixSparseCSC.diag() : FMatrixSparseCSC {
    val output = FMatrixSparseCSC(1,1)
    CommonOps_FSCC.extractDiag(this,output)
    return output
}
fun FMatrixSparseCSC.trace() : Float {return CommonOps_FSCC.trace(this)}

fun FMatrixSparseCSC.normF() : Float {return NormOps_FSCC.normF(this)}

fun FMatrixSparseCSC.toFDRM() : FMatrixRMaj {
    val output = FMatrixRMaj(this.numRows,this.numCols)
    ConvertFMatrixStruct.convert(this,output)
    return output
}


