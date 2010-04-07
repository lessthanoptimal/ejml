                 Efficient Java Matrix Library
                         Version 0.3
                        2009 August 26
----- Introduction

Efficient Java Matrix Library (EJML) is a linear algebra library for manipulating dense matrices. Its design goals are; 1) to be as computationally efficient as possible, and 2) to be accessible to both novices and experts.  It is 100% Java with no native code required.

EJML is designed from the ground up for speed.  One way this is accomplished is by minimizing the overhead.  For example, unlike many other Java matrix libraries, there is very little abstraction.  This does reduce the flexibility, but removes a lot of overhead.  It also includes multiple implementations of the same algorithms that are optimal under different conditions and selects the appropriate one at runtime.  An effort was made to improve the speed of both small and large matrices.  There are  situations where it is ideal to predeclared all data.  In EJML it is possible to do so, with no hidden memory being created inside of algorithms.

A benchmark package (jmatbench) has been created to compare the speed of EJML to other popular matrix libraries.  Benchmarking the performance of an algorithm in Java is a challenge due to the large variance in runtime each time the code is invoked and large differences in performance between different versions of the Java VM.  EJML has been benchmarked against the Jama and MTJ libraries.  For small matrices it is the clear winner running between 5 and 150 times faster depending on the operation.  For large matrices it is effectively a statistical tie but runs about 20% faster on average.

There is always a trade off between simplicity and elegance of design and computational efficiency.  EJML attempts to handle this issue by providing different ways to interface with this library; simple, operators, and algorithmic.  Simple provides a wrapper over the code that is designed to make the code more readable, easier to learn, much less tedious to write, and accessible to more people.  The operator interface provides a set of functions/operators that provide much more control to the developer than the simplified interface.  The selection of individual algorithms is hidden from the developer and depending on the matrix provided different algorithms are called.  The algorithmic way of interacting with the library requires calling the specific algorithms of interest directly.  This can lead to significant speed improvement by taking advantage of more knowledge of the problem.

EJML has no external dependencies.  It does use junit 4.x for its unit tests, but nothing else is dependent on it.  Design wise the simplified interface was inspired from Jama and the operations interface from MTJ.

The author of this library is Peter Abeles and can be contacted at peter.abeles@gmail.com.

----- Building

To build the library simply invoke the default ant script in the ejml directory.  The compiled jar file will be included in the build/jar/ directory after it has finished running.

----- Functionality

This library can perform basic linear algebra operations (addition, multiplication, ... etc), more advanced (inverse, solve, ... etc), and has the following algorithms:

- LU decomposition
- Cholesky decomposition
- Gauss Jordan
- Singular Value Decomposition

The following are going to be added in the future:
- Eigenvalues decomposition
- Fast matrix matrix multiplication for large matrices.

Specialized algorithms for small matrices are available that run much faster than the general purpose ones.  Additional algorithms are also provided but are primarily used to test the correctness of the other algorithms.

----- File System

benchmarks/
-  contains code for several speed and stability benchmarks that were used during development.

docs/
- Documentation for this library.

examples/
- Contains example code showing how this library can be used to implement a Kalman filter (three different ways) or Levenberg-Marquardt optimization.

src/
- Is where the source code for the library is contained.

test/
- Is where the unit test source code is contained.

----- License

This library is released under the LGPL Version 3
