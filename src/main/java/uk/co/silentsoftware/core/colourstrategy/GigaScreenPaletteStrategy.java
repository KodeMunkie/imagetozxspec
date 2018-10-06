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
package uk.co.silentsoftware.core.colourstrategy;

import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Marker class for the GigaScreen palette.
 */
public class GigaScreenPaletteStrategy implements ColourChoiceStrategy {

	public String toString() {
		return getCaption("colour_mode_gigascreen");
	}

	/**
	 * Processing of the colours cannot be applied to both Spectrum screens used in Gigascreen at the same time.
	 * If this method is called then the wrong processor implementation is being used. 
	 */
	@Override
	public int chooseBestPaletteMatch(int originalAlphaRgb, int[] mostPopularAlphaRgbColours) {
		throw new UnsupportedOperationException("GigaScreen palette colouring cannot be applied to dither image processors - this is a placeholder class to allow identification during getClosestColour conversions for the GigaScreenConverter.");
	}

	@Override
	public int getClosestColour(int r, int g, int b) {
		return  ColourHelper.getClosestGigascreenColour(r, g, b);
	}

	@Override
	public int getClosestColour(int rgb) {
		return ColourHelper.getClosestGigascreenColour(rgb);
	}

	@Override
	public BufferedImage colourAttributes(BufferedImage image) {
		return image;
	}
}
