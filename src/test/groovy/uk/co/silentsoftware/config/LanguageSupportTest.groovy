package uk.co.silentsoftware.config

import org.junit.Assert
import org.junit.Test

class LanguageSupportTest {

	@Test
	void getCaptionIsUtf8() {
		// Indirectly tests UTF8Control.java
		Assert.assertEquals("Random Text+€€€£££ééé", LanguageSupport.getCaption("testutf8"))
	}
}
