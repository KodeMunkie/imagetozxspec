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
package uk.co.silentsoftware.core.helpers;

import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute.GigaScreenColour;
/**
 * Utility class to provide common colour functionality
 */
public final class ColourHelper {

	private static final int MAXIMUM_COMPONENT_VALUE = 255;

	private static final int PREFER_DETAIL_COMPONENT_BOUNDARY = 127;
	
	/**
	 * Private constructor since we want static use only
	 */
	private ColourHelper(){}
	
	/**
	 * Retrieves the spectrum colour most like the provided rgb colour
	 * 
	 * @param rgb the colour to get the spectrum colour for
	 * @return the spectrum colour
	 */
	public static int getClosestSpectrumColour(int rgb) {
		return getClosestColourWithDetail(rgb, SpectrumDefaults.SPECTRUM_COLOURS_ALL);
	}

	/**
	 * Retrieves the spectrum colour most like the provided rgb colour
	 * 
	 * @param red the red component to get the spectrum colour for
	 * @param green the green component to get the spectrum colour for
	 * @param blue the blue component to get the spectrum colour for
	 * @return the spectrum colour
	 */
	public static int getClosestSpectrumColour(int red, int green, int blue) {
		return getClosestColourWithDetail(red, green, blue, SpectrumDefaults.SPECTRUM_COLOURS_ALL, OptionsObject.getInstance().getPreferDetail());
	}

	/**
	 * Retrieves the spectrum bright colour most like the provided rgb colour
	 * 
	 * @param rgb the colour to get the spectrum colour for
	 * @return the spectrum bright colour
	 */
	public static int getClosestBrightSpectrumColour(int rgb) {
		return getClosestColourWithDetail(rgb, SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT);
	}

