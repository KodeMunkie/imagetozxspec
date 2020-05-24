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
package uk.co.silentsoftware.core.converters.image.processors;

import uk.co.silentsoftware.config.GigaScreenPaletteOrder;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;
import uk.co.silentsoftware.core.helpers.TallyValue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
import static uk.co.silentsoftware.core.helpers.ColourHelper.luminositySum;

/**
 * A converter that wraps the GigaScreen logic around a base dithering converter.
 *
 * @see GigaScreenAttribute for full details of implementation
 */
public class GigaScreenConverterImpl implements ImageConverter {

    /**
     * Threshold as to whether a block should be on the first screen
     * based on the less popular secondary colour.
     */
    private static final int SECONDARY_COLOUR_THRESHOLD = 24;

    private ImageConverter imageConverter;

    public GigaScreenConverterImpl(ImageConverter imageConverter) {
        this.imageConverter = imageConverter;
    }

    private void sampleBlock(int x, int y, GigaScreenAttribute combo, BufferedImage gs, BufferedImage output, BufferedImage output1, BufferedImage output2, boolean interlaced) {
        int[] block = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int[] block1 = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int[] block2 = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int relativeY = y;
        for (int row=0; row<ATTRIBUTE_BLOCK_SIZE; row++) {
            int[] merged = new int[ATTRIBUTE_BLOCK_SIZE];
            int[] merged1 = new int[ATTRIBUTE_BLOCK_SIZE];
            int[] merged2 = new int[ATTRIBUTE_BLOCK_SIZE];

            int[] even = gs.getRGB(x, relativeY, ATTRIBUTE_BLOCK_SIZE, 1, null, 0, ATTRIBUTE_BLOCK_SIZE);
            int[] odd = null;
            if (interlaced) {
                relativeY++;
                odd = gs.getRGB(x, relativeY, ATTRIBUTE_BLOCK_SIZE, 1, null, 0, ATTRIBUTE_BLOCK_SIZE);
            }
            // For every pixel find a merged colour
            for (int i = 0; i < ATTRIBUTE_BLOCK_SIZE; ++i) {
                int gigascreenColour = even[i];
                if (interlaced) {
                    gigascreenColour = averageColour(even[i], odd[i]);
                }
                GigaScreenAttribute.GigaScreenColour col = ColourHelper.getClosestGigaScreenColour(gigascreenColour, combo);
                merged[i] = col.getGigascreenColour();
                merged1[i] = col.getScreen1Colour();
                merged2[i] = col.getScreen2Colour();
            }
            System.arraycopy(merged, 0, block, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
            System.arraycopy(merged1, 0, block1, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
            System.arraycopy(merged2, 0, block2, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
            relativeY++;
        }
        int newY = interlaced ? y/2 : y;
        output.setRGB(x, newY, ATTRIBUTE_BLOCK_SIZE,ATTRIBUTE_BLOCK_SIZE, block, 0, ATTRIBUTE_BLOCK_SIZE);
        output1.setRGB(x, newY, ATTRIBUTE_BLOCK_SIZE,ATTRIBUTE_BLOCK_SIZE, block1, 0, ATTRIBUTE_BLOCK_SIZE);
        output2.setRGB(x, newY, ATTRIBUTE_BLOCK_SIZE,ATTRIBUTE_BLOCK_SIZE, block2, 0, ATTRIBUTE_BLOCK_SIZE);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public ResultImage[] convert(BufferedImage original) {
        OptionsObject oo = OptionsObject.getInstance();
        int height = original.getHeight();
        int atrributeHeightMultipler = 1;
        boolean interlaced = OptionsObject.INTERLACED == oo.getScaling();
        if (interlaced) {
            height /= 2;
            atrributeHeightMultipler = 2;
        }
        final BufferedImage output =  new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage output1 = new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage output2 = new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);

        // Dithers the images to the GigaScreen palette
        ResultImage[] resultImage = imageConverter.convert(ImageHelper.copyImage(original));
        BufferedImage gs = resultImage[0].getImage();

        // Algorithm replaces each pixel with the colour from the closest matching
        // 4 colour GigaScreen attribute block.
        GigaScreenAttribute[][] quad = ((GigaScreenPaletteStrategy)oo.getColourMode()).getGigaScreenAttributes(gs, oo.getGigaScreenAttributeStrategy().getPalette());
        GigaScreenAttribute combo;


        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= gs.getHeight(); y += (ATTRIBUTE_BLOCK_SIZE*atrributeHeightMultipler)) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= gs.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= gs.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                combo = quad[x / ATTRIBUTE_BLOCK_SIZE][y / ATTRIBUTE_BLOCK_SIZE];
                sampleBlock(x, y, combo, gs, output, output1, output2, interlaced);
            }
        }

        if (oo.getExportTape() || oo.getExportScreen()) {
           orderByGigaScreenPaletteOrder(output1, output2);
        }

        if (imageConverter.getDrawStrategyLabel()) {
            PreviewLabeller.drawPreviewStrategyWithName(output, imageConverter.getDitherStrategyLabel().toString());
        }

        return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output),
                new ResultImage(ResultImageType.SUPPORTING_IMAGE, output1),
                new ResultImage(ResultImageType.SUPPORTING_IMAGE, output2)};
    }

    @Override
    public String getDitherStrategyLabel() {
        return imageConverter.getDitherStrategyLabel();
    }

    @Override
    public boolean getDrawStrategyLabel() {
        return imageConverter.getDrawStrategyLabel();
    }

    private int averageComponent(int comp1, int comp2) {
        if (OptionsObject.getInstance().getColourspaceAveraging()) {
            return Math.round(Math.round(Math.sqrt((Math.pow(comp1,2)+Math.pow(comp2,2))/2)));
        }
        return (comp1+comp2)/2;
    }

    private int averageColour(int col1, int col2) {
        int[] rgb1 = ColourHelper.intToRgbComponents(col1);
        int[] rgb2 = ColourHelper.intToRgbComponents(col2);
        return ColourHelper.componentsToAlphaRgb(averageComponent(rgb1[0],rgb2[0]),averageComponent(rgb1[1],rgb2[1]),averageComponent(rgb1[2],rgb2[2]));
    }

    /**
     * Orders attributes by grouping them into 2 colour sets one for each screen
     * Uses a threshold to determine borderline cases that may reduce the amount
     * of flicker.
     *
     * @param output1 the first image to reorder attributes in
     * @param output2 the second image to reorder attributes in
     */
    private void orderByAesthetics(BufferedImage output1, BufferedImage output2) {
        Map<Integer, Integer> map2 = new HashMap<>();
        List<TallyValue> tallyValues2 = new LinkedList<>();

        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output1.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                int outRgb1[] = output1.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                int outRgb2[] = output2.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                map2.clear();
                tallyValues2.clear();
                for (int i = 0; i < outRgb1.length; ++i) {
                    int count2 = 1;
                    if (map2.containsKey(outRgb2[i])) {
                        count2 = map2.get(outRgb2[i]) + 1;
                    }
                    map2.put(outRgb2[i], count2);
                }

                map2.keySet().stream().forEach(colour -> {
                    Integer tally = map2.get(colour);
                    tallyValues2.add(new TallyValue(colour, tally));
                });
                tallyValues2.sort(TallyValue.TALLY_COMPARATOR);

                int mostPopularColour = tallyValues2.get(0).getColour();
                int secMostPopularColour = tallyValues2.size() > 1?tallyValues2.get(1).getColour():mostPopularColour;
                int secTally = tallyValues2.size() > 1?tallyValues2.get(1).getCount():tallyValues2.get(0).getCount();

                List<Integer> groupedColours = SpectrumDefaults.GIGASCREEN_GROUPED_COLOURS;
                if (groupedColours.contains(mostPopularColour) || (groupedColours.contains(secMostPopularColour) && secTally >= SECONDARY_COLOUR_THRESHOLD)) {
                    output1.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb2, 0, ATTRIBUTE_BLOCK_SIZE);
                    output2.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb1, 0, ATTRIBUTE_BLOCK_SIZE);
                }
            }
        }
    }

    /**
     * Reorders attributes by luminosity
     *
     * @param output1 the first image to reorder attributes in
     * @param output2 the second image to reorder attributes in
     */
    private void orderByLuminosity(BufferedImage output1, BufferedImage output2) {
        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output1.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                int outRgb1[] = output1.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                int outRgb2[] = output2.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                float sum1 = luminositySum(outRgb1);
                float sum2 = luminositySum(outRgb2);
                if (sum1 > sum2) {
                    output1.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb2, 0, ATTRIBUTE_BLOCK_SIZE);
                    output2.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb1, 0, ATTRIBUTE_BLOCK_SIZE);
                }
            }
        }
    }

    /**
     * Reorders the colour between the two screens to minimise the amount of flicker or
     * other artifacts in actual ZX Spectrum screen output.
     *
     * @param output1 the first image to reorder attributes in
     * @param output2 the second image to reorder attributes in
     */
    private void orderByGigaScreenPaletteOrder(BufferedImage output1, BufferedImage output2) {
        // TODO: Yuk. Defer to instances of ordering types instead in future.
        GigaScreenPaletteOrder paletteOrder = OptionsObject.getInstance().getGigaScreenPaletteOrder();
        if (GigaScreenPaletteOrder.None == paletteOrder) {
            return;
        }
        if (GigaScreenPaletteOrder.Luminosity == paletteOrder) {
            orderByLuminosity(output1, output2);
            return;
        }
        if (GigaScreenPaletteOrder.Intelligent == paletteOrder) {
            orderByAesthetics(output1, output2);
            return;
        }
        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output1.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                int outRgb1[] = output1.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                int outRgb2[] = output2.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                float totalCount1 = 0;
                float totalCount2 = 0;
                Set<Integer> cols = new HashSet<>();
                for (int rgb : outRgb1) {
                    cols.add(rgb);
                    if (cols.size() == 4) {
                        break;
                    }
                }
                for (int rgb : cols) {
                    int[] rgbComps = ColourHelper.intToRgbComponents(rgb);
                    float[] hsb = Color.RGBtoHSB(rgbComps[0], rgbComps[1], rgbComps[2], null);
                    totalCount1 += getGigaScreenHSBCount(hsb, paletteOrder);
                }
                cols = new HashSet<>();
                for (int rgb : outRgb2) {
                    cols.add(rgb);
                    if (cols.size() == 4) {
                        break;
                    }
                }
                for (int rgb : cols) {
                    int[] rgbComps = ColourHelper.intToRgbComponents(rgb);
                    float[] hsb = Color.RGBtoHSB(rgbComps[0], rgbComps[1], rgbComps[2], null);
                    totalCount2 += getGigaScreenHSBCount(hsb, paletteOrder);
                }
                if (totalCount1 > totalCount2) {
                    output1.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb2, 0, ATTRIBUTE_BLOCK_SIZE);
                    output2.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb1, 0, ATTRIBUTE_BLOCK_SIZE);
                }
            }
        }
    }

    /**
     * Calculates a value for a given hue/saturation/brightness on a given pixel
	 *
	 * @param hsb the hue saturation brightness float
     * @param hsbOption the chosen export option
     * @return the chosen hsboption value
     */
    private float getGigaScreenHSBCount(float[] hsb, GigaScreenPaletteOrder hsbOption) {
        switch (hsbOption) {
            case Hue:
                return hsb[0];
            case Saturation:
                return hsb[1];
            case Brightness:
                return hsb[2];
            case HueBrightness:
                return hsb[0] + hsb[2];
            case HueSaturation:
                return hsb[0] + hsb[1];
            case SaturationBrightness:
                return hsb[1] + hsb[2];
            default:
                return hsb[0];
        }
    }
}
