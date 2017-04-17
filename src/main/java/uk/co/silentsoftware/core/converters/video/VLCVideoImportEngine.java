/* Image to ZX Spec
 * Copyright (C) 2017 Silent Software Silent Software (Benjamin Brown)
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

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * VLCJ wrapper for platform native video decoding.
 * Note that this decoder be design is unable to decode faster than
 * real time owning to streaming realtime from VLC.
 */
public class VLCVideoImportEngine implements VideoImportEngine {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Amount of time as a minimum before we can take a single image from the
	 * video in seconds
	 */
	private static final int MINIMUM_INTRO_WAIT = (int)TimeUnit.SECONDS.toMillis(3);

	/**
	 * Amount of (up to) random time we add to MINIMUM_INTRO_WAIT before we can
	 * take a single image from the video in seconds.
	 */
	private static final int RANDOM_INTRO_WAIT = (int)TimeUnit.SECONDS.toMillis(5);
	
	/**
	 * Amount of time to wait between checks to see if the video has loaded (there
	 * is no other way unless the VLC UI is displayed).
	 */
	private static final int MEDIA_PRELOAD_WAIT = (int)TimeUnit.SECONDS.toMillis(2);
	
	private volatile boolean cancel = false;
	
	/**
	 * Converts the specified video file to a series of images and adds them to
	 * the given shared queue. 
	 * 
	 * @param f the video file
	 * @param singleImage whether just a single preview image is required
	 * @param sharedQueue the shared processing queue
	 * @throws IOException if there is a problem loading the media file
	 * @throws InterruptedException if loading is interupted
	 */
	public void convertVideoToImages(File f, boolean singleImage, final BlockingQueue<Image> sharedQueue, VideoLoadedLock videoLoadedLock) throws IOException, InterruptedException {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		JWindow frame = new JWindow();
		frame.setBounds(0,0,1,1);
		frame.add(mediaPlayerComponent);
		frame.setVisible(true);
		final MediaPlayer player = mediaPlayerComponent.getMediaPlayer();
		player.mute();
		player.playMedia(f.getAbsolutePath());
		long len = player.getLength();
		if (len == 0) {
			Thread.sleep(MEDIA_PRELOAD_WAIT);
			len = player.getLength();
		}
		int singleImageSelectionTime = -1;
		if (singleImage) {
			singleImageSelectionTime = new Random().nextInt(RANDOM_INTRO_WAIT) + MINIMUM_INTRO_WAIT;
			if (singleImageSelectionTime > len) {
				singleImageSelectionTime = (int) len-1;
			}
		}
		frame.setVisible(false);
		log.debug("Player time: {}, Len: {}", player.getTime(), len);
		videoLoadedLock.preloadFinished();
		try {
			while (player.getTime() < len) {
				if (cancel) {
					break;
				}
				
				// Always add images if we don't want a single preview
				// image or when the time exceeds the point we want a
				// single image from
				if (singleImageSelectionTime == -1 || player.getTime() >= singleImageSelectionTime) {
					sharedQueue.add(player.getSnapshot());
	
					// We only want one image if single image selection
					// is turned on
					if (singleImageSelectionTime != -1 && player.getTime() >= singleImageSelectionTime) {
						break;
					}
				}
			}
		} finally {
			player.stop();
			player.release();
			mediaPlayerComponent.release();
			cancel = false;
		}
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override 
	public String toString() {
		return getCaption("VLC");
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		this.cancel = true;
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void initVideoImportEngine(Optional<String> pathToLibrary) {
		if (pathToLibrary.isPresent()) {
			log.debug("Path to library {}", pathToLibrary);
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), pathToLibrary.get());
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
			log.debug("Library loaded");
		}
	}
}
