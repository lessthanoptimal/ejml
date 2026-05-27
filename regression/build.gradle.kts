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
}

dependencies {
	api(project(":main:autocode"))
	api(project(":main:ejml-all"))
	api(project(":main:ejml-experimental"))

	api(project(":main:ejml-cdense", configuration = "benchmarksOutput"))
	api(project(":main:ejml-ddense", configuration = "benchmarksOutput"))
	api(project(":main:ejml-dsparse", configuration = "benchmarksOutput"))
	api(project(":main:ejml-fdense", configuration = "benchmarksOutput"))
	api(project(":main:ejml-fsparse", configuration = "benchmarksOutput"))
	api(project(":main:ejml-zdense", configuration = "benchmarksOutput"))

	testImplementation(project(":main:ejml-test"))
	testImplementation(project(":main:ejml-core", configuration = "testOutput"))
	testImplementation(project(":main:ejml-simple", configuration = "testOutput"))
	testImplementation(project(":main:ejml-experimental", configuration = "testOutput"))
	testImplementation(project(":main:ejml-cdense", configuration = "testOutput"))
	testImplementation(project(":main:ejml-dsparse", configuration = "testOutput"))
	testImplementation(project(":main:ejml-ddense", configuration = "testOutput"))
	testImplementation(project(":main:ejml-fdense", configuration = "testOutput"))
	testImplementation(project(":main:ejml-fsparse", configuration = "testOutput"))
	testImplementation(project(":main:ejml-zdense", configuration = "testOutput"))

	testImplementation(Libs.COMMONS_IO)

	implementation("com.peterabeles:regression:${Versions.AUTO64TO32}") {
		exclude(group = "org.openjdk.jmh")
	}
	api("com.peterabeles:language:${Versions.AUTO64TO32}")
	api(Libs.JMH_CORE)
}

// Run the regression using a gradle command
// Currently this is the only way to get paths set up for benchmarks. See comment below.
//
// Example: ./gradlew runtimeRegression run --console=plain -Dexec.args="--SummaryOnly"
tasks.register<JavaExec>("runtimeRegression") {
	dependsOn(tasks.named("build"))
	group = "execution"
	description = "Run the mainClass from the output jar in classpath with ExecTask"
	classpath = sourceSets.main.get().runtimeClasspath
	mainClass.set("org.ejml.EjmlRuntimeRegressionApp")
	args = System.getProperty("exec.args", "").split(" ")
}

// Creating a jar would be easier to pass in arguments with, but it seems like only the first
// META-INF/BenchmarkList it sees is used. This limited the benchmarks to one module