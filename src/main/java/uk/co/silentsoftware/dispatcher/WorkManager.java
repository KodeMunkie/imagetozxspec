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
package uk.co.silentsoftware.dispatcher;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import uk.co.silentsoftware.config.LanguageSupport;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.converters.image.CharacterDitherStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.video.VideoLoadedLock;
import uk.co.silentsoftware.core.helpers.ImageHelper;
import uk.co.silentsoftware.ui.ImageToZxSpec.UiCallback;
import uk.co.silentsoftware.ui.PopupPreviewFrame;

/**
 * The main work managing class that reads the input files and delegates work to
 * work processors via the work dispatcher class.
 */
public class WorkManager {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Polling timeout for waiting on the work queue
     */
    private static final int VIDEO_POLL_TIMEOUT = 8;

    /**
     * Maximum shared queue size (approximately 2-12 seconds of video depending
     * on frame rate)
     */
    private static final int MAX_QUEUE_SIZE = 120;

    /**
     * Thread pool for jobs
     */
    private final ExecutorService exec = Executors.newCachedThreadPool();


    /**
     * Thread for ensuring correct sequencing of preview images
     */
    private final ExecutorService uiFeederThread = Executors.newSingleThreadExecutor();

    /**
     * Work dispatcher instance for dispatching the frames to a work processor
     */
    private final WorkDispatcher workDispatcher = new WorkDispatcher();

    /**
     * Whether to cancel processing
     */
    private volatile boolean cancel = false;

    /**
     * The total frame count
     */
    private volatile float fpsFrameCount = 0.0f;

    /**
     * Actual frames per second
     */
    private volatile float fps;

    /**
     * The debug frames per second counter
     */
    private static Thread fpsThread;

    /**
     * Method to begin the (multi dither, not WIP) preview which submits work to
     * the work engine
     *
     * @param uiCallback the ui callback to re-enable input
     * @param preview    the frame to draw the preview images on
     * @param inFiles    the files from which to draw the preview, uses the first if
     *                   one exists
     */
    public void processPreview(UiCallback uiCallback, PopupPreviewFrame preview, File[] inFiles) {
        if (!preview.isShowing()) {
            return;
        }
        PopupPreviewFrame.reset();
        OptionsObject oo = OptionsObject.getInstance();
        exec.execute(() -> {
            try {
                if (ArrayUtils.isNotEmpty(inFiles)) {
                    Image image = getImage(inFiles[0]);
                    image = ImageHelper.quickScaleImage(image, SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT);

                    // uiCallback.disableInput();
                    generatePopupPreviewDithers(image, oo.getOrderedDithers());
                    generatePopupPreviewDithers(image, oo.getErrorDithers());
                    generatePopupPreviewDithers(image, oo.getOtherDithers());
                    // uiCallback.enableInput();
                }
            } catch (Throwable e) {
                log.error("Preview failed", e);
                enableInput(uiCallback, e.getMessage());
            }
        });
    }

    /**
     * Creates an image from the file or a random frame from a video
     *
     * @param f the image or video file
     * @return an image from the file or video file
     * @throws Exception if there are any processing errors
     */
    private Image getImage(File f) throws Exception {
        if (isVideo(f)) {
            return getRandomFrameFromVideo(f);
        }
        return ImageIO.read(f);
    }

    /**
     * Gets a random frame from the video file
     *
     * @param f the video file
     * @return an image frame from the video file
     * @throws Exception if there are any processing errors
     */
    private Image getRandomFrameFromVideo(File f) throws Exception {
        BlockingQueue<Image> queue = new DisruptorBlockingQueue<>(MAX_QUEUE_SIZE);
        final VideoLoadedLock videoLoadedLock = new VideoLoadedLock();
        OptionsObject.getInstance().getVideoImportEngine().convertVideoToImages(f, true, queue, videoLoadedLock);
        videoLoadedLock.waitFor();
        return queue.poll();
    }

