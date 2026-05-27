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
    id("ejml.java-conventions")
    id("com.peterabeles.gversion")
}

dependencies {
    implementation(Libs.AUTOFLOAT)
    implementation(Libs.AUTOCONCURRENT)
}

gversion {
    srcDir = "../ejml-core/src"
    classPackage = "org.ejml"
    className = "EjmlVersion"
    language = "java"
    indent = "    "
    annotate = true
}

val autogenerate by tasks.registering(JavaExec::class) {
    dependsOn("classes")
    mainClass.set("org.ejml.MasterCodeGenerator")
    classpath = sourceSets.main.get().runtimeClasspath
}

// Create the EjmlVersion file only when the autogenerate command is called. This speeds up build time significantly.
autogenerate { dependsOn("createVersionFile") }