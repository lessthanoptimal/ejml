/*
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.ops;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestReadMatrixCsv {
    /**
     * Make sure incorrectly formatted data is handled gracefully
     */
    @Test(expected=IOException.class)
    public void bad_matrix_row() throws IOException {
        String s = "3 2\n0 0\n1 1";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }

    @Test(expected=IOException.class)
    public void bad_matrix_col() throws IOException {
        String s = "3 2\n0 0\n1\n0 3";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }
}