    /**
     * Draws a set of dithers, derived from the provided image, onto the popup
     * preview frame
     *
     * @param image   the image to apply the dithers to
     * @param dithers the dither strategies to apply
     */
    private <T extends DitherStrategy> void generatePopupPreviewDithers(Image image, @SuppressWarnings("unchecked") T... dithers) {
        Stream<T> stream = Arrays.stream(dithers);
        Runnable runnable = () -> {
            stream.forEach(dither -> {
                WorkContainer workContainer = workDispatcher.submitPopupPreview(image, dither);
                Optional<ResultImage> resultImage = ResultImage.getFinalImage(workContainer.getResultImage());
                if (!resultImage.isPresent()) {
                    log.error("Unable to get final image for preview");
                    return;
                }
                ImageHelper.copyImage(resultImage.get().getImage(), PopupPreviewFrame.getPreviewImage(), PopupPreviewFrame.getPoint());
                PopupPreviewFrame.repaintImage();
            });
        };
        exec.submit(runnable);
    }

    /**
     * Method to begin the WIP preview which submits work to the work engine and
     * controls the UI settings and validates the content being loaded. If a
     * video is found in the input only this file is processed.
     *
     * @param uiCallback the callback to control the ui
     * @param inFiles    the files to process, video or image files
     * @param outFolder  the output folder
     */
    public void processFiles(final UiCallback uiCallback, File[] inFiles, File outFolder) {
        this.cancel = false;
        validateSettings(uiCallback);
        uiCallback.disableInput();
        uiCallback.setStatusMessage(LanguageSupport.getCaption("main_working"));
        uiCallback.repaint();
        exec.execute(() -> {
            try {
                log.debug("Processing {} files", inFiles.length);
                for (File f : inFiles) {
                    if (cancel) {
                        log.debug("Processing cancelled");
                        return;
                    }
                    // We have a video so only deal with this file
                    if (isVideo(f)) {
                        log.debug("Video found, processing just the one file to {}...", outFolder);
                        processVideo(uiCallback, f, outFolder);
                        return;
                    }
                }
                log.debug("Images found, processing all files to {}...", outFolder);
                processSingleFiles(uiCallback, inFiles, outFolder);
            } catch (Exception e) {
                log.error("Unable to process files", e);
            } finally {
                enableInput(uiCallback, LanguageSupport.getCaption("main_operation_finished"));
            }
        });
    }

    /**
     * Polls video and submits frames to the work engine for processing
     *
     * @param uiCallback the callback to control the ui
     * @param inputFile  the video file to process
     * @param outFolder  the output folder
     * @throws InterruptedException if the processing is interrupted
     */
    private void processVideo(UiCallback uiCallback, File inputFile, File outFolder) throws InterruptedException {
        final BlockingQueue<Image> sharedQueue = new DisruptorBlockingQueue<>(MAX_QUEUE_SIZE);
        waitForVideoToSpoolUp(sharedQueue, uiCallback, inputFile);
        Image buf;
        Map<Integer, WorkContainer> results = new ConcurrentHashMap<>();

        // Unique frame number for a job
        int sequenceNumber = 0;

        // The last unique frame number that was processed and outputed
        int outputSequenceNumber = 0;

        WorkOutputter workOutputter = null;
        try {
            workOutputter = new WorkOutputter(this, uiCallback, outFolder);
            while ((buf = sharedQueue.poll(VIDEO_POLL_TIMEOUT, TimeUnit.SECONDS)) != null) {
                if (cancel) {
                    OptionsObject.getInstance().getVideoImportEngine().cancel();
                    return;
                }
                int oldOutputSequenceNumber = outputSequenceNumber;
                processFrame(sequenceNumber+"_"+inputFile.getName(), sequenceNumber, results, buf, uiCallback);
                outputSequenceNumber = outputNextImage(results, outputSequenceNumber, workOutputter);
                sequenceNumber++;
                // Only increase the fps count if it's actually removed an image from
                // the queue (as opposed to starting the job and waiting for it to become
                // available)
                if (outputSequenceNumber > oldOutputSequenceNumber) {
                    fpsFrameCount++;
                }
            }
            log.debug("Image relay finished awaiting remaining results");
            outputRemainingFrames(outputSequenceNumber, sequenceNumber, results, workOutputter);
        } finally {
            try {
                if (workOutputter != null) {
                    workOutputter.processEndStep();
                }
            } catch (Exception e) {
                log.error("Unable to process end step", e);
            }
            enableInput(uiCallback, LanguageSupport.getCaption("main_operation_finished"));
        }
        log.debug("Finished polling result queue");
    }

