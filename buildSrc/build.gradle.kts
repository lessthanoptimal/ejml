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

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}


// While this project is Java 25, we want to build using older versions of Java. Hence, Java 17 here.
kotlin {jvmToolchain(17)}

dependencies {
    implementation("com.peterabeles.gversion:com.peterabeles.gversion.gradle.plugin:1.10.3")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.0.1")
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.4.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
}