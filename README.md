# Efficient Java Matrix Library

                    Author: Peter Abeles
                            peter.abeles@gmail.com 

#####  Project Website: http://ejml.org

[![Build Status](https://github.com/lessthanoptimal/ejml//workflows/SNAPSHOT/badge.svg)](https://github.com/lessthanoptimal/ejml/actions?query=workflow%3A%22SNAPSHOT%22)

## Introduction

Efficient Java Matrix Library (EJML) is a linear algebra library for manipulating real/complex/dense/sparse matrices. Its design goals are; 1) to be as computationally and memory efficient as possible for both small and large matrices, and 2) to be accessible to both novices and experts. These goals are accomplished by dynamically selecting the best algorithms to use at runtime, clean API, and multiple interfaces. EJML is free, written in 100% Java and has been released under an Apache v2.0 license.

EJML has three distinct ways to interact with it: 1) Operations, 2) SimpleMatrix, and 3) Equations. Operations provides all capabilities of EJML and almost complete control over memory creation, speed, and specific algorithms with a procedural API. SimpleMatrix provides a simplified subset of the core capabilities in an easy to use flow styled object-oriented API, inspired by Jama. Equations is a symbolic interface, similar in spirit to Matlab and other CAS, that provides a compact way of writing equations.
The following functionality is provided:

* Basic Operators (addition, multiplication, ...)
* Matrix Manipulation (extract, insert, combine, ...)
* Linear Solvers (linear, least squares,incremental, ...)
* Decompositions (LU, QR, Cholesky, SVD, Eigenvalue, ...)
* Matrix Features (rank, symmetric, definitiveness, ...)
* Random Matrices (covariance, orthogonal, symmetric, ...)
* Different Internal Formats (row-major, block, sparse, ...)
* Graph BLAS (Semirings)
* Single Thread and Concurrent Implementations
* Unit Testing
* Kotlin Extensions

Unit tests are extensively used to ensure correctness of each algorithm's implementation.  Internal benchmarks and [Java Matrix Benchmark](https://lessthanoptimal.github.io/Java-Matrix-Benchmark/) are both used to ensure the speed of this library.

---------------------------------------------------------------------------
## Documentation

For a more detailed explanation of how to use the library see:

http://ejml.org/wiki/index.php?title=Manual

The JavaDoc has also been posted online at:

http://ejml.org/javadoc/

---------------------------------------------------------------------------
## Maven Central

EJML is in Maven central repository and can easily be added to Gradle, Maven, and similar project managers.

```
<groupId>org.ejml</groupId>
<artifactId>ejml-all</artifactId>
<version>0.41</version>
```

This will add the entire library.  Alternatively, you can include the required modules individually:

|     Name         |                 Description                           
|------------------|-------------------------------------------------------
| ejml-core        | Contains core data structures and common code
| ejml-fdense      | Algorithms for dense real 32-bit floats
| ejml-ddense      | Algorithms for dense real 64-bit floats
| ejml-cdense      | Algorithms for dense complex 32-bit floats
| ejml-zdense      | Algorithms for dense complex 64-bit floats
| ejml-fsparse     | Algorithms for sparse real 32-bit floats
| ejml-dsparse     | Algorithms for sparse real 64-bit floats
| ejml-simple      | Object oriented SimpleMatrix and Equations interfaces

---------------------------------------------------------------------------

## Building

Unless you need a bleeding edge new feature or are contributing to EJML you probably don't need to build it yourself
since pre-build jars are readily available on Maven Central.  Gradle is the official tool environment for EJML. Java 11
or higher is required to build EJML, but it will generate Java 8 (a.k.a. Java 1.8) byte code. This is because it uses
a few recent language features.

To build EJML from the command line use the following commands. These will generate all the source code and install
it in your local Maven repository. 
```bash
cd ejml
./gradlew autogenerate
./gradlew publishToMavenLocal
```

Here are a few other useful Gradle commands:

* createLibraryDirectory : To build all the modules as jars and save them in ejml/libraries
* oneJar : To compile all the modules into a single jar at ejml/EJML.jar

---

## File System

* **docs/** :
         Documentation for this library. This documentation is often out of date and online is the best place to get the latest.
* **examples/** :
         Contains several examples of how EJML can be used to solve different problems or how EJML can be modified for different applications.
* **main/** :
         Library source code
* **change.txt** :
         History of what changed between each version.

---------------------------------------------------------------------------

# Procedural, SimpleMatrix, and Equations API

EJML provides three different ways to access the library.  This lets the user trade off ease of use for control/complexity.  An example of each is shown below.  All of which implement Kalman gain function:

[Procedural](http://ejml.org/wiki/index.php?title=Procedural)
```
mult(H,P,c);
multTransB(c,H,S);
addEquals(S,R);
if( !invert(S,S_inv) ) throw new RuntimeException("Invert failed");
multTransA(H,S_inv,d);
mult(P,d,K);
```

[SimpleMatrix](http://ejml.org/wiki/index.php?title=SimpleMatrix)
```
SimpleMatrix S = H.mult(P).mult(H.transpose()).plus(R);
SimpleMatrix K = P.mult(H.transpose().mult(S.invert()));
```

[Equations](http://ejml.org/wiki/index.php?title=Equations)
```
eq.process("K = P*H'*inv( H*P*H' + R )");
```

---------------------------------------------------------------------------

## Procedural API: Matrix and Class Names

EJML supports a variety of different matrix types and uses the following pattern for matrix class names:

```
Patterns:

<data type>Matrix<structure>
<data type>MatrixSparse<structure>

Description:

<data type> is a single character
  'D' for real double 
  'F' for real float 
  'Z' for complex double
  'C' for complex float
  'B' for binary
<structure> is the internal data structure's name, see below.

Matrix Suffix   Abbreviation   Description
=========================================================================
   RMaj            RM         dense row-major
   RBlock          RB         dense block row-major
   NxN             FN         dense fixed sized matrix of size N
   N               FN         dense fixed sized vector of length N  
   CSC             CC         compressed sparse column
   Triplet         TR         sparse triplet
=========================================================================

Examples:

  DMatrixRMaj         double real dense row-major matrix
  CMatrixRMaj         float complex dense row-major matrix
  ZMatrixSparseCSC    double complex sparse CSC matrix
  
  CommonOps_DDRM      Operations on DMatrixRMaj
  CommonOps_DSCC      Operations on DMatrixSparseCSC
```

Algorithms which operate on a specific matrix type have a suffix that's 5 characters, e.g. _DDRM.  The first letter 'D' is the data type, the second letter 'D' is for dense (sparse is 'S'), and the last two letters are an abbreviation for the structure.

---------------------------------------------------------------------------
## Questions and Comments

A public message board has been created for asking questions and making comments:

http://groups.google.com/group/efficient-java-matrix-library-discuss

Bugs can either be posted on that message board or at:

https://github.com/lessthanoptimal/ejml/issues

---------------------------------------------------------------------------
## Acknowledgements

I would like to thank all the people have made various comments, suggestions, and reported bugs.  Also David Watkins
for writing "Fundamentals of Matrix Computations", which clearly explains algorithms and yet addresses important
implementation issues.  Timothy A. Davis for his book "Direct Methods for Sparse Linear Systems" and for CSparse
which provided the initial seed for the sparse algorithms.

---------------------------------------------------------------------------
## License

EJML is released under the Apache v2.0 open source license
