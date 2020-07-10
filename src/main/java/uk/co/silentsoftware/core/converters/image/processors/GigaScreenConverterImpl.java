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

    private void convertAttributePixelRow(int[] attributeRowData, int[] attributeRowDataOdd, int[] rowPixels, int[] rowScreen1Pixels, int[] rowScreen2Pixels, GigaScreenAttribute combo, boolean interlaced) {
        // For every pixel on an attribute row of pixels find a gigascreen colour
        for (int i = 0; i < ATTRIBUTE_BLOCK_SIZE; ++i) {
            int gigascreenColour = attributeRowData[i];
            if (interlaced) {
                gigascreenColour = averageColour(attributeRowData[i], attributeRowDataOdd[i]);
            }
            GigaScreenAttribute.GigaScreenColour col = ColourHelper.getClosestGigaScreenColour(gigascreenColour, combo);
            rowPixels[i] = col.getGigascreenColour();
            rowScreen1Pixels[i] = col.getScreen1Colour();
            rowScreen2Pixels[i] = col.getScreen2Colour();
        }
    }

    private void convertAttributeBlock(int x, int y, GigaScreenAttribute combo, BufferedImage gs, BufferedImage output, BufferedImage output1, BufferedImage output2, boolean interlaced) {
        int[] block = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int[] block1 = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int[] block2 = new int[ATTRIBUTE_BLOCK_SIZE*ATTRIBUTE_BLOCK_SIZE];
        int relativeY = y;
        for (int row=0; row<ATTRIBUTE_BLOCK_SIZE; row++) {
            int[] rowPixels = new int[ATTRIBUTE_BLOCK_SIZE];
            int[] rowScreen1Pixels = new int[ATTRIBUTE_BLOCK_SIZE];
            int[] rowScreen2Pixels = new int[ATTRIBUTE_BLOCK_SIZE];

            int[] even = gs.getRGB(x, relativeY, ATTRIBUTE_BLOCK_SIZE, 1, null, 0, ATTRIBUTE_BLOCK_SIZE);
            int[] odd = null; // if not interlaced we don't need to scan 2 lines at once to sample the difference
            if (interlaced) {
                relativeY++;
                odd = gs.getRGB(x, relativeY, ATTRIBUTE_BLOCK_SIZE, 1, null, 0, ATTRIBUTE_BLOCK_SIZE);
            }
            convertAttributePixelRow(even,odd,rowPixels,rowScreen1Pixels,rowScreen2Pixels,combo,interlaced);
            System.arraycopy(rowPixels, 0, block, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
            System.arraycopy(rowScreen1Pixels, 0, block1, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
            System.arraycopy(rowScreen2Pixels, 0, block2, row * ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE);
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

        // If interlace we have a double height input for a regular output (over 2 screens), so we need to
        // compare odd and even fields to halve the height.
        boolean interlaced = OptionsObject.INTERLACED == oo.getScaling();
        int yStride = ATTRIBUTE_BLOCK_SIZE;
        if (interlaced) {
            height /= 2;
            yStride = ATTRIBUTE_BLOCK_SIZE*2;
        }
        final BufferedImage output =  new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage output1 = new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage output2 = new BufferedImage(original.getWidth(), height, BufferedImage.TYPE_INT_ARGB);

        // Dithers the images to the GigaScreen palette
        ResultImage[] resultImage = imageConverter.convert(ImageHelper.copyImage(original));
        BufferedImage gs = resultImage[0].getImage();

        // Algorithm replaces each pixel with the colour from the closest matching
        // 4 colour GigaScreen attribute palette. Quad is the array of the possible 4 colour palettes.
        GigaScreenAttribute[][] quad = ((GigaScreenPaletteStrategy)oo.getColourMode()).getGigaScreenAttributes(gs, oo.getGigaScreenAttributeStrategy().getPalette());
        GigaScreenAttribute chosenQuad;

        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= gs.getHeight(); y += yStride) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= gs.getWidth() && y + yStride <= gs.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                chosenQuad = quad[x / ATTRIBUTE_BLOCK_SIZE][y / ATTRIBUTE_BLOCK_SIZE];
                convertAttributeBlock(x, y, chosenQuad, gs, output, output1, output2, interlaced);
            }
        }

        if (oo.getExportTape() || oo.getExportScreen()) {
           orderByGigaScreenPaletteOrder(output1, output2);
        }

        if (imageConverter.getDrawStrategyLabel()) {
            PreviewLabeller.drawPreviewStrategyWithName(output, imageConverter.getDitherStrategyLabel());
        }

        return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output),
                new ResultImage(ResultImageType.SUPPORTING_IMAGE, output1),
                new ResultImage(ResultImageType.SUPPORTING_IMAGE, output2)};
    }

    /**
     * Produces an average of 2 rgb component values.
     * Depending on averaging method chosen in options may return different values.
     *
     * @param comp1 rgb component 1
     * @param comp2 rgb component 2
     * @return the averaged component
     */
    private int averageComponent(int comp1, int comp2) {
        if (OptionsObject.getInstance().getColourspaceAveraging()) {
            return Math.round(Math.round(Math.sqrt((Math.pow(comp1,2)+Math.pow(comp2,2))/2)));
        }
        return (comp1+comp2)/2;
    }

    /**
     * Averages two colours.
     * Depending on averaging method chosen in options may return different values.
     *
     * @param col1 colour 1
     * @param col2 colour 2
     * @return the average colour
     */
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
        orderByHsb(output1, output2, paletteOrder);
    }

    /**
     * Finds the total of a given HSB value within an array
     *
     * @param outRgb the pixel array to find the HSB total from
     * @param paletteOrder the hsb option choice
     * @return the hsb float total value
     */
    private float totalChosenHsbValue(int outRgb[], GigaScreenPaletteOrder paletteOrder) {
        Set<Integer> cols = new HashSet<>();
        for (int rgb : outRgb) {
            cols.add(rgb);
            if (cols.size() == 4) {
                break;
            }
        }
        float totalCount = 0;
        for (int rgb : cols) {
            int[] rgbComps = ColourHelper.intToRgbComponents(rgb);
            float[] hsb = Color.RGBtoHSB(rgbComps[0], rgbComps[1], rgbComps[2], null);
            totalCount += getGigaScreenHSBCount(hsb, paletteOrder);
        }
        return totalCount;
    }

    /**
     * Reorders attributes by chosen hsb option
     *
     * @param output1 the first image to reorder attributes in
     * @param output2 the second image to reorder attributes in
     * @param paletteOrder the option to total the hsb values by (e.g. find total of brightness)
     */
    private void orderByHsb(BufferedImage output1, BufferedImage output2, GigaScreenPaletteOrder paletteOrder) {
        for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
            for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output1.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
                int outRgb1[] = output1.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                int outRgb2[] = output2.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
                float totalCount1 = totalChosenHsbValue(outRgb1, paletteOrder);
                float totalCount2 = totalChosenHsbValue(outRgb2, paletteOrder);

                // Place attribute block with lowest hsb total on first screen
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

    @Override
    public String getDitherStrategyLabel() {
        return imageConverter.getDitherStrategyLabel();
    }

    @Override
    public boolean getDrawStrategyLabel() {
        return imageConverter.getDrawStrategyLabel();
    }


}
