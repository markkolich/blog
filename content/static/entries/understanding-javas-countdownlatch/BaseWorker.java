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

import java.util.concurrent.CountDownLatch;

public abstract class BaseWorker extends Thread {
	
	// We extend Thread instead of implementing Runnable
	// so that extending classes of BaseWorker don't
	// have to say new Thread(t).start(), they can simply
	// say t.start().  Big deal, I know.
		
	/**
	 * The latch that ensures things happen in order.
	 * Extending classes do not have access to the latch.
	 */
	private CountDownLatch latch_;
	
	/**
	 * Indicates if the worker thread completed it's job
	 * normally.  Extending classes do not have access
	 * to the status boolean because they shouldn't have
	 * a need to maniuplate this directly.
	 */
	private boolean success_;
	
	public BaseWorker() {
		super();
		latch_ = null;
		success_ = false;
	}
		
	/**
	 * Returns true if this Thread completed its job
	 * successfully, without error.  Returns false
	 * if this thread encountered an exception that
	 * prevented it from making forward progress.
	 * @return
	 */
	public boolean success() {
		return success_;
	}
	
	/**
	 * Set the latch this worker will be reporting to.
	 * @param latch
	 */
	public void setLatch(CountDownLatch latch) {
		latch_ = latch;
	}
		
	@Override
	public void run() {
		try {
			myRun();
			success_ = true;
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			success_ = false;
		} finally {
			// Ensures that each worker thread decrements the latch
			// when it's done, either successfully or not.
			if(latch_ != null) {
				latch_.countDown();
			}
		}
	}
	
	/**
	 * All worker threads must implement a myRun() method
	 * which is called by the framework when it's time to
	 * do actual work.
	 */
	public abstract void myRun() throws Exception;
	
	/**
	 * Worker threads must be able to give their name
	 * to a requesting thread runner.
	 * @return
	 */
	public abstract String getWorkerName();
	
}
