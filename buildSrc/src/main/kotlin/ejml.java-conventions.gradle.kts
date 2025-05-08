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

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    eclipse
    id("com.peterabeles.gversion")
    id("net.ltgt.errorprone")
    id("com.diffplug.spotless")
}

group = "org.ejml"
version = "0.45.2-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        // Specify vendor to get around a GitHub Java 25 issue
        vendor.set(JvmVendorSpec.AZUL)
    }
}

// EJML uses a non-standard layout: src/, test/, resources/src, resources/test,
// plus custom `generate` and `benchmarks` source sets.
sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("resources/src")
    }
    test {
        java.srcDirs("test")
        resources.srcDirs("resources/test")
    }
    create("generate") {
        java.srcDirs("generate")
    }
    create("benchmarks") {
        java.srcDirs("benchmarks/src")
        resources.srcDirs("benchmarks/resources")
    }
}

// Expose benchmarks source set output for sibling modules (mainly :regression)
val benchmarksOutput by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
sourceSets["benchmarks"].output.classesDirs.forEach { dir ->
    artifacts.add("benchmarksOutput", dir) {
        builtBy(tasks.named("benchmarksClasses"))
    }
}

// Expose test source set output for sibling modules that need test fixtures
val testOutput by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
sourceSets["test"].output.classesDirs.forEach { dir ->
    artifacts.add("testOutput", dir) {
        builtBy(tasks.named("testClasses"))
    }
}

// Prevents tons of errors if someone is using ASCII
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Produces Java 11 bytecode but accepts Java 25 syntax (via Jabel)
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "25"
    options.release.set(11)
}

// Needed by ErrorProne under Java 25
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.removeAll { it.startsWith("--should-stop=ifError") }
    options.compilerArgs.add("--should-stop=ifError=FLOW")
}

// Fail on jar conflict + force specific versions
configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

        force("org.jetbrains:annotations:${Versions.JETBRAINS_ANNOTATIONS}")
        force("com.google.guava:guava:${Versions.GUAVA}")
        force("com.google.errorprone:error_prone_annotations:${Versions.ERRORPRONE_CORE}")
        force("com.google.code.findbugs:jsr305:${Versions.JSR305}")
        force("org.checkerframework:checker-qual:2.10.0")

        // Needed for Java 25 + Jabel
        force("net.bytebuddy:byte-buddy:${Versions.BYTEBUDDY}")
        force("net.bytebuddy:byte-buddy-agent:${Versions.BYTEBUDDY}")
    }
}

dependencies {
    compileOnly(Libs.MFL_EJML)
    compileOnly(Libs.LOMBOK)
    compileOnly(Libs.JETBRAINS_ANNOTATIONS) // @Nullable
    compileOnly(Libs.JSR250)                // @Generated

    // Tests can see main's compile-only dependencies
    testCompileOnly(files(sourceSets.main.get().compileClasspath))

    testImplementation(Libs.JUNIT_API)
    testImplementation(Libs.JUNIT_PARAMS)
    testRuntimeOnly(Libs.JUNIT_ENGINE)
    testRuntimeOnly(Libs.JUNIT_LAUNCHER)

    // The `generate` source set needs main's compile classpath plus the autocode module
    "generateCompileOnly"(files(sourceSets.main.get().compileClasspath))
    "generateImplementation"(project(":main:autocode"))

    annotationProcessor(Libs.JABEL)
    testAnnotationProcessor(Libs.JABEL)

    annotationProcessor(Libs.LOMBOK) // @Getter @Setter

    errorprone(Libs.ERRORPRONE_CORE)

    // Benchmarks need main's runtime + compile classpaths
    "benchmarksImplementation"(files(sourceSets.main.get().runtimeClasspath))
    "benchmarksImplementation"(files(sourceSets.main.get().compileClasspath))
    "benchmarksImplementation"(Libs.JMH_CORE)
    "benchmarksAnnotationProcessor"(Libs.JMH_ANNPROCESS)

    // NullAway is consumed via ErrorProne but must be on the annotation processor path
    annotationProcessor(Libs.NULLAWAY)
    testAnnotationProcessor(Libs.NULLAWAY)
    "benchmarksAnnotationProcessor"(Libs.NULLAWAY)
    "generateAnnotationProcessor"(Libs.NULLAWAY)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    reports.html.required.set(false)
    testLogging {
        showStandardStreams = true                 // Print stdout making debugging easier
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).apply {
        links("https://docs.oracle.com/en/java/javase/11/docs/api/")
    }
    isFailOnError = false
    // Disable to stop it from spamming stdout on snapshot builds and speed them up
    enabled = !project.version.toString().contains("SNAPSHOT")
}

