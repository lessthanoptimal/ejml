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
    `maven-publish`
    signing
}

// Force the release build to fail if it depends on a SNAPSHOT
tasks.named("jar") {
    dependsOn("checkDependsOnSNAPSHOT")
}

// Force publish to fail if trying to upload a stable release and git is dirty
tasks.named("publish") {
    dependsOn("failDirtyNotSnapshot")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("EJML")
                description.set("A fast and easy to use dense and sparse matrix linear algebra library written in Java.")
                url.set("https://ejml.org/")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pabeles")
                        name.set("Peter Abeles")
                        email.set("peter.abeles@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/lessthanoptimal/ejml.git")
                    developerConnection.set("scm:git:git://github.com/lessthanoptimal/ejml.git")
                    url.set("https://github.com/lessthanoptimal/ejml")
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = "https://ossrh-staging-api.central.sonatype.com/service/local/"
            val snapshotsRepoUrl = "https://central.sonatype.com/repository/maven-snapshots/"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            )
            credentials {
                username = (project.findProperty("ossrhUsername") as String?) ?: "dummy"
                password = (project.findProperty("ossrhPassword") as String?) ?: "dummy"
            }
        }
    }
}

// Only sign when real credentials are provided
val ossrhPassword = project.findProperty("ossrhPassword") as String?
if (ossrhPassword != null && ossrhPassword != "dummy") {
    signing {
        sign(publishing.publications["mavenJava"])
    }
}