package uk.co.silentsoftware.dispatcher

import org.junit.Assert
import org.junit.Test
import uk.co.silentsoftware.config.OptionsObject
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy
import uk.co.silentsoftware.core.converters.image.processors.CharacterConverterImpl
import uk.co.silentsoftware.core.converters.image.processors.ErrorDiffusionConverterImpl
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenConverterImpl
import uk.co.silentsoftware.core.converters.image.processors.OrderedDitherConverterImpl

class WorkProcessorTest {

    @Test
    void testCharacterDitherStrategyUsesCharacterConverter() {
        OptionsObject oo = OptionsObject.getInstance()
        Assert.assertEquals(1, oo.getOtherDithers().length) // deliberately fragile, other dithers may have many different converters
        oo.setSelectedDitherStrategy(oo.getOtherDithers()[0])
        ColourChoiceStrategy ccs = oo.getColourModes()[0]
        Assert.assertEquals(FullPaletteStrategy.class, ccs.getClass()) // Verify we pulled out the right palette strategy to test with
        oo.setColourMode(ccs)
        WorkProcessor wp = new WorkProcessor()
        Assert.assertEquals(CharacterConverterImpl.class, wp.imageConverter.class)
    }

    @Test
    void testErrorDiffusionDitherStrategyUsesErrorDiffusionConverter() {
        OptionsObject oo = OptionsObject.getInstance()
        oo.setSelectedDitherStrategy(oo.getErrorDithers()[0])
        ColourChoiceStrategy ccs = oo.getColourModes()[0]
        Assert.assertEquals(FullPaletteStrategy.class, ccs.getClass()) // Verify we pulled out the right palette strategy to test with
        oo.setColourMode(ccs)
        WorkProcessor wp = new WorkProcessor()
        Assert.assertEquals(ErrorDiffusionConverterImpl.class, wp.imageConverter.class)
    }

    @Test
    void testOrderedDitherStrategyUsesOrderedDitherConverter() {
        OptionsObject oo = OptionsObject.getInstance()
        oo.setSelectedDitherStrategy(oo.getOrderedDithers()[0])
        ColourChoiceStrategy ccs = oo.getColourModes()[0]
        Assert.assertEquals(FullPaletteStrategy.class, ccs.getClass()) // Verify we pulled out the right palette strategy to test with
        oo.setColourMode(ccs)
        WorkProcessor wp = new WorkProcessor()
        Assert.assertEquals(OrderedDitherConverterImpl.class, wp.imageConverter.class)
    }

    @Test
    void testGigaScreenPaletteStrategyUsesGigaScreenConverter() {
        OptionsObject oo = OptionsObject.getInstance()
        oo.setSelectedDitherStrategy(oo.getErrorDithers()[0])
        ColourChoiceStrategy ccs = oo.getColourModes()[1]
        Assert.assertEquals(GigaScreenPaletteStrategy.class, ccs.getClass()) // Verify we pulled out the right palette strategy to test with
        oo.setColourMode(ccs)
        WorkProcessor wp = new WorkProcessor()
        Assert.assertEquals(GigaScreenConverterImpl.class, wp.imageConverter.class)
    }
}
