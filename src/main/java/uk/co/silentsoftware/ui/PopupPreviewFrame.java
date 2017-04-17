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
package uk.co.silentsoftware.ui;

import uk.co.silentsoftware.config.LanguageSupport;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.ScalingObject;
import uk.co.silentsoftware.ui.ImageToZxSpec.UiCallback;
import uk.co.silentsoftware.ui.listener.CustomDropTargetListener;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.image.BufferedImage;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Frame to display many preview results
 * This frame has a fixed width and height that is
 * 4 Spectrum images across by 4 in height to cater
 * for all dither strategies currently available.
 */
public class PopupPreviewFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 768;
	private static final int HEIGHT = 1152;
	private static final int PANEL_HEIGHT = 640;
	private static BufferedImage previewImage;
	private static int imagePositionX = 0;
	private static int imagePositionY = 0;
	private static JPanel imagePanel;
	private int maxWidth;
	private int maxHeight;
	private JScrollPane scrollPane;
	
	/**
	 * Dither preview frame that locks the resize
	 * to the fixed dimensions required for all dithers
	 */
	PopupPreviewFrame(UiCallback uiCallback) {
		setTitle(getCaption("preview_title"));
		setIconImage(ImageToZxSpec.IMAGE_ICON.getImage());
		createPreviewImagePanel();
		setDropTarget(new DropTarget(this, new CustomDropTargetListener(uiCallback)));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createRefreshButton(uiCallback), BorderLayout.PAGE_START);
		getContentPane().add(createScrollPane(), BorderLayout.CENTER);
		setPreferredSize(new Dimension(calculateWidth(), calculateHeight()));
		setResizable(false);
		pack();
	}
	
	/**
	 * Calculates the frame width
	 * 
	 * @return the frame width
	 */
	private int calculateWidth() {
		Insets insets = this.getInsets();
		return WIDTH+insets.left+insets.right+scrollPane.getVerticalScrollBar().getSize().width+2;
	}
	
	/**
	 * Calculates the frame height
	 * 
	 * @return the frame height
	 */
	private int calculateHeight() {
		Insets insets = this.getInsets();
		return PANEL_HEIGHT+insets.top+insets.bottom;
	}
	
	/**
	 * Creates the scroll pane for the frame
	 * 
	 * @return the scroll pane
	 */
	private JScrollPane createScrollPane() {
		scrollPane = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}
	
	/**
	 * Creates a refresh button to manually refresh the frame's
	 * dither content
	 * 
	 * @param uiCallback the callback to tell the main ui to re-generate the preview
	 * @return the refresh button
	 */
	private Component createRefreshButton(UiCallback uiCallback) {
		JButton refreshButton = new JButton(getCaption("preview_refresh"));
		refreshButton.addActionListener(e -> uiCallback.processPopupPreview());
		return refreshButton;
	}

	/**
	 * Creates a preview image panel that has an overridden
	 * paint context for rendering the preview
	 * The reference to the panel is not returned but is a class
	 * variable.
	 */
	private void createPreviewImagePanel() {
		previewImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		imagePanel = new JPanel() {
			private static final long serialVersionUID = 1L;
		
			public void paint(Graphics g) {
				g.drawImage(previewImage, 0, 0, null);
			}		
		};
		imagePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}

	/**
	 * Repaint method to allow repainting while threads
	 * are still processing the results to draw on this
	 * dialog
	 */
	public static void repaintImage() {
		imagePanel.repaint(100);
	}

	/**
	 * Retrieves the next point on the dither preview image
	 * that should be drawn on by the preview controller. 
	 * Not synchronized since this is only called by the one
	 * controlling thread.
	 * 
	 * @return the point to draw at
	 */
	public static Point getPoint() {
		final ScalingObject scaleObject = OptionsObject.getInstance().getZXDefaultScaling();
		int singleImageWidth = scaleObject.getWidth();
		int singleImageHeight = scaleObject.getHeight();
		if (imagePositionX+singleImageWidth > WIDTH) {
			imagePositionY+=singleImageHeight;
			imagePositionX = 0;
		}
		Point p = new Point(imagePositionX, imagePositionY);
		imagePositionX+=singleImageWidth;
		if (imagePositionY >= HEIGHT) {
			imagePositionX = 0;
			imagePositionY = 0;
		}
		return p;
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		maxWidth = calculateWidth();
		maxHeight = calculateHeight();
		setPreferredSize(new Dimension(maxWidth, maxHeight));
		setMaximumSize(new Dimension(maxWidth, maxHeight));
		pack();
	}

	/**
	 * Resets the current point to display at on the panel
	 * as provided by getPoint() to 0,0
	 */
	public static void reset() {
		imagePositionX = 0;
		imagePositionY = 0;
		previewImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		drawPreviewWaitText();
	}
	
	/**
	 * Draws a blank background on the panel whilst no results exist
	 */
	private static void drawPreviewWaitText() {
		imagePanel.getGraphics().setColor(Color.BLACK);
		imagePanel.getGraphics().clearRect(0, 0, WIDTH, HEIGHT);
		imagePanel.getGraphics().setColor(Color.WHITE);
		imagePanel.getGraphics().drawString(LanguageSupport.getCaption("main_working"), WIDTH/3, HEIGHT/4);
	}

	/**
	 * Retrieves the dither preview image to draw on 
	 * 
	 * @return the preview image
	 */
	public static BufferedImage getPreviewImage() {
		return previewImage;
	}
}
