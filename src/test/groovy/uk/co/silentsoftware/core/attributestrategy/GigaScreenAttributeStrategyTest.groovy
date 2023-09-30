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