    private void waitForVideoToSpoolUp(BlockingQueue<Image> sharedQueue, UiCallback uiCallback, File inputFile) {
        final VideoLoadedLock videoLoadedLock = new VideoLoadedLock();
        exec.execute(() -> {
            try {
                OptionsObject.getInstance().getVideoImportEngine().convertVideoToImages(inputFile, false, sharedQueue, videoLoadedLock);
            } catch (Throwable t) {
                log.error("Failed to convert video", t);
                enableInput(uiCallback, t.getMessage());
            }
        });
        videoLoadedLock.waitFor();
    }

    /**
     * Inner core method for the process files method that specifically deals
     * with a single files. The files are loaded as images and these are put
     * into the work engine for processing. A waiter thread is initialised to
     * pick the completed work up and as the images are loaded in order the
     * future tasks' results remain correctly ordered when they are collected.
     *
     * @param uiCallback the callback to control the ui
     * @param inFiles    the single image files to process
     * @param outFolder  the output folder
     */
    private void processSingleFiles(UiCallback uiCallback, File[] inFiles, File outFolder) {
        if (ArrayUtils.isEmpty(inFiles)) {
            return;
        }
        WorkOutputter workOutputter = null;
        try {
            int sequenceNumber = 0;
            int outputSequenceNumber = 0;
            Map<Integer, WorkContainer> results = new ConcurrentHashMap<>();
            List<File> files = Arrays.asList(inFiles);
            workOutputter = new WorkOutputter(this, uiCallback, outFolder);
            for (File f : files) {
                if (cancel) {
                    return;
                }
                int oldOutputSequenceNumber = outputSequenceNumber;
                processFrame(f.getName(), sequenceNumber, results, readImage(f), uiCallback);
                outputSequenceNumber = outputNextImage(results, outputSequenceNumber, workOutputter);
                sequenceNumber++;
                // Only increase the fps count if it's actually removed an image
                // from the queue (as opposed to starting the job and waiting for it to become
                // available)
                if (outputSequenceNumber > oldOutputSequenceNumber) {
                    fpsFrameCount++;
                }
            }
            outputRemainingFrames(outputSequenceNumber, sequenceNumber, results, workOutputter);
        } finally {
            try {
                workOutputter.processEndStep();
            } catch (Exception e) {
                log.error("Unable to process end step", e);
            }
            enableInput(uiCallback, LanguageSupport.getCaption("main_operation_finished"));
        }
    }

    private void outputRemainingFrames(int outputSequenceNumber, int sequenceNumber, Map<Integer, WorkContainer> results, WorkOutputter workOutputter) {
        if (!cancel) {
            while (outputSequenceNumber < sequenceNumber) {
                outputSequenceNumber = outputNextImage(results, outputSequenceNumber, workOutputter);
            }
        }
    }

    /**
     * Processes a single frame in a new thread
     *
     * @param name                 the output name
     * @param sequenceNumber       the sequence number for this frame
     * @param results              the results map with frame number to image
     * @param image                the image to convert
     * @param uiCallback           the uicallback for error messages
     * @return the last outputted sequence number
     */
    private void processFrame(String name, int sequenceNumber, Map<Integer, WorkContainer> results,
                             Image image, UiCallback uiCallback) {
        try {
            exec.execute(() -> results.put(sequenceNumber, workDispatcher.submitFrame(image, StringUtils.EMPTY + name)));
        } catch (OutOfMemoryError oome) {
            uiCallback.setStatusMessage(oome.getMessage());
            log.error("Out of memory on frame", oome);
        } catch (Throwable t) {
            // Ignore it and try to continue
            log.error("Unhandled throwable", t);
        }
    }

