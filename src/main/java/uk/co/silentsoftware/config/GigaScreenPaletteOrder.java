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
package uk.co.silentsoftware.config;

/**
 * Enum storing screen colour ordering modes, e.g "Brightness"
 * would group whichever screen's attributes were darker on
 * screen 1. 
 */
public enum GigaScreenPaletteOrder {
	None, Intelligent, Luminosity, Hue, Saturation, Brightness, HueSaturation, HueBrightness, SaturationBrightness;
	@Override
	public String toString() {
		return LanguageSupport.getCaption("scr_export_giga_"+this.name().toLowerCase());
	}
}
