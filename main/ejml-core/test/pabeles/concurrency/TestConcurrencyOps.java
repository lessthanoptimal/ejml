/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package pabeles.concurrency;

import org.ejml.UtilEjml;
import org.ejml.data.IGrowArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
class TestConcurrencyOps {

	final int numThreads = 4;

	TestConcurrencyOps() {
		// need to know the max number of threads for many of these checks
		ConcurrencyOps.setMaxThreads(numThreads);
	}

	@Test void loopFor() {
		Counter counter = new Counter();
		ConcurrencyOps.loopFor(10,100,i->{
			counter.increment();
		});

		assertEquals(90,counter.value);
	}

	@Test void loopFor_step() {
		final Counter counter = new Counter();
		ConcurrencyOps.loopFor(10,100, 10,i->{
			synchronized (counter) {
				counter.value += i;
			}
		});

		int expected = 0;
		for (int i = 1; i < 10; i++) {
			expected += i*10;
		}
		assertEquals(expected,counter.value);
	}

	@Test void loopFor_step_workspace() {
		// Don't clear the array each time
		GrowArray<IGrowArray> workspace = new GrowArray<>(IGrowArray::new);
		workspace.grow();

		ConcurrencyOps.loopFor(10,100,10,workspace, IGrowArray::add);

		assertEquals(numThreads,workspace.size);
		int total = 0;
		for (int i = 0; i < workspace.size; i++) {
			total += workspace.get(i).length;
		}
		assertEquals( 9,total);
	}


	@Test void loopBlocks() {
		IGrowArray found = new IGrowArray();

		ConcurrencyOps.loopBlocks(10,100,new BlockTask(found));

		assertEquals(8,found.length);
		// the timing was set up so that they should be approximately in reverse order, if run in parallel
		assertTrue(found.data[0] != 10 && found.data[2] != 32 );
		findPair(found,10,32);
		findPair(found,32,54);
		findPair(found,54,76);
		findPair(found,76,100);
	}

	@Test void loopBlocks_minBlock() {
		IGrowArray found = new IGrowArray();

		ConcurrencyOps.loopBlocks(10,100,12,new BlockTask(found));

		assertEquals(8,found.length);
		// the timing was set up so that they should be approximately in reverse order, if run in parallel
		assertTrue(found.data[0] != 10 && found.data[2] != 32 );
		findPair(found,10,32);
		findPair(found,32,54);
		findPair(found,54,76);
		findPair(found,76,100);
	}

	@Test void loopBlocks_workspace() {
		GrowArray<IGrowArray> workspace = new GrowArray<>(IGrowArray::new,IGrowArray::clear);
		workspace.grow().add(123);
		ConcurrencyOps.loopBlocks(10, 100, workspace, ( work, idx0, idx1 ) -> {
			for (int i = idx0; i < idx1; i++) {
				work.add(i);
			}
		});
		assertTrue(workspace.size > 0);
		IGrowArray results = new IGrowArray();
		for (int i = 0; i < workspace.size; i++) {
			IGrowArray w = workspace.get(i);
			assertTrue(w.length >= 12);
			for (int j = 0; j < w.length; j++) {
				results.add(w.get(j));
			}
		}
		assertEquals(90, results.length);
		for (int i = 0; i < results.length; i++) {
			assertEquals(i+10,results.get(i));
		}
	}

	@Test void loopBlocks_minBlock_workspace() {
		GrowArray<IGrowArray> workspace = new GrowArray<>(IGrowArray::new,IGrowArray::clear);
		workspace.grow().add(123);
		ConcurrencyOps.loopBlocks(10, 100, 12, workspace, ( work, idx0, idx1 ) -> {
			for (int i = idx0; i < idx1; i++) {
				work.add(i);
			}
		});
		assertTrue(workspace.size > 0);
		IGrowArray results = new IGrowArray();
		for (int i = 0; i < workspace.size; i++) {
			IGrowArray w = workspace.get(i);
			assertTrue(w.length >= 12);
			for (int j = 0; j < w.length; j++) {
				results.add(w.get(j));
			}
		}
		assertEquals(90, results.length);
		for (int i = 0; i < results.length; i++) {
			assertEquals(i+10,results.get(i));
		}
	}

	private void findPair( IGrowArray found , int val0 , int val1 ) {
		for (int i = 0; i < found.length; i += 2) {
			if( found.get(i) == val0 && found.get(i+1) == val1 ) {
				return;
			}
		}
		fail("Couldn't find pair "+val0+" "+val1);
	}

	@Test void selectBlockSize() {
		assertEquals(10,ConcurrencyOps.selectBlockSize(100,5,10));
		assertEquals(20,ConcurrencyOps.selectBlockSize(100,20,10));
		assertEquals(16,ConcurrencyOps.selectBlockSize(100,15,10));
		assertEquals(100,ConcurrencyOps.selectBlockSize(100,80,10));

		assertEquals(22,ConcurrencyOps.selectBlockSize(90,12,4));
	}

	static class BlockTask implements IntRangeConsumer {

		final IGrowArray found;

		BlockTask(IGrowArray found) { this.found = found; }

		@Override
		public void accept(int minInclusive, int maxExclusive) {
			// sleep such that they will be out of order. This is manually checked to make sure it is in parallel
			sleep(100+(100-minInclusive));
			synchronized (found) {
				found.add(minInclusive);
				found.add(maxExclusive);
			}
		}
	}

	@Test void sum() {
		int foundI = ConcurrencyOps.sum(5,10,int.class,i->i+2).intValue();
		assertEquals(45,foundI);

		double foundD = ConcurrencyOps.sum(5,10,double.class,i->i+2.5).doubleValue();
		assertEquals(47.5,foundD, UtilEjml.TEST_F64);
	}

	@Test void max() {
		int foundI = ConcurrencyOps.max(5,10,int.class,i->i+2).intValue();
		assertEquals(9+2,foundI);

		double foundD = ConcurrencyOps.max(5,10,double.class,i->i+2.5).doubleValue();
		assertEquals(9.0+2.5,foundD, UtilEjml.TEST_F64);
	}

	@Test void min() {
		int foundI = ConcurrencyOps.min(5,10,int.class,i->i+2).intValue();
		assertEquals(5+2,foundI);

		double foundD = ConcurrencyOps.min(5,10,double.class,i->i+2.5).doubleValue();
		assertEquals(5.0+2.5,foundD, UtilEjml.TEST_F64);
	}

	private static class Counter {
		int value = 0;
		public synchronized void increment() {
			value++;
		}
	}

	public static void sleep( long milli ) {
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}