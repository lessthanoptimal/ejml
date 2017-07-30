* cs_sparse algorithms do not appear to be scale invariant.
  * Create a numerical stability test
  * Might need to explain to people what's going on too
  * Benchmark to see how much scale invariance hurts performance
* Compare QR algorithm with each other.
  * Synchronize documentation and use same variable names
  * createHouseholder in sparse code doesn't appear to be the same
* LU can only handle square matrices. 
  * Make it able to handle wide and tall.
  * Update unit test