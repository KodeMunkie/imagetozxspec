package uk.co.silentsoftware.core.attributestrategy

import org.junit.Assert
import org.junit.Test

class FavourHalfBrightAttributeStrategyTest {
	
	/**
	 * Both are already half bright
	 */
	@Test
	void testEnforceAttributeRuleWithBothHalfBright() {
		AttributeStrategy strategy = new FavourHalfBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFCDCD00i, 0xFFCDCDCDi)
		Assert.assertEquals(0xFFCDCD00i,result[0])
		Assert.assertEquals(0xFFCDCDCDi,result[1])
	}
	
	/**
	 * Both are bright so we leave them as is (we are not forcing a change of set)
	 */
	@Test
	void testEnforceAttributeRuleWithBothBright() {
		AttributeStrategy strategy = new FavourHalfBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFFFFFF00i, 0xFFFFFFFFi)
		Assert.assertEquals(0xFFFFFF00i,result[0])
		Assert.assertEquals(0xFFFFFFFFi,result[1])
	}
	
	/**
	 * Favour switching the bright to half bright set
	 */
	@Test
	void testEnforceAttributeRuleWithOneHalfBrightOneBright() {
		AttributeStrategy strategy = new FavourHalfBrightAttributeStrategy()
		int[] result = strategy.enforceAttributeRule(0xFF0000CDi, 0xFFFFFFFFi)
		Assert.assertEquals(0xFF0000CDi,result[0])
		Assert.assertEquals(0xFFCDCDCDi,result[1]) // Switched to half bright
	}
}
