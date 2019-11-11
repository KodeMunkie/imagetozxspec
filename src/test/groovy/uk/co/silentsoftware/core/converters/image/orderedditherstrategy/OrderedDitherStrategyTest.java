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
