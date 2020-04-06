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
import org.ejml.ops.ConvertMatrixData

fun DMatrixSparseCSC.toFSCC() : FMatrixSparseCSC {
    val output = FMatrixSparseCSC(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}

fun FMatrixSparseCSC.toDSCC() : DMatrixSparseCSC {
    val output = DMatrixSparseCSC(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}

fun DMatrixRMaj.toFDRM() : FMatrixRMaj {
    val output = FMatrixRMaj(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}

fun FMatrixRMaj.toFDRM() : DMatrixRMaj {
    val output = DMatrixRMaj(this.numRows,this.numCols)
    ConvertMatrixData.convert(this,output)
    return output
}