package uk.co.silentsoftware.config

import org.junit.Assert
import org.junit.Test

class PreferencesServiceTest {

	@Test
	void testSaveThrowsNoExceptions() {
		PreferencesService.save()
	}
	
	@Test
	void testLoadThrowsNoExceptions() {
		PreferencesService.save()
		Optional<OptionsObject> options = PreferencesService.load()
		Assert.assertNotNull(options)
	}
}
