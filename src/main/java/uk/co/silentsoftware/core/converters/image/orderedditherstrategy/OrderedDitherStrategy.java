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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy;

import uk.co.silentsoftware.core.converters.image.DitherStrategy;


/**
 * Interface for ordered dither algorithms
 */
public interface OrderedDitherStrategy extends DitherStrategy {

	/**
	 * The matrix width of this dither
	 * (just 1 dimension)
	 * @return the width
	 */
	public int getMatrixWidth();
	
	/**
	 * The matrix height of this dither
	 * (just 1 dimension)
	 * @return the height
	 */
	public int getMatrixHeight();
	
	/**
	 * Applies the dither to the set of rgb pixels
	 * @param outRgb the rgb pixels to apply the dither to
	 * @return the dither applied to a set of rgb pixels
	 */
	public int[] applyDither(int[] outRgb);
}
