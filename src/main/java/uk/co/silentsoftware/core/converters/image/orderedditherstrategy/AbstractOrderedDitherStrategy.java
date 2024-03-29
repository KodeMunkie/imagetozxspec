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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.processors.ImageConverter;
import uk.co.silentsoftware.core.converters.image.processors.OrderedDitherConverterImpl;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.*;
import java.awt.image.ColorModel;

/**
 * Base class for applying an ordered dither strategy
 */
public abstract class AbstractOrderedDitherStrategy implements OrderedDitherStrategy, DitherStrategy {

	/**
	 * Applies the sub class' dither coefficients to
	 * the given rgb matrix from an image.
	 * The source pixels array and the coefficient's array
	 * must be the same length but this is not tested.
	 * 
	 * @param rgbStrip the strip to apply the coefficients to
	 * @return the dithered pixels
	 */
	public int[] applyDither(int[] rgbStrip) {
		OptionsObject oo = OptionsObject.getInstance();
		int[] coeffs = getCoefficients();
		int[] colDist = ColourHelper.getAverageColourDistance(oo.getColourMode().getPalette());
		for(int i=0; i<rgbStrip.length; i++) {
			int[] rgb = ColourHelper.intToRgbComponents(rgbStrip[i]);
			float adjustedCoeff = ((float)(coeffs[i])/(float)getCoefficients().length)-0.5f;
			int oldRed = ColourHelper.correctRange(Math.round(rgb[0]+(colDist[0]*adjustedCoeff)));
			int oldGreen = ColourHelper.correctRange(Math.round(rgb[1]+(colDist[1]*adjustedCoeff)));
			int oldBlue = ColourHelper.correctRange(Math.round(rgb[2]+(colDist[2]*adjustedCoeff)));
			rgbStrip[i] = new Color(oldRed, oldGreen, oldBlue).getRGB();
		}
		return rgbStrip;
	}
	
	/**
	 * Retrieves the coefficients to apply
	 * @return the coefficients
	 */
	public abstract int[] getCoefficients();
	
	/**
	 * Retrieves the width of the matrix (1 dimension only)
	 * @return the matrix width
	 */
	public abstract int getMatrixWidth();
	
	/**
	 * Retrieves the height of the matrix (1 dimension only)
	 * @return the matrix height
	 */
	public abstract int getMatrixHeight();

	@Override
	public ImageConverter createImageConverter() {
		return new OrderedDitherConverterImpl();
	}

	@Override
	public ImageConverter createPreviewImageConverter(ColourChoiceStrategy colourChoiceStrategy) {
		return new OrderedDitherConverterImpl(this, colourChoiceStrategy);
	}
}
