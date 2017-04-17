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

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT;

/**
 * Marker class for the monochrome strategy
 */
public class MonochromePaletteStrategy implements ColourChoiceStrategy {

	public String toString() {
		return getCaption("colour_mode_mono");
	}

	@Override
	public int getClosestColour(int originalAlphaRgb, int[] mostPopularAlphaRgbColours){
		return originalAlphaRgb;
	}

	@Override
	public int getClosestColour(int r, int g, int b) {
		// Get OptionsObject on access to prevent circular dependency
		OptionsObject oo = OptionsObject.getInstance();
		return ColourHelper.getMonochromeColour(r, g, b,
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()],
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]);
	}

	@Override
	public int getClosestColour(int rgb) {
		// Get OptionsObject on access to prevent circular dependency
		OptionsObject oo = OptionsObject.getInstance();
		return ColourHelper.getMonochromeColour(rgb,
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()],
				SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]);
	}

	@Override
	public BufferedImage colourAttributes(BufferedImage image) {
		return ColourHelper.colourAttributes(image, this);
	}
}
