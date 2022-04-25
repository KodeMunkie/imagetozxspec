/* Image to ZX Spec
 * Copyright (C) 2020 Silent Software (Benjamin Brown)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.silentsoftware.config;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.core.attributestrategy.*;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.CharacterDitherStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.*;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.*;
import uk.co.silentsoftware.core.converters.video.HumbleVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VLCVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VideoImportEngine;
import uk.co.silentsoftware.core.helpers.colourdistance.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * The monolithic backing object behind the OptionDialog, holding the particular user
 * configuration. This object's marked @PreferencesFields are primitives which are 
 * persisted after modification by the UI.
 */
public class OptionsObject {

	private static final Logger log = LoggerFactory.getLogger(OptionsObject.class);
	
	/**
	 * The version of preferences
	 */
	@PreferencesField
	private int prefsVersion = 1;
	
	/**
	 * The number of starts this app has had
	 */
	@PreferencesField
	private int starts = 0;
	
	/**
	 * Path to the VLC binary
	 */
	@PreferencesField
	private String pathToVideoEngineLibrary = null;

	/**
	 * The number of frames to sample from the video per second
	 */
	@PreferencesField
	private volatile double videoFramesPerSecond = 10;

	/**
	 * The number of milliseconds delay for each gif frame
	 */
	@PreferencesField
	private volatile int gifDisplayTimeMillis = 100;

	/**
	 * CPU time is diverted more to processing than rendering or other less important tasks
	 */
	@PreferencesField
	private volatile boolean turboMode = false;

	/**
	 * Prefix identifier for custom basic loaders
	 */
	public final static String CUSTOM_LOADER_PREFIX = getCaption("loader_custom") + " ";

	/**
	 * Basic loader for slideshows/video (tap output)
	 */
	private final List<BasicLoader> basicLoaders;
	{
		basicLoaders = new ArrayList<>();
		basicLoaders.add(new BasicLoader(getCaption("loader_simple"), "/simple.tap"));
		basicLoaders.add(new BasicLoader(getCaption("loader_buffered"), "/buffered.tap"));
		basicLoaders.add(new BasicLoader(getCaption("loader_gigascreen"), "/gigascreen.tap"));
		basicLoaders.add(new BasicLoader(CUSTOM_LOADER_PREFIX, null));
	}

	/**
	 * The chosen basic loader
	 */
	@PreferencesField
	private volatile int basicLoader = 0;

	/**
	 * Error diffusion dither strategies available
	 */
	private final List<ErrorDiffusionDitherStrategy> errorDithers;
	{
		errorDithers = new ArrayList<>();
		errorDithers.add(new AtkinsonDitherStrategy());
		errorDithers.add(new BurkesDitherStrategy());
		errorDithers.add(new FloydSteinbergDitherStrategy());
		errorDithers.add(new JarvisJudiceNinkeDitherStrategy());
		errorDithers.add(new LowErrorAtkinsonDitherStrategy());
		errorDithers.add(new NoDitherStrategy());
		errorDithers.add(new SierraFilterLightStrategy());
		errorDithers.add(new StuckiDitherStrategy());
	}

	/**
	 * Ordered dither strategies available
	 */
	private final List<OrderedDitherStrategy> orderedDithers;
	{
		orderedDithers = new ArrayList<>();
		orderedDithers.add(new BayerTwoByOneOrderedDitherStrategy());
		orderedDithers.add(new BayerTwoByTwoOrderedDitherStrategy());
		orderedDithers.add(new OmegaOrderedDitherStrategy()); 
		orderedDithers.add(new BayerFourByFourDitherStrategy());
		orderedDithers.add(new BayerEightByEightDitherStrategy());
		orderedDithers.add(new MagicSquareDitherStrategy());
		orderedDithers.add(new NasikMagicSquareDitherStrategy());
	}

	/**
	 * Any non standard dither strategies (e.g character dither)
	 */
	private final List<DitherStrategy> otherDithers;
	{
		otherDithers = new ArrayList<>();
		otherDithers.add(new CharacterDitherStrategy());
	}
	
	/**
	 * The chosen dither strategy
	 */
	@PreferencesField
	private volatile int selectedDitherStrategy = 0;

	/**
	 * The chosen dither strategy type
	 */
	@PreferencesField
	private volatile String selectedDitherStrategyType = ErrorDiffusionDitherStrategy.class.getName();

