package uk.co.silentsoftware.core.colourstrategy

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import uk.co.silentsoftware.config.OptionsObject

class ColourChoiceStrategyTest {

	@Before
	void setUp() {
		OptionsObject.getInstance().setPreferDetail(false)
	}
	
	static int[] createPopularColours() {
		[0xFFFF0000,0xFF0000FF] as int[] // red and blue
	}
	
	@Test
	void testFullPaletteStrategyColour() {
		ColourChoiceStrategy strategy = new FullPaletteStrategy()
		int result = strategy.chooseBestPaletteMatch(0xFFBBCCDDi, createPopularColours()) // blue hue getClosestColour
		Assert.assertEquals(0xFF0000FFi, result)
	}

	@Test
	void testMonochromePaletteStrategyColour() {
		ColourChoiceStrategy strategy = new MonochromePaletteStrategy()
		int result = strategy.chooseBestPaletteMatch(0xFFBBCCDDi, createPopularColours())

		// Just passes back the same value
		Assert.assertEquals(0xFFBBCCDDi, result)
	}
	
	@Test(expected=UnsupportedOperationException.class)
	void testGigascreenPaletteStrategyColour() {
		ColourChoiceStrategy strategy = new GigaScreenPaletteStrategy()
		strategy.chooseBestPaletteMatch(0xFFBBCCDDi, createPopularColours())
	}
}
