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

package org.ejml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adds tests to enforce standards, such as no printing to stdout or stderr, unless it's an error.
 *
 * @author Peter Abeles
 */
public class EjmlStandardJUnit {
	// Always provide a random number generator since it's needed so often
	protected final Random rand = new Random(345);

	// Override output streams to keep log spam to a minimum
	protected final MirrorStream out = new MirrorStream(System.out);
	protected final MirrorStream err = new MirrorStream(System.err);
	protected final PrintStream systemOut = System.out;
	protected final PrintStream systemErr = System.err;

	@BeforeEach
	public void captureStreams() {
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	@AfterEach
	public void revertStreams() {
		assertFalse(out.used,"stdout was written to which is forbidden by default");
		assertFalse(err.used,"stderr was written to which is forbidden by default");
		System.setOut(systemOut);
		System.setErr(systemErr);
	}

	/**
	 * Checks that setTo() copies all fields correctly and returns 'this'
	 */
	protected void checkSetTo( Class<?> type ) {
		try {
			// Find setTo(SameType) method
			Method setTo = null;
			for (Method m : type.getMethods()) {
				if (!m.getName().equals("setTo")) continue;
				Class<?>[] params = m.getParameterTypes();
				if (params.length == 1 && params[0].isAssignableFrom(type)) {
					setTo = m;
					break;
				}
			}
			assertNotNull(setTo, "No setTo(" + type.getSimpleName() + ") method found");

			// Create src with non-default values, and a fresh dst
			Object src = createNotDefault(type);
			Object dst = type.getConstructor().newInstance();

			// Verify src actually differs from dst (i.e. createNotDefault worked)
			for (Field f : type.getFields()) {
				if (Modifier.isStatic(f.getModifiers())) continue;
				assertNotEquals(f.get(src), f.get(dst),
						"createNotDefault did not change field: " + f.getName());
			}

			// Invoke setTo and check it returns 'this'
			Object ret = setTo.invoke(dst, src);
			assertSame(dst, ret, "setTo() must return 'this' for chaining");

			// All fields must now match
			for (Field f : type.getFields()) {
				if (Modifier.isStatic(f.getModifiers())) continue;
				assertEquals(f.get(src), f.get(dst),
						"Field not copied by setTo(): " + f.getName());
			}
		} catch (Exception e) {
			fail("checkSetTo failed: " + e.getMessage());
		}
	}

	/**
	 * Creates an instance where all non-static fields differ from their default values
	 */
	protected Object createNotDefault( Class<?> type ) throws Exception {
		Object o = type.getConstructor().newInstance();
		for (Field f : type.getFields()) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			if (f.getType() == boolean.class) {
				f.set(o, !(boolean)f.get(o));
			} else if (f.getType() == int.class) {
				f.set(o, (int)f.get(o) + 1);
			} else if (f.getType() == char.class) {
				f.set(o, (char)((char)f.get(o) + 1));
			} else if (f.getType() == String.class) {
				f.set(o, f.get(o) + "_modified");
			} else {
				fail("Unhandled field type in createNotDefault: " + f.getType() + " for field " + f.getName());
			}
		}
		return o;
	}

	public static class MirrorStream extends OutputStream {

		public PrintStream out;
		public boolean used = false;

		public MirrorStream( PrintStream out ) {
			this.out = out;
		}

		@Override public void write( int b ) throws IOException {
			used = true;
			out.write(b);
		}

		@Override public void write( byte[] b, int off, int len ) throws IOException {
			used = true;
			out.write(b, off, len);
		}

		@Override public void flush() throws IOException {
			out.flush();
		}

		@Override public void close() throws IOException {
			out.close();
		}
	}
}