	public final static ScalingObject INTERLACED = new ScalingObject(getCaption("scaling_interlace"),
			SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT*2);

	/**
	 * Scaling modes available
	 */
	private final List<ScalingObject> scalings;
	{
		scalings = new ArrayList<>();
		scalings.add(new ScalingObject(getCaption("scaling_default"), SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT));
		scalings.add(INTERLACED);
		scalings.add(new ScalingObject(getCaption("scaling_none"), -1, -1));
		scalings.add(new ScalingObject(getCaption("scaling_width_prop"), SpectrumDefaults.SCREEN_WIDTH, -1));
		scalings.add(new ScalingObject(getCaption("scaling_height_prop"), -1, SpectrumDefaults.SCREEN_HEIGHT));
	}

	/**
	 * ZX Spectrum scaling mode
	 */
	public final ScalingObject zxScaling = scalings.get(0);	
	
	/**
	 * Currently selected scaling mode
	 */
	@PreferencesField
	public volatile int scaling = 0;

	public static final GigaScreenPaletteStrategy GIGASCREEN_PALETTE_STRATEGY = new GigaScreenPaletteStrategy();

	/**
	 * Pixel colouring strategy - akin to screen modes on the Spectrum
	 * i.e. 2, 15 or 102 colours.
	 */
	private final List<ColourChoiceStrategy> colourModes;
	{
		colourModes = new ArrayList<>();
		colourModes.add(new FullPaletteStrategy());
		colourModes.add(GIGASCREEN_PALETTE_STRATEGY);
		colourModes.add(new MonochromePaletteStrategy());
	}

	/**
	 * Currently selected pixel colouring strategy
	 */
	@PreferencesField
	private volatile int colourMode = 0;

	/**
	 * Attribute favouritism choice - when colours need to be changed to a two
	 * colour attribute block of 8x8 what is favoured?
	 */
	private final List<AttributeStrategy> attributeModes;
	{
		attributeModes = new ArrayList<>();
		attributeModes.add(new FavourHalfBrightAttributeStrategy());
		attributeModes.add(new FavourBrightAttributeStrategy());
		attributeModes.add(new FavourMostPopularAttributeStrategy());
		attributeModes.add(new ForceHalfBrightAttributeStrategy()); 
		attributeModes.add(new ForceBrightAttributeStrategy());
		attributeModes.add(new ForceReducedHalfBrightAttributeStrategy());
	};

	/**
	 * Currently selected attribute favouritism mode
	 */
	@PreferencesField
	private volatile int attributeMode = 0;

	/**
	 * Attribute favouritism choice for Gigascreen. This is similar to the regular
	 * attribute modes but needs to take account for the fact that the extra
	 * colours are created by using two Spectrum screens (e.g. black+white full bright = grey)
	 */
	private final List<GigaScreenAttributeStrategy> gigaScreenAttributeModes;
	{
		gigaScreenAttributeModes = new ArrayList<>();
		gigaScreenAttributeModes.add(new GigaScreenHalfBrightPaletteStrategy());
		gigaScreenAttributeModes.add(new GigaScreenBrightPaletteStrategy());
		gigaScreenAttributeModes.add(new GigaScreenMixedPaletteStrategy());
	};

	private final List<ColourDistanceStrategy> colourDistancesModes;
	{
		colourDistancesModes = new ArrayList<>();
		colourDistancesModes.add(new LuminanceColourDistance());
		colourDistancesModes.add(new CompuphaseColourDistanceStrategy());
		colourDistancesModes.add(new ClassicColourDistanceStrategy());
		colourDistancesModes.add(new EuclideanColourDistance());
	}

	/**
	 * Currently selected gigascreen attribute mode
	 */
	@PreferencesField
	private volatile int gigaScreenAttributeMode = 0;

	/**
	 * The chosen giga screen hsb option 
	 */
	@PreferencesField
	private volatile String gigaScreenPaletteOrder = GigaScreenPaletteOrder.Luminosity.name();

	/**
	 * The video converting libraries supported
	 */
	private final List<VideoImportEngine> videoImportEngines;
	{
		videoImportEngines = new ArrayList<>();
		videoImportEngines.add(new HumbleVideoImportEngine());
		
		// VLCJ doesn't work on MacOS.
		if (SystemUtils.IS_OS_MAC_OSX) {
			log.info("Disabling VLCJ import engine - not supported on OSX");
		} else {
			videoImportEngines.add(new VLCVideoImportEngine());
		}
	};

