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
package uk.co.silentsoftware.core.attributestrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute;

/**
 * Palette derived from one screen having a bright attribute and the other half bright
 */
public class GigaScreenMixedPaletteStrategy implements GigaScreenAttributeStrategy {

	@Override
	public GigaScreenAttribute[] getPalette() {
		return SpectrumDefaults.GIGASCREEN_MIXED_ATTRIBUTES;
	}

	@Override
	public String toString() {
		return getCaption("attr_giga_mixed") ;
	}
}
