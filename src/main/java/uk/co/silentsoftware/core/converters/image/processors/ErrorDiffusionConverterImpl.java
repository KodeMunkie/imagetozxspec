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
package uk.co.silentsoftware.core.converters.image.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

import java.awt.image.BufferedImage;

import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT;

/**
 * An error diffusion dithering converter
 */
public class ErrorDiffusionConverterImpl implements ImageConverter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private OptionsObject oo = OptionsObject.getInstance();
	private ErrorDiffusionDitherStrategy ditherStrategy = null;
	private ColourChoiceStrategy colourChoiceStrategy = null;
	private boolean drawStrategyLabel = false;

	public ErrorDiffusionConverterImpl(ErrorDiffusionDitherStrategy ditherStrategy, ColourChoiceStrategy colourChoiceStrategy) {
		this.ditherStrategy = ditherStrategy;
		this.colourChoiceStrategy = colourChoiceStrategy;
		this.drawStrategyLabel = true;
	}

	public ErrorDiffusionConverterImpl(){
		ditherStrategy = (ErrorDiffusionDitherStrategy) oo.getSelectedDitherStrategy();
		colourChoiceStrategy = oo.getColourMode();
	}
	/*
	 * {@inheritDoc}
	 */
	public ResultImage[] convert(BufferedImage original) {
		BufferedImage output = ImageHelper.copyImage(original);
		final ErrorDiffusionDitherStrategy edds = ditherStrategy != null ? ditherStrategy : (ErrorDiffusionDitherStrategy)oo.getSelectedDitherStrategy();
		Integer xBound = null;
		Integer yBound = null;
		for (int y = 0; y < output.getHeight(); ++y) {
			if (y%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
				yBound = y+SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
			}
			// Switch scan traversal direction is serpentine dithering enabled
			if (oo.getSerpentine() && y % 2 == 0) {		
				for (int x = output.getWidth() - 1; x >= 0; --x) {
					if (x%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
						xBound = x-SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
					}
					processPixel(output, colourChoiceStrategy, edds, x, y, xBound, yBound);
				}
			} else {
				for (int x = 0; x < output.getWidth(); ++x) {
					if (x%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
						xBound = x+SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
					}
					processPixel(output, colourChoiceStrategy, edds, x, y, xBound, yBound);
				}
			}
		}

		output = colourChoiceStrategy.colourAttributes(output);

		// Print the name of the preview strategy
		if (drawStrategyLabel) {
			PreviewLabeller.drawPreviewStrategyWithName(output, ditherStrategy.toString());
		}
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}

	/**
	 * Dithers an individual pixel according to the error diffusion strategy
	 * 
	 * @param output the output image
	 * @param colourMode the Spectrum colouring mode to use
	 * @param edds the dither strategy
	 * @param x the x coordinate of the original pixel
	 * @param y the y coordinate of the original pixel
	 * @param boundX the x boundary in the range 0 to length
	 * @param boundY the y boundary in the range 0 to length
	 */
	private void processPixel(BufferedImage output,  ColourChoiceStrategy colourMode, ErrorDiffusionDitherStrategy edds, int x, int y, Integer boundX, Integer boundY) {
		int oldPixel = output.getRGB(x, y);
		int newPixel = colourMode.getClosestColour(oldPixel);
		output.setRGB(x, y, newPixel);
		edds.distributeError(output, oldPixel, newPixel, x, y, boundX, boundY);
	}
}
