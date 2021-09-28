/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
 *
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

package org.ejml.data;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementLocation extends EjmlStandardJUnit {
    @Test
    void set_row_col() {
        var loc = new ElementLocation();
        loc.setTo(1, 2);
        assertEquals(1, loc.row);
        assertEquals(2, loc.col);
    }

    @Test
    void set_loc() {
        var loc = new ElementLocation();
        loc.setTo(new ElementLocation(1, 2));
        assertEquals(1, loc.row);
        assertEquals(2, loc.col);
    }
}
