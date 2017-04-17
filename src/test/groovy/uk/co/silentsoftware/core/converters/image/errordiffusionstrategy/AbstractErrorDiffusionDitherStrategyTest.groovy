package uk.co.silentsoftware.core.converters.image.errordiffusionstrategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.AbstractErrorDiffusionDitherStrategy

import java.awt.image.BufferedImage;

class AbstractErrorDiffusionDitherStrategyTest {

	@Before
	public void setUp() {
		OptionsObject.getInstance().setPreferDetail(false);
	}
	
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
