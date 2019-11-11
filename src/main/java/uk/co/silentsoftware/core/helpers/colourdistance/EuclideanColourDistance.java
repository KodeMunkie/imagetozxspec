package uk.co.silentsoftware.core.helpers.colourdistance;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Fast Euclidean colour distance calculation that doesn't consider human perception
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
