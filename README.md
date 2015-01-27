                 Efficient Java Matrix Library
[![Build Status](https://api.travis-ci.org/nknize/ejml.png?branch=master)](https://travis-ci.org/nknize/ejml)

                         Version 0.26
                         September 15, 2014

                    Author: Peter Abeles
                            peter.abeles@gmail.com 

  Project Website: http://code.google.com/p/efficient-java-matrix-library/

==========================================================================
## Introduction

Efficient Java Matrix Library (EJML) is a linear algebra library for manipulating dense matrices. Its design goals are; 1) to be as computationally and memory efficient as possible for both small and large matrices, and 2) to be accessible to both novices and experts.  These goals are accomplished by dynamically selecting the best algorithms to use at runtime and by designing a clean API.  EJML is free, written in 100% Java and has been released under the Apache v2.0 open source license.

EJML has three distinct ways to interact with it. This allows a programmer to choose between simplicity and efficiency. 1) A simplified interface that allows a more object oriented way of programming. 2) Procedural interface that provides complete control over memory, speed, and specific algorithms. 3) Equations which provide a way to integrate symbolic equations similar to Matlab and other Compute Algebra Systems (CAS). In general EJML is one the fastest single threaded pure Java library and has additional optimizations for small matrices. See Java Matrix Benchmark for a detailed comparison of different libraries.

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

http://code.google.com/p/efficient-java-matrix-library/wiki/EjmlManual

The JavaDoc has also been posted online at:

http://ejml.org/javadoc/

==========================================================================
## Central Repository

Instead of including EJML's jars directly you can reference them using Maven's central repository.

If using Maven:
```
<groupId>com.googlecode.efficient-java-matrix-library</groupId>
<artifactId>MODULE</artifactId>
<version>0.26</version>
```
where MODULE is 'core' or 'equation'

If using Gradle:
```
compile group: 'com.googlecode.efficient-java-matrix-library', name: 'core', version: '0.26'
compile group: 'com.googlecode.efficient-java-matrix-library', name: 'equation', version: '0.26'
```
==========================================================================
## Building

Gradle build scripts are provided for building EJML.  If Gradle is installed, then type "gradle createLibraryDirectory" to build the jar files.  They can be found in the ejml/libraries directory.

==========================================================================
## File System


docs/
- Documentation for this library. This documentation is often out of date and online is the best place to get the latest.

examples/
- Contains several examples of how EJML can be used to solve different problems or how EJML can be modified for different applications.

main/core
- Contains all the essential source code for EJML

main/equation
- Contains source code for Equations API

main/experimental/
- Where experimental or alternative approaches and possibly buggy code goes that is not ready to be used by most users.

change.txt
- History of what changed between each version.

TODO_Algorithms.txt
- Contains a list of what needs to be added to this library.

==========================================================================
## Questions and Comments 

A public message board has been created for asking questions and making comments:

http://groups.google.com/group/efficient-java-matrix-library-discuss

Bugs can either be posted on that message board or at:

http://code.google.com/p/efficient-java-matrix-library/issues/list

==========================================================================
## Acknowledgements

I would like to thank all the people have made various comments, suggestions, and reported bugs.  Also David Watkins
for writing "Fundamentals of Matrix Computations", which clearly explains algorithms and yet addresses important
implementation issues.

==========================================================================
## License

EJML is released under the Apache v2.0 open source license
