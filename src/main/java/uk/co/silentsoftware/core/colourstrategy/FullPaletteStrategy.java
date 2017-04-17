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
package uk.co.silentsoftware.core.colourstrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;

/**
 * Colouring strategy to colour all pixels using
 * whichever of the two popular colours is closest
 * to the original.
 */
public class FullPaletteStrategy implements ColourChoiceStrategy {
	
	public String toString() {
		return getCaption("colour_mode_full");
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public int getClosestColour(int originalAlphaRgb, int[] mostPopularRgbColours) {
		return ColourHelper.getClosestColour(originalAlphaRgb, mostPopularRgbColours);
	}

	@Override
	public int getClosestColour(int r, int g, int b) {
		return ColourHelper.getClosestSpectrumColour(r, g, b);
	}

	@Override
	public int getClosestColour(int rgb) {
		return ColourHelper.getClosestSpectrumColour(rgb);
	}

	@Override
	public BufferedImage colourAttributes(BufferedImage image) {
		return ColourHelper.colourAttributes(image, this);
	}
}