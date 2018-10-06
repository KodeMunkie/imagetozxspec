/* Image to ZX Spec
 * Copyright (C) 2018 Silent Software Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.converters.image.errordiffusionstrategy;

import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.processors.ErrorDiffusionConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ImageConverter;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Direct colour conversion with no error distribution
 * (hence no error is distributed in distributeError()!)
 */
public class NoDitherStrategy implements ErrorDiffusionDitherStrategy {

	@Override
	public void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer xBound, Integer yBound) {
		return;
	}
	
	@Override
	public String toString() {
		return getCaption("dit_none");
	}

	@Override
	public ImageConverter createImageConverter() {
		return new ErrorDiffusionConverterImpl();
	}

	@Override
	public ImageConverter createPreviewImageConverter(ColourChoiceStrategy colourChoiceStrategy) {
		return new ErrorDiffusionConverterImpl(this, colourChoiceStrategy);
	}
}
