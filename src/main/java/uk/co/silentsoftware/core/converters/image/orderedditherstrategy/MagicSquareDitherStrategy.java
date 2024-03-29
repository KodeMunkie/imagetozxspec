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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy;
import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Traditional "magic square" 4x4 dither algorithm whose sum of rows and columns are equal
 */
public class MagicSquareDitherStrategy extends AbstractOrderedDitherStrategy implements OrderedDitherStrategy {

	private static final int[] COEFFS = new int[]{
		0,15,10,5,
		5,0,15,10,
		10,5,0,15,
		15,10,5,0
	};
	
	/*
	 * {@inheritDoc}
	 */
	public int[] getCoefficients() {
		return COEFFS;
	}
	
	/*
	 * {@inheritDoc}
	 */
	public int getMatrixWidth() {
		return 4;
	}
	
	/*
	 * {@inheritDoc}
	 */
	public int getMatrixHeight() {
		return 4;
	}

	/*
	 * {@inheritDoc}
	 */
	public String toString() {
		return "Magic Square 4x4 ("+getCaption("ordered_dither")+")";
	}
}
