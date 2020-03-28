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
import org.ejml.data.FMatrixRMaj
import org.ejml.data.FMatrixSparseCSC
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.NormOps_DDRM
import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.ops.ConvertFMatrixStruct
import org.ejml.ops.ConvertMatrixData
import org.ejml.ops.MatrixIO
import org.ejml.sparse.csc.CommonOps_DSCC
import org.ejml.sparse.csc.CommonOps_FSCC
import org.ejml.sparse.csc.NormOps_DSCC
import org.ejml.sparse.csc.NormOps_FSCC

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

operator fun DMatrixRMaj.minus(a : Double) : DMatrixRMaj {
    val out = DMatrixRMaj(1,1);
    CommonOps_DDRM.subtract(this,a, out)
    return out
}

operator fun DMatrixRMaj.minusAssign(a : Double) {CommonOps_DDRM.subtract(this,a, this)}

fun DMatrixRMaj.transpose() : DMatrixRMaj {
    return CommonOps_DDRM.transpose(this,DMatrixRMaj(1,1))
}
fun DMatrixRMaj.diag() : DMatrixRMaj {
    val output = DMatrixRMaj(1,1)
    CommonOps_DDRM.extractDiag(this,output)
    return output
}
fun DMatrixRMaj.trace() : Double {return CommonOps_DDRM.trace(this)}

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

fun DMatrixRMaj.normF() : Double {return NormOps_DDRM.normF(this)}
fun DMatrixRMaj.normP( p : Double ) : Double {return NormOps_DDRM.normP(this,p)}
fun DMatrixRMaj.normP1() : Double {return NormOps_DDRM.normP1(this)}
fun DMatrixRMaj.normP2() : Double {return NormOps_DDRM.normP2(this)}
fun DMatrixRMaj.normPInf() : Double {return NormOps_DDRM.normPInf(this)}

fun String.toDDRM() : DMatrixRMaj {
    return MatrixIO.matlabToDDRM(this)
}

fun DMatrixRMaj.toDSCC() : DMatrixSparseCSC {
    val output = DMatrixSparseCSC(this.numRows,this.numCols)
    ConvertDMatrixStruct.convert(this,output)
    return output
}
fun DMatrixRMaj.toFDRM() : FMatrixRMaj {
    val output = FMatrixRMaj(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}

//----------------------- FDRM ------------------------------------------------------

//----------------------- DSCC ------------------------------------------------------

operator fun DMatrixSparseCSC.times(a : DMatrixSparseCSC) : DMatrixSparseCSC {
    val out = DMatrixSparseCSC(1,1)
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

fun DMatrixSparseCSC.toDDRM() : DMatrixRMaj {
    val output = DMatrixRMaj(this.numRows,this.numCols)
    ConvertDMatrixStruct.convert(this,output)
    return output
}

fun DMatrixSparseCSC.toFSCC() : FMatrixSparseCSC {
    val output = FMatrixSparseCSC(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}

//----------------------- FSCC ------------------------------------------------------

operator fun FMatrixSparseCSC.times(a : FMatrixSparseCSC) : FMatrixSparseCSC {
    val out = FMatrixSparseCSC(1,1)
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

fun FMatrixSparseCSC.toDDRM() : FMatrixRMaj {
    val output = FMatrixRMaj(this.numRows,this.numCols)
    ConvertFMatrixStruct.convert(this,output)
    return output
}

fun FMatrixSparseCSC.toDSCC() : DMatrixSparseCSC {
    val output = DMatrixSparseCSC(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}