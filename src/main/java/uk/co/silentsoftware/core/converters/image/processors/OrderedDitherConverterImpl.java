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

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
* An ordered dithering converter
 */
public class OrderedDitherConverterImpl implements ImageConverter {

	private OptionsObject oo = OptionsObject.getInstance();
	private OrderedDitherStrategy ditherStrategy = null;
	private ColourChoiceStrategy colourChoiceStrategy;
	private boolean drawStrategyLabel = false;
	
	public OrderedDitherConverterImpl(OrderedDitherStrategy ditherStrategy, ColourChoiceStrategy colourChoiceStrategy) {
		this.ditherStrategy = ditherStrategy;
		this.colourChoiceStrategy = colourChoiceStrategy;
		this.drawStrategyLabel = true;
	}
	
	public OrderedDitherConverterImpl() {
		ditherStrategy = (OrderedDitherStrategy)oo.getSelectedDitherStrategy();
		colourChoiceStrategy = oo.getColourMode();
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public ResultImage[] convert(final BufferedImage original) {

		OptionsObject oo = OptionsObject.getInstance();
		BufferedImage output = ImageHelper.copyImage(original);
		int xMax = ditherStrategy.getMatrixWidth();
		int yMax = ditherStrategy.getMatrixHeight();
		for (int y=0; y+yMax<=original.getHeight(); y+=yMax) {
			for (int x=0; x+xMax<=original.getWidth() && y+yMax<=original.getHeight(); x+=xMax) {
				int outRgb[] = original.getRGB(x, y, xMax, yMax, null, 0, xMax);
				outRgb = ditherStrategy.applyDither(outRgb);
				output.setRGB(x, y, xMax, yMax, outRgb, 0, xMax);	
			}
		}

		output = colourChoiceStrategy.colourAttributes(output);
		
		// Print the name of the preview strategy
		if (drawStrategyLabel) {
			PreviewLabeller.drawPreviewStrategyWithName(output, ditherStrategy.toString());
		}
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}
}
