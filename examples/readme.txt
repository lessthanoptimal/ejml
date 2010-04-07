Here are some examples of common algorithms created using EJML library.  Each example is designed
to provided a practicle example of how the library can be used.

There are two different algorithms provided as examples; Kalman filter
and Levenberg-Marquardt.  The Kalman filter is implemented different ways to show the library
can be used to write code easily and/or very efficiently.  Levenberg-Marquardt shows another
 type of algorithm and how to effectively use reshaping to avoid creating new memory unnecisarily.

-------------------------------

Three different example of how to program a Kalman filter using this library are provided.
One example uses the simplified interface, the other uses generic matrix operation functions,
the last one uses direct calls to an algorithm to speed things up.  These represent three
different levels in terms of coding complexity and performance.

KalmanFilterSimple
- uses the simplified interface.
- easiest to program.
- slowest to run.

KalmanFilterOps
- Uses generic functions.
- Reduces the amount of memory created/destroyed significantly.
- Requires more work to program.
- Runs about 23% faster than the simplified case

KalmanFilterAlg
- Uses a specialized algorithm for performing the matrix inversion operation.
- Requires knowedge of which algorithms to use and how to use them.
- No new memory is created after initialization.
- Runs about 28% faster than the simplified case.

To run the speed benchmark application tell ant to run "kalman-benchmark".

---------------------------------

The provided ant script can compile all the example code and run the unit tests.

- Peter Abeles