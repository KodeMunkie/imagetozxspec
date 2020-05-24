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
package uk.co.silentsoftware.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.config.*;
import uk.co.silentsoftware.core.attributestrategy.AttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.video.VLCVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VideoImportEngine;
import uk.co.silentsoftware.core.helpers.colourdistance.ColourDistanceStrategy;
import uk.co.silentsoftware.ui.listener.DitherChangedListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLORS;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT;

/**
 * The options selection dialog
 *
 * Note that this class is tied quite tightly to
 * business logic (e.g. slider ranges/magic numbers) 
 * really just because there isn't need to do
 * the separation for a program this small or at this
 * point.
 * 
 * TODO: This class needs further decomposition for single responsibility principle
 */
class PreferencesDialog extends JFrame  {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final long serialVersionUID = 1L;
	
	private final List<DitherChangedListener> ditherChangedListeners = new ArrayList<>();
	
	private final JFrame currentInstance;
	
	/**
	 * Default constructor that initialises all the tabs
	 */
	PreferencesDialog() {
		
		// Initialise options
		//noinspection ResultOfMethodCallIgnored
		OptionsObject.getInstance();
		
		// Init the look and feel
		initLookAndFeel();		
		
		// The frame defaults
		setIconImage(ImageToZxSpec.IMAGE_ICON.getImage());
		setTitle(getCaption("tab_item_control_panel"));
		setSize(600,480);
	    setLocationRelativeTo(null); 
	    setResizable(false);
	    
	    // Set up the tabbed panel
		JTabbedPane pane = new JTabbedPane();
		pane.addTab(getCaption("tab_item_misc_options"), createGeneralOptions());
		pane.addTab(getCaption("tab_item_pre_process_options"), createPreProcessOptions());
		pane.addTab(getCaption("tab_item_dither_options"), createDitherOptions());
		pane.addTab(getCaption("tab_item_advanced_options"), createAdvancedOptions());
		getContentPane().add(pane);
		currentInstance = this;
	}
	
