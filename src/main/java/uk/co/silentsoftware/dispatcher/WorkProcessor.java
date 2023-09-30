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
package uk.co.silentsoftware.dispatcher;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.ScalingObject;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ImageConverter;
import uk.co.silentsoftware.core.converters.spectrum.ScrConverter;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

/**
 * Wrapper class for a work processing unit that
 * contains conversion actioning methods (i.e. the bits
 * that actually call the bits that do work :) ).
 * This class is not entirely static due to the need for
 * separate instances when used by the many dithers
 * preview requiring a different image processor each time.
 */
class WorkProcessor {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * The image processor to use
	 */
	private ImageConverter imageConverter;

	/**
	 * The "SCREEN$" converter
	 */
	private static final ScrConverter screenConverter = new ScrConverter();

	private final OptionsObject oo = OptionsObject.getInstance();
	
	/**
	 * Main work processor constructor used for actual results
	 */
	WorkProcessor() {
		imageConverter = oo.getSelectedDitherStrategy().createImageConverter();
		if (oo.getColourMode() instanceof GigaScreenPaletteStrategy) {
			imageConverter = new GigaScreenConverterImpl(imageConverter);
		}
	}

	/**
	 * Preview constructor for when we need a result from a given
	 * dither strategy as opposed to that selected in options
	 *
	 * @param dither the dither strategy to use
	 */
	WorkProcessor(DitherStrategy dither) {
		imageConverter = dither.createPreviewImageConverter(oo.getColourMode());
		if (oo.getColourMode() instanceof GigaScreenPaletteStrategy) {
			imageConverter = new GigaScreenConverterImpl(imageConverter);
		}
	}
	
	/**
	 * Converts the given image to the SCR (SCREEN) format,
	 * optionally saves the file to disk and wraps any errors
	 * during conversion and shows them as a UI dialog message.
	 * 
	 * @param original the original image to convert
	 * @return the scr byte data
	 */
	byte[] convertScreen(ResultImage[] original) {
		try {
			return screenConverter.convert(original, imageConverter);
		} catch(Exception e) {
			log.error("Error occurred converting scr", e);
			JOptionPane.showMessageDialog(null, "An error has occurred: "+e.getMessage(), "Guru meditation", JOptionPane.OK_OPTION);  
		}
		return null;
	}
	
	/**
	 * Converts the given image to the to a "Spectrumified" format
	 * which is returned as a BufferedImage. Any errors during 
	 * conversion are shown as a UI dialog message.
	 * 
	 * @param original the image to convert
	 * @return the converted image as an array - may be more than one output image for per input image
	 */
	 ResultImage[] convertImage(final BufferedImage original) {
		try {
			return imageConverter.convert(original);
		} catch (ClassCastException cce) {
			// Occurs mid conversion if dither type switched
			log.warn("Dither type changed during conversion");
		} catch(Exception e) {
			log.error("Unable to convert image", e);
			JOptionPane.showMessageDialog(null, "An error has occurred: "+e.getMessage(), "Guru meditation", JOptionPane.OK_OPTION);  
		}
		log.error("Unable to convert image");
		return null;
	}
	
	/**
	 * Pre processes the given bufferedimage applying the given scaling
	 * and the specified option set contrast, saturation and brightness
	 * 
	 * @param original the original image
	 * @return the scaled and preprocessed image
	 */
	BufferedImage preProcessImage(final Image original) {
		ScalingObject so = oo.getScaling();
		BufferedImage scaled = ImageHelper.quickScaleImage(original, so.getWidth(), so.getHeight());
		scaled = ColourHelper.changeContrast(scaled, oo.getContrast());
		scaled = ColourHelper.changeSaturation(scaled, oo.getSaturation());
		scaled = ColourHelper.changeBrightness(scaled, oo.getBrightness());
		return scaled;
	}
}
