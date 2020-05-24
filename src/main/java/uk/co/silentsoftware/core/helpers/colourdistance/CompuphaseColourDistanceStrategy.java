/* Image to ZX Spec
 * Copyright (C) 2020 Silent Software (Benjamin Brown)
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
 * Algorithm based on a paper at https://www.compuphase.com/cmetric.htmhttps://www.compuphase.com/cmetric.htm
 * (Thiadmer Riemersma, CompuPhase)
 */
public class CompuphaseColourDistanceStrategy implements ColourDistanceStrategy {

    @Override
    public double getColourDistance(int red, int green, int blue, int[] colourSetComps) {
        long rmean = ((long) colourSetComps[0] + (long) red) / 2;
        long r = (long) colourSetComps[0] - (long) red;
        long g = (long) colourSetComps[1] - (long) green;
        long b = (long) colourSetComps[2] - (long) blue;
        return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }

    @Override
    public String toString() {
        return getCaption("adv_colour_dist_compuphase") ;
    }
}
