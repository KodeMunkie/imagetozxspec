package uk.co.silentsoftware.core.attributestrategy

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import uk.co.silentsoftware.config.OptionsObject

class FavourBrightAttributeStrategyTest {
	
	@Before
	void setUp() {
		OptionsObject.getInstance().setPreferDetail(false)
	}
	
	/**
	 * Both are half bright so we leave them as is (we are not forcing a change of set)
	 */
	@Test
	void testEnforceAttributeRuleWithBothHalfBright() {
		AttributeStrategy strategy = new FavourBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFCDCD00i, 0xFFCDCDCDi)
		Assert.assertEquals(0xFFCDCD00i, result[0])
		Assert.assertEquals(0xFFCDCDCDi, result[1])
	}
	
	/**
	 * Both are already bright
	 */
	@Test
	void testEnforceAttributeRuleWithBothBright() {
		AttributeStrategy strategy = new FavourBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFFFFF00i, 0xFFFFFFFFi)
		Assert.assertEquals(0xFFFFFF00i,result[0])
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
	
	/**
	 * Favour switching the half bright to bright set
	 */
	@Test
	void testEnforceAttributeRuleWithOneHalfBrightOneBright() {
		AttributeStrategy strategy = new FavourBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFF0000CDi, 0xFFFFFFFFi)
		Assert.assertEquals(0xFF0000FFi,result[0]) // Switched to bright
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
}
