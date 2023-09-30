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

/**
 * Special low error dither strategy especially for Spectrum image conversion
 * and only (currently) in use in this program, based on the Atkinson distribution
 * but using only 25% error distribution. This strategy tends to get the best
 * results in most cases, however images with high detail but similar colours
 * (e.g. blues underwater/red light on a face etc etc) may lose some detail depending
 * on pre process contrast. The trade off is that the error distribution is far less 
 * likely to propagate to surrounding *ZX Spectrum attribute blocks* resulting in
 * less colour clash than the classical diffusion algorithms.
 * 
 * This strategy's distribution of error was by Bill Atkinson but improved by me, 
 * Benjamin Brown, if anybody feels like documenting/publishing it :)
 */
public class LowErrorAtkinsonDitherStrategy extends AbstractErrorDiffusionDitherStrategy {

	public final static float TWENTY_FOURTH = 1f/24f;
	
	/*
	 * {@inheritDoc}
	 */
	public void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer boundX, Integer boundY) {
		if (isInBounds(output, x+1, y, boundX, boundY)) {output.setRGB(x+1, y, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+1, y), TWENTY_FOURTH));}
		if (isInBounds(output, x+2, y, boundX, boundY)) {output.setRGB(x+2, y, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+2, y), TWENTY_FOURTH));}
		if (isInBounds(output, x-1, y+1, boundX, boundY)) {output.setRGB(x-1, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-1, y+1), TWENTY_FOURTH));}
		if (isInBounds(output, x, y+1, boundX, boundY)) {output.setRGB(x, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x, y+1), TWENTY_FOURTH));}
		if (isInBounds(output, x+1, y+1, boundX, boundY)) {output.setRGB(x+1, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+1, y+1), TWENTY_FOURTH));}
		if (isInBounds(output, x, y+2, boundX, boundY)) {output.setRGB(x, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x, y+2), TWENTY_FOURTH));}
	}
	
	@Override
	public String toString() {
		return "Low Error Atkinson ("+getCaption("error_diffusion")+")";
	}
}
