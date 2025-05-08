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

// Resolvable, non-transitive config holding the jars to merge. It is NOT wired into
// api/implementation, so these never show up as dependencies in the published POM.
val bundled by configurations.creating {
    isTransitive = false
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    bundled(project(":main:ejml-cdense"))
    bundled(project(":main:ejml-core"))
    bundled(project(":main:ejml-ddense"))
    bundled(project(":main:ejml-dsparse"))
    bundled(project(":main:ejml-fdense"))
    bundled(project(":main:ejml-fsparse"))
    bundled(project(":main:ejml-simple"))
    bundled(project(":main:ejml-zdense"))
}

val bundledPath: Provider<String> = bundled.elements.map { jars ->
    jars.joinToString(File.pathSeparator) { it.asFile.absolutePath }
}

val compileModuleInfo by tasks.registering(JavaCompile::class) {
    source(layout.projectDirectory.file("module/module-info.java"))
    destinationDirectory.set(layout.buildDirectory.dir("generated/module-info"))
    classpath = files()             // no classpath; patch instead
    inputs.files(bundled)  // wires the dependency on the sibling :jar tasks
    modularity.inferModulePath.set(false)
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--patch-module", "ejml.java9module=${bundledPath.get()}")
    })
}

// It publishes the module in the jar's manifest
tasks.named<Jar>("jar") {
    // Put module-info.class in first so EXCLUDE always prefers ours
    from(compileModuleInfo)

    // Unzip each module's classes/resources into this jar.
    from(bundled.elements.map { locations -> locations.map { zipTree(it.asFile) } }) {
        // Don't let the bundled jars' own manifests/signatures clobber ours.
        exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Same seven projects as `bundled`, but resolved to their -sources variant.
val bundledSources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
    extendsFrom(bundled)                       // reuse the same project deps
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named<DocsType>(DocsType.SOURCES))
    }
}

tasks.named<Jar>("sourcesJar") {
    from(bundledSources.elements.map { locs -> locs.map { zipTree(it.asFile) } })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val explodeBundledSources by tasks.registering(Sync::class) {
    from(bundledSources.elements.map { locs -> locs.map { zipTree(it.asFile) } })
    into(layout.buildDirectory.dir("bundled-sources"))
    include("**/*.java")                       // javadoc only wants the sources
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val bundledJavadoc by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
    extendsFrom(bundled)
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named<DocsType>(DocsType.JAVADOC))
    }
}

tasks.named<Jar>("javadocJar") {
    from(bundledJavadoc.elements.map { locs -> locs.map { zipTree(it.asFile) } }) {
        exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}