/* Image to ZX Spec
 * Copyright (C) 2019 Silent Software (Benjamin Brown)
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.AbstractErrorDiffusionDitherStrategy

import java.awt.image.BufferedImage;

class AbstractErrorDiffusionDitherStrategyTest {
	
	/**
	 * Old: 153,153,153. new: 51,51,51, diffusion is 0.2F, expected result: 173,173,173
	 * 
	 * Error: 153-51 = 102
	 * Diffused amount: 102*0.2=20.4
	 * Result: 153+20.4 (rounded) = 173  
	 */
	@Test
	void testCalculateAdjustedRGB() {
		AbstractErrorDiffusionDitherStrategy strategy = new AbstractErrorDiffusionDitherStrategy(){
			@Override
			void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer boundX, Integer boundY) {
				// Not needed
			}
		};
		int result = strategy.calculateAdjustedRGB(0xFF999999i, 0xFF333333i, 0xFF999999i, 0.2f);
		Assert.assertEquals(0xFFADADADi, result);
	}
}
