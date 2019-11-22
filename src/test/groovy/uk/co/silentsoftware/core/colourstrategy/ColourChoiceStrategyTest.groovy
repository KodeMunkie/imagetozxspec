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
package uk.co.silentsoftware.core.colourstrategy

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import uk.co.silentsoftware.config.OptionsObject

class ColourChoiceStrategyTest {
	
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
		Assert.assertEquals((int)0xFFFFFFFF, result)
	}
	
	@Test(expected=UnsupportedOperationException.class)
	void testGigascreenPaletteStrategyColour() {
		ColourChoiceStrategy strategy = new GigaScreenPaletteStrategy()
		strategy.chooseBestPaletteMatch(0xFFBBCCDDi, createPopularColours())
	}
}
