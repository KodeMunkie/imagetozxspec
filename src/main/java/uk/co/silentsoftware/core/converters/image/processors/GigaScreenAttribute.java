/* Image to ZX Spec
 * Copyright (C) 2023 Silent Software (Benjamin Brown)
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

import org.apache.commons.lang3.StringUtils;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.util.Set;
import java.util.TreeSet;

/**
 * Representation of an 8x8 pixel attribute block across two screens. This class
 * stores 4 colours each representing one of the (derived) GigaScreen 102
 * colours as a "GigaScreenColour" inner class. The GigaScreenColours also hold
 * the base 4 Spectrum colours (2 per screen attribute) used to derive the 4
 * Gigascreen colours.
 * 
 * This works on actual hardware by using persistence of vision and flashing
 * between 2 screens to give a perceived palette of up 102 unique colours. This
 * class holds just one attribute block worth of palette e.g.
 * 
 * Screen 1: 
 * ink: Red 
 * paper: Yellow
 * 
 * Screen 2: 
 * ink: Red 
 * paper: Blue
 * 
 * Computed Gigascreen colours: 
 * 1) Red x Red = Red 
 * 2) Red x Blue = Purple 
 * 3) Yellow x Red = Orange 
 * 4) Yellow x Blue = Green
 * 
 * Obviously if there are duplicated colours in each screen we have fewer colours in this attribute
 */
public class GigaScreenAttribute {

	// The actual Gigascreen 32 bit palette representing up to 4 colours
	private int[] palette;

	// The array storing the Gigascreen colours' composite parts (Spectrum
	// colour for each screen, separate RGB values)
	private GigaScreenColour[] gigaScreenColours = new GigaScreenColour[4];
	private int uniqueColourCount;
	private String uniqueHash = StringUtils.EMPTY;

	/**
	 * Constructor for a GigaScreenAttribute
	 * 
	 * @param inkScreen1 the rgb colour for the ink on screen 1
	 * @param paperScreen1 the rgb colour for the paper on screen 1
	 * @param inkScreen2 the rgb colour for the ink on screen 2
	 * @param paperScreen2 the rgb colour for the paper on screen 2
	 */
	public GigaScreenAttribute(int inkScreen1, int paperScreen1, int inkScreen2, int paperScreen2) {
		gigaScreenColours[0] = new GigaScreenColour(inkScreen1, inkScreen2);
		gigaScreenColours[1] = new GigaScreenColour(inkScreen1, paperScreen2);
		gigaScreenColours[2] = new GigaScreenColour(paperScreen1, inkScreen2);
		gigaScreenColours[3] = new GigaScreenColour(paperScreen1, paperScreen2);

		// Build an ordered set
		Set<Integer> uniqueColours = new TreeSet<>();
		uniqueColours.add(gigaScreenColours[0].gigascreenColour);
		uniqueColours.add(gigaScreenColours[1].gigascreenColour);
		uniqueColours.add(gigaScreenColours[2].gigascreenColour);
		uniqueColours.add(gigaScreenColours[3].gigascreenColour);
		uniqueColourCount = uniqueColours.size();

		palette = new int[] { gigaScreenColours[0].gigascreenColour, gigaScreenColours[1].gigascreenColour, gigaScreenColours[2].gigascreenColour,
				gigaScreenColours[3].gigascreenColour};

		for (Integer uniqueColour : uniqueColours) {
			// Builds a string, not an integer
			uniqueHash += uniqueColour;
		}
	}

	public int[] getPalette() {
		return palette;
	}

	/**
	 * Calculate how close a match this attribute set of 4 colours compares to
	 * the provided attribute block. Lower is better.
	 * 
	 * @param attributeBlock the sample block to compare against this attribute
	 * @return the score in range >= 0
	 */
	  public double getScoreForAttributeBlock(int[] attributeBlock) {
		double totalDistance = 0;
		for (int pixel : attributeBlock) {
			int[] components = ColourHelper.intToRgbComponents(pixel);
			double distance = ColourHelper.getClosestColourDistanceForGigascreenColours(components[0], components[1], components[2], gigaScreenColours);
			totalDistance += distance;
		}
		return totalDistance;
	}

	public int getUniqueColourCount() {
		return uniqueColourCount;
	}

	public GigaScreenColour getGigaScreenColour(int index) {
		return gigaScreenColours[index];
	}

	/**
	 * Representation of an attribute block across two screens i.e. a gigascreen
	 * colour and the two base Spectrum colours (one colour per screen).
	 */
	public class GigaScreenColour {

		private int gigascreenColour;
		private int[] gigascreenColourRGB;
		private int screen1Colour;
		private int screen2Colour;

		GigaScreenColour(int screen1Colour, int screen2Colour) {
			this.screen1Colour = screen1Colour;
			this.screen2Colour = screen2Colour;

			int[] rgbS1 = ColourHelper.intToRgbComponents(screen1Colour);
			int[] rgbS2 = ColourHelper.intToRgbComponents(screen2Colour);
			gigascreenColour = ColourHelper.componentsToAlphaRgb(
					(int)(((long)rgbS1[0] + (long)rgbS2[0]) / 2l),
					(int)(((long)rgbS1[1] + (long)rgbS2[1]) / 2l),
					(int)(((long)rgbS1[2] + (long)rgbS2[2]) / 2l));
			gigascreenColourRGB = ColourHelper.intToRgbComponents(gigascreenColour);
		}

		public int[] getGigascreenColourRGB() {
			return gigascreenColourRGB;
		}

		public int getGigascreenColour() {
			return gigascreenColour;
		}

		int getScreen1Colour() {
			return screen1Colour;
		}

		int getScreen2Colour() {
			return screen2Colour;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uniqueHash.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GigaScreenAttribute other = (GigaScreenAttribute) obj;
		return uniqueHash.equals(other.uniqueHash);
	}
}