// ErrorProne configuration. Disabled for benchmarks, examples, regression, and tests.
// InconsistentCapitalization is disabled because in math, capital letters denote matrices and
// lowercase denote vectors or scalars — a verbose alternative isn't worth the noise.
tasks.withType<JavaCompile>().configureEach {
    options.errorprone.isEnabled.set(false)

    val pathStr = path
    val nameStr = name
    val skip = pathStr.contains("Benchmarks") ||
            pathStr.contains("examples") ||
            pathStr.contains("regression") ||
            nameStr.contains("Test") ||
            nameStr == "compileModuleInfo" // need for java9module

    if (skip)
        return@configureEach

    options.errorprone.isEnabled.set(true)
    options.errorprone.disableWarningsInGeneratedCode.set(true)
    options.errorprone.disable(
        "TypeParameterUnusedInFormals",
        "StringSplitter",
        "InconsistentCapitalization",
        "AssignmentExpression",          // used throughout and intentional
        "HidingField",                   // sometimes done when specific type is known by child; clean up later
        "ClassNewInstance",              // deprecated, but replacement is more verbose with ignored errors
        "FloatingPointLiteralPrecision", // too many false positives in test code
        "MissingSummary",
        "UnescapedEntity",
        "EmptyBlockTag",
    )
    options.errorprone.error(
        "MissingOverride",
        "MissingCasesInEnumSwitch",
        "BadInstanceof",
        "EmptyCatch",
        "NarrowingCompoundAssignment",
        "JdkObsolete",
    )

    if (nameStr.startsWith("compileTest")) {
        options.errorprone.disable("ReferenceEquality", "IntLongMath", "ClassCanBeStatic")
    }

    options.errorprone.check("NullAway", CheckSeverity.ERROR)
    options.errorprone.option("NullAway:TreatGeneratedAsUnannotated", "true")
    options.errorprone.option("NullAway:AnnotatedPackages", "org.ejml")
}

// Skip jar publishing for these codeless container modules
val codelessModules = setOf("main", "examples", "autocode", "regression")
if (name in codelessModules) {
    tasks.named<Jar>("jar") { enabled = false }
}

spotless {
    ratchetFrom("origin/SNAPSHOT")

    format("misc") {
        target("*.gradle", "*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingSpacesToTabs()
        endWithNewline()
    }

    java {
        // There's no good way to ignore auto-generated code, so spotless is applied only to
        // modules with hand-written sources. To re-apply, run the autocode wipe first.
        target(
            "**/ejml-core/src/org/ejml/**/*.java",
            "**/ejml-ddense/src/org/ejml/**/*.java",
            "**/ejml-dsparse/src/org/ejml/**/*.java",
            "**/ejml-kotlin/src/org/ejml/**/*.java",
            "**/ejml-simple/src/org/ejml/**/*.java",
            "**/ejml-zdense/src/**/*.java",
        )

        toggleOffOn("formatter:off", "formatter:on")
        removeUnusedImports()
        endWithNewline()

        licenseHeaderFile("${project.rootDir}/docs/copyright.txt", "package ")
    }
}