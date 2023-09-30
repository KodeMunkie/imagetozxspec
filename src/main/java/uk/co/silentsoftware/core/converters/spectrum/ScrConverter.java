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
package uk.co.silentsoftware.core.converters.spectrum;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.attributestrategy.AttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenBrightPaletteStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenHalfBrightPaletteStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenMixedPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.processors.CharacterConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ImageConverter;
import uk.co.silentsoftware.core.helpers.ByteHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

import javax.imageio.ImageIO;

/**
 * Converter to save images to the Spectrum SCR 
 * memory model/dump format.
 * 
 * @see <a href="http://retrocomputing.stackexchange.com/questions/212/what-format-is-the-timex-sinclair-zx-spectrum-screen-scr-file/217">http://retrocomputing.stackexchange.com/questions/212/what-format-is-the-timex-sinclair-zx-spectrum-screen-scr-file/217</a>
 * @see <a href="http://www.zx-modules.de/fileformats/scrformat.html">http://www.zx-modules.de/fileformats/scrformat.html</a>
 */
public class ScrConverter {

	/**
	 * SCR images are fixed size (representing Spectrum memory)
	 * - this is the size in bytes
	 */
	private static final int SCR_SIZE = 6912;
	
	/**
	 * GigaScreen SCR size in bytes (two SCRs together)
	 */
	private static final int GIGASCREEN_SCR_SIZE = SCR_SIZE*2;
	
	/**
	 * SCR images are fixed size (representing Spectrum memory)
	 * - this is the size in bits
	 */
	private static final int SCR_BITS_SIZE = SCR_SIZE*8;
	
	/**
	 * Retrieves the Spectrum two ink/paper colour data 
	 * for blocks in the provided image. I.e. a 256x192
	 * image is passed in, this is divided by the Spectrum
	 * colour block size (8x8) and returns a two colour 
	 * ColourData array of 32x24 holding ink and paper.
	 * Note this method EXPECTS PIXELS TO BE IN SPECTRUM
	 * COLOURS ONLY - NO VALIDATION IS DONE!
	 * 
	 * A popularity check is also performed in choosing 
	 * which colour is ink and which is paper - the most
	 * popular is paper (i.e. usually a background)
	 *  
	 * @param img the image to convert
	 * @param imageConverter the image converter to do the conversion
	 * @param screen get SCR data for a given Spectrum screen number (1 if regular, up to 2 if Gigascreen)
	 * @return the colour attributes [width/8][height/8] of the provided image
	 */
	private ColourAttribute[][] getBlockedColourData(BufferedImage img, ImageConverter imageConverter, int screen) {
		int width = img.getWidth()/SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
		int height = img.getHeight()/SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
		OptionsObject optionsObject = OptionsObject.getInstance();
		ColourAttribute[][] data = new ColourAttribute[width][height];
		for (int y=0; y<height; ++y) {
			for (int x=0; x<width; ++x) {
				data[x][y] = new ColourAttribute();
				int[] block = img.getRGB(x*SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE, y*SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE, SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE, SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE, null, 0, SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE);
				Map<Integer, Integer> check = new HashMap<>();
				for (int i=0; i<block.length; ++i) {
					if (check.containsKey(block[i])) {
						check.put(block[i], check.get(block[i])+1);
					} else {
						check.put(block[i], 1);
					}
				}
				data[x][y].setInkRGB(block[0]);
				data[x][y].setPaperRGB(block[0]);
				int inkCount = 0;
				int paperCount = 0;
				for (int rgb : block) {
					if (rgb != data[x][y].getInkRGB()) {
						data[x][y].setPaperRGB(rgb);
						++paperCount;
					} else {
						++inkCount;
					}
				}
				
				// Apply the GigaScreen palette ordering strategy
				if (imageConverter instanceof GigaScreenConverterImpl) {
					GigaScreenAttributeStrategy gas = optionsObject.getGigaScreenAttributeStrategy();
					if (gas instanceof GigaScreenHalfBrightPaletteStrategy) {
						data[x][y].setBrightSet(false);
					} else if (gas instanceof GigaScreenMixedPaletteStrategy) {
						data[x][y].setBrightSet(screen!=1);
					} else if (gas instanceof GigaScreenBrightPaletteStrategy) {
						data[x][y].setBrightSet(true);
					}
				} else {
					AttributeStrategy attributeStrategy = optionsObject.getAttributeMode();
					// Swap the ink and paper around if ink is more popular (makes it look more aesthetically pleasing when loading)
					if (inkCount > paperCount) {
						int temp = data[x][y].getPaperRGB();
						data[x][y].setPaperRGB(data[x][y].getInkRGB());
						data[x][y].setInkRGB(temp);
					}
					data[x][y].setBrightSet(attributeStrategy.isBrightSet(data[x][y].getPaperRGB(), data[x][y].getInkRGB()));
				}				
			}
		}
		return data;
	}
	
