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
package uk.co.silentsoftware.core.converters.image;

import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.processors.CharacterConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ImageConverter;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Marker for the image processor that this requires a non (character) standard dithering 
 */
public class CharacterDitherStrategy implements DitherStrategy {

	@Override
	public String toString() {
		return getCaption("character_dither");
	}

	@Override
	public ImageConverter createImageConverter() {
		return new CharacterConverterImpl();
	}

	@Override
	public ImageConverter createPreviewImageConverter(ColourChoiceStrategy colourChoiceStrategy) {
		return new CharacterConverterImpl(this, colourChoiceStrategy);
	}
}
