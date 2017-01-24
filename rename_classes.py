#!/usr/bin/python

import fnmatch
import os
import sys
from subprocess import call

def git_rename_files(directory, find, replace):
    changed = 0
    for path, dirs, files in os.walk(os.path.abspath(directory)):
        for filename in fnmatch.filter(files, "*"+find+"*.java"):
            changed += 1
            origpath = os.path.join(path, filename)
            modpath = origpath.replace(find,replace)
            # print filename
            call(['git','mv',origpath,modpath])
    print "changed {} files".format(changed)

if len(sys.argv) < 2:
    print "Need to specify where to apply the script to"
    os.exit(0)

location = sys.argv[1]

print "Recursively apply search and replace to "+location

git_rename_files(location,"DMatrixRow_C32","CMatrixRMaj")
git_rename_files(location,"DMatrixRow_C64","ZMatrixRMaj")
git_rename_files(location,"DMatrixRow_F32","FMatrixRMaj")
git_rename_files(location,"DMatrixRow_F64","DMatrixRMaj")

git_rename_files(location,"DMatrixBlock_F32","FMatrixRBlock")
git_rename_files(location,"DMatrixBlock_F64","DMatrixRBlock")

git_rename_files(location,"D1Matrix_C32","CMatrixD1")
git_rename_files(location,"D1Matrix_C64","ZMatrixD1")
git_rename_files(location,"D1Matrix_F32","FMatrixD1")
git_rename_files(location,"D1Matrix_F64","DMatrixD1")

git_rename_files(location,"D1Submatrix_F32","FSubmatrixD1")
git_rename_files(location,"D1Submatrix_F64","DSubmatrixD1")

git_rename_files(location,"D1Martix_F32","FMatrixD1")
git_rename_files(location,"D1Martix_F64","DMatrixD1")

git_rename_files(location,"DMatrixFixed_F32","FMatrixFixed")
git_rename_files(location,"DMatrixFixed_F64","DMatrixFixed")

git_rename_files(location,"DMatrixRow_B","BMatrixRMaj")

git_rename_files(location,"Matrix_C32","CMatrix")
git_rename_files(location,"Matrix_C64","ZMatrix")
git_rename_files(location,"Matrix_F32","FMatrix")
git_rename_files(location,"Matrix_F64","DMatrix")

git_rename_files(location,"_R64","_DDRM")
git_rename_files(location,"_CR32","_CDRM")
git_rename_files(location,"_CR64","_ZDRM")
git_rename_files(location,"_O64","_DSCC")
git_rename_files(location,"_B64","_DDRB")

for n in range(2,7):
    git_rename_files(location,"FixedFeatures{:d}_F64".format(n),"MatrixFeatures_DDF{:d}".format(n))
    git_rename_files(location,"FixedNormOps{:d}_F64".format(n),"NormOps_DDF{:d}".format(n))
    git_rename_files(location,"FixedOps{:d}_F64".format(n),"CommonOps_DDF{:d}".format(n))
    git_rename_files(location,"DMatrixFixed{:d}x{:d}_F32".format(n,n),"FMatrix{:d}x{:d}".format(n,n))
    git_rename_files(location,"DMatrixFixed{:d}_F32".format(n,n),"FMatrix{:d}".format(n,n))
    git_rename_files(location,"DMatrixFixed{:d}x{:d}_F64".format(n,n),"DMatrix{:d}x{:d}".format(n,n))
    git_rename_files(location,"DMatrixFixed{:d}_F64".format(n,n),"DMatrix{:d}".format(n,n))


print "Finished!"
