package uk.co.silentsoftware.core.helpers.colourdistance;

/**
 * Algorithm to provide a distance value between a rgb component values and a given palette entry's rgb components
 */
public interface ColourDistanceStrategy {
    double getColourDistance(int red, int green, int blue, int[] paletteComps);
}