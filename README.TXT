                 Efficient Java Matrix Library

                         Version 0.12
                         2010 June 26

                    Author: Peter Abeles
                            peter.abeles@gmail.com 


  Project Website: http://code.google.com/p/efficient-java-matrix-library/

==========================================================================
----- Introduction

Efficient Java Matrix Library (EJML) is a linear algebra library for manipulating dense matrices. Its design goals are; 1) to be as computationally efficient as possible for both small and large matrices, and 2) to be accessible to both novices and experts. These goals are accomplished by dynamically selecting the best algorithms to use at runtime and by designing a clean API. EJML is free, written in 100% Java and has been released under an LGPL license.

EJML has three distinct ways to interact with it. This allows a programmer to choose between simplicity and efficiency. 1) A simplified interface that allows a more object oriented way of programming. 2) Letting EJML select the best algorithm to use. 3) Directly calling specialized algorithms. In general EJML is one the fastest single threaded pure Java library. See Java Matrix Benchmark for a detailed comparison of different libraries.

The following functionality is provided:

    * Basic operators (addition, multiplication, ... ).
    * Linear Solvers (batch and incremental).
    * Decompositions (LU, QR, Cholesky, SVD, Eigenvalue).
    * Matrix Features (rank, symmetric, definitiveness, ... ).
    * Random Matrices (covariance, orthogonal, symmetric, ... ). 

Unit tests are extensively used to ensure correctness of each algorithms implementation.  Internal benchmarks and Java Matrix Benchmark are both used to ensure the speed of this library.

==========================================================================
----- Documentation

For a more detailed explanation of how to use the library see:

http://code.google.com/p/efficient-java-matrix-library/wiki/EjmlManual

The JavaDoc has also been posted online at:

http://efficient-java-matrix-library.googlecode.com/svn/javadoc/ver0.12/index.html

==========================================================================
----- Building

The provided ant script is the easiest way to build an EJML jar. In linux it can be built using a command line by changing into the EJML directory and typing "ant". Then you should see text like:


Buildfile: build.xml

clean:
   [delete] Deleting directory ejml/trunk/build

compile:
    [mkdir] Created dir: ejml/trunk/build/classes
    [javac] Compiling 93 source files to ejml/trunk/build/classes

jar:
    [mkdir] Created dir: ejml/trunk/build/jar
      [jar] Building jar: ejml/trunk/build/jar/EJML.jar

To create the JavaDoc simply type "ant javadoc" and "ant test" to run unit tests.

==========================================================================
----- File System

benchmarks/
-  contains code for several speed and stability benchmarks that were used during development.

docs/
- Documentation for this library. This documentation is often out of date and is the best place to get the latest.

examples/
- Contains example code showing how this library can be used to implement a Kalman filter (three different ways) or Levenberg-Marquardt optimization.

experimental/
- Where experimental and most likely buggy code goes that is not ready to be used by most users.

src/
- Source code.

test/
- Unit tests for source code.

change.txt
- History of what changed between each version.

TODO_Algorithms.txt
- Contains a list of what needs to be added to this library.

==========================================================================
----- Questions and Comments 

A public message board has been created for asking questions and making comments:

http://groups.google.com/group/efficient-java-matrix-library-discuss

Bugs can either be posted on that message board or at:

http://code.google.com/p/efficient-java-matrix-library/issues/list


==========================================================================
----- Acknowledgements

I would like to thank all the people have made various comments, suggestions, and reported bugs.

==========================================================================
----- License

EJML is released under the LGPL Version 3
