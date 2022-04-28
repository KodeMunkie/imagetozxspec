/* Image to ZX Spec
 * Copyright (C) 2019 Silent Software (Benjamin Brown)
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

import com.sun.jna.NativeLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.media.MediaEventListener;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.silentsoftware.ui.ImageToZxSpec;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

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
	 * Amount of time in millis to wait between checks to see if the video has loaded (there
	 * is no other way unless the VLC UI is displayed).
	 */
	private static final int MEDIA_PRELOAD_WAIT = 100;

	/**
	 * Amount of time in millis before media load fails
	 */
	private static final int MEDIA_PRELOAD_TIMEOUT = MEDIA_PRELOAD_WAIT * 20;
	private volatile boolean cancel = false;
	
	/**
	 * Converts the specified video file to a series of images and adds them to
	 * the given shared queue. 
	 * 
	 * @param f the video file
	 * @param singleImage whether just a single preview image is required
	 * @param sharedQueue the shared processing queue
	 * @throws InterruptedException if loading is interupted
	 */
	public void convertVideoToImages(File f, boolean singleImage, final BlockingQueue<Image> sharedQueue, VideoLoadedLock videoLoadedLock) throws InterruptedException {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		JWindow frame = new JWindow();
		frame.setBounds(0,0,1,1);
		frame.add(mediaPlayerComponent);
		frame.setVisible(true);
		final MediaPlayer player = mediaPlayerComponent.mediaPlayer();
		player.media().play(f.getAbsolutePath());
		player.media().events().addMediaEventListener(new AudioPlayerComponent() {
			boolean isMuted = false;
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				super.playing(mediaPlayer);
				if (!isMuted) {
					isMuted = this.mediaPlayer().audio().mute();
				}
			}
		});
		// TODO: Following can be done with listeners now but I'm only patching this up for Java17+new VLC4J for now.
		int preloadWait = 0;
		while(!mediaPlayerComponent.mediaPlayer().status().isPlaying() && preloadWait < MEDIA_PRELOAD_TIMEOUT)
		{
			preloadWait += MEDIA_PRELOAD_WAIT;
			Thread.sleep(MEDIA_PRELOAD_WAIT);
		}
		long len = player.media().info().duration();
		int singleImageSelectionTime = -1;
		if (singleImage) {
			singleImageSelectionTime = new Random().nextInt(RANDOM_INTRO_WAIT) + MINIMUM_INTRO_WAIT;
			if (singleImageSelectionTime > len) {
				singleImageSelectionTime = (int) len-1;
			}
		}
		frame.setVisible(false);
		log.debug("Player time: {}, Len: {}", player.status().time(), len);
		videoLoadedLock.preloadFinished();
		try {
			while (player.status().time() < len) {
				// Best effort :(
				if (!player.audio().isMute()) {
					player.audio().mute();
				}
				if (cancel) {
					break;
				}
				
				// Always add images if we don't want a single preview
				// image or when the time exceeds the point we want a
				// single image from
				if (singleImageSelectionTime == -1 || player.status().time() >= singleImageSelectionTime) {
					sharedQueue.add(player.snapshots().get());
	
					// We only want one image if single image selection
					// is turned on
					if (singleImageSelectionTime != -1 && player.status().time() >= singleImageSelectionTime) {
						break;
					}
				}
			}
		} finally {
			try {
				player.controls().stop();
				player.release();
				mediaPlayerComponent.release();
				cancel = false;
			} catch (Error e) {
				// Thrown from release on Win64 for an unknown reason
				log.warn("Error stopping/releasing video", e);
			}
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
}