	/**
	 * Currently selected video converter
	 */
	@PreferencesField
	private volatile int videoImportEngine = 0;

	/**
	 * Display frames per second
	 */
	@PreferencesField
	private volatile boolean fpsCounter = false;

	/**
	 * Display frames per second
	 */
	@PreferencesField
	private volatile boolean showWipPreview = true;

	/**
	 * Export image formats available
	 */
	private final String[] imageFormats = new String[] { "png", "jpg" };

	/**
	 * Currently selected image export format
	 */
	@PreferencesField
	private volatile String imageFormat = imageFormats[0];

	/**
	 * Image pre-process contrast setting
	 */
	@PreferencesField
	private volatile float contrast = 1;

	/**
	 * Image pre-process brightness setting
	 */
	@PreferencesField
	private volatile float brightness = 0;

	/**
	 * Image pre-process saturation setting
	 */
	@PreferencesField
	private volatile float saturation = 0;

	/**
	 * Monochrome b/w threshold - determines what value a colour rgb values
	 * must all be below for it to be considered black
	 */
	@PreferencesField
	private volatile int blackThreshold = 128;

	/**
	 * Flag for allowing regular image export
	 */
	@PreferencesField
	private volatile boolean exportImage = true;

	/**
	 * Flag for allowing Spectrum screen file export
	 */
	@PreferencesField
	private volatile boolean exportScreen = false;

	/**
	 * Flag for allowing Spectrum tape (slideshow) file export
	 */
	@PreferencesField
	private volatile boolean exportTape = false;

	/**
	 * Flag to export an anim gif
	 */
	@PreferencesField
	private volatile boolean exportAnimGif = false;

	/**
	 * Flag to export a text dither as a text file
	 */
	@PreferencesField
	private volatile boolean exportText = false;

	/**
	 * The monochrome mode ink colour (spectrum palette index)
	 */
	@PreferencesField
	private volatile int monochromeInkIndex = 0;

	/**
	 * The monochrome mode paper colour (spectrum palette index)
	 */
	@PreferencesField
	private volatile int monochromePaperIndex = 7;

	/**
	 * Intensity for ordered dithering
	 */
	@PreferencesField
	private volatile int orderedDitherIntensity = 1;

	/**
	 * Serpentine dithering mode (image rows traversed 
	 * in alternate directions)
	 */
	@PreferencesField
	private volatile boolean serpentine = false;

	/**
	 * Constrains the error of the error diffusion to 
	 * 8x8 pixels (a single attribute block). Can lead
	 * to a grid pattern effect.
	 */
	@PreferencesField
	private volatile boolean constrainedErrorDiffusion = false;

	/**
	 * Algorithm to compare colour likeness
	 */
	@PreferencesField
	private volatile int colourDistanceStrategy = 0;

	/**
	 * Singleton instance of this class
	 */
	private final static OptionsObject instance;

	static {
		Optional<OptionsObject> tempRef = PreferencesService.load();
		if (tempRef.isPresent()) {
			instance = tempRef.get();
		} else {
			instance = new OptionsObject();
		}
		// Start this early as some engines take a number of seconds to load
		instance.getVideoImportEngine().initVideoImportEngine(Optional.ofNullable(instance.getPathToVideoEngineLibrary()));
		log.info("Options initialised");
	}
	
	/**
	 * Retrieves the only option object instance
	 * 
	 * @return the singleton instance of this object
	 */
	public static OptionsObject getInstance() {
		if (instance == null) {
			log.error("Options instance is null!");
		}
		return instance;
	}
	
	public int getStarts() {
		return starts;
	}

	public void setStarts(int starts) {
		this.starts = starts;
	}

	public void setTurboMode(boolean turbo) {
		this.turboMode = turbo;
	}
	
	public boolean getTurboMode() {
		return turboMode;
	}	
	
	public ErrorDiffusionDitherStrategy[] getErrorDithers() {
		return errorDithers.toArray(new ErrorDiffusionDitherStrategy[0]);
	}

	public ScalingObject[] getScalings() {
		return scalings.toArray(new ScalingObject[0]);
	}

