/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

plugins {
    id("ejml.libs-conventions")
}

dependencies {
    api(project(":main:ejml-core"))
    api(project(":main:ejml-fdense"))
    api(project(":main:ejml-ddense"))
    api(project(":main:ejml-cdense"))
    api(project(":main:ejml-zdense"))
    api(project(":main:ejml-dsparse"))
    api(project(":main:ejml-fsparse"))
    testImplementation(project(":main:ejml-experimental"))
    testImplementation(project(":main:ejml-test"))
}