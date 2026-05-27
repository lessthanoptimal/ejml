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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("ejml.libs-conventions")
    kotlin("jvm")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:${Versions.JETBRAINS_ANNOTATIONS}")
        force("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.KOTLIN}")
    }
}

listOf(configurations.annotationProcessor, configurations.testAnnotationProcessor).forEach { config ->
    config.configure {
        resolutionStrategy {
            force("com.google.code.findbugs:jsr305:${Versions.JSR305}")
            force("com.google.errorprone:error_prone_annotations:${Versions.ERRORPRONE_CORE}")
            force("com.google.guava:guava:${Versions.GUAVA}")
            force("org.checkerframework:checker-qual:2.10.0")
        }
    }
}

dependencies {
    api(project(":main:ejml-all"))
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}