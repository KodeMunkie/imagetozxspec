/* Image to ZX Spec
 * Copyright (C) 2020 Silent Software (Benjamin Brown)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.silentsoftware.core.converters.video;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Semaphore locking class to ensure processing
 * cannot begin until the video has finished loading
 */
public class VideoLoadedLock {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private long WAIT_TIME = 5;
	private Semaphore lock = new Semaphore(1);
	
	public VideoLoadedLock() {
		try {
			// Whatever thread has just initialised this semphore 
			// takes ownership here
			lock.tryAcquire(WAIT_TIME, TimeUnit.SECONDS);
			log.debug("Lock locked");
		} catch (InterruptedException e) {
			log.error("Error with lock", e);
		}
	}
	
	/**
	 * Notification method to release this lock to other threads
	 */
	void preloadFinished() {
		lock.release();
		log.debug("Lock unlocked");
	}
	
	/**
	 * Blocking method to pause threads attempting to procede with processing
	 * Threads are unblocked on the owning thread's call to preloadFinished()
	 */
	public void waitFor() {
		log.debug("Waiting for lock");
		lock.acquireUninterruptibly();
		log.debug("Lock released");
	}
}
