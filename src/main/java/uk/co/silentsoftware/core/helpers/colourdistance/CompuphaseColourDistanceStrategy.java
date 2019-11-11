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
