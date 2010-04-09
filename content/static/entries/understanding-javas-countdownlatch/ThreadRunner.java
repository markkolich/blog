/**
 * Copyright (c) 2010 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ThreadRunner {

	private CountDownLatch latch_;
	private List<BaseWorker> runners_;
		
	public ThreadRunner(List<BaseWorker> workers) {
		if(workers == null) {
			throw new IllegalArgumentException("Worker list " +
					"cannot be null.");
		} else if(workers.size() < 1) {
			throw new IllegalArgumentException("Worker list " +
					"cannot be empty (must have size >= 1).");
		}
		runners_ = workers;
		// Create a new latch, one tick for every
		// worker thread in the List.
		latch_ = new CountDownLatch(runners_.size());
		// Loop over each of the workers, and set the
		// latch to this latch.
		for(final BaseWorker worker : runners_) {
			worker.setLatch(latch_);
		}
	}
	
	/**
	 * Ladies and gentlemen, start your runners!
	 * Starts all of the runners in this ThreadRunner.
	 */
	public final void start() {
		for(final BaseWorker w : runners_) {
			w.setName(w.getWorkerName());
			w.setDaemon(true);
			w.start();
		}
	}
	
	/**
	 * Causes the current thread to wait until the latch
	 * has counted down to zero, unless the thread is
	 * interrupted.
	 */
	public final void await() {
		try {
			latch_.await();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new Error(e);
		}
	}
	
	/**
	 * Returns true if all of the workers in this runner
	 * completed successfully, without error.  Returns
	 * false if one or more workers failed for any reason.
	 * @return
	 */
	public final boolean wasSuccessful() {
		for(final BaseWorker worker : runners_) {
			if(!worker.success()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Given a single base worker, constructs a list
	 * of size one and returns a new ThreadRunner with
	 * that single worker.
	 * @param lone
	 * @return
	 */
	public static final ThreadRunner getSingleRunner(
		final BaseWorker lone) {
		final List<BaseWorker> worker =
					new ArrayList<BaseWorker>();
		worker.add(lone);
		return new ThreadRunner(worker);
	}
	
}
