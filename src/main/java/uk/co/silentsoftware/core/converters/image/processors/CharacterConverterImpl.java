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

import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.StuckiDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;

import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

/**
 * Converts images to the ZX spectrum character set,
 * requires that the image be in monochrome and
 * dithered by another processor first (hardcoded Stucki).
 */
@SuppressWarnings("unchecked")
public class CharacterConverterImpl implements ImageConverter {

	/**
	 * The UTF-8 character to 8x8 pixel map, map. This is a java object representing
	 * the position of b/w pixels in 32bit colour space for each character in the Spectrum font set.
	 * Each character's data is stored in the map keyed against the actual UTF-8 character. 
	 * Note that the UTF-8 characters only partially mapped to the Spectrum's full charset,
	 * the block graphics (etc) are omitted since we need to be able to output this
	 * map as actual text later,
	 */
	public static Map<String, int[]> charSet = null;
	static {
		XMLDecoder d = null;
		try {
			InputStream inputStream = CharacterConverterImpl.class.getResourceAsStream("/characters.xml.gz");
			d = new XMLDecoder(new GZIPInputStream(inputStream));
			charSet = (Map<String, int[]>) d.readObject();
		} catch (Exception e) {
			// If this fails exceptions will be thrown by the thread which are caught
		} finally {
			if (d != null) {
				d.close();
			}
		}
	}

	private boolean drawStrategyLabel = false;

	/**
	 * Image quality is improved if a pre-dither to black and white is used
	 */
	private final ImageConverter preDither = new ErrorDiffusionConverterImpl(new StuckiDitherStrategy(), new MonochromePaletteStrategy());

	public CharacterConverterImpl(){}

	@SuppressWarnings("unused")
	public CharacterConverterImpl(DitherStrategy ditherStrategy, ColourChoiceStrategy colourChoiceStrategy) {
		// Ignore strategies as we don't use them
		drawStrategyLabel = true;
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public ResultImage[] convert(BufferedImage original) {
		
		// Dither the image beforehand to improve monochrome conversion
		ResultImage[] resultImages = preDither.convert(original);
		BufferedImage output = null;
		for (ResultImage ri : resultImages) {
			if (ResultImageType.FINAL_IMAGE == ri.getResultImageType()) {
				output = ri.getImage();
				break;
			}
		}	
		// Replace attribute block with equivalent character image
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb[] = output.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
				outRgb = findBestCharacterMatch(outRgb);
				output.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb, 0, ATTRIBUTE_BLOCK_SIZE);
			}
		}
		// Print the name of the preview strategy
		if (drawStrategyLabel) {
			PreviewLabeller.drawPreviewStrategyWithName(output, getCaption("character_dither"));
		}
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}

	@Override
	public String getDitherStrategyLabel() {
		return  getCaption("character_dither");
	}

	@Override
	public boolean getDrawStrategyLabel() {
		return drawStrategyLabel;
	}

	/**
	 * Iterates through the character-pixel map to find a character
	 * that has the most similarly positioned number of pixels which
	 * it accumulates as a score. Note that the character-pixel map
	 * is black-white based and we need to map to the monochrome
	 * colours in use during counting.
	 * 
	 * @param sample the attribute block sample
	 * @return the character's image as an attribute block
	 */
	private int[] findBestCharacterMatch(int[] sample) {
		Map<String, Integer> stats = new HashMap<>();
		Set<String> keys = charSet.keySet();
		for (String key : keys) {
			int score = 0;
			int[] character = charSet.get(key);
			for (int i = 0; i < character.length; ++i) {
				int monochromeMappedColour = ColourHelper.getMonochromeFromBlackAndWhite(character[i]);
				if (monochromeMappedColour == sample[i]) {
					score++;
				}
			}
			stats.put(key, score);
		}
		int bestScore = Integer.MIN_VALUE;
		String chosenKey = null;
		for (String key : keys) {
			int score = stats.get(key);
			if (score > bestScore) {
				bestScore = score;
				chosenKey = key;
			}
		}
		return ColourHelper.getMonochromeFromBlackAndWhite(charSet.get(chosenKey));
	}
}
