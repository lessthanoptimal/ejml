#!/usr/bin/python

import fnmatch
import os
import sys


def findReplace(directory, find, replace, filePattern):
    changed = 0
    examined = 0
    for path, dirs, files in os.walk(os.path.abspath(directory)):
        for filename in fnmatch.filter(files, filePattern):
            examined += 1
            filepath = os.path.join(path, filename)
            with open(filepath) as f:
                s = f.read()
            c = s.replace(find, replace)
            if s != c:
                changed += 1
                with open(filepath, "w") as f:
                    f.write(c)
    if changed > 0:
        print "changed {:4d} examined {:4d} {:s} -> {:s}".format(changed,examined,find,replace)

if len(sys.argv) < 2:
    print "Need to specify where to apply the script to"
    exit(0)

location = sys.argv[1]

print "Recursively apply search and replace to "+location

def F(find,replace):
    findReplace(location,find,replace,"*.java")

def G(patA,patB):
    F(patA+"32F",patB+"_F32")
    F(patA+"64F",patB+"_F64")

def H(patA,patB):
    F(patA+"32F",patB+"_C32")
    F(patA+"64F",patB+"_C64")

# G("DenseMatrix","DMatrixRow")
# G("BlockMatrix","DMatrixBlock")
# G("EigenPair","EigenPair")
# G("Complex","Complex")
# G("ComplexPolar","ComplexPolar")
# G("ComplexMath","ComplexMath")
# H("CDenseMatrix","DMatrixRow")
# H("ComplexMatrix","Matrix")
#
# F("DenseMatrixBool","DMatrixRow_B")
#
# for n in range(2,7):
#     suf1 = str(n)
#     suf2 = str(n)+"x"+str(n)
#
#     F("FixedMatrix"+suf1+"_64F","DMatrixFixed"+suf1+"_F64")
#     F("FixedMatrix"+suf2+"_64F","DMatrixFixed"+suf2+"_F64")
#     F("FixedMatrix"+suf1+"_32F","DMatrixFixed"+suf1+"_F32")
#     F("FixedMatrix"+suf2+"_32F","DMatrixFixed"+suf2+"_F32")
#     F("FixedOps"+suf1+"\.","FixedOps"+suf1+"_F64\.")
#
# F("_D64","_R64")
# F("_D32","_R32")
# F("_CD64","_CR64")
# F("_CD32","_CR32")
#
# F("CommonOps\.","CommonOps_R64\.")
# F("CovarianceOps\.","CovarianceOps_R64\.")
# F("EigenOps\.","EigenOps_R64\.")
# F("MatrixFeatures\.","MatrixFeatures_R64\.")
# F("NormOps\.","NormOps_R64\.")
# F("RandomMatrices\.","RandomMatrices_R64\.")
# F("SingularOps\.","SingularOps_R64\.")
# F("SpecializedOps\.","SpecializedOps_R64\.")

F("org.ejml.ops.CommonOps_R64","org.ejml.dense.row.CommonOps_DDRM")
F("org.ejml.ops.CommonOps_R32","org.ejml.dense.row.CommonOps_DFRM")
F("org.ejml.ops.MatrixFeatures_R64","org.ejml.dense.row.MatrixFeatures_DDRM")
F("org.ejml.ops.RandomMatrices_R64","org.ejml.dense.row.RandomMatrices_DDRM")
F("org.ejml.alg.dense.mult.MatrixMultProduct_R64","org.ejml.dense.row.mult.MatrixMultProduct_DDRM")
F("org.ejml.alg.dense.mult.VectorVectorMult_R64","org.ejml.dense.row.mult.VectorVectorMult_DDRM")
F("org.ejml.factory.LinearSolverFactory_R64","org.ejml.dense.row.factory.LinearSolverFactory_DDRM")
F("org.ejml.alg.dense.linsol.LinearSolverSafe","org.ejml.LinearSolverSafe")
F("org.ejml.ops.NormOps_R64","org.ejml.dense.row.NormOps_DDRM")
F("org.ejml.ops.SpecializedOps_R64","org.ejml.dense.row.SpecializedOps_DDRM")
F("org.ejml.factory.DecompositionFactory_R64","org.ejml.dense.row.factory.DecompositionFactory_DDRM")
F("org.ejml.alg.fixed","org.ejml.dense.fixed")
F("org.ejml.ops.SingularOps_R64","org.ejml.dense.row.SingularOps_DDRM")
F("org.ejml.alg.dense.decomposition.svd","org.ejml.dense.row.decomposition.svd")
F("DMatrixRow_C32","CMatrixRMaj")
F("DMatrixRow_C64","ZMatrixRMaj")
F("RowMatrix_F32","FMatrixRMaj")
F("RowMatrix_F64","DMatrixRMaj")
F("ConvertMatrixStruct_F32","ConvertFMatrixStruct")
F("ConvertMatrixStruct_F64","ConvertDMatrixStruct")

F("DMatrixBlock_F32","FMatrixRBlock")
F("DMatrixBlock_F64","DMatrixRBlock")

F("D1Matrix_C32","CMatrixD1")
F("D1Matrix_C64","ZMatrixD1")
F("D1Matrix_F32","FMatrixD1")
F("D1Matrix_F64","DMatrixD1")

F("D1Submatrix_F32","FSubmatrixD1")
F("D1Submatrix_F64","DSubmatrixD1")

F("D1Martix_F32","FMatrixD1")
F("D1Martix_F64","DMatrixD1")

F("DMatrixFixed_F32","FMatrixFixed")
F("DMatrixFixed_F64","DMatrixFixed")

F("DMatrixRow_B","BMatrixRMaj")

F("Matrix_C32","CMatrix")
F("Matrix_C64","ZMatrix")
F("Matrix_F32","FMatrix")
F("Matrix_F64","DMatrix")

F("_R32","_FDRM")
F("_R64","_DDRM")
F("_CR64","_ZDRM")
F("_CR32","_FDRM")
F("_O64","_DSCC")
F("_B64","_DDRB")

F("DDRM.createRandom","DDRM.rectangle")
F("DDRM.createSymmetric","DDRM.symmetric")
F("DDRM.createOrthogonal","DDRM.orthogonal")

for n in range(2,7):
    F("FixedFeatures{:d}_F64".format(n),"MatrixFeatures_DDF{:d}".format(n))
    F("FixedNormOps{:d}_F64".format(n),"NormOps_DDF{:d}".format(n))
    F("FixedOps{:d}_F64".format(n),"CommonOps_DDF{:d}".format(n))
    F("FixedMatrix{:d}x{:d}_F32".format(n,n),"FMatrix{:d}x{:d}".format(n,n))
    F("FixedMatrix{:d}_F32".format(n,n),"FMatrix{:d}".format(n,n))
    F("FixedMatrix{:d}x{:d}_F64".format(n,n),"DMatrix{:d}x{:d}".format(n,n))
    F("FixedMatrix{:d}_F64".format(n,n),"DMatrix{:d}".format(n,n))


print "Finished!"
