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
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

// Sonatype publishing coordination. Only configured when credentials are present;
// otherwise the relevant tasks are unusable but the build still works.
if (project.hasProperty("ossrhUsername")) {
    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
                snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
                username.set(project.property("ossrhUsername") as String)
                password.set(project.property("ossrhPassword") as String)
            }
        }
    }
}

// Modules whose compiled output should be packaged into the all-jars directory and the fat jar
val publishedModules: List<Project>
    get() = subprojects.filter { it.plugins.hasPlugin("ejml.libs-conventions") }

// Copies every published module's jar and sources jar into a single `libraries/` directory
tasks.register("createLibraryDirectory") {
    publishedModules.forEach { evaluationDependsOn(it.path) }
    dependsOn(publishedModules.map { it.tasks.named("jar") })
    dependsOn(publishedModules.map { it.tasks.named("sourcesJar") })

    doLast {
        val outputDir = layout.projectDirectory.dir("libraries").asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val jars = publishedModules.map { proj ->
            proj.tasks.named<Jar>("jar").get().archiveFile.get().asFile
        }
        val sourceJars = publishedModules.map { proj ->
            proj.tasks.named<Jar>("sourcesJar").get().archiveFile.get().asFile
        }

        copy {
            from(jars)
            from(sourceJars)
            into(outputDir)
        }
    }
}

// Aggregate Javadoc across the published modules.
// Only includes source under `src/` to avoid pulling in third-party code.
// NOTE: You need to do --no-parallel when invoking it because of some Gradle 9 thing
tasks.register<Javadoc>("alljavadoc") {
    publishedModules.forEach { evaluationDependsOn(it.path) }

    source(publishedModules.map { it.fileTree("src").include("**/*.java") })
    classpath = files(publishedModules.map {
        it.extensions.getByType<SourceSetContainer>()["main"].compileClasspath
    })

    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)

    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("-allow-script-in-comments", true)

        if (JavaVersion.current() < JavaVersion.VERSION_13) {
            addBooleanOption("-no-module-directories", true)
        }

        use(true)
        isFailOnError = false
        docTitle = "Efficient Java Matrix Library (EJML) v${project.version}"
        links("http://docs.oracle.com/javase/8/docs/api/")
        bottom = file("docs/bottom.txt").readText()
    }
}

// Single fat-jar of all class output across the javadoc-covered modules
tasks.register<Jar>("oneJarBin") {
    publishedModules.forEach { evaluationDependsOn(it.path) }
    dependsOn(publishedModules.map { it.tasks.named("compileJava") })

    archiveFileName.set("ejml-v${project.version}.jar")
    destinationDirectory.set(layout.projectDirectory)

    from(publishedModules.map {
        it.extensions.getByType<SourceSetContainer>()["main"].output.classesDirs
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}

// Classpath for running any single code generator that lives in each module's `generate` directory.
//
//   ./gradlew runGenerator -Pgenerator=org.ejml.dense.block.GeneratorTileMultiplication_F64
//
tasks.register<JavaExec>("runGenerator") {
    group = "ejml"
    description = "Runs a code generator by fully qualified class name. Use -Pgenerator=<fqcn>."
    workingDir = rootDir
    mainClass.set(providers.gradleProperty("generator"))

    val parts = project.subprojects
        .filter { it.plugins.hasPlugin("ejml.java-conventions") }
        .map { dependencies.project(mapOf("path" to it.path, "configuration" to "generateOutput")) }
        .plus(dependencies.project(mapOf("path" to ":main:autocode", "configuration" to "runtimeElements")))
    classpath = configurations.detachedConfiguration(*parts.toTypedArray())

    doFirst {
        if (!mainClass.isPresent)
            throw GradleException("Specify the generator's fully qualified class name, e.g.\n" +
                "  ./gradlew runGenerator -Pgenerator=org.ejml.dense.block.GeneratorTileMultiplication_F64")
    }
}
