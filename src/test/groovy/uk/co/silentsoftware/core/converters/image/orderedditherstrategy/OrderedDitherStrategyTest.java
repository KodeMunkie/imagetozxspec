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
package uk.co.silentsoftware.core.converters.image.orderedditherstrategy;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class OrderedDitherStrategyTest {

    @Test
    public void assertBayerEightByEightCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new BayerEightByEightDitherStrategy());
    }

    @Test
    public void assertBayerFourByFourCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new BayerEightByEightDitherStrategy());
    }

    @Test
    public void assertBayerTwoByTwoCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new BayerEightByEightDitherStrategy());
    }

    @Test
    public void assertBayerTwoByOneCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new BayerEightByEightDitherStrategy());
    }

    @Test
    public void assertMagicSquareCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new MagicSquareDitherStrategy());
    }

    @Test
    public void assertNasikMagicSquareCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new NasikMagicSquareDitherStrategy());
    }

    @Test
    public void assertOmegaCoeffsWithinRange() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertCoeffsWithinRange(new OmegaOrderedDitherStrategy());
    }

    void assertCoeffsWithinRange(AbstractOrderedDitherStrategy strat) {
        int[] coeffs = strat.getCoefficients();
        int maxCoEff = coeffs.length-1;
        for (int value : coeffs) {
            if (value < 0) {
                Assert.fail("Invalid value "+value);
            }
            if (value > maxCoEff) {
                Assert.fail("Value "+value+" exceeded expected max value "+maxCoEff);
            }
        }
    }
}