	public ScalingObject getScaling() {
		return scalings.get(scaling);
	}

	public ScalingObject getZXDefaultScaling() {
		return zxScaling;
	}

	public void setScaling(ScalingObject scaling) {
		this.scaling = scalings.indexOf(scaling);
	}

	public float getContrast() {
		return contrast;
	}

	public void setContrast(float contrast) {
		this.contrast = contrast;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public void setFpsCounter(boolean fpsCounter) {
		this.fpsCounter = fpsCounter;
	}

	public void setShowWipPreview(boolean showWipPreview) {
		this.showWipPreview = showWipPreview;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}

	public String[] getImageFormats() {
		return imageFormats;
	}

	public boolean getFpsCounter() {
		return fpsCounter;
	}

	public boolean getShowWipPreview() {
		return showWipPreview;
	}

	public boolean getExportImage() {
		return exportImage;
	}

	public void setExportImage(boolean exportImage) {
		this.exportImage = exportImage;
	}

	public boolean getExportScreen() {
		return exportScreen;
	}

	public void setExportScreen(boolean exportScreen) {
		this.exportScreen = exportScreen;
	}

	public boolean getExportAnimGif() {
		return exportAnimGif;
	}

	public void setExportAnimGif(boolean exportAnimGif) {
		this.exportAnimGif = exportAnimGif;
	}

	public boolean getExportText() {
		return exportText;
	}

	public void setExportText(boolean exportText) {
		this.exportText = exportText;
	}

	public boolean getExportTape() {
		return exportTape;
	}

	public void setExportTape(boolean exportTape) {
		this.exportTape = exportTape;
	}

	public ColourChoiceStrategy getColourMode() {
		return colourModes.get(colourMode);
	}

	public void setColourMode(ColourChoiceStrategy colourMode) {
		this.colourMode = colourModes.indexOf(colourMode);
	}

	public ColourChoiceStrategy[] getColourModes() {
		return colourModes.toArray(new ColourChoiceStrategy[0]);
	}

	public int getMonochromeInkIndex() {
		return monochromeInkIndex;
	}

	public void setMonochromeInkIndex(int monochromeInkIndex) {
		this.monochromeInkIndex = monochromeInkIndex;
	}

	public int getMonochromePaperIndex() {
		return monochromePaperIndex;
	}

	public void setMonochromePaperIndex(int monochromePaperIndex) {
		this.monochromePaperIndex = monochromePaperIndex;
	}

	public int getBlackThreshold() {
		return blackThreshold;
	}

	public void setBlackThreshold(int blackThreshold) {
		this.blackThreshold = blackThreshold;
	}

	public OrderedDitherStrategy[] getOrderedDithers() {
		return orderedDithers.toArray(new OrderedDitherStrategy[0]);
	}

	public DitherStrategy[] getOtherDithers() {
		return otherDithers.toArray(new DitherStrategy[0]);
	}
	
	public DitherStrategy getSelectedDitherStrategy() {
		if (ErrorDiffusionDitherStrategy.class.getName().equals(selectedDitherStrategyType)) {
			return errorDithers.get(selectedDitherStrategy);
		}
		if (OrderedDitherStrategy.class.getName().equals(selectedDitherStrategyType)) {
			return orderedDithers.get(selectedDitherStrategy);
		}
		if (CharacterDitherStrategy.class.getName().equals(selectedDitherStrategyType)) {
			return otherDithers.get(selectedDitherStrategy);
		}
		throw new IllegalStateException("Unknown dither strategy "+selectedDitherStrategy);
	}

	public void setSelectedDitherStrategy(DitherStrategy selectedDitherStrategy) {
		if (errorDithers.contains(selectedDitherStrategy)) {
			this.selectedDitherStrategy = errorDithers.indexOf(selectedDitherStrategy);
			this.selectedDitherStrategyType = ErrorDiffusionDitherStrategy.class.getName();
		}
		if (orderedDithers.contains(selectedDitherStrategy)) {
			this.selectedDitherStrategy = orderedDithers.indexOf(selectedDitherStrategy);
			this.selectedDitherStrategyType = OrderedDitherStrategy.class.getName();
		}
		if (otherDithers.contains(selectedDitherStrategy)) {
			this.selectedDitherStrategy = otherDithers.indexOf(selectedDitherStrategy);
			this.selectedDitherStrategyType = CharacterDitherStrategy.class.getName();
		}
	}

	public int getOrderedDitherIntensity() {
		return orderedDitherIntensity;
	}

	public void setOrderedDitherIntensity(int orderedDitherIntensity) {
		this.orderedDitherIntensity = orderedDitherIntensity;
	}

	public AttributeStrategy getAttributeMode() {
		return attributeModes.get(attributeMode);
	}

	public void setAttributeMode(AttributeStrategy attributeMode) {
		this.attributeMode = attributeModes.indexOf(attributeMode);
	}

	public AttributeStrategy[] getAttributeModes() {
		return attributeModes.toArray(new AttributeStrategy[0]);
	}

	public BasicLoader[] getBasicLoaders() {
		return basicLoaders.toArray(new BasicLoader[0]);
	}

	public BasicLoader getBasicLoader() {
		return basicLoaders.get(basicLoader);
	}

	public void setBasicLoader(BasicLoader basicLoader) {
		this.basicLoader = basicLoaders.indexOf(basicLoader);
	}

	public double getVideoFramesPerSecond() {
		return videoFramesPerSecond;
	}

	public void setVideoFramesPerSecond(double videoFramesPerSecond) {
		this.videoFramesPerSecond = videoFramesPerSecond;
	}

	public int getGifDisplayTimeMillis() {
		return gifDisplayTimeMillis;
	}

	public void setGifDisplayTimeMillis(int gifDisplayTimeMillis) {
		this.gifDisplayTimeMillis = gifDisplayTimeMillis;
	}

	public boolean getSerpentine() {
		return serpentine;
	}

	public void setSerpentine(boolean serpentine) {
		this.serpentine = serpentine;
	}

	public boolean getConstrainedErrorDiffusion() {
		return constrainedErrorDiffusion;
	}

	public void setConstrainedErrorDiffusion(boolean constrainedErrorDiffusion) {
		this.constrainedErrorDiffusion = constrainedErrorDiffusion;
	}

	public VideoImportEngine getVideoImportEngine() {
		return videoImportEngines.get(videoImportEngine);
	}

	public void setVideoImportEngine(VideoImportEngine videoImportEngine) {
		this.videoImportEngine = videoImportEngines.indexOf(videoImportEngine);
	}

	public VideoImportEngine[] getVideoImportEngines() {
		return videoImportEngines.toArray(new VideoImportEngine[0]);
	}

	public String getPathToVideoEngineLibrary() {
		return pathToVideoEngineLibrary;
	}

	public void setPathToVideoEngineLibrary(String path) {
		this.pathToVideoEngineLibrary = path;
	}

	public GigaScreenAttributeStrategy[] getGigaScreenAttributeStrategies() {
		return gigaScreenAttributeModes.toArray(new GigaScreenAttributeStrategy[0]);
	}

	public GigaScreenAttributeStrategy getGigaScreenAttributeStrategy() {
		return gigaScreenAttributeModes.get(gigaScreenAttributeMode);
	}

	public void setGigaScreenAttributeStrategy(GigaScreenAttributeStrategy gigaScreenAttributeStrategy) {
		this.gigaScreenAttributeMode = gigaScreenAttributeModes.indexOf(gigaScreenAttributeStrategy);
	}

	public GigaScreenPaletteOrder[] getGigaScreenPaletteOrders() {
		return GigaScreenPaletteOrder.values();
	}

	public GigaScreenPaletteOrder getGigaScreenPaletteOrder() {
		return GigaScreenPaletteOrder.valueOf(gigaScreenPaletteOrder);
	}

	public void setGigaScreenPaletteOrder(GigaScreenPaletteOrder gigaScreenAttributeOrderingOption) {
		this.gigaScreenPaletteOrder = gigaScreenAttributeOrderingOption.name();
	}

	public ColourDistanceStrategy getColourDistanceMode() {
		return colourDistancesModes.get(colourDistanceStrategy);
	}

	public ColourDistanceStrategy[] getColourDistances() {
		return colourDistancesModes.toArray(new ColourDistanceStrategy[0]);
	}

	public void setColourDistanceStrategy(ColourDistanceStrategy colourDistanceStrategy) {
		this.colourDistanceStrategy =  colourDistancesModes.indexOf(colourDistanceStrategy);
	}
}