	/**
	 * Initialises the platform look and feel
	 */
	private void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			log.debug("Unable to set the platform lnf",e);
			// Pah just ignore this error, we'll just have a naff looking UI
		}
	}
	
	/**
	 * Method that adds the pre process options tab and
	 * its action listeners.
	 * 
	 * @return the pre process options panel
	 */
	private JPanel createPreProcessOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6,2));
		JLabel label = new JLabel(getCaption("pp_scaling"), JLabel.CENTER);
		final JPanel scalingPadding = new JPanel(new GridLayout(3,1));
		
		final JComboBox<ScalingObject> scaling = new JComboBox<>(oo.getScalings());
		scaling.setSelectedItem(oo.getScaling());
		scaling.addActionListener(event -> {
			if (OptionsObject.INTERLACED == scaling.getSelectedItem()) {
				colourModes.setSelectedItem(OptionsObject.GIGASCREEN_PALETTE_STRATEGY);
				colourModes.setEnabled(false);
			} else {
				colourModes.setEnabled(true);
			}
            oo.setScaling((ScalingObject)scaling.getSelectedItem());
            PreferencesService.save();
        });
		panel.add(label);
		// TODO: this is a bit lame and Java 1.1 style using padding like this
		scalingPadding.add(new JPanel());
		scalingPadding.add(scaling);
		scalingPadding.add(new JPanel());
		panel.add(scalingPadding);
		label = new JLabel(getCaption("pp_video_rate"), JLabel.CENTER);
		final JTextField sampleRate = new JTextField();
		final JPanel samplePadding = new JPanel(new GridLayout(3,1));
		sampleRate.setHorizontalAlignment(JTextField.RIGHT);
		sampleRate.setText(""+oo.getVideoFramesPerSecond());
		sampleRate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e)
			{
				String value = sampleRate.getText();
				if (value != null && value.trim().length() > 0) {
					try {
						double d = Double.parseDouble(value);
						if (d > 0) {
							oo.setVideoFramesPerSecond(d);
							return;
						}
					} catch (NumberFormatException nfe) {
						log.debug("Not a number for vfps", nfe);
					}
				}
				sampleRate.setText(""+oo.getVideoFramesPerSecond());
				PreferencesService.save();
			}
		});
		panel.add(label);
		samplePadding.add(new JPanel());
		samplePadding.add(sampleRate);
		samplePadding.add(new JPanel());
		panel.add(samplePadding);
		label = new JLabel(getCaption("pp_saturation"), JLabel.CENTER);
		final JSlider satSlider = new JSlider(-100, 100);
		satSlider.setMajorTickSpacing(25);
		satSlider.setPaintTicks(true);
		satSlider.setPaintLabels(true);
		satSlider.setLabelTable(satSlider.createStandardLabels(25));
		satSlider.setValue(Math.round(oo.getSaturation()*100f));
		satSlider.addChangeListener(ce -> {
            oo.setSaturation(satSlider.getValue()/100f);
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(satSlider);
		label = new JLabel(getCaption("pp_contrast"), JLabel.CENTER);
		final JSlider contrastSlider = new JSlider(-100, 100);
		contrastSlider.setMajorTickSpacing(25);
		contrastSlider.setPaintTicks(true);
		contrastSlider.setPaintLabels(true);
		contrastSlider.setLabelTable(contrastSlider.createStandardLabels(25));
		contrastSlider.setValue(Math.round((oo.getContrast() -1)*100f));
		contrastSlider.addChangeListener(ce -> {
            oo.setContrast((contrastSlider.getValue()/100f)+1);
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(contrastSlider);
		label = new JLabel(getCaption("pp_brightness"), JLabel.CENTER);
		final JSlider brightnessSlider = new JSlider(-100, 100);
		brightnessSlider.setMajorTickSpacing(25);
		brightnessSlider.setPaintTicks(true);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setLabelTable(brightnessSlider.createStandardLabels(25));
		brightnessSlider.setValue(Math.round((oo.getBrightness() /2.56f)));
		brightnessSlider.addChangeListener(ce -> {
            oo.setBrightness(brightnessSlider.getValue()*2.56f);
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(brightnessSlider);


		label = new JLabel(getCaption("pp_reset_sliders"), JLabel.CENTER);
		JButton button = new JButton(getCaption("pp_reset"));
		button.addActionListener(e -> {
            brightnessSlider.setValue(0);
            contrastSlider.setValue(0);
            satSlider.setValue(0);
        });
		final JPanel resetPadding = new JPanel(new GridLayout(3,1));
		resetPadding.add(new JPanel());
		resetPadding.add(button);
		resetPadding.add(new JPanel());
		panel.add(label);
		panel.add(resetPadding);

		return panel;
	}
	
	/**
	 * Creates the advanced options panel
	 * 
	 * @return the advances options panel
	 */
	private JPanel createAdvancedOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(7,2));
		JLabel label = new JLabel(getCaption("adv_serpentine"), JLabel.CENTER);
		final JCheckBox serpentine = new JCheckBox();
		serpentine.setSelected(oo.getSerpentine());
		serpentine.addActionListener(event -> {
            oo.setSerpentine(serpentine.isSelected());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(serpentine);
		label = new JLabel(getCaption("adv_constrain_diffusion"), JLabel.CENTER);
		final JCheckBox constrain = new JCheckBox();
		constrain.setSelected(oo.getConstrainedErrorDiffusion());
		constrain.addActionListener(event -> {
            oo.setConstrainedErrorDiffusion(constrain.isSelected());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(constrain);
		label = new JLabel(getCaption("adv_colourspace_averaging"), JLabel.CENTER);
		final JCheckBox colourspaceAveraging = new JCheckBox();
		colourspaceAveraging.setSelected(oo.getColourspaceAveraging());
		colourspaceAveraging.addActionListener(event -> {
			oo.setSerpentine(colourspaceAveraging.isSelected());
			PreferencesService.save();
		});
		panel.add(label);
		panel.add(colourspaceAveraging);

		label = new JLabel(getCaption("adv_scr_hsb_order"), JLabel.CENTER);
		final JComboBox<GigaScreenPaletteOrder> paletteOptions = new JComboBox<>(oo.getGigaScreenPaletteOrders());
		paletteOptions.setSelectedItem(oo.getGigaScreenPaletteOrder());
		paletteOptions.addActionListener(e -> {
            oo.setGigaScreenPaletteOrder((GigaScreenPaletteOrder)paletteOptions.getSelectedItem());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(paletteOptions);
		label = new JLabel(getCaption("adv_colour_dist"), JLabel.CENTER);
		final JComboBox<ColourDistanceStrategy> colourOptions = new JComboBox<>(oo.getColourDistances());
		colourOptions.setSelectedItem(oo.getColourDistanceMode());
		colourOptions.addActionListener(e -> {
			oo.setColourDistanceStrategy((ColourDistanceStrategy) colourOptions.getSelectedItem());
			PreferencesService.save();
		});
		panel.add(label);
		panel.add(colourOptions);
		
		label = new JLabel(getCaption("adv_video_import_engine"), JLabel.CENTER);
		final JComboBox<VideoImportEngine> importEngine = new JComboBox<>(oo.getVideoImportEngines());
		importEngine.setSelectedItem(oo.getVideoImportEngine());
		importEngine.addActionListener(createImportEngineActionListener(importEngine));
		final JPanel importPadding = new JPanel(new GridLayout(3,1));
		importPadding.add(new JPanel());
		importPadding.add(importEngine);
		importPadding.add(new JPanel());
		panel.add(label);
		panel.add(importPadding);
		

		label = new JLabel(getCaption("adv_video_turbo"), JLabel.CENTER);
		final JCheckBox turbo= new JCheckBox();
		turbo.setSelected(oo.getTurboMode());
		turbo.addActionListener(event -> {
            oo.setTurboMode(turbo.isSelected());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(turbo); 	
		return panel;
	}
	
	/**
	 * Creates the import engine action listener that allows selection of
	 * an associated the native library for VLC
	 * 
	 * @param importEngine the jcombobox with the import engine instances
	 * @return the action listener
	 */
	private ActionListener createImportEngineActionListener(JComboBox<VideoImportEngine> importEngine) {
		return event -> {
            if (importEngine.getSelectedItem() instanceof VLCVideoImportEngine) {
                try {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setDialogTitle(getCaption("dialog_choose_vlc_folder"));
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(null)) {
                        String path = jfc.getSelectedFile().getAbsolutePath();
                        ((VideoImportEngine)importEngine.getSelectedItem()).initVideoImportEngine(Optional.of(path));
                        OptionsObject.getInstance().setPathToVideoEngineLibrary(path);
                        JOptionPane.showMessageDialog(null, getCaption("adv_video_vlc_success"), getCaption("adv_video_vlc_success_title"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new IllegalArgumentException("File not selected");
                    }
                } catch (Throwable t) {
                    log.warn("Unable to choose VLC path",t);
                    JOptionPane.showMessageDialog(null, getCaption("adv_video_vlc_fail"), getCaption("adv_video_vlc_fail_title"), JOptionPane.WARNING_MESSAGE);

                    // TODO: Fix this hack - I just need to set a safe default so chose the first
                    importEngine.setSelectedItem(OptionsObject.getInstance().getVideoImportEngines()[0]);
                    return;
                }
            }
            OptionsObject.getInstance().setVideoImportEngine((VideoImportEngine)importEngine.getSelectedItem());
            PreferencesService.save();
        };
	}


	JComboBox<ColourChoiceStrategy> colourModes;
	/**
	 * Method that adds the dither options tab and
	 * its action listeners.
	 * 
	 * @return the dithering options tab
	 */
	private JPanel createDitherOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(7,2));
		final JComboBox<DitherStrategy> dithers = new JComboBox<>();
		createDitherComboBox(dithers, dithers::setSelectedItem);
		JLabel label = new JLabel(getCaption("dit_dithering_mode"), JLabel.CENTER);
		panel.add(label);
		panel.add(dithers);
		label = new JLabel(getCaption("dit_colour_mode"), JLabel.CENTER);
		colourModes = new JComboBox<>(oo.getColourModes());
		colourModes.setSelectedItem(oo.getColourMode());
		colourModes.addActionListener(event -> {
            ColourChoiceStrategy ccs = (ColourChoiceStrategy)colourModes.getSelectedItem();
            oo.setColourMode(ccs);
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(colourModes);	
		
		label = new JLabel(getCaption("dit_attribute_fav"), JLabel.CENTER);
		final JComboBox<AttributeStrategy> attributeModes = new JComboBox<>(oo.getAttributeModes());
		attributeModes.setSelectedItem(oo.getAttributeMode());
		attributeModes.addActionListener(event -> {
            oo.setAttributeMode((AttributeStrategy)attributeModes.getSelectedItem());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(attributeModes);
		
		label = new JLabel(getCaption("dit_giga_attribute_fav"), JLabel.CENTER);
		final JComboBox<GigaScreenAttributeStrategy> gigaScreenAttributeModes = new JComboBox<>(oo.getGigaScreenAttributeStrategies());
		gigaScreenAttributeModes.setSelectedItem(oo.getGigaScreenAttributeStrategy());
		gigaScreenAttributeModes.addActionListener(event -> {
            oo.setGigaScreenAttributeStrategy((GigaScreenAttributeStrategy)gigaScreenAttributeModes.getSelectedItem());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(gigaScreenAttributeModes);
			
		label = new JLabel(getCaption("dit_mono_ink_colour"), JLabel.CENTER);
		panel.add(label);
		final JButton ink = new JButton(getCaption("dit_click_ink"));
		ink.setOpaque(true);
		ink.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()]));
		ink.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()]));
		ink.addActionListener(event -> {
            int newIndex = oo.getMonochromeInkIndex()+1;
            if (newIndex >= SPECTRUM_COLOURS_BRIGHT.length) {
                newIndex = 0;
            }
            ink.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
            ink.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
            oo.setMonochromeInkIndex(newIndex);
            PreferencesService.save();
        });
		
		panel.add(ink);
		label = new JLabel(getCaption("dit_mono_paper_colour"), JLabel.CENTER);
		panel.add(label);
		final JButton paper = new JButton(getCaption("dit_click_paper"));
		paper.setOpaque(true);
		paper.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]));
		paper.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]));
		paper.addActionListener(event -> {
            int newIndex = oo.getMonochromePaperIndex()+1;
            if (newIndex >= SPECTRUM_COLOURS_BRIGHT.length) {
                newIndex = 0;
            }
            paper.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
            paper.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
            oo.setMonochromePaperIndex(newIndex);
            PreferencesService.save();
        });
		panel.add(paper);
		label = new JLabel(getCaption("dit_threshold"), JLabel.CENTER);
		final JSlider thresholdSlider = new JSlider(0, 256);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setLabelTable(thresholdSlider.createStandardLabels(32));
		thresholdSlider.setMajorTickSpacing(32);
		thresholdSlider.setValue(oo.getBlackThreshold());
		thresholdSlider.addChangeListener(ce -> {
            oo.setBlackThreshold(thresholdSlider.getValue());
            PreferencesService.save();
        });
		panel.add(label);
		panel.add(thresholdSlider);				
		
		return panel;
	}
	
	/**
	 * Updates a combo box with all the provided dithers and hooks up a dither listener
	 * to process the selected choice
	 *  
	 * @param dithers the dithers to add to the box
	 * @param ditherChangedListener the 
	 */
	void createDitherComboBox(final JComboBox<DitherStrategy> dithers, final DitherChangedListener ditherChangedListener) {
		ditherChangedListeners.add(ditherChangedListener);
		final OptionsObject oo = OptionsObject.getInstance();
		Vector<DitherStrategy> v = new Vector<>();
		v.addAll(Arrays.asList(oo.getErrorDithers()));
		v.addAll(Arrays.asList(oo.getOrderedDithers()));
		v.addAll(Arrays.asList(oo.getOtherDithers()));

		dithers.setModel(new DefaultComboBoxModel<>(v));
		dithers.setSelectedItem(oo.getSelectedDitherStrategy());
		dithers.addActionListener(event -> {
            DitherStrategy obj = (DitherStrategy)dithers.getSelectedItem();
            oo.setSelectedDitherStrategy(obj);
            PreferencesService.save();
            for (DitherChangedListener dcl : ditherChangedListeners) {
                if (dcl != ditherChangedListener) {
                    dcl.ditherSelected(obj);
                }
            }
        });
	}
	
	/**
	 * Method that adds the misc/output options tab and
	 * its action listeners. A file dialog also populates
	 * the custom tape loader.
	 * 
	 * @return the general options jpanel tab
	 */
	private JPanel createGeneralOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(11,2));
		JLabel label = new JLabel(getCaption("misc_image_output"), JLabel.CENTER);
		panel.add(label);
		final JComboBox<String> formatsBox = new JComboBox<>(oo.getImageFormats());
		formatsBox.setSelectedItem(oo.getImageFormat());
		formatsBox.addActionListener(event -> {
            oo.setImageFormat((String)formatsBox.getSelectedItem());
            PreferencesService.save();
        });
		panel.add(formatsBox);
		label = new JLabel(getCaption("misc_tap_loader"), JLabel.CENTER);
		panel.add(label);
		final JComboBox<BasicLoader> loadersBox = new JComboBox<>(oo.getBasicLoaders());
		loadersBox.setSelectedItem(oo.getBasicLoader());
		loadersBox.addActionListener(createLoaderChoiceActionListener(loadersBox));
		panel.add(loadersBox);
		label = new JLabel(getCaption("misc_show_fps"), JLabel.CENTER);
		panel.add(label);
		final JCheckBox fpsCheckBox = new JCheckBox("", oo.getFpsCounter());
		fpsCheckBox.addActionListener(event -> {
            oo.setFpsCounter(fpsCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(fpsCheckBox);
		label = new JLabel(getCaption("misc_show_wip"), JLabel.CENTER);
		panel.add(label);
		final JCheckBox wipPreviewCheckBox = new JCheckBox("", oo.getShowWipPreview());
		wipPreviewCheckBox.addActionListener(event -> {
            oo.setShowWipPreview(wipPreviewCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(wipPreviewCheckBox);
		panel.add(new JLabel(getCaption("misc_output_options"), JLabel.CENTER));
		final JCheckBox scrCheckBox = new JCheckBox(getCaption("misc_scr_output"), oo.getExportScreen());
		scrCheckBox.addActionListener(event -> {
            oo.setExportScreen(scrCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(scrCheckBox);
		panel.add(new JPanel());
		final JCheckBox tapeCheckBox = new JCheckBox(getCaption("misc_tap_output"), oo.getExportTape());
		tapeCheckBox.addActionListener(event -> {
            oo.setExportTape(tapeCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(tapeCheckBox);
		panel.add(new JPanel());
		final JCheckBox textCheckBox = new JCheckBox(getCaption("misc_text_output"), oo.getExportText());
		textCheckBox.addActionListener(event -> {
            oo.setExportText(textCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(textCheckBox);		
		panel.add(new JPanel());
		final JCheckBox imageCheckBox = new JCheckBox(getCaption("misc_image_output"), oo.getExportImage());
		imageCheckBox.addActionListener(event -> {
            oo.setExportImage(imageCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(imageCheckBox);
		panel.add(new JPanel());
		final JCheckBox animGifCheckBox = new JCheckBox(getCaption("misc_gif_output"), oo.getExportAnimGif());
		animGifCheckBox.addActionListener(event -> {
            oo.setExportAnimGif(animGifCheckBox.isSelected());
            PreferencesService.save();
        });
		panel.add(animGifCheckBox);
		panel.add(new JLabel(getCaption("misc_gif_delay"), JLabel.CENTER));
		final JTextField gifDelay = new JTextField();
		gifDelay.setHorizontalAlignment(JTextField.RIGHT);
		gifDelay.setText(""+oo.getGifDisplayTimeMillis());
		gifDelay.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String value = gifDelay.getText();
				if (value != null && value.trim().length() > 0) {
					try {
						int d = Integer.parseInt(value);
						if (d > 0) {
							oo.setGifDisplayTimeMillis(d);
							return;
						}
					} catch (NumberFormatException nfe) {
						log.debug("Unable to set gif display time", nfe);
					}
				}
				gifDelay.setText(""+oo.getGifDisplayTimeMillis());
				PreferencesService.save();
			}
		});
		panel.add(gifDelay);
		return panel;
	}

	/**
	 * Creates an action listener that shows a dialog for a custom file selection loader
	 * 
	 * @param loadersBox the basic loaders combo box
	 * @return the action listener
	 */
	private ActionListener createLoaderChoiceActionListener(JComboBox<BasicLoader> loadersBox) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				BasicLoader loader = (BasicLoader)loadersBox.getSelectedItem();
				OptionsObject.getInstance().setBasicLoader(loader);
				if (loader.getName().startsWith(OptionsObject.CUSTOM_LOADER_PREFIX)) {
					JFileChooser jfc = new JFileChooser(){
						static final long serialVersionUID = 1L;
						public void approveSelection() {
							for (File f:this.getSelectedFiles()) {
								if (f.isDirectory()) {
									return;
								}
							}
							super.approveSelection();
						}
					};
					jfc.setDialogTitle(getCaption("misc_choose_loader"));
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfc.setAcceptAllFileFilterUsed(false);
					jfc.setFileFilter(new FileFilter() {
						
						public String getDescription() {
							return getCaption("misc_tap_desc");
						}
						public boolean accept(File f) {
							String name = f.getAbsolutePath().toLowerCase();
							return (f.isDirectory() || name.endsWith(".tap")); 
						}
					});
					jfc.setMultiSelectionEnabled(false);
					if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(currentInstance)) {
						File file = jfc.getSelectedFile();
						try{
							loader.setName(OptionsObject.CUSTOM_LOADER_PREFIX+"("+file.getName()+")");
							loader.setPath(file.getCanonicalPath());
						} catch(IOException io){
							return;
						}					
					}
				}
				PreferencesService.save();
			}
		};
	}
}
