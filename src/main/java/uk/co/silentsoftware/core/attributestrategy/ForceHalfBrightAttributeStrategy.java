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
package uk.co.silentsoftware.core.attributestrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * Returns the half brightness variant of any colours passed in regardless of
 * the closest real colour in the spectrum palette i.e. two colours that
 * ordinarily would both be in the bright set would be converted to half bright.
 */
public class ForceHalfBrightAttributeStrategy implements AttributeStrategy {

	/*
	 * {@inheritDoc}
	 */
	@Override
	public int[] enforceAttributeRule(int mostPopRgb, int secMostPopRgb) {

		// Get the closest half bright colours.
		mostPopRgb = ColourHelper.getClosestColour(mostPopRgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
		secMostPopRgb = ColourHelper.getClosestColour(secMostPopRgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);

		return new int[] { mostPopRgb, secMostPopRgb };
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBrightSet(int mostPopularColour, int secondMostPopularColour) {
		return false;
	}

	@Override
	public String toString() {
		return getCaption("attr_force_half_bright");
	}
}
