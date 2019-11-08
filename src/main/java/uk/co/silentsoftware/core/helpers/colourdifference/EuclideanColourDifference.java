package uk.co.silentsoftware.core.helpers.colourdifference;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

public class EuclideanColourDifference implements ColourDifferenceStrategy {

    @Override
    public double getColourDifference(int red, int green, int blue, int[] colourSetComps) {
        return Math.pow(red - colourSetComps[0], 2d) + Math.pow(green - colourSetComps[1], 2d) + Math.pow(blue - colourSetComps[2], 2d);
    }

    @Override
    public String toString() {
        return getCaption("adv_colour_diff_euclidean") ;
    }
}
