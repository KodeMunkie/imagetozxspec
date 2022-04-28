/* Image to ZX Spec
 * Copyright (C) 2022 Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.config;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic text only internationalisation support.
 * Falls back to country if the specific dialect
 * cannot be loaded.
 */
public class LanguageSupport {

	private static Logger log = LoggerFactory.getLogger(LanguageSupport.class);
	
	/**
	 * The base file name for the internalisation
	 */
	private final static String MESSAGES_FILE = "Messages";
	
	/**
	 * The captions from the loaded file. Ordinarily the properties file
	 * is ISO charset specific and would be saved in the local machine 
	 * locale but for ease of use UTF8 is used for all of them, this
	 * prevents accidental char set changes.
	 */
	private static ResourceBundle captions;
	static {
		try {
			// Try full language/country code
			captions = ResourceBundle.getBundle(MESSAGES_FILE, Locale.getDefault(), new UTF8Control());
		} catch (MissingResourceException mre) {
			log.warn("Unable to find properties for captions", mre);
		}
		if (captions == null) {
			try {
				// Fallback to English if nothing else suitable
				captions = ResourceBundle.getBundle(MESSAGES_FILE, new Locale("en"), new UTF8Control());
			} catch (MissingResourceException ignore) {
				log.error("All captions are missing!");
			}
		}
	}
	
	/**
	 * Retrieves the translation under the specified key
	 * 
	 * @param key the key to get the translation for
	 * @return the translation for this system's locale, or the key if it cannot be found
	 */
	public static String getCaption(String key) {
		String translation = null;
		if (captions != null) {
			try {
				translation = captions.getString(key);
			} catch(MissingResourceException ignore){}
		}
		// We're missing a key's value, use the key as the value :(
		if (translation == null || translation.trim().length()==0) {
			translation=key;
		}
		return translation;
	}
}
