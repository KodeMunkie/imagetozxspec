package uk.co.silentsoftware.core.helpers.colourdistance;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Original colour distance algorithm used by Image to ZX Spec
 */
public class ClassicColourDistanceStrategy implements ColourDistanceStrategy {

    @Override
    public double getColourDistance(int red, int green, int blue, int[] colourSetComps) {
        return Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
    }

    @Override
    public String toString() {
        return getCaption("adv_colour_dist_classic") ;
    }
}
