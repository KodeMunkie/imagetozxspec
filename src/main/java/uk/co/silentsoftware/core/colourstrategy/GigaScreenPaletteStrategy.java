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
package uk.co.silentsoftware.core.colourstrategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

/**
 * GigaScreen palette strategy
 */
public class GigaScreenPaletteStrategy implements ColourChoiceStrategy {

	private static final int CACHE_TIME_SECONDS = 10;

	private static final Cache<String, GigaScreenAttribute[][]> CACHE = Caffeine.newBuilder().expireAfterAccess(CACHE_TIME_SECONDS, TimeUnit.SECONDS).build();

	public String toString() {
		return getCaption("colour_mode_gigascreen");
	}

	/**
	 * Processing of the colours cannot be applied to both Spectrum screens used in Gigascreen at the same time.
	 * If this method is called then the wrong processor implementation is being used. 
	 */
	@Override
	public int chooseBestPaletteMatch(int originalRgb, int[] mostPopularRgbColours) {
		throw new UnsupportedOperationException("GigaScreen palette colouring cannot be applied to dither image processors - this is a placeholder class to allow identification during getClosestColour conversions for the GigaScreenConverter.");
	}

	@Override
	public int chooseBestPaletteMatch(int rgb) {
		return ColourHelper.getClosestColour(rgb, SpectrumDefaults.GIGASCREEN_COLOURS_ALL);
	}

	@Override
	public int[] getPalette() {
		return SpectrumDefaults.GIGASCREEN_COLOURS_ALL;
	}

	@Override
	public BufferedImage colourAttributes(BufferedImage output) {
		// Algorithm replaces each pixel with the colour from the closest matching
		// 4 colour GigaScreen attribute block.
		GigaScreenAttribute[] palette = OptionsObject.getInstance().getGigaScreenAttributeStrategy().getPalette();
		GigaScreenAttribute[][] quad = getGigaScreenAttributes(output, palette);
		GigaScreenAttribute currentGigaScreenAttribute = null;
		for (int y = 0; y < output.getHeight(); ++y) {
			for (int x = 0; x < output.getWidth(); ++x) {
				if (x % ATTRIBUTE_BLOCK_SIZE == 0) {
					currentGigaScreenAttribute = quad[x / ATTRIBUTE_BLOCK_SIZE][y / ATTRIBUTE_BLOCK_SIZE];
				}
				GigaScreenAttribute.GigaScreenColour c = ColourHelper.getClosestGigaScreenColour(output.getRGB(x, y), currentGigaScreenAttribute);
				output.setRGB(x, y, c.getGigascreenColour());
			}
		}
		return output;

	}

	/**
	 * Creates a map of the 32x24 Spectrum attribute set for two screens.
	 * For each attribute block it finds the closest (best fitting) gigascreen
	 * palette of 4 colours.
	 *
	 * @param original the original image
	 * @return the giga screen attribute array
	 */
	public GigaScreenAttribute[][] getGigaScreenAttributes(BufferedImage original, GigaScreenAttribute[] palette) {
		String key = getKey(original, palette, OptionsObject.getInstance().getGigaScreenAttributeStrategy());
		GigaScreenAttribute[][] entries = CACHE.getIfPresent(key);
		if (entries != null) {
			//return entries;
		}
		entries = new GigaScreenAttribute[original.getWidth() / ATTRIBUTE_BLOCK_SIZE][original.getHeight() / ATTRIBUTE_BLOCK_SIZE];
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= original.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int[] outRgb = original.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
				double lowest = Double.MAX_VALUE;
				GigaScreenAttribute chosen = palette[0];
				for (GigaScreenAttribute combo : palette) {
					double score = combo.getScoreForAttributeBlock(outRgb);
					if (score < lowest) {
						lowest = score;
						chosen = combo;
					}
				}
				entries[x / ATTRIBUTE_BLOCK_SIZE][y / ATTRIBUTE_BLOCK_SIZE] = chosen;
			}
		}
		CACHE.put(key, entries);
		return entries;
	}

	private String getKey(BufferedImage original, GigaScreenAttribute[] palette, GigaScreenAttributeStrategy attributeStrategy) {
		return original.hashCode()+"-"+ Arrays.hashCode(palette)+"-"+attributeStrategy.hashCode();
	}
}
