/* Image to ZX Spec
 * Copyright (C) 2017 Silent Software Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.converters.image.processors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Writes a label onto a given image used in previews 
 */
class PreviewLabeller {
	
	/**
	 * Draws the given label onto the output image in the top left corner in white.
	 * 
	 * @param output the image to write the label on
	 * @param label the label, usually the preview strategy name, to write onto the image
	 */
	static void drawPreviewStrategyWithName(BufferedImage output, String label) {
		Graphics g = output.getGraphics();
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		int height = g.getFontMetrics().getHeight();
		int width = g.getFontMetrics().stringWidth(label);
		g.setColor(Color.WHITE);
		g.fillRect(0, 20-g.getFontMetrics().getAscent(), width, height);
		g.setColor(Color.BLACK);
		g.drawString(label,0,20);
		g.dispose();
	}
}
