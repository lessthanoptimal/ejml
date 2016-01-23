# Efficient Java Matrix Library

                    Author: Peter Abeles
                            peter.abeles@gmail.com 

#####  Project Website: http://ejml.org

[![Build Status](https://travis-ci.org/lessthanoptimal/ejml.svg?branch=master)](https://travis-ci.org/lessthanoptimal/ejml)

## Introduction

Efficient Java Matrix Library (EJML) is a linear algebra library for manipulating dense matrices. Its design goals are; 1) to be as computationally and memory efficient as possible for both small and large matrices, and 2) to be accessible to both novices and experts. These goals are accomplished by dynamically selecting the best algorithms to use at runtime, clean API, and multiple interfaces. EJML is free, written in 100% Java and has been released under an Apache v2.0 license.

EJML has three distinct ways to interact with it: 1) procedural, 2) SimpleMatrix, and 3) Equations. Procedure provides all capabilities of EJML and almost complete control over memory creation, speed, and specific algorithms. SimpleMatrix provides a simplified subset of the core capabilities in an easy to use flow styled object-oriented API, inspired by Jama. Equations is a symbolic interface, similar in spirit to Matlab and other CAS, that provides a compact way of writing equations.
The following functionality is provided:

* Basic operators (addition, multiplication, ...)
* Matrix Manipulation (extract, insert, combine, ...)
* Linear Solvers (linear, least squares,incremental, ...)
* Decompositions (LU, QR, Cholesky, SVD, Eigenvalue, ...)
* Matrix Features (rank, symmetric, definitiveness, ...)
* Random Matrices (covariance, orthogonal, symmetric, ...)
* Different Internal Formats (row-major, block)
* Unit Testing

Unit tests are extensively used to ensure correctness of each algorithm's implementation.  Internal benchmarks and Java Matrix Benchmark are both used to ensure the speed of this library.

==========================================================================
## Documentation

For a more detailed explanation of how to use the library see:

http://ejml.org/wiki/index.php?title=Manual

The JavaDoc has also been posted online at:

http://ejml.org/javadoc/

==========================================================================
## Including in Gradle and Maven Projects

EJML is on the Maven central repository and can easily be included in projects by adding the following code to the dependency section of your Maven or Gradle project.  This will include all the modules in EJML.

Gradle:
```
compile group: 'org.ejml', name: 'all', version: '0.29'
```

Maven:
```
<groupId>org.ejml</groupId>
<artifactId>all</artifactId>
<version>0.29</version>
```
Or you can include the required modules individually

     Name        |                 Description
-----------------|-------------------------------------------------------
core             | Contains core data structures
dense64          | Algorithms for dense real 64-bit floats
denseC64         | Algorithms for dense complex 64-bit floats
equation         | Equations interface
simple           | Object oriented SimpleMatrix interface

==========================================================================
## Building

Gradle is the official build environment for EJML.  In addition to all the standard commands the following are also available.

* createLibraryDirectory : To build all the modules as jars and save them in ejml/libraries
* oneJar : To compile all the modules into a single jar at ejml/EJML.jar

==========================================================================
## File System

* **docs/** :
         Documentation for this library. This documentation is often out of date and online is the best place to get the latest.
* **examples/** :
         Contains several examples of how EJML can be used to solve different problems or how EJML can be modified for different applications.
* **main/core** :
         Contains all essential data structures
* **main/dense64** :
         Algorithms for real dense 64-bit floating point matrices
* **main/denseC64** :
         Algorithms for complex dense 64-bit floating point matrices
* **main/equation** :
         Contains source code for Equations API
* **main/simple** :
         Contains source code for SimpleMatrix
* **main/experimental/** :
         Where experimental or alternative approaches and possibly buggy code goes that is not ready to be used by most users.
* **change.txt** :
         History of what changed between each version.
* **TODO_Algorithms.txt** :
         Contains a list of what needs to be added to this library.

==========================================================================
## Questions and Comments

A public message board has been created for asking questions and making comments:

http://groups.google.com/group/efficient-java-matrix-library-discuss

Bugs can either be posted on that message board or at:

https://github.com/lessthanoptimal/ejml/issues

==========================================================================
## Acknowledgements

I would like to thank all the people have made various comments, suggestions, and reported bugs.  Also David Watkins
for writing "Fundamentals of Matrix Computations", which clearly explains algorithms and yet addresses important
implementation issues.

==========================================================================
## License

EJML is released under the Apache v2.0 open source license
