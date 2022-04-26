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
package uk.co.silentsoftware.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.PreferencesService;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Opens a one off Coffee Dialog if the app has been started a
 * certain number of times. 
 */
class CoffeeDialog {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final int STARTS_BEFORE_COFFEE = 10;
	private static final String COFFEE_LINK = "https://paypal.me/silentsoftware";
	private static final OptionsObject optionsObject = OptionsObject.getInstance();
	
	void showPopupIfNecessary() {
		log.debug("Application starts: {}", optionsObject.getStarts());

		// Don't show popup if the user has actually read the text
		if (optionsObject.getStarts() == -1) {
			return;
		}
		optionsObject.setStarts((optionsObject.getStarts() + 1));
		if (optionsObject.getStarts() > STARTS_BEFORE_COFFEE) {
			JTextPane aboutField = new JTextPane();
			aboutField.setContentType("text/html");
			aboutField.setText("<h2>You seem to be enjoying Image to ZX Spec!</h2>" +
					"Did you know Image to ZX Spec has been developed for the last 13 years?<br><br>" +
					"In that time just one person kindly donated £5 to the developer's coffee buying<br>" +
					"budget so please consider a contribution of any amount!<br>" +
					"<h3><a href='" + COFFEE_LINK + "'>Buy the developer a coffee</a></h3>" +
					"This popup will now continue to show on each start should you want to buy a<br>" +
					"a coffee later. Incidentally, if you read this far, just clicking the buy the<br>" +
					"developer a coffee link will disable this popup in future.<br><br>" +
					"<b>Thank you in advance for your support.</b>");
			aboutField.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			aboutField.setEditable(false);
			aboutField.setOpaque(false);
			aboutField.addHyperlinkListener(e -> {
				if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
					openLink(e.getURL());
					// Even if not donated we disable the popup because I'm a nice guy and the user
					// spent the time to at least read the entire text.
					optionsObject.setStarts(-1);
				}
			});
			JOptionPane.showMessageDialog(null, aboutField, "Information", JOptionPane.INFORMATION_MESSAGE, ImageToZxSpec.IMAGE_ICON);
		}
		PreferencesService.save();		
	}
	
	/**
	 * Opens the given URL in the default browser
	 * @param url the url to open
	 */
	private void openLink(URL url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(url.toURI());
			} catch (URISyntaxException | IOException e) {
				log.error("Unable to open URL {}",url);
			}
		}
	}
}
