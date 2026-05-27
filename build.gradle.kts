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
val publishedModules = listOf(
    ":main:ejml-core",
    ":main:ejml-cdense",
    ":main:ejml-ddense",
    ":main:ejml-dsparse",
    ":main:ejml-fdense",
    ":main:ejml-fsparse",
    ":main:ejml-zdense",
    ":main:ejml-simple",
    ":main:ejml-experimental",
)

// Modules whose javadoc should be combined into the aggregate javadoc
val javadocModules = listOf(
    ":main:ejml-core",
    ":main:ejml-ddense",
    ":main:ejml-dsparse",
    ":main:ejml-fdense",
    ":main:ejml-fsparse",
    ":main:ejml-zdense",
    ":main:ejml-cdense",
    ":main:ejml-simple",
)

// Copies every published module's jar and sources jar into a single `libraries/` directory
tasks.register("createLibraryDirectory") {
    publishedModules.forEach { evaluationDependsOn(it) }
    dependsOn(publishedModules.map { "$it:jar" })
    dependsOn(publishedModules.map { "$it:sourcesJar" })

    doLast {
        val outputDir = layout.projectDirectory.dir("libraries").asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val jars = publishedModules.map { path ->
            project(path).tasks.named<Jar>("jar").get().archiveFile.get().asFile
        }
        val sourceJars = publishedModules.map { path ->
            project(path).tasks.named<Jar>("sourcesJar").get().archiveFile.get().asFile
        }

        copy {
            from(jars)
            from(sourceJars)
            into(outputDir)
        }
    }
}

// Aggregate Javadoc across the published modules.
// Only includes source under `src/` to avoid pulling in third-party code that some modules vendor.
tasks.register<Javadoc>("alljavadoc") {
    javadocModules.forEach { evaluationDependsOn(it) }

    source(javadocModules.map { project(it).fileTree("src").include("**/*.java") })
    classpath = files(javadocModules.map {
        project(it).extensions.getByType<SourceSetContainer>()["main"].compileClasspath
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
    javadocModules.forEach { evaluationDependsOn(it) }
    dependsOn(javadocModules.map { "$it:compileJava" })

    archiveFileName.set("ejml-v${project.version}.jar")

    from(javadocModules.map {
        project(it).extensions.getByType<SourceSetContainer>()["main"].output.classesDirs
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}
