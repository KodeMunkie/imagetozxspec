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

import uk.co.silentsoftware.core.converters.image.DitherStrategy;

/**
 * Interface for error diffusion strategies
 */
public interface ErrorDiffusionDitherStrategy extends DitherStrategy {

	/**
	 * Distributes the error on the output image at the 
	 * given x,y, using the difference between the original
	 * (old) pixel and the new pixel.
	 * 
	 * @param output the output image
	 * @param oldPixel the original pixel
	 * @param newPixel the new pixel
	 * @param x the x coordinate for the diffused pixel
	 * @param y the y coordinate for the diffused pixel
	 * @param boundX the x boundary of the image or attribute block in the range 0 to length
	 * @param boundY the y boundary of the image or attribute block in the range 0 to length 
	 */
	public void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer boundX, Integer boundY);	
}
