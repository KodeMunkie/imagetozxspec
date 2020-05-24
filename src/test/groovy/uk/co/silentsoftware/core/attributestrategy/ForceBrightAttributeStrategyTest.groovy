/* Image to ZX Spec
 * Copyright (C) 2020 Silent Software (Benjamin Brown)
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
import org.junit.Before
import org.junit.Test

import uk.co.silentsoftware.config.OptionsObject

class ForceBrightAttributeStrategyTest {

	/**
	 * Both are half bright so we leave them as is (we are not forcing a change of set)
	 */
	@Test
	void testEnforceAttributeRuleWithBothHalfBright() {
		AttributeStrategy strategy = new ForceBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFCDCD00i, 0xFFCDCDCDi)
		Assert.assertEquals(0xFFFFFF00i,result[0]) // switch both to bright
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
	
	/**
	 * Both are already bright
	 */
	@Test
	void testEnforceAttributeRuleWithBothBright() {
		AttributeStrategy strategy = new ForceBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFFFFF00i, 0xFFFFFFFFi)
		Assert.assertEquals(0xFFFFFF00i,result[0])
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
	
	/**
	 * Force both to bright
	 */
	@Test
	void testEnforceAttributeRuleWithOneHalfBrightOneBright() {
		AttributeStrategy strategy = new ForceBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFF0000CDi, 0xFFFFFFFFi)
		Assert.assertEquals(0xFF0000FFi,result[0])// Switch to bright
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
}
