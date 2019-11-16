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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import uk.co.silentsoftware.config.OptionsObject
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy

class AbstractOrderedDitherStrategyTest {

	private AbstractOrderedDitherStrategy strategy
	
	@Before
	void setUp() {
		OptionsObject.getInstance().setPreferDetail(false)
		strategy = new AbstractOrderedDitherStrategy(){

			@Override
			int[] getCoefficients() {
				[0xFFFFFFFF, 0xFF000000,0xFF000000,0xFFFFFFFF] as int[]
			}

			@Override
			int getMatrixWidth() {
				2
			}

			@Override
			int getMatrixHeight() {
				2
			}
			
		}
	}
	
	private static int[] createRgbStrip() {
		[0xFF888888,0xFFCCCCCC,0xFFCCCCCC,0xFF888888] as int[]
	}

	@Test
	void testColourModes() {
		ColourChoiceStrategy[] strategies = OptionsObject.getInstance().getColourModes()
		Assert.assertEquals(FullPaletteStrategy.class, strategies[0].getClass())
		Assert.assertEquals(GigaScreenPaletteStrategy.class, strategies[1].getClass())
		Assert.assertEquals(MonochromePaletteStrategy.class, strategies[2].getClass())
	}
	
	@Test
	void testApplyDitherWithFullColour() {
		OptionsObject.getInstance().setColourMode(OptionsObject.getInstance().getColourModes()[0])
		int[] dither = strategy.applyDither(createRgbStrip())
		Assert.assertArrayEquals([0xFF2D2D2D,0xFF000000,0xFF000000,0xFF2D2D2D] as int[], dither)
	}
	
	@Test
	void testApplyDitherWithGigascreen() {
		OptionsObject.getInstance().setColourMode(OptionsObject.getInstance().getColourModes()[1])
		int[] dither = strategy.applyDither(createRgbStrip())
		Assert.assertArrayEquals([0xFF3E3E3E,0xFF000000,0xFF000000,0xFF3E3E3E] as int[], dither)
	}
	
	@Test
	void testApplyDitherWithMonochrome() {
		OptionsObject.getInstance().setColourMode(OptionsObject.getInstance().getColourModes()[2])
		int[] dither = strategy.applyDither(createRgbStrip())
		Assert.assertArrayEquals([0xFF282828,0xFF000000,0xFF000000,0xFF282828] as int[], dither)
	}
}
