package uk.co.silentsoftware.core.attributestrategy

import org.junit.Assert
import org.junit.Test

import uk.co.silentsoftware.config.SpectrumDefaults

class GigaScreenAttributeStrategyTest {

	@Test
	void testBrightPaletteStrategy() {
		GigaScreenAttributeStrategy strategy = new GigaScreenBrightPaletteStrategy()
		Assert.assertArrayEquals(SpectrumDefaults.GIGASCREEN_BRIGHT_ATTRIBUTES, strategy.getPalette())
	}
	
	@Test
	void testHalfBrightPaletteStrategy() {
		GigaScreenAttributeStrategy strategy = new GigaScreenHalfBrightPaletteStrategy()
		Assert.assertArrayEquals(SpectrumDefaults.GIGASCREEN_HALF_BRIGHT_ATTRIBUTES, strategy.getPalette())
	}
	
	@Test
	void testMixedPaletteStrategy() {
		GigaScreenAttributeStrategy strategy = new GigaScreenMixedPaletteStrategy()
		Assert.assertArrayEquals(SpectrumDefaults.GIGASCREEN_MIXED_ATTRIBUTES, strategy.getPalette())
	}
	
}
