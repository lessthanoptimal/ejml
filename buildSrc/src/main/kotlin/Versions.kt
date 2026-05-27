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

object Versions {
    const val BYTEBUDDY = "1.17.7"
    const val COMMONS_IO = "2.16.1"
    const val KOTLIN = "2.3.0"
    const val LOMBOK = "1.18.42"
    const val JABEL = "1.0.1-1"
    const val GUAVA = "33.2.1-jre"
    const val JUNIT = "5.11.4"
    const val JUNIT_PLATFORM = "1.11.4"  // Aligns with Jupiter 5.11.4
    const val ERRORPRONE_CORE = "2.41.0"
    const val NULLAWAY = "0.12.10"
    const val AUTO64TO32 = "3.3.0"
    const val JMH = "1.36"
    const val JETBRAINS_ANNOTATIONS = "23.0.0"
    const val JSR250 = "1.0"
    const val JSR305 = "3.0.2"
}

object Libs {
    const val AUTOFLOAT = "com.peterabeles:autofloat:${Versions.AUTO64TO32}"
    const val AUTOCONCURRENT = "com.peterabeles:autoconcurrent:${Versions.AUTO64TO32}"
    const val COMMONS_IO = "commons-io:commons-io:${Versions.COMMONS_IO}"
    const val LOMBOK = "org.projectlombok:lombok:${Versions.LOMBOK}"
    const val JABEL = "com.pkware.jabel:jabel-javac-plugin:${Versions.JABEL}"
    const val JUNIT_API = "org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}"
    const val JUNIT_PARAMS = "org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}"
    const val JUNIT_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}"
    const val JUNIT_LAUNCHER = "org.junit.platform:junit-platform-launcher:${Versions.JUNIT_PLATFORM}"
    const val ERRORPRONE_CORE = "com.google.errorprone:error_prone_core:${Versions.ERRORPRONE_CORE}"
    const val NULLAWAY = "com.uber.nullaway:nullaway:${Versions.NULLAWAY}"
    const val JMH_CORE = "org.openjdk.jmh:jmh-core:${Versions.JMH}"
    const val JMH_ANNPROCESS = "org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}"
    const val JETBRAINS_ANNOTATIONS = "org.jetbrains:annotations:${Versions.JETBRAINS_ANNOTATIONS}"
    const val JSR250 = "javax.annotation:jsr250-api:${Versions.JSR250}"
    const val JSR305 = "com.google.code.findbugs:jsr305:${Versions.JSR305}"
    const val GUAVA = "com.google.guava:guava:${Versions.GUAVA}"
    const val BYTEBUDDY = "net.bytebuddy:byte-buddy:${Versions.BYTEBUDDY}"
    const val BYTEBUDDY_AGENT = "net.bytebuddy:byte-buddy-agent:${Versions.BYTEBUDDY}"
    const val MFL_EJML = "us.hebi.matlab.mat:mfl-ejml:0.5.7"
}