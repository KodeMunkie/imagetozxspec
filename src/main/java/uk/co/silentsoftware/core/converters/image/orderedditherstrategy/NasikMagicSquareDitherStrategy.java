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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy;
import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Special case of a magic square which has horizontal, vertical 
 * and diagonal totals all equal.
 * 
 * An example of this dither is at http://en.wikipedia.org/wiki/Magic_square
 */
public class NasikMagicSquareDitherStrategy extends AbstractOrderedDitherStrategy implements OrderedDitherStrategy {

	private static final int[] COEFFS = new int[]{
		0,56,12,52,
		44,20,32,24,
		48,8,60,4,
		28,36,16,40
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
		return "Nasik Magic Square 4x4 ("+getCaption("ordered_dither")+")";
	}
}
