/* Image to ZX Spec
 * Copyright (C) 2022 Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.helpers;

import java.util.Comparator;

/**
 * Class to hold a colour and its frequency
 * N.b. intended use is just within this package
 * and thus variables are just package protected.
 */
public class TallyValue {
	private final Integer colour;
	private final Integer count;

	public TallyValue(Integer colour, Integer count) {
		this.colour = colour;
		this.count = count;
	}

	public Integer getColour() {
		return colour;
	}

	public Integer getCount() {
		return count;
	}

	/**
	 * Comparator to sort by value most popular first
	 */
	public final static Comparator<TallyValue> TALLY_COMPARATOR = (o1, o2) -> o2.count.compareTo(o1.count);
}
