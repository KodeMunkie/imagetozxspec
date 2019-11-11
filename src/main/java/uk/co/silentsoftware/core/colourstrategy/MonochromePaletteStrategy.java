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
package uk.co.silentsoftware.core.colourstrategy;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT;
import static uk.co.silentsoftware.core.helpers.ColourHelper.intToRgbComponents;

/**
 * Monochrome palette strategy
 */
public class MonochromePaletteStrategy implements ColourChoiceStrategy {

	public String toString() {
		return getCaption("colour_mode_mono");
	}

	@Override
	public int chooseBestPaletteMatch(int originalRgb, int[] mostPopularRgbColours){
		return getMonoColour(originalRgb);
	}

	@Override
	public int chooseBestPaletteMatch(int rgb) {
		return getMonoColour(rgb);
	}

	private int getMonoColour(int rgb) {
		OptionsObject oo = OptionsObject.getInstance();
		return getMonochromeColour(rgb,
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()],
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]);
	}

	/**
	 * Based on the darkness of the pixel colour determines whether a pixel is
	 * ink or paper and returns that colour. Used for converting colour to
	 * monochrome based on whether a pixel can be considered black using the
	 * isBlack threshold.
	 *
	 * @param rgb the rgb colour to get the monochrome colour from
	 * @param ink the spectrum ink colour
	 * @param paper the spectrum paper colour
	 * @return the ink colour if black, otherwise paper colour
	 */
	public static int getMonochromeColour(int rgb, int ink, int paper) {
		int[] comps = intToRgbComponents(rgb);
		if (rgb == ink || isBlack(comps[0], comps[1], comps[2]))
			return ink;
		return paper;
	}

	/**
	 * Determines whether a pixel is closer to black (than white)
	 *
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @return whether this component is closer to black than white
	 */
	private static boolean isBlack(int red, int green, int blue) {
		int threshold = OptionsObject.getInstance().getBlackThreshold();
		return red < threshold && green < threshold && blue < threshold;
	}

	@Override
	public int[] getPalette() {
		return SPECTRUM_COLOURS_BRIGHT;
	}

	@Override
	public BufferedImage colourAttributes(BufferedImage image) {
		return ColourHelper.colourAttributes(image, this);
	}
}
