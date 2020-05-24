/* Image to ZX Spec
 * Copyright (C) 2020 Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.attributestrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * If either colour is from a different set the bright set colour is moved to
 * the half bright set.
 */
public class FavourHalfBrightAttributeStrategy implements AttributeStrategy {

	/*
	 * {@inheritDoc}
	 */
	@Override
	public int[] enforceAttributeRule(int mostPopRgb, int secMostPopRgb) {
		boolean popIsBright = ColourHelper.isBrightSet(mostPopRgb);
		boolean secIsBright = ColourHelper.isBrightSet(secMostPopRgb);
		if (popIsBright != secIsBright) {
			// Not black (identical in both sets - no need to do anything)
			if (mostPopRgb != secMostPopRgb) {
				// If the less popular colour is bright make it like the popular
				// one (favour half bright)
				if (secIsBright) {
					secMostPopRgb = ColourHelper.getClosestColour(secMostPopRgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
					// Most popular colour is bright, we'll need to darken it
					// (favour half bright)
				} else {
					mostPopRgb = ColourHelper.getClosestColour(mostPopRgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
				}
			}
		}
		return new int[] { mostPopRgb, secMostPopRgb };
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBrightSet(int mostPopularColour, int secondMostPopularColour) {
		boolean popIsBright = ColourHelper.isBrightSet(mostPopularColour);
		boolean secIsBright = ColourHelper.isBrightSet(secondMostPopularColour);
		if (popIsBright != secIsBright) {
			return false;
		}
		return popIsBright && secIsBright;
	}

	@Override
	public String toString() {
		return getCaption("attr_fav_half_bright");
	}
}