    /**
     * Outputs the given output sequence number frame if it has been processed
     * otherwise returns
     *
     * @param results              the map of workcontainer results
     * @param outputSequenceNumber the frame number to output
     * @param workOutputter        the output object instance
     * @return the updated outputted sequence number if a frame was output,
     * otherwise the original outputSequenceNumber
     */
    private int outputNextImage(Map<Integer, WorkContainer> results, int outputSequenceNumber, WorkOutputter workOutputter) {
        // Yield gives the system a chance to breath - the work has just been added for processing
        // but may not yet be available. Removing this call results in 10-20% better performance but
        // stuttering video preview
        if (!OptionsObject.getInstance().getTurboMode()) {
            Thread.yield();
        }

        if (!cancel && results.size() > 0) {
            WorkContainer workContainer = results.get(outputSequenceNumber);
            if (workContainer != null) {
                workOutputter.outputFrame(workContainer);
                uiFeederThread.execute(() -> workOutputter.previewFrame(workContainer));
                results.remove(outputSequenceNumber);
                outputSequenceNumber++;
            }
        }
        return outputSequenceNumber;
    }

    /**
     * Reads an image file
     *
     * @param f the image file to read
     * @return the buffered image for the file
     */
    private BufferedImage readImage(final File f) {
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            log.error("Unable to read file", f, e);
            throw new IllegalStateException("Reading file failed", e);
        }
    }

    /**
     * A simple implementation of an FPS calculator
     * TODO: Not very single responsibility in this class, move out.
     */
    public void startFpsCalculator() {
        // Old school threading for the fps calculator
        resetFrameCount();
        if (OptionsObject.getInstance().getFpsCounter() && (fpsThread == null || !fpsThread.isAlive())) {
            fpsThread = new Thread(() -> {
                while (OptionsObject.getInstance().getFpsCounter()) {
                    final float startCount = fpsFrameCount;
                    final long time = System.currentTimeMillis();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                    final long diff = System.currentTimeMillis() - time;
                    float framesPerSecond = (fpsFrameCount - startCount) / ((float) diff / 1000f);

                    // Ignore below 0 - caused by frame counter being reset
                    if (framesPerSecond >= 0) {
                        fps = framesPerSecond;
                    }
                }
            });
            fpsThread.setDaemon(true);
            fpsThread.setPriority(Thread.MIN_PRIORITY);
            fpsThread.start();
        }
    }

    /**
     * Tests if the file is a video
     *
     * @param f the file to test
     * @return whether the file is a video
     */
    private boolean isVideo(File f) {
        String path = f.getPath().toLowerCase();
        return isVideo(path);
    }

    /**
     * Tests if the path ends with a video suffix, either avi, mov, mp4 or mpg
     *
     * @param path the path to test
     * @return whether the path is a video
     */
    private boolean isVideo(String path) {
        return path.endsWith(".avi") || path.endsWith(".mov") || path.endsWith(".mp4") || path.endsWith(".mpg");
    }

    /**
     * Re-enables the UI via a callback
     *
     * @param uiCallback the callback
     * @param message    the status message to display on re-enabling
     */
    private void enableInput(final UiCallback uiCallback, final String message) {
        uiCallback.setStatusMessage(message);
        uiCallback.enableInput();
    }

    /**
     * Validates the options combinations and warns if there's a problem
     *
     * @param uiCallback to display a warning popup
     */
    private void validateSettings(final UiCallback uiCallback) {

        // SCR export chosen but result image does not conform to SCR (Spectrum)
        // resolution
        if (OptionsObject.getInstance().getExportScreen() && OptionsObject.getInstance().getScaling().getName().equals(getCaption("scaling_none"))) {
            uiCallback.displayWarning(getCaption("dialog_warning_title"), getCaption("dialog_no_scaling_with_scr"));
        }
        // Text export chosen but result image was not created with the
        // Character Dither
        if (OptionsObject.getInstance().getExportText() && !(OptionsObject.getInstance().getSelectedDitherStrategy() instanceof CharacterDitherStrategy)) {
            uiCallback.displayWarning(getCaption("dialog_warning_title"), getCaption("dialog_no_char_dither_with_text"));
        }
    }

    /**
     * Gets the fps value
     *
     * @return the fps value
     */
    float getFps() {
        return fps;
    }

    /**
     * Terminates the thread pool
     */
    public void shutdown() {
        exec.shutdownNow();
    }

    /**
     * Stops image/video processing
     */
    public void cancel() {
        this.cancel = true;
    }

    /**
     * Reset the number of frames processed
     */
    private void resetFrameCount() {
        fpsFrameCount = 0;
    }
}