	/**
	 * Convert the original image to a SCR formatted byte array which may be a double
	 * size SCR if the converter is a GigascreenConverter.
	 * 
	 * @param original the original result image
	 * @param imageConverter the image converter that was used to create the result image
	 * @return the SCR byte array
	 */
	public byte[] convert(final ResultImage[] original, ImageConverter imageConverter) {
		if (OptionsObject.getInstance().getColourMode() instanceof GigaScreenPaletteStrategy && !(imageConverter instanceof CharacterConverterImpl)) {
			List<byte[]> data = convertInternal(original, imageConverter);
			
			// Combine both SCR screens into one array suitable for "tap" usage and other
			// non standard SCR viewer programs.
			byte[] combined = new byte[data.get(0).length+data.get(1).length];
			combined = ByteHelper.copyBytes(data.get(0), combined, 0);
			combined = ByteHelper.copyBytes(data.get(1), combined, data.get(0).length);
			return combined;
		}
		return convertInternal(original, imageConverter).get(0);
	}	
	
	/**
	 * Resizes the output image before further processing - it could be dealing
	 * with a source image with dimensions that are not 256x192.
	 * Note that if the images are not already 256x192 then the resulting SCR 
	 * may be sub optimal in that the dither patterns may be broken resulting
	 * in incorrectly dithered colours being placed adjacent to each other.
	 * 
	 * @param resultImage the processed result image to resize
	 * @return new result image with all images resized to 256x192 
	 */
	private ResultImage[] resize(ResultImage[] resultImage) {
		ResultImage[] resized = new ResultImage[resultImage.length];
		for (int i=0; i<resultImage.length; ++i) {
			resized[i] = new ResultImage(resultImage[i].getResultImageType(), ImageHelper.quickScaleImage(resultImage[i].getImage(), SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT));
		}
		return resized;
	}
	
	/**
	 * Converts the given image into SCR images
	 * 
	 * @param original the result image
	 * @param imageConverter the image converter used for the result image
	 * @return a list containing the SCR images in little endian byte order. if Gigascreen this will be 2 images, otherwise 1
	 */
	private List<byte[]> convertInternal(final ResultImage[] original, ImageConverter imageConverter) {
		ResultImage[] output = resize(original);
		List<byte[]> scrs = new ArrayList<>();
		for (int screenIndex=0; screenIndex<output.length; screenIndex++) {
			
			// If Gigascreen we don't want to use the final image but the two supporting images
			if (imageConverter instanceof GigaScreenConverterImpl 
					&& ResultImageType.SUPPORTING_IMAGE != output[screenIndex].getResultImageType()) {
				continue;
			}
			BufferedImage image = output[screenIndex].getImage();
			BitSet bitset = new BitSet(SCR_BITS_SIZE);
			int counter = 0;
		
			// Get the palette data
			ColourAttribute[][] colourData = getBlockedColourData(image, imageConverter, screenIndex);
			
			// Set the bit data for our image in the weird Spectrum format
			counter = toScr(image, bitset, counter, colourData);
			
			// Fix the endianness (Java is big endian by default, Spectrum (Z80) 
			// is little endian)
			bitset = ByteHelper.reverseBitSet(bitset);
			
			// Write the palette info into the bitset
			for (int y=0; y<SpectrumDefaults.ROWS; y++) {
				for (int x = 0; x < SpectrumDefaults.COLUMNS; x++) {

					// Convert the Spectrum RGBs to Spectrum palette indexes
					int ink = colourData[x][y].getInkRGB();
					int paper = colourData[x][y].getPaperRGB();
					int specInk = SpectrumDefaults.SPECTRUM_ARGB.get(ink);
					int specPaper = SpectrumDefaults.SPECTRUM_ARGB.get(paper);

					// Set the correct bits (noddy method but does the trick)
					counter = colourToBitSet(specInk, bitset, counter);
					counter = colourToBitSet(specPaper, bitset, counter);

					//Bright
					if (colourData[x][y].isBrightSet()) {
						bitset.set(counter);
					}
					counter++;

					// Flash
					//bitset.set(counter);
					counter++;
				}
			}
			scrs.add(Arrays.copyOf(bitset.toByteArray(), SCR_SIZE));
		}
		return scrs;
	}

