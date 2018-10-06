/* Image to ZX Spec
 * Copyright (C) 2018 Silent Software Silent Software (Benjamin Brown)
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

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;

/**
 * Class to dispatch work - images to process - to work processors
 */
class WorkDispatcher {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final OptionsObject oo = OptionsObject.getInstance();

	/**
	 * Submits a frame for processing
	 * 
	 * @param original the original image
	 * @param frameId the id of this frame
	 * @return a work container holding the results of the processing
	 */
	WorkContainer submitFrame(final Image original, final String frameId) {
		WorkProcessor wp = new WorkProcessor();
		return submitInternal(original, wp, (BufferedImage preProcessed, ResultImage[] processed) -> { 
			WorkContainer workContainer = new WorkContainer(processed, preProcessed, frameId);
			if (scrOutputNeeded()) {
				log.debug("Creating SCR output");
				workContainer.setScrData(wp.convertScreen(processed));
			}
			return workContainer;
		});
	}

	/**
	 * Submits a frame for pop up window preview processing
	 * 
	 * @param original the original image
	 * @param dither the dither strategy to use
	 * @return a work container holding the results of the processing
	 */
	WorkContainer submitPopupPreview(final Image original, final DitherStrategy dither) {
		final WorkProcessor wp = new WorkProcessor(dither);
		return submitInternal(original, wp, (BufferedImage preProcessed, ResultImage[] processed) -> new WorkContainer(processed));
	}

	/**
	 * Internal method to pre process, convert and output an image
	 * 
	 * @param original the original image
	 * @param wp the work processor for processing/dithering
	 * @param outputChoices the lambda function for deciding what to do with the output images
	 * @return a work container holding the results of the processing
	 */
	private WorkContainer submitInternal(final Image original, WorkProcessor wp, WorkContainerOutputChoices outputChoices) {
		BufferedImage preProcessed = wp.preProcessImage(original);
		ResultImage[] processed = wp.convertImage(preProcessed);
		return outputChoices.outputFrame(preProcessed, processed);
	}

	/**
	 * Determines whether the SCR output is needed
	 * @return true if needed
	 */
	private boolean scrOutputNeeded() {
		return oo.getExportScreen() || oo.getExportTape();
	}
	
	/**
	 * Lambda interface to determine what to do with the outputted images
	 */
	private interface WorkContainerOutputChoices {
		WorkContainer outputFrame(BufferedImage preProcessed, ResultImage[] processed);
	}
}
