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
package uk.co.silentsoftware.ui.listener;

import uk.co.silentsoftware.core.converters.image.DitherStrategy;

/**
 * Interface to notify of change of dither strategy
 */
public interface DitherChangedListener {

	/**
	 * A new dither strategy has been chosen on the UI
	 * 
	 * @param dither the chosen dither strategy
	 */
	void ditherSelected(DitherStrategy dither);
}