	/**
	 * Retrieves the spectrum half bright colour most like the provided rgb
	 * colour
	 * 
	 * @param rgb the colour to get the spectrum colour for
	 * @return the spectrum half bright colour
	 */
	public static int getClosestHalfBrightSpectrumColour(int rgb) {
		return getClosestColourWithDetail(rgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
	}

	/**
	 * Retrieves the reduced set spectrum half bright colour most like the
	 * provided rgb colour
	 * 
	 * @param rgb the colour to get the spectrum colour for
	 * @return the spectrum reduced set colour
	 */
	public static int getClosestReducedHalfBrightSpectrumColour(int rgb) {
		return getClosestColourWithDetail(rgb, SpectrumDefaults.SPECTRUM_COLOURS_REDUCED_HALF_BRIGHT);
	}

	/**
	 * Retrieves the Gigascreen colour most like the provided rgb colour
	 * 
	 * @param red the red component to get the gigascreen colour for
	 * @param green the green component to get the gigascreen colour for
	 * @param blue the blue component to get the gigascreen colour for
	 * 
	 * @return the gigascreen colour
	 */
	public static int getClosestGigascreenColour(int red, int green, int blue) {
		return getClosestColourWithDetail(red, green, blue, SpectrumDefaults.GIGASCREEN_COLOURS_ALL, OptionsObject.getInstance().getPreferDetail());
	}

	/**
	 * Retrieves the Gigascreen colour most like the provided rgb colour
	 * 
	 * @param rgb the colour to get the gigascreen colour for
	 * @return the gigascreen colour
	 */
	public static int getClosestGigascreenColour(int rgb) {
		return getClosestColourWithDetail(rgb, SpectrumDefaults.GIGASCREEN_COLOURS_ALL);
	}

	/**
	 * Gets the closest colour in the colourset for the provided rgb getClosestColour
	 *
	 * @param rgb the colour to find the closest match for
	 * @param colourSet the colourset to get the closest colour from
	 * @return the closest colour set golour
	 */
	private static int getClosestColourWithDetail(int rgb, int[] colourSet) {
		final int[] comps = intToRgbComponents(rgb);
		return getClosestColourWithDetail(comps[0], comps[1], comps[2], colourSet, OptionsObject.getInstance().getPreferDetail());
	}
	
	/**
	 * Retrieves the colour from the colourSet most like the provided rgb components
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @param colourSet the colour set from which to retrieve the closest colour
	 * @return the closest colour from the given colour set
	 */
	private static int getClosestColourWithDetail(int red, int green, int blue, int[] colourSet, boolean preferDetail) {

		 // If we prefer detail then make more of the darker shades black.
		if (preferDetail) {
			int white = colourSet[colourSet.length - 1];
			int black = colourSet[0];

			if (red < PREFER_DETAIL_COMPONENT_BOUNDARY
					&& green < PREFER_DETAIL_COMPONENT_BOUNDARY
					&& blue < PREFER_DETAIL_COMPONENT_BOUNDARY) {
				return black;
			}
			if (red >= PREFER_DETAIL_COMPONENT_BOUNDARY
					&& green >= PREFER_DETAIL_COMPONENT_BOUNDARY
					&& blue >= PREFER_DETAIL_COMPONENT_BOUNDARY) {
				return white;
			}
		}
		return getClosestColour(red, green, blue, colourSet);
	}

	/**
	 * Gets the closest colour distance (smallest sum of difference) from the gigascreen colours for the rgb components
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @param colours the gigascreen colours to search
	 * @return the difference as a value  greater than or equal to 0
	 */
	public static int getClosestColourDistanceForGigascreenColours(int red, int green, int blue, GigaScreenColour[] colours) {
		int bestMatch = Integer.MAX_VALUE;
		for (GigaScreenColour colour : colours) {
			final int[] colourSetComps = colour.getGigascreenColourRGB();
			int diff = Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
			bestMatch = Math.min(diff, bestMatch);
		}
		return bestMatch;
	}
	
	/**
	 * Gets the closest Gigascreen colour from a GigaScreenAttribute
	 * 
	 * @param rgb the rgb value to find the closest gigascreen colour for
	 * @param colourSet the attribute containing the colours
	 * @return the closest matching giga screen colour
	 */
	public static GigaScreenAttribute.GigaScreenColour getClosestGigaScreenColour(int rgb, GigaScreenAttribute colourSet) {
		final int[] comps = ColourHelper.intToRgbComponents(rgb);
		int bestMatch = Integer.MAX_VALUE;
		Integer closestMatchPaletteIndex = null;
		int[] palette = colourSet.getPalette();
		for (int paletteIndex = 0; paletteIndex < palette.length; ++paletteIndex) {
			int colour = palette[paletteIndex];
			final int[] colourSetComps = ColourHelper.intToRgbComponents(colour);
			int diff = Math.abs(comps[0] - colourSetComps[0]) + Math.abs(comps[1] - colourSetComps[1]) + Math.abs(comps[2] - colourSetComps[2]);
			if (diff < bestMatch) {
				closestMatchPaletteIndex = paletteIndex;
				bestMatch = diff;
			}
		}
		return colourSet.getGigaScreenColour(closestMatchPaletteIndex);
	}

	public static int getClosestColour(int originalAlphaRgb, int[] mostPopularRgbColours) {

		// Break the colours into their RGB components
		int[] originalRgbComps = ColourHelper.intToRgbComponents(originalAlphaRgb);
		return ColourHelper.getClosestColour(originalRgbComps[0], originalRgbComps[1], originalRgbComps[2], mostPopularRgbColours);
	}

	/**
	 * Gets the closest colour in the colourset for the provided rgb components
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @param colourSet the colours to search
	 * @return the closest colour
	 */
	private static int getClosestColour(int red, int green, int blue, int[] colourSet) {
		int bestMatch = Integer.MAX_VALUE;
		Integer closest = null;
		for (int colour : colourSet) {
			final int[] colourSetComps = intToRgbComponents(colour);
			int diff = Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
			if (diff <= bestMatch) {
				closest = colour;
				bestMatch = diff;
			}
		}
		return closest;
	}

	/**
	 * Colours an entire image using the given colourstrategy based on the
	 * original and output images. Colours the Spectrum attribute blocks by
	 * selecting xMax by yMax parts of the output image (i.e. usually 8x8
	 * pixels), chooses the most popular two colours. The colour choice strategy
	 * then decides how to colour individual pixels based on these two colours.
	 * 
	 * Note it is expected that this method will be called AFTER the pixels have
	 * been changed to Spectrum colours.
	 * 
	 * @param image the image to colour
	 * @param colourChoiceStrategy the colour choice strategy
	 * @return the modified image
	 */
	public static BufferedImage colourAttributes(BufferedImage image, ColourChoiceStrategy colourChoiceStrategy) {

		// Do not use bidimap because inverse map key values will be lost (i.e. many tallies will produce same key)	
		Map<Integer, Integer> map = new HashMap<>();
		List<TallyValue> tallyValues = new LinkedList<>();
		
		// Analyse block and choose the two most popular colours in attribute block
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= image.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= image.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= image.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				map.clear();
				tallyValues.clear();
				int outRgb[] = image.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);

				for (int rgb : outRgb) {
					final int[] comps = intToRgbComponents(rgb);
					int value = getClosestColour(comps[0], comps[1], comps[2], SpectrumDefaults.SPECTRUM_COLOURS_ALL);
					int count = 1;
					if (map.containsKey(value)) {
						count = map.get(value) + 1;
					}
					map.put(value, count);
				}
				map.keySet().stream().forEach(colour -> {
					Integer tally = map.get(colour);
					tallyValues.add(new TallyValue(colour, tally));
				});
				tallyValues.sort(TallyValue.TALLY_COMPARATOR);
				
				int mostPopularColour = tallyValues.get(0).getColour();
				int secondMostPopularColour = tallyValues.size()>1?tallyValues.get(1).getColour():mostPopularColour;
				
				// Enforce attribute favouritism rules on the two spectrum
				// attribute colours (fixes the problem that colours could be from both the bright
				// and half bright set).
				int[] correctedAlphaColours = OptionsObject.getInstance().getAttributeMode().enforceAttributeRule(mostPopularColour, secondMostPopularColour);
							
				// Replace all colours in attribute block (which can be any spectrum colours) with the just the popular two
				for (int i = 0; i < outRgb.length; ++i) {
					outRgb[i] = colourChoiceStrategy.getClosestColour(outRgb[i], correctedAlphaColours);
				}
				image.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb, 0, ATTRIBUTE_BLOCK_SIZE);
			}
		}
		return image;
	}

	/**
	 * Determines whether the colour is from the Spectrum's bright or half
	 * bright colour set.
	 * 
	 * @param rgb the colour to test
	 * @return whether this colour is in the bright set
	 */
	public static boolean isBrightSet(int rgb) {
		if (rgb == 0xFF000000) {
			return false;
		}
		for (int i = 0; i < SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT.length; ++i) {
			int def = SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[i];
			if (def == rgb) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Changes the contrast of an image
	 * 
	 * @see java.awt.image.RescaleOp
	 * 
	 * @param img the image to change the contrast of
	 * @param amount the amount to change it (scale factor)
	 * @return the modified image
	 */
	public static BufferedImage changeContrast(BufferedImage img, float amount) {
		if (amount == 1) {
			return img;
		}
		RescaleOp rescaleOp = new RescaleOp(amount, 0, null);
		return rescaleOp.filter(img, null);
	}

	/**
	 * Changes brightness by increasing all pixel values by a given amount
	 * 
	 * @see java.awt.image.RescaleOp
	 * 
	 * @param img the image to change the brightness of
	 * @param amount the amount to change it
	 * @return the modified image
	 */
	public static BufferedImage changeBrightness(BufferedImage img, float amount) {
		if (amount == 0) {
			return img;
		}
		RescaleOp rescaleOp = new RescaleOp(1, amount, null);
		return rescaleOp.filter(img, null);
	}

	/**
	 * Changes image saturation by a given amount
	 * 
	 * @param img the image to change
	 * @param amount the amount of saturation (0-1 range)
	 * @return the modified image
	 */
	public static BufferedImage changeSaturation(BufferedImage img, float amount) {
		if (amount == 0) {
			return img;
		}
		for (int y = 0; y < img.getHeight(); ++y) {
			for (int x = 0; x < img.getWidth(); ++x) {
				img.setRGB(x, y, changePixelSaturation(img.getRGB(x, y), amount));
			}
		}
		return img;
	}

	/**
	 * Changes the saturation of an individual pixel by the given amount (0-1
	 * range)
	 * 
	 * @param pixel the pixel rgb to saturate
	 * @param amount the amount to saturate
	 * @return the modified rgb pixel
	 */
	private static int changePixelSaturation(int pixel, float amount) {
		int[] rgb = intToRgbComponents(pixel);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hsb[1] += amount;
		float saturation = correctRange(hsb[1], 0, 1);
		return Color.HSBtoRGB(hsb[0], saturation, hsb[2]);
	}

	/**
	 * Ensures a value is within a given range. If it exceeds or is below it is
	 * set to the high value or low value respectively
	 * 
	 * @param value the value to test
	 * @param low the low value
	 * @param high the high value
	 * @return the ranged valued
	 */
	private static int correctRange(int value, int low, int high) {
		if (value < low) {
			return low;
		}
		if (value > high) {
			return high;
		}
		return value;
	}
	
	/**
	 * Ensures a value is within a given range. If it exceeds or is below it is
	 * set to the high value or low value respectively
	 * 
	 * @param value the value to test
	 * @param low the low value
	 * @param high the high value
	 * @return the ranged valued
	 */
	private static float correctRange(float value, float low, float high) {
		if (value < low) {
			return low;
		}
		if (value > high) {
			return high;
		}
		return value;
	}

	/**
	 * Convert rgb to its components
	 * 
	 * @param rgb the value to split
	 * @return the rgb components
	 */
	public static int[] intToRgbComponents(int rgb) {
		return new int[] { rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF };
	}

	/**
	 * Convert individual RGB components into a 32 bit ARGB value
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @return the argb value
	 */
	public static int componentsToAlphaRgb(int red, int green, int blue) {
		return new Color(correctRange(red), correctRange(green), correctRange(blue)).getRGB();
	}

	/**
	 * Corrects and individual colour component value's range to 0>channel<255
	 * 
	 * @param component the component to restrict
	 * @return the corrected component
	 */
	private static int correctRange(int component) {
		return ColourHelper.correctRange(component, 0, MAXIMUM_COMPONENT_VALUE);
	}

	/**
	 * Determines whether a pixel is closer to black (than white)
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @return whether this component is closer to black than white
	 */
	private static boolean isBlack(int red, int green, int blue) {
		int threshold = OptionsObject.getInstance().getBlackThreshold();
		return red < threshold && green < threshold && blue < threshold;
	}

	/**
	 * Based on the darkness of the pixel colour determines whether a pixel is
	 * ink or paper and returns that colour. Used for converting colour to
	 * monochrome based on whether a pixel can be considered black using the
	 * isBlack threshold.
	 * 
	 * @param rgb the rgb colour to get the monochrome colour from
	 * @param ink the spectrum ink colour
	 * @param paper the spectrum paper colour
	 * @return the ink colour if black, otherwise paper colour
	 */
	public static int getMonochromeColour(int rgb, int ink, int paper) {
		int[] comps = intToRgbComponents(rgb);
		return getMonochromeColour(comps[0], comps[1], comps[2], ink, paper);
	}

	/**
	 * Based on the darkness of the pixel colour determines whether a pixel is
	 * ink or paper and returns that colour. Used for converting colour to
	 * monochrome based on whether a pixel can be considered black using the
	 * isBlack threshold.
	 * 
	 * @param red the red component
	 * @param green the green component
	 * @param blue the blue component
	 * @param ink the spectrum ink colour
	 * @param paper the spectrum paper colour
	 * @return the ink colour if black, otherwise paper colour
	 */
	public static int getMonochromeColour(int red, int green, int blue, int ink, int paper) {
		if (isBlack(red, green, blue))
			return ink;
		return paper;
	}

	/**
	 * Returns an array of black and white colours representing the ink (black)
	 * and paper (white) monochrome colours.
	 * 
	 * Opposite function to getMonochromeFromBlackAndWhite
	 * 
	 * @param image the monochrome image to scan
	 * @return an array of black and white rgb values
	 */
	public static int[] getBlackAndWhiteFromMonochrome(int[] image) {
		int[] copy = Arrays.copyOf(image, image.length);
		for (int i = 0; i < copy.length; ++i) {
			if (copy[i] == SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[OptionsObject.getInstance().getMonochromePaperIndex()]) {
				copy[i] = Color.WHITE.getRGB();
			} else {
				copy[i] = Color.BLACK.getRGB();
			}
		}
		return copy;
	}

	/**
	 * Returns an array of monochrome (chosen ink and paper) colours based on an
	 * input array of black (ink) and white (paper).
	 * 
	 * Opposite function to getBlackAndWhiteFromMonochrome
	 * 
	 * @param data the black and white array to get the monochrome colours for
	 * @return the equivalent monochrome array
	 */
	public static int[] getMonochromeFromBlackAndWhite(final int[] data) {
		int[] copy = Arrays.copyOf(data, data.length);
		for (int i = 0; i < copy.length; ++i) {
			copy[i] = getMonochromeFromBlackAndWhite(copy[i]);
		}
		return copy;
	}

	/**
	 * Returns an a monochrome (chosen ink and paper) colour based on an
	 * input array of black (ink) and white (paper).
	 * 
	 * @param original the black and white rgb value to get the monochrome colour for
	 * @return the equivalent monochrome colour
	 */
	public static int getMonochromeFromBlackAndWhite(int original) {
		OptionsObject oo = OptionsObject.getInstance();
		if (original == Color.WHITE.getRGB()) {
			return SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()];
		}
		return SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()];
	}
}
