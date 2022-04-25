package uk.co.silentsoftware.core.helpers.colourdistance;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Luminance colour distance adapted from http://appleoldies.ca/a2b/DHGRColors2017.htm
 */
public class LuminanceColourDistance implements ColourDistanceStrategy {

    public static final double LUMA_RED = 0.298839;
    public static final double LUMA_GREEN = 0.586811;
    public static final double LUMA_BLUE = 0.114350;

    @Override
    public double getColourDistance(int red, int green, int blue, int[] paletteComps) {
        double luma1 = (red*LUMA_RED + green*LUMA_GREEN + blue*LUMA_BLUE) / (255.0*1000);
        double luma2 = (paletteComps[0]*LUMA_RED + paletteComps[1]*LUMA_GREEN + paletteComps[2]*LUMA_BLUE) / (255.0*1000);
        double lumaDiff = luma1-luma2;
        double diffR = (paletteComps[0]-red)/255.0;
        double diffG = (paletteComps[1]-green)/255.0;
        double diffB = (paletteComps[2]-blue)/255.0;
        return (diffR*diffR*LUMA_RED + diffG*diffG*LUMA_GREEN+ diffB*diffB*LUMA_BLUE)*0.75+ lumaDiff*lumaDiff;
    }

    @Override
    public String toString() {
        return getCaption("adv_colour_dist_luminance") ;
    }
}
