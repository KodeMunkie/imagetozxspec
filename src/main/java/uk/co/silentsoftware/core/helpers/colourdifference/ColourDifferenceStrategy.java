package uk.co.silentsoftware.core.helpers.colourdifference;

public interface ColourDifferenceStrategy {
    double getColourDifference(int red, int green, int blue, int[] colourSetComps);
}