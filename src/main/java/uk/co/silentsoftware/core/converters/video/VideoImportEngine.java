/* Image to ZX Spec
 * Copyright (C) 2023 Silent Software (Benjamin Brown)
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

import java.awt.Image;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Interface wrapper for the various Java video libraries
 */
public interface VideoImportEngine {
	
	/**
	 * Converts a video file to image frames
	 * 
	 * @param f the video file
	 * @param singleImage whether we just need a single image (for preview)
	 * @param sharedQueue the concurrent queue for adding the images to
	 * @param videoLoadedLock lock to delay queue processing until the video is loaded and streaming has started
	 * @throws Exception if conversion fails for any reason
	 */
	void convertVideoToImages(File f, boolean singleImage, final BlockingQueue<Image> sharedQueue, VideoLoadedLock videoLoadedLock) throws Exception;

	/**
	 * Cancels the frame extraction
	 */
	void cancel();
}