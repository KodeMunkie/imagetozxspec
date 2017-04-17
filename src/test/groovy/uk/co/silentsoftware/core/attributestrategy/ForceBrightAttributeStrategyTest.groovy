package uk.co.silentsoftware.core.attributestrategy

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import uk.co.silentsoftware.config.OptionsObject

class ForceBrightAttributeStrategyTest {
	
	@Before
	void setUp() {
		OptionsObject.getInstance().setPreferDetail(false)
	}
	
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
