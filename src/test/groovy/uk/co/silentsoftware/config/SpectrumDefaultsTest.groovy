package uk.co.silentsoftware.config

import org.junit.Assert
import org.junit.Test
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute
import uk.co.silentsoftware.core.helpers.ColourHelper

class SpectrumDefaultsTest {

	@Test
	void testGeneratedGigascreenAttributesThresholdsAtExactly4UniqueColours() {
		GigaScreenAttribute[] gigaScreenAttribute = SpectrumDefaults.generateGigascreenAttributes(SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT, SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT)
		Assert.assertEquals(784, gigaScreenAttribute.length) // 784 combinations of exactly 4 unique colours (not 3 or less) from palette of 102
	}
	
	@Test
	void testFullGigascreenPalette() throws InterruptedException {
		Set<Integer> colours = new TreeSet<>()
		for (int pal1 : SpectrumDefaults.SPECTRUM_COLOURS_ALL) {
			for (int pal2 : SpectrumDefaults.SPECTRUM_COLOURS_ALL) {
				int[] rgbS1 = ColourHelper.intToRgbComponents(pal1)
				int[] rgbS2 = ColourHelper.intToRgbComponents(pal2)
				colours.add(ColourHelper.componentsToAlphaRgb((int)((rgbS1[0] + rgbS2[0]) / 2), (int)((rgbS1[1] + rgbS2[1]) / 2), (int)((rgbS1[2] + rgbS2[2]) / 2)))
			}
		}
		Assert.assertEquals(102, colours.size())
	}
}

