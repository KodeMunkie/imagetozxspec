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
package uk.co.silentsoftware.core.helpers.colourdistance;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Fast Euclidean colour distance calculation that doesn't consider human colour perception
 */
public class EuclideanColourDistance implements ColourDistanceStrategy {
    @Override
    public double getColourDistance(int red, int green, int blue, int[] colourSetComps) {
        return Math.pow(red - colourSetComps[0], 2d) + Math.pow(green - colourSetComps[1], 2d) + Math.pow(blue - colourSetComps[2], 2d);
    }
    @Override
    public String toString() {
        return getCaption("adv_colour_dist_euclidean") ;
    }
}
