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

// Copyright (c) 2026 NINOX 360 LLC. Licensed under the BSD 3-Clause; see LICENSE.TXT in project root

rootProject.name = "ejml"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
        maven { url = uri("https://jitpack.io") }
    }
}

include("main:ejml-test", "main:ejml-core","main:ejml-experimental","main:ejml-ddense","main:ejml-zdense",
    "main:ejml-simple","examples","main:ejml-all","main:ejml-fdense","main:ejml-cdense","main:autocode",
    "main:ejml-dsparse","main:ejml-fsparse","main:ejml-kotlin", "main:ejml-java9module",
    "regression")

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
