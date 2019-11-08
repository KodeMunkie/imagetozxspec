package uk.co.silentsoftware.core.helpers.colourdifference;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

public class ClassicColourDifferenceStrategy implements ColourDifferenceStrategy {

    @Override
    public double getColourDifference(int red, int green, int blue, int[] colourSetComps) {
        return Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
    }

    @Override
    public String toString() {
        return getCaption("adv_colour_diff_classic") ;
    }
}
