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
package uk.co.silentsoftware.ui.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.ui.ImageToZxSpec;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Opens a message dialog containing the about information
 */
public class AboutListener implements ActionListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final static String HOME_PAGE = "http://www.silentsoftware.co.uk";
	
	private static final String COFFEE_LINK = "https://paypal.me/silentsoftware";
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {		
		long maxHeap = Runtime.getRuntime().maxMemory();
		int cpus = Runtime.getRuntime().availableProcessors();
		JTextPane aboutField = new JTextPane();
		aboutField.setContentType("text/html");
		aboutField.setText("Image to ZX Spec is a simple to use program which applies a Sinclair ZX Spectrum<br>" +
				"effect to images, creates Spectrum playable slideshows from images or \"video\"<br>" +
				"from compatible video files.<br><br>"+
				"This software is copyright Silent Software (Benjamin Brown), uses Caprica Software<br>"+
				"Limited's VLCJ and Art Clarke's Humble Video as well as other open source libraries.<br>"+
				"See the included licences.txt for full details.<br><br>" +
				"Processors: "+cpus+"<br>"+
				"Total Java Memory: "+maxHeap/1024/1024+"MB<br><br>"+
				"Visit Silent Software at <a href='"+HOME_PAGE+"'>"+HOME_PAGE+"</a><br><br>"+
				"If you like this program and find it useful don't forget to <a href='"+COFFEE_LINK+"'>buy the developer a coffee!</a>");
		aboutField.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		aboutField.setEditable(false);
		aboutField.setOpaque(false);
		aboutField.addHyperlinkListener(e1 -> {
            if (HyperlinkEvent.EventType.ACTIVATED == e1.getEventType())	{
                openLink(e1.getURL());
            }
        });
		JOptionPane.showMessageDialog(null, aboutField, "About Image to ZX Spec", JOptionPane.INFORMATION_MESSAGE, ImageToZxSpec.IMAGE_ICON);
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
