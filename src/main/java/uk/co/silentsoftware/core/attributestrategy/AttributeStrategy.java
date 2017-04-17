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

import uk.co.silentsoftware.core.helpers.ColourHelper;


/**
 * Interface to enforce an attribute choice strategy
 */
public interface AttributeStrategy {

	/**
	 * Enforce the rule by modifying the rgb component objects
	 * and changing the attribute colour set they are from if
	 * necessary (i.e. bright or half bright).
	 * 
	 * @param mostPopularColour the most popular colour
	 * @param secondMostPopularColour the second most popular colour
	 * @return the modified colours which will be either in the bright or half bright set, but not both
	 */
	public int[] enforceAttributeRule(int mostPopularColour, int secondMostPopularColour);

	/**
	 * Similar to ColorHelper.isBrightSet but uses the strategy
	 * implementation to determine whether *both* colours should
	 * be in the bright or half bright set.
	 * @see ColourHelper#isBrightSet(int)
	 * 
	 * @param mostPopularColour the most popular colour
	 * @param secondMostPopularColour the second most popular colour
	 * @return whether the bright set should be used
	 */
	public boolean isBrightSet(int mostPopularColour, int secondMostPopularColour);
}
