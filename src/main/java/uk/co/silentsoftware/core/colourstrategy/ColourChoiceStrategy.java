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
package uk.co.silentsoftware.core.colourstrategy;

import java.awt.image.BufferedImage;

/**
 * Pixel colouring strategy used during the conversion process
 */
public interface ColourChoiceStrategy {
	
	/**
	 * Method to return the RGB int colour (Spectrum colour) that should be
	 * used for a given pixel based on the Spectrum colours
	 * passed in as mostPopularColour and secondMostPopularColour.
	 * originalRgb is either the original pixel colour or
	 * part converted colour (e.g. a pixel that has already undergone
	 * colour processing). 
	 * 
	 * @param originalRgb the source rgb value
	 * @param mostPopularRgbColours the rgb values from which to choose the closest match
	 * @return the closest colour from the most popular array
	 */
	int chooseBestPaletteMatch(int originalRgb, int[] mostPopularRgbColours);

	int chooseBestPaletteMatch(int rgb);

	int[] getPalette();

	BufferedImage colourAttributes(BufferedImage image);

}