	/**
	 * Copies the buffered image input to the given bitset, but only
	 * copies the "ink" values, i.e. those that match the colour data
	 * for the relevant pixel 8x8 range. The data in the bitset is in
	 * correctly formatted scr order.
	 * 
	 * @param image the source image
	 * @param bs the bitset to copy the image into
	 * @param counter the pointer into the bitset
	 * @param colourData the attribute data set for the image
	 * @return the counter's new position
	 */
	private int toScr(BufferedImage image, BitSet bs, int counter, ColourAttribute colourData[][]) {
		for (int thirdChunk=0; thirdChunk < SpectrumDefaults.SCREEN_HEIGHT; thirdChunk+=SpectrumDefaults.SCREEN_HEIGHT_THIRD)
			for (int z=0; z<SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE; z++) { // num horiz lines in 8x8 block
				for (int y=thirdChunk+z; y<thirdChunk+SpectrumDefaults.SCREEN_HEIGHT_THIRD; y+=SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE) { // num lines in third of screen 
					for (int x=0;x<SpectrumDefaults.SCREEN_WIDTH; x++) { //line of horiz pixels
						int xBlock = x/SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
						int yBlock = y/SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
						
						// If the pixel matches the most popular attribute colour (ink colour) then mark it in the bit set
						if (colourData[xBlock][yBlock].getInkRGB() == image.getRGB(x,y)) {
							bs.set(counter);
						}
						++counter;
					}
				}
		}
		return counter; //Should be 49152;
	}
	
	/**
	 * Shockingly bad method to convert Spectrum colour indexes to
	 * the relevant little endian bits. There is probably a better
	 * way of doing this but I'm tired.
	 * 
	 * "There is 1 byte of attribute for each character cell. Stored left to 
	 * right, top to bottom. Bits 2-0 store the foreground colour, or "ink" 
	 * colour (colour assigned to "on" pixels). Bits 5-3 store the background 
	 * colour or "paper" colour (colour assigned to "off" pixels). Bit 6 is 
	 * bright. If setted, both paper and ink colours are lighter. Bit 7 is 
	 * flash. If setted, the paper and ink colour swap every 640 ms to give 
	 * a kind of flashing character."
	 * 
	 * @param colour the Spectrum colour
	 * @param bs The bitset data to return with Spectrum colour (3 bit chunk) 
	 * @param counter the index into the bitset for this operation
	 * @return the new index for the next operation
	 */
	int colourToBitSet(int colour, BitSet bs, int counter) {
		if (colour == 0) {
			counter +=3;//000
		} else if (colour == 1 || colour == 8) {
			bs.set(counter);
			counter+=3;//100
		} else if (colour == 2 || colour == 9) {
			counter++;//010
			bs.set(counter);
			counter+=2;
		} else if (colour == 3 || colour == 10) {
			bs.set(counter);//110
			counter++;
			bs.set(counter);
			counter+=2;
		} else if (colour == 4 || colour == 11) {
			counter+=2;//001
			bs.set(counter);
			counter++;
		} else if (colour == 5 || colour == 12) {
			bs.set(counter);//101
			counter+=2;
			bs.set(counter);
			counter++;
		} else if (colour == 6 || colour == 13) {
			counter++;//011
			bs.set(counter);
			counter++;
			bs.set(counter);
			counter++;
		} else if (colour == 7 || colour == 14) {
			bs.set(counter);//111
			counter++;
			bs.set(counter);
			counter++;
			bs.set(counter);
			counter++;
		}
		return counter;
	}

	/**
	 * Gets the first of any screens from the provided scrData
	 * 
	 * @param scrData the scr data to get the first screen from
	 * @return a single screen
	 */
	public byte[] getScr1(final byte[] scrData) {
		return Arrays.copyOf(scrData, ScrConverter.SCR_SIZE);
	}
	
	/**
	 * Gets the second of any screens from the provided scrData
	 * 
	 * @param scrData the scr data to get the second screen from
	 * @return a second screen if it exists, otherwise nothing.
	 */
	public Optional<byte[]> getScr2(final byte[] scrData) {
		if (scrData.length < ScrConverter.GIGASCREEN_SCR_SIZE) {
			return Optional.empty();
		}
		return Optional.ofNullable(Arrays.copyOfRange(scrData, ScrConverter.SCR_SIZE, ScrConverter.GIGASCREEN_SCR_SIZE));
	}
}
