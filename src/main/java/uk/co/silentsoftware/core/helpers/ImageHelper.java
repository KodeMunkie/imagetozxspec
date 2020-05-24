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
package uk.co.silentsoftware.core.helpers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Helper class for basic image manipulation
 */
public final class ImageHelper {

	private static final Logger log = LoggerFactory.getLogger(ImageHelper.class);

	/**
	 * Private constructor since we want static use only
	 */
	private ImageHelper() {}

	/**
	 * Scale an image to a given width and height with fast scaling
	 * 
	 * @param img the image to scale
	 * @param width the width to scale to, or -1 to use full Spectrum width
	 * @param height the height to scale to, or -1 to use full Spectrum height
	 * @return the resized image
	 */
	public static BufferedImage quickScaleImage(Image img, int width, int height) {
		return scaleImage(img, width, height, BufferedImage.SCALE_FAST);
	}

	/**
	 * Scale an image to a given width and height with smooth scaling
	 *
	 * @param img the image to scale
	 * @param width the width to scale to, or -1 to use full Spectrum width
	 * @param height the height to scale to, or -1 to use full Spectrum height
	 * @return the resized image
	 */
	public static BufferedImage smoothScaleImage(Image img, int width, int height) {
		return scaleImage(img, width, height, BufferedImage.SCALE_SMOOTH);
	}

	/**
	 * Scale an image to a given width and height with the given scaling mode
	 *
	 * @param img the image to scale
	 * @param width the width to scale to, or -1 to use full Spectrum width
	 * @param height the height to scale to, or -1 to use full Spectrum height
	 * @param scalingMode the BufferedImage scaling option
	 * @return the resized image
	 */
	private static BufferedImage scaleImage(Image img, int width, int height, int scalingMode) {

		int imgWidth = img.getWidth(null);
		int imgHeight = img.getHeight(null);

		// Skip if resize not required or dimensions are -1 (no resize)
		if (imgWidth == width && imgHeight == height) {
			return copyImage(img);
		}

		// TODO: Consider re-write in a formal way, using -1 as undefined is a bit shabby
		if (-1 == width && -1 == height) {
			width = SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE * ((imgWidth / SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE));
			height = SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE * ((imgHeight / SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE));
		}
		log.debug("Original width and height {}x{}, new width and height {}x{}", imgWidth, imgHeight, width, height);
		final Image scaled = img.getScaledInstance(width, height, scalingMode);
		if (-1 == width) {
			width = SpectrumDefaults.SCREEN_WIDTH;
		}
		if (-1 == height) {
			height = SpectrumDefaults.SCREEN_HEIGHT;
		}
		BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		copyImage(scaled, copy);
		return copy;
	}

	/**
	 * Convenience method for copying an image without passing in an image to
	 * copy to.
	 * 
	 * @param source the image to copy
	 * @return the copied image
	 */
	public static BufferedImage copyImage(final Image source) {

		if (source instanceof BufferedImage) {
			BufferedImage bi = ((BufferedImage)source);
			if (BufferedImage.TYPE_INT_RGB == bi.getType()) {	
				ColorModel cm = bi.getColorModel();
				return new BufferedImage(cm, bi.copyData(null), cm.isAlphaPremultiplied(), null);	
			}
		}
		BufferedImage copy = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_RGB);
		copyImage(source, copy);
		return copy;
	}

	/**
	 * Copies the source image to the destination buffered image
	 * 
	 * @param source the source image
	 * @param dest the destination image
	 */
	private static void copyImage(final Image source, BufferedImage dest) {
		Graphics2D g = dest.createGraphics();
		g.drawImage(source, null, null);
		g.dispose();
	}

	/**
	 * Copies the source image to the destination buffered image at the given coordinates
	 * 
	 * Important - does not check the point is valid and the destination image is large enough
	 * 
	 * @param source the source image
	 * @param dest the destination image
	 * @param p the point on the destination image to copy the source image to
	 */
	public static void copyImage(final Image source, BufferedImage dest, Point p) {
		Graphics g = dest.getGraphics();
		g.drawImage(source, p.x, p.y, null);
		g.dispose();
	}


	/**
	 * Prepares the main preview window image prior to rendering (puts the preprocessed image left, processed
	 * image right, does any scaling necessary to fit the screen)
	 *
	 * @param preprocessed the preprocessed image
	 * @param result the result image
	 * @param  fpsString the current processing frame rate
	 * @return the image ready to render in the main window
	 */
	public static BufferedImage prepareMainPreview(BufferedImage preprocessed, BufferedImage result, float fpsString) {
		BufferedImage left = preprocessed;
		BufferedImage right = result;
		int width = result.getWidth();
		int height = result.getHeight();
		boolean needsScaling = false;
		while (imageOutOfScreenBounds(width*2, height)) {
			width /=2;
			height /=2;
			needsScaling = true;
		}
		if (needsScaling) {
			left = ImageHelper.quickScaleImage(preprocessed, width, height);
			right = ImageHelper.smoothScaleImage(result, width, height); // Smooth scale needed in order to maintain dither patterns in preview
		}
		BufferedImage mainPreviewImage = new BufferedImage(width*2, height, result.getType());
		Graphics preBuffer = mainPreviewImage.createGraphics();
		BufferedImage leftResized = left;
		if (OptionsObject.INTERLACED == OptionsObject.getInstance().getScaling()) {
			leftResized = ImageHelper.quickScaleImage(left, width, height);
		}
		preBuffer.drawImage(leftResized, 0, 0, null);
		preBuffer.drawImage(right, width, 0, null);
		if (OptionsObject.getInstance().getFpsCounter()) {
			preBuffer.setColor(Color.WHITE);
			preBuffer.drawString(getCaption("main_fps_overlay") + " " + fpsString, 10, 20);
		}
		preBuffer.dispose();
		return mainPreviewImage;
	}

	/**
	 * Determines if the given image will render outside of the screen bounds
	 *
	 * @param imageWidth the image width
	 * @param imageHeight the image height
	 * @return whether it is outside of the screen
	 */
	private static boolean imageOutOfScreenBounds(int imageWidth, int imageHeight) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return (width <= imageWidth || height <= imageHeight);
	}
}
