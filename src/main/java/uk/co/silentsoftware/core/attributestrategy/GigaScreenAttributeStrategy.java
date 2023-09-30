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
package uk.co.silentsoftware.core.attributestrategy;

import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAttribute;

/**
 * Represents the screen-attribute distribution strategy by providing a 
 * per-attribute palette of 4 Gigascreen colours (i.e. 
 * each Gigascreen colour is from a palette of 102 possible colours, 
 * the strategy specifies whether the 2 Spectrum screens making up this attribute
 * are bright, half bright or mixed). 
 */
public interface GigaScreenAttributeStrategy {

	/**
	 * Retrieves the GigaScreenAttribute which represents a Spectrum
	 * attribute block and the possible 4 colours that can be displayed
	 * in it in Gigascreen mode.  
	 * e.g both screen brights would be one strategy.
	 * 
	 * @return the gigascreenattribute's palette
	 */
	GigaScreenAttribute[] getPalette();
}
