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
    F(patA+"64F",patB+"_F64")

def H(patA,patB):
    F(patA+"64F",patB+"_C64")

F("org.ejml.factory.DecompositionFactory","org.ejml.dense.row.factory.DecompositionFactory_DDRM")
F("org.ejml.alg.dense.mult.VectorVectorMult","org.ejml.dense.row.mult.VectorVectorMult_DDRM")
F("org.ejml.factory.LinearSolverFactory","org.ejml.dense.row.factory.LinearSolverFactory_DDRM")
F("org.ejml.ops.CommonOps","org.ejml.dense.row.CommonOps_DDRM")
F("org.ejml.ops.NormOps","org.ejml.dense.row.NormOps_DDRM")
F("org.ejml.ops.SingularOps","org.ejml.dense.row.SingularOps_DDRM")
F("org.ejml.ops.SpecializedOps","org.ejml.dense.row.SpecializedOps_DDRM")
F("org.ejml.ops.MatrixFeatures","org.ejml.dense.row.MatrixFeatures_DDRM")
F("org.ejml.ops.RandomMatrices","org.ejml.dense.row.RandomMatrices_DDRM")
F("org.ejml.alg.dense.linsol.LinearSolverSafe","org.ejml.LinearSolverSafe")
F("org.ejml.alg.dense.mult.MatrixMultProduct","org.ejml.dense.row.mult.MatrixMultProduct_DDRM")
F("org.ejml.interfaces.decomposition.EigenDecomposition","org.ejml.interfaces.decomposition.EigenDecomposition_F64")
F("org.ejml.alg.fixed.FixedOps","org.ejml.dense.fixed.CommonOps_DDF")
F("org.ejml.ops.ConvertMatrixType","org.ejml.ops.ConvertDMatrixStruct")
# F("","")

for n in ["CholeskyDecomposition","EigenDecomposition",
          "CholeskyLDLDecomposition","BidiagonalDecomposition",
          "LUDecomposition","QRPDecomposition",
          "SingularValueDecomposition","TridiagonalSimilarDecomposition"]:
    package_path = "org.ejml.interfaces.decomposition."
    F(package_path+n+";",package_path+n+"_F64;")
    F(n+"<DenseMatrix64F>",n+"_F64<DMatrixRMaj>")

F("CommonOps.","CommonOps_DDRM.")
F("CovarianceOps.","CovarianceOps_DDRM.")
F("EigenOps.","EigenOps_R64.")
F("MatrixFeatures.","MatrixFeatures_DDRM.")
F("NormOps.","NormOps_DDRM.")
F("RandomMatrices.","RandomMatrices_DDRM.")
F("SingularOps.","SingularOps_DDRM.")
F("SpecializedOps.","SpecializedOps_DDRM.")
F("DecompositionFactory.","DecompositionFactory_DDRM.")
F("VectorVectorMult.","VectorVectorMult_DDRM.")
F("LinearSolverFactory.","LinearSolverFactory_DDRM.")
F("MatrixMultProduct.","MatrixMultProduct_DDRM.")
F("MatrixFeatures.","MatrixFeatures_DDRM.")
F("ConvertMatrixType.","ConvertDMatrixStruct.")
F("UtilEjml.parseMatrix","UtilEjml.parse_DDRM")


G("Complex","Complex")
G("ComplexMath","ComplexMath")

F("DenseMatrix64F","DMatrixRMaj")
F("BlockMatrix64F","DMatrixRBlock")

# Random matrix functions that got refactored
F("DDRM.createSymmetric","DDRM.symmetric")
F("DDRM.createRandom","DDRM.rectangle")
F("DDRM.createOrthogonal","DDRM.orthogonal")
F("DDRM.setRandom","DDRM.fillUniform")

for n in range(2,7):
    F("FixedFeatures{:d}".format(n),"MatrixFeatures_DDF{:d}".format(n))
    F("FixedNormOps{:d}".format(n),"NormOps_DDF{:d}".format(n))
    F("FixedOps{:d}".format(n),"CommonOps_DDF{:d}".format(n))
    F("FixedMatrix{:d}x{:d}_64F".format(n,n),"DMatrix{:d}x{:d}".format(n,n))
    F("FixedMatrix{:d}_64F".format(n,n),"DMatrix{:d}".format(n,n))


print "Finished!"
