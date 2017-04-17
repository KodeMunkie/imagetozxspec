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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import io.humble.ferry.Buffer;
import org.magicwerk.brownies.collections.BigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.spectrum.ScrConverter;
import uk.co.silentsoftware.core.converters.spectrum.TapeConverter;
import uk.co.silentsoftware.core.converters.spectrum.TextConverter;
import uk.co.silentsoftware.core.converters.video.GifConverter;
import uk.co.silentsoftware.core.helpers.ImageHelper;
import uk.co.silentsoftware.core.helpers.SaveHelper;
import uk.co.silentsoftware.ui.ImageToZxSpec.UiCallback;

/**
 * Manages the work output, i.e. using the work container results it 
 * decides what needs to be output based on the user's chosen options 
 * and calls the relevant converter classes.
 */
class WorkOutputter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Text file output for text dithers
	 */
	private static final TextConverter textConverter = new TextConverter();

	/**
	 * The ".tap" file converter
	 */
	private static final TapeConverter tapeConverter = new TapeConverter();

	/**
	 * The ".gif" file converter
	 */
	private static final GifConverter gifConverter = new GifConverter();

	/**
	 * The SCR file converter
	 */
	private static final ScrConverter scrConverter = new ScrConverter();

	/**
	 * The default base file name to use for export files that don't specify a name
	 */
	private static final String DEFAULT_BASE_FILE_NAME = "Img2ZXSpec";

	private final UiCallback uiCallback;

	private final File outFolder;
	
	private List<byte[]> convertedTap = new BigList<>();

	private final WorkManager workManager;	
	
	private final OptionsObject optionsObject = OptionsObject.getInstance();

	WorkOutputter(final WorkManager workManager, final UiCallback uiCallback, final File outFolder) {
		this.workManager = workManager;
		this.uiCallback = uiCallback;
		this.outFolder = outFolder;
		OptionsObject oo = OptionsObject.getInstance();
		if (oo.getExportAnimGif()) {
			gifConverter.createSequence();	
		}
	}

	/**
	 * Outputs the the result from the work container
	 * E.g. shows a preview, adds a frame to the gif etc.
	 * 
	 * @param workContainer the populated work container
	 */
	void outputFrame(final WorkContainer workContainer) {
		final String processingText = getCaption("main_processed") + " ";
		uiCallback.setStatusMessage(processingText + (workContainer.getImageId()));
		Optional<ResultImage> finalImage = ResultImage.getFinalImage(workContainer.getResultImage());
		if (!finalImage.isPresent()) {
			log.warn("Final image was not present");
			return;
		}
		BufferedImage imageResult = finalImage.get().getImage();
		
		// Add a section to the tape
		addTapePart(workContainer.getScrData());
		
		// Add a frame to the give
		addGifPart(imageResult);
		
		String name = workContainer.getImageId();
		
		// Export the image to the filesystem
		try {
			exportImage(imageResult, name);
		} catch (IOException io) {
			uiCallback.setStatusMessage("Failed to write image for input: "+name);
		} 
		// Export an SCR to the filesystem
		try {
			exportScreen(workContainer.getScrData(), name);
		} catch (IOException io) {
			uiCallback.setStatusMessage("Failed to write SCR for input: "+name);			
		}
		// Export a text file of the image to the filesystem
		try {
			exportText(imageResult, name);
		} catch (IOException io) {
			uiCallback.setStatusMessage("Failed to write text file for input: "+name);			
		}
	}
	
	/**
	 * Use the uicallback to display the current frame in the main dialog
	 * 
	 * @param workContainer the workContainer with the result
	 */
	void previewFrame(WorkContainer workContainer) {
		if (!optionsObject.getShowWipPreview()) {
			return;
		}
		if (workContainer == null) {
			return;
		}
		BufferedImage preProcessedResult = workContainer.getPreprocessedImageResult();
		Optional<ResultImage> finalImage = ResultImage.getFinalImage(workContainer.getResultImage());
		if (!finalImage.isPresent()) {
			return;
		}
		drawAndUpdatePreview(uiCallback, preProcessedResult, finalImage.get().getImage());
	}
	
	/**
	 * Some exporters require closing or finalising, this step does that
	 * then uses the uicallback to tell the user the work is complete
	 */
	void processEndStep() {
		try {
			exportTape();
			exportGif();
		} catch (Exception e) {
			log.error("Unable to export gif or tape", e);
			uiCallback.setStatusMessage(e.getMessage());
		} finally {
			uiCallback.resetStatusMessage();
			uiCallback.enableInput();
		}
	}

	/**
	 * Adds an image to the gif buffer
	 * 
	 * @param image the image to add 
	 */
	private void addGifPart(BufferedImage image) {
		if (OptionsObject.getInstance().getExportAnimGif()) {
			log.debug("Adding gif part");
			gifConverter.addFrame(image);
		}
	}

	/**
	 * Adds the scrData to the tape file buffer
	 * 
	 * @param scrData the raw scr byte data
	 */
	private void addTapePart(byte[] scrData) {
		if (OptionsObject.getInstance().getExportTape()) {
			log.debug("Adding scr to tape part");
			// Gigascreens are 2 screens in 1 and thus we need to split the scr data
			convertedTap.add(tapeConverter.createTapPart(scrConverter.getScr1(scrData)));
			Optional<byte[]> scr2 = scrConverter.getScr2(scrData);
			if (scr2.isPresent()) {
				log.debug("Adding scr2 to tape part");
				convertedTap.add(tapeConverter.createTapPart(scr2.get()));
			}
		}	
	}
	
	/**
	 * Dumps the tape buffer into a file
	 * 
	 * throws IOException if the export fails 
	 */
	private void exportTape() throws IOException {
		if (OptionsObject.getInstance().getExportTape() && convertedTap.size() > 0) {
			log.debug("Exporting tap result");
			byte[] result = tapeConverter.createTap(convertedTap);
			if (result != null && result.length > 0) {
				SaveHelper.saveBytes(result, new File(outFolder + "/" + DEFAULT_BASE_FILE_NAME + ".tap"));
			}
		}
	}
	
	/**
	 * Dumps the gif buffer into a file
	 * 
	 * @throws IOException if the export fails 
	 */
	private void exportGif() throws IOException {
		if (OptionsObject.getInstance().getExportAnimGif()) {
			log.debug("Exporting gif result");
			uiCallback.setStatusMessage(getCaption("main_saving_gif"));
			byte[] result = gifConverter.createGif();
			if (result != null && result.length > 0) {
				SaveHelper.saveBytes(result, new File(outFolder + "/" + DEFAULT_BASE_FILE_NAME + ".gif"));
			}
		}
	}

	/**
	 * Dumps the image to a text file
	 * 
	 * @param image the image to output as text
	 * @param name the file name
	 * @throws IOException if the export fails
	 */
	private void exportText(BufferedImage image, String name) throws IOException {
		if (OptionsObject.getInstance().getExportText()) {
			log.debug("Exporting text result");
			SaveHelper.saveBytes(textConverter.createText(image).getBytes(), new File(outFolder + "/" + name + ".txt"));
		}
	}

	/**
	 * Dumps the scr data to a file
	 * 
	 * @param scrData the byte data to output
	 * @param name the file name
	 * @throws IOException if the export fails
	 */
	private void exportScreen(byte[] scrData, String name) throws IOException {
		if (OptionsObject.getInstance().getExportScreen()) {
			log.debug("Exporting scr result");
			SaveHelper.saveBytes(scrData, new File(outFolder + "/" + name + ".scr"));
		}
	}

	/**
	 * Dumps the image data to a file
	 * 
	 * @param imageResult the image to output
	 * @param name the file name
	 * @throws IOException if the export fails
	 */
	private void exportImage(BufferedImage imageResult, String name) throws IOException {
		if (OptionsObject.getInstance().getExportImage()) {
			log.debug("Exporting image result");
			SaveHelper.saveImage(imageResult, outFolder, name, OptionsObject.getInstance().getImageFormat());
		}
	}
	
	/**
	 * Internal method to build a static preview image to display in the main
	 * panel during video processing
	 * 
	 * @param uiCallback the callback for drawing the preview
	 * @param preprocessed the preprocessed image
	 * @param result the final image
	 */
	private void drawAndUpdatePreview(UiCallback uiCallback, BufferedImage preprocessed, BufferedImage result) {
		uiCallback.updateMainImage(ImageHelper.prepareMainPreview(preprocessed, result, workManager.getFps()));
	}
}
