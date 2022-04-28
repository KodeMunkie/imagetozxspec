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

import cz.adamh.utils.NativeUtils;
import io.humble.video.*;
import io.humble.video.MediaDescriptor.Type;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.config.OptionsObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Video decoding class based on the rather obtuse example at 
 * 
 * <a href="https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayVideo.java">https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayVideo.java</a>
 */
public class HumbleVideoImportEngine implements VideoImportEngine {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private volatile boolean cancel = false;

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void convertVideoToImages(File f, boolean singleImage, BlockingQueue<Image> sharedQueue, VideoLoadedLock videoLoadedLock) throws Exception {
		cancel = false;
		Demuxer demuxer = Demuxer.make();
		demuxer.open(f.getAbsolutePath(), null, false, true, null, null);
		long streamStartTime = Global.NO_PTS;
		int videoStreamId = -1;
		Decoder videoDecoder = null;
		Rational streamTimeBase = Global.getDefaultTimeBase();
		for (int i = 0; i < demuxer.getNumStreams(); i++) {
			final DemuxerStream stream = demuxer.getStream(i);
			streamStartTime = stream.getStartTime();
			final Decoder decoder = stream.getDecoder();
			if (decoder != null && decoder.getCodecType() == Type.MEDIA_VIDEO) {
				videoStreamId = i;
				videoDecoder = decoder;
				streamTimeBase = stream.getTimeBase();
				log.debug("Timebase {}", stream.getTimeBase());
				// stop at the first one.
				break;
			}
		}
		videoDecoder.open(null, null);

		final MediaPicture picture = MediaPicture.make(videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());
		final MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
		final MediaPacket packet = MediaPacket.make();
		videoLoadedLock.preloadFinished();
		Long nextSnapShotTime = null;
		long sampleRateNanos = Math.round(1000000000d/OptionsObject.getInstance().getVideoFramesPerSecond());
		log.debug("Sampling every {}ns", sampleRateNanos);
		final Rational systemTimeBase = Rational.make(1, 1000000000);
		log.debug("Stream start time {}", streamStartTime);
		try {
			while (demuxer.read(packet) >= 0) {
				// Now we have a packet, let's see if it belongs to our video stream
				if (packet.getStreamIndex() == videoStreamId) {
					/*
					  A packet can actually contain multiple sets of samples (or
					  frames of samples in decoding speak). So, we may need to call
					  decode multiple times at different offsets in the packet's
					  data. We capture that here.
					 */
					int offset = 0;
					int bytesRead = 0;
					do {
						bytesRead += videoDecoder.decode(picture, packet, offset);
						nextSnapShotTime = snapshotImageIfAtSampleTime(picture,systemTimeBase, streamStartTime, streamTimeBase, nextSnapShotTime, sharedQueue, converter, sampleRateNanos);
						if (cancel || singleImage) {
							return;
						}
						offset += bytesRead;
					} while (offset < packet.getSize());
				}
			}	
		} finally {
			do {
				videoDecoder.decode(picture, null, 0);
				snapshotImageIfAtSampleTime(picture,systemTimeBase, streamStartTime, streamTimeBase, nextSnapShotTime, sharedQueue, converter, sampleRateNanos);
			} while (picture.isComplete());
			demuxer.close();
			cancel = false;
		}
	}
	
	/**
	 * Snapshots an image to the sharedQueue if at the requested time interval
	 * 
	 * @param picture from which to get the timestamp
	 * @param systemTimeBase the time base for this system
	 * @param streamStartTime the start time for the stream
	 * @param streamTimeBase the stream time base
	 * @param nextSnapShotTime the time at which the snapshot is expected
	 * @param sharedQueue the shared processing queue
	 * @param converter the media converter for making the snampshot
	 * @param sampleRateNanos the sampling rate for the video in nanoseconds
	 * @return the next snapshot time (current snapshot time  + sampleRateNanos)
	 * @throws InterruptedException if the snapshot cannot be added to the shared queue
	 */
	private Long snapshotImageIfAtSampleTime(MediaPicture picture, Rational systemTimeBase, long streamStartTime, Rational streamTimeBase, Long nextSnapShotTime, 
			BlockingQueue<Image> sharedQueue, MediaPictureConverter converter, long sampleRateNanos) throws InterruptedException {
		if (picture.isComplete()) {
			long streamTimestamp = picture.getTimeStamp();
		    
			// Convert streamTimestamp into system units (i.e. nano-seconds)
			streamTimestamp = systemTimeBase.rescale(streamTimestamp-streamStartTime, streamTimeBase);
		    if (nextSnapShotTime == null || streamTimestamp >= nextSnapShotTime) {
				BufferedImage image = converter.toImage(null, picture);
				boolean added = sharedQueue.offer(image);
				if (!added && !cancel) {
					log.debug("Video queue full at {}... blocking", streamTimestamp);
					sharedQueue.put(image);
				} else {
					log.debug("Video queue had space");
				}
				
				nextSnapShotTime = streamTimestamp+sampleRateNanos;
				log.debug("Timestamp {}, Next snapshot {}", streamTimestamp, nextSnapShotTime);
			}
		}
		return nextSnapShotTime;
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
	public String toString() {
		return "Humble Video";
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void initVideoImportEngine(Optional<String> pathToLibrary) {
		if (pathToLibrary.isEmpty()) {
			return;
		}
		Thread t = new Thread(() -> {
            try {
                log.debug("Path to library {}", pathToLibrary);
                if (SystemUtils.IS_OS_WINDOWS) {
                    log.debug("Windows OS");
					NativeUtils.loadLibraryFromJar("libhumblevideo-0.dll");
                } else if (SystemUtils.IS_OS_MAC_OSX) {
                    log.debug("Mac OS");
					NativeUtils.loadLibraryFromJar("libhumblevideo.dylib");
                } else if (SystemUtils.IS_OS_LINUX) {
                    log.debug("Linux OS");
					NativeUtils.loadLibraryFromJar("libhumblevideo.so");
                } else {
                    log.error("Unknown OS");
                    throw new IllegalStateException("Unable to determine OS");
                }
                /*
                  Effectively refreshing the library path "stirs the tanks" and preloads the library
                  before its actual usage by JNI in humble video - note the loadClass doesn't actually
                  need doing manually but if we don't the initialisation later of the video decoder
                  is delayed by 5 to 10 seconds and the UI may appear to be frozen.
                 */
                ClassLoader.getSystemClassLoader().loadClass("io.humble.ferry.FerryJNI").getDeclaredConstructor().newInstance();
            } catch (Throwable t1) {
                log.error("Unable to load native library", t1);
            }
        });
		t.setDaemon(true);
		// This method is usually quite CPU intensive and can even block the GUI rendering 
		// despite being in another thread so ensure it is minimum priority
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();		
	}
}
