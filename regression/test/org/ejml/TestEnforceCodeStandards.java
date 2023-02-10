/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml;


import com.peterabeles.lang.CheckForbiddenHelper;
import com.peterabeles.lang.CheckForbiddenLanguage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Makes sure all Configuration classes are compliant
 *
 * @author Peter Abeles
 */
public class TestEnforceCodeStandards {
	// Only run this on performance critical code
	String[] blacklistConfig = new String[]{"autocode", "ejml-all", "ejml-kotlin"};
	String[] blackListLanguage = new String[]{
			"autocode", "ejml-all", "ejml-kotlin", "ejml-experimental", "ejml-simple"};

	/**
	 * This code needs to run fast and minimize memory usage. Lint check for certain illegal operations.
	 */
	@Test void performanceCodeLanguageApiCheck() throws IOException {
		String pathToMain = path("main");

		CheckForbiddenLanguage checker = new CheckForbiddenLanguage();
		CheckForbiddenHelper.forbidNonExplicitVar(checker, true, true);
		CheckForbiddenHelper.forbidForEach(checker);

		File[] moduleDirectories = new File(pathToMain).listFiles();
		assertNotNull(moduleDirectories);

		for (File module : moduleDirectories) {
//			System.out.println("module "+module.getPath());
			File dirCode = new File(module, "src");

			if (!dirCode.exists())
				continue;

			boolean inBlackList = Arrays.asList(blackListLanguage).contains(module.getName());
			if (inBlackList)
				continue;

			Collection<File> files = FileUtils.listFiles(dirCode,
					new RegexFileFilter("\\S*.java"), DirectoryFileFilter.DIRECTORY);

			boolean failed = false;
			for (File classFile : files) {
				String text = FileUtils.readFileToString(classFile, StandardCharsets.UTF_8);
				assertNotNull(text);

				if (!checker.process(text)) {
					failed = true;
					System.err.println();
					for (var failure : checker.getFailures()) {
						System.err.println(classFile.getAbsolutePath()+":"+failure.line);
						System.err.println("  code:   " + failure.code.trim());
						System.err.println("  reason: " + failure.check.reason);
						System.err.println();
					}
				}
			}

			if (failed)
				System.err.println(
						"\nIf this is a false positive, the error can be turned off for the file or specific lines. E.g.\n" +
						"    Ignore current line: \"// lint:forbidden ignore_line\"\n" +
						"    Disable line below:  \"// lint:forbidden ignore_below\"\n" +
						"    See documentation for more options.\n");

			assertFalse(failed);
		}
	}

	/**
	 * Makes sure all unit tests extend BoofStandardJUnit
	 */
	@Test void unitTestsMustExtendEjmlStandardJUnit() {
		String pathToMain = path("main");

		File[] moduleDirectories = new File(pathToMain).listFiles();
		assertNotNull(moduleDirectories);

		for (File module : moduleDirectories) {
//			System.out.println("module "+module.getPath());
			File dirTest = new File(module, "test");

			if (!dirTest.exists())
				continue;

			boolean inBlackList = Arrays.asList(blacklistConfig).contains(module.getName());
			if (inBlackList)
				continue;

			Collection<File> files = FileUtils.listFiles(dirTest,
					new RegexFileFilter("Test[A-Z]\\S*.java"),
					DirectoryFileFilter.DIRECTORY);

			boolean failed = false;
			for (File classFile : files) {
				Path f = dirTest.toPath().relativize(classFile.toPath());
				String classPath = f.toString().replace(File.separatorChar, '.').replace(".java", "");

				// Load the class
				Class<?> c;
				try {
					c = Class.forName(classPath);
				} catch (NoClassDefFoundError e) {
					System.err.println("loading " + classPath);
					e.printStackTrace(System.err);
					fail(e.getMessage());
					return;
				} catch (ClassNotFoundException e) {
					System.err.println("Class not found: " + classPath);
					failed = true;
					continue;
				}

				// See if it extends BoofStandardJUnit
				boolean found = false;
				while (c != null) {
					c = c.getSuperclass();
					if (c == EjmlStandardJUnit.class) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.err.println("Does not extend BoofStandardJUnit");
					System.err.println(classFile.getAbsolutePath()+":1"); // todo real line number
					failed = true;
				}
			}

			assertFalse(failed, "All tests must extend BoofStandardJUnit. See stderr.");
		}
	}
	public static String getPathToBase() {
		String path = new File(".").getAbsoluteFile().getParent();

		while (path != null) {
			File f = new File(path);
			if (!f.exists())
				break;
			String[] files = f.list();
			if (files == null)
				break;

			if (new File(f, "settings.gradle").exists() && new File(f, "LICENSE-2.0.txt").exists())
				break;

			path = f.getParent();
		}
		return path;
	}

	public static String path( String path ) {
		String pathToBase = getPathToBase();
		if (pathToBase == null)
			return path;
		return new File(pathToBase, path).getAbsolutePath();
	}
}
