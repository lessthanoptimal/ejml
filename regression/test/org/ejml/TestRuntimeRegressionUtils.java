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

package org.ejml;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestRuntimeRegressionUtils {
    @Test void encode_decode_allBenchmarks() {
        Map<String, Double> expected = new HashMap<>();
        expected.put("asfd.asfd.sfdf,foo:100,bar:12.83", 99.0);

        String encoded = RuntimeRegressionUtils.encodeAllBenchmarks(expected);

        Map<String, Double> found = RuntimeRegressionUtils.loadAllBenchmarks(
                new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected.size(), found.size());

        for (var e : expected.entrySet()) {
            assertEquals(e.getValue(), found.get(e.getKey()));
        }
    }
}
