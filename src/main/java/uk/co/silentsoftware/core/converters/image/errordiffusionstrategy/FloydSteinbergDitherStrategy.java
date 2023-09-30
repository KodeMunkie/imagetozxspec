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
package uk.co.silentsoftware.core.converters.image.errordiffusionstrategy;

import java.awt.image.BufferedImage;
import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import uk.co.silentsoftware.config.OptionsObject;

/**
 * Implementation of the Floyd-Steinberg error diffusion algorithm
 */
public class FloydSteinbergDitherStrategy extends AbstractErrorDiffusionDitherStrategy {

	public static final float SIXTEENTH = 1f / 16f;
	public static final float THREE_SIXTEENTHS = 3f / 16f;
	public static final float FIVE_SIXTEENTHS = 5f / 16f;
	public static final float SEVEN_SIXTEENTHS = 7f / 16f;

	/*
	 * {@inheritDoc}
	 */
	public void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer boundX, Integer boundY) {

		int multiplier = 1;
		if (OptionsObject.getInstance().getSerpentine() && y % 2 == 0) {
			multiplier = -1;
		}		
		if (isInBounds(output, x+multiplier, y, boundX, boundY)) {output.setRGB(x+multiplier, y, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+multiplier, y), SEVEN_SIXTEENTHS));}
		if (isInBounds(output, x-multiplier, y+1, boundX, boundY)) {output.setRGB(x-multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-multiplier, y+1), THREE_SIXTEENTHS));}
		if (isInBounds(output, x, y+1, boundX, boundY)) {output.setRGB(x, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x, y+1), FIVE_SIXTEENTHS));}
		if (isInBounds(output, x+multiplier, y+1, boundX, boundY)) {output.setRGB(x+multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+multiplier, y+1), SIXTEENTH));}
	}

	@Override
	public String toString() {
		return "Floyd-Steinberg ("+getCaption("error_diffusion")+")";
	}
}
