/* Image to ZX Spec
 * Copyright (C) 2022 Silent Software (Benjamin Brown)
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
import uk.co.silentsoftware.config.BuildProperties;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.helpers.SaveHelper;
import uk.co.silentsoftware.dispatcher.WorkManager;
import uk.co.silentsoftware.ui.listener.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * The main UI class. This class initialises the entire system, related dialogs
 * and controls the state of the workmanager.
 */
public class ImageToZxSpec {
	private static final Logger log = LoggerFactory.getLogger(ImageToZxSpec.class);
	private static final String BUILD_VERSION = BuildProperties.getProperty("version");
	private static final String NAME_COPYRIGHT = "Image to ZX Spec "+BUILD_VERSION+" © Silent Software 2022";
	public static final ImageIcon IMAGE_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/logo.png"));
	private static final ImageIcon OPEN_FILE_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/New Document.png"));
	private static final ImageIcon EXPORT_DIR_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Export.png"));
	private static final ImageIcon CONVERT_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Play.png"));
	private static final ImageIcon CONVERT_CANCEL_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Stop.png"));
	private static final ImageIcon SETTINGS_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Gear.png"));
	private static final ImageIcon PREVIEW_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Coherence.png"));	
	private static final String SINCLAIR_IMAGE_PATH = "/icons/sinclair.png";

	private static final int DEFAULT_FONT_SIZE = 14;

	/**
	 * The message to show when idle
	 */
	private static final String DEFAULT_STATUS_MESSAGE = getCaption("main_waiting");
	
	/**
	 * Input folders to process
	 */
	private static File[] inFiles = null;
	
	/**
	 * Destination folder to output files to
	 */
	private static File outFolder = null;
		
	/**
	 * The spectrum logo when no images are loaded
	 */
	private static volatile BufferedImage specLogo = null;

	/**
	 * The main UI frame
	 */
	private static final JFrame frame = new JFrame(NAME_COPYRIGHT);

	/**
	 * The main preview panel
	 */
	private static Panel renderPanel;

	/**
	 * The status box
	 */
	private static JTextField statusBox = null;
	
	/**
	 * The input folder menu item
	 */
	private static JMenuItem folder = null; 
	
	/**
	 * The output folder menu item
	 */
	private static JMenuItem outputFolder = null;
	
	/**
	 * The menu bar
	 */
	private static JMenuBar menubar = null;
	
	private static final WorkManager workManager = new WorkManager();
	
	/**
	 * The options dialog
	 */
	private static PreferencesDialog preferences;
	
	/**
	 * The preview window
	 */
	private PopupPreviewFrame preview;
	
	/**
	 * Main dialog preview panel showing progress
	 */
	private volatile static BufferedImage mainPreviewImage = null; 

	/**
	 * The main dialog toolbar
	 */
	private static JToolBar toolBar;

	/**
	 * The main dialog toolbar start button
	 */
	private static JButton convertButton;
	
	/**
	 * Main method that initialises the UI
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		ImageToZxSpec imageToZxSpec = new ImageToZxSpec();
		imageToZxSpec.createUserInterface();
		imageToZxSpec.coffeeDialog();
	}
	
	/**
	 * Creates the preprocessed/processed main window
	 */
	private void createUserInterface() {
		
		// Initialises this app's look and feel settings
		initLookAndFeel();

		preferences  = new PreferencesDialog();
		preview = new PopupPreviewFrame(new UiCallback());

		// Drag and drop support and the icon image
		frame.setDropTarget(new DropTarget(frame, new CustomDropTargetListener(new UiCallback())));
		
		// Set the top left icon for OS that support it
		frame.setIconImage(IMAGE_ICON.getImage());
		
		// Add a clean up window listener
		frame.addWindowListener(createWindowListener());
		
		// Set the layout of the content pane
		frame.getContentPane().setLayout(new BorderLayout());
		
		// Add the standard toolbar
		frame.getContentPane().add(createToolbar(), BorderLayout.PAGE_START);
		
		// Add the panel for rendering the original +
		renderPanel = createRenderPanel();
		frame.getContentPane().add(renderPanel, BorderLayout.CENTER);
	    
	    // Add the status message box at the bottom of the pane
		frame.getContentPane().add(createStatusBox(), BorderLayout.PAGE_END);
		
		// Add the menu bar options
		menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createOptionsMenu());
		menubar.add(createAboutMenu());
		
		// Add the default settings for window
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setJMenuBar(menubar);
	    frame.setVisible(true);
	    frame.setResizable(false);
	    
	    // Prep the frame for a logo image
	    prepareFrameForLogoImage();
	    
	    // Pack the frame to the correct dimensions
		frame.pack();
	}

	/**
	 * Initialises the platform look and feel
	 */
	private void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setDefaultFontSize(DEFAULT_FONT_SIZE);
		} catch(Exception e) {
			log.debug("Unable to set the platform lnf",e);
			// Pah just ignore this error, we'll just have a naff looking UI
		}
	}


	/**
	 * Sets the a sensible font size suitable for hidef displays
	 * @param size
	 */
	public void setDefaultFontSize(int size) {
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);
		for (Object key : keys) {
			if (key != null && key.toString().toLowerCase().contains("font")) {
				Font font = UIManager.getDefaults().getFont(key);
				if (font != null) {
					font = font.deriveFont((float)size);
					UIManager.put(key, font);
				}
			}
		}
	}

	/**
	 * Creates a window listener that shuts down the app gracefully
	 * 
	 * @return the window listener
	 */
	private WindowListener createWindowListener() {
		// Override the window close listener to clear up resources 
		return new WindowAdapter() {
				public void windowClosing(WindowEvent w) {
					shutdown();
				}
		};
	}
	
	/**
	 * Gracefully shuts does the app and its processes
	 */
	private void shutdown() {
		workManager.shutdown();
		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}
	
	/**
	 * Prepares the frame size for the logo image (and loads it)
	 * and centres the frame on screen
	 */
	private void prepareFrameForLogoImage() {
		 // Shows a nice logo while nothing loaded
	    try {
			specLogo = ImageIO.read(ImageToZxSpec.class.getResource(SINCLAIR_IMAGE_PATH).openStream());
			Dimension dim = new Dimension(specLogo.getWidth(),
					specLogo.getHeight()+toolBar.getHeight()+statusBox.getHeight()+frame.getInsets().top+frame.getInsets().bottom+menubar.getHeight());
			frame.setSize(dim);
			frame.setMinimumSize(dim);
		    frame.setPreferredSize(dim);
			frame.repaint();
			frame.setLocationRelativeTo(null);
		} catch (IOException io) {
			log.error("Unable to load logo", io);
		}
	}
	/**
	 * Creates the file menu
	 * 
	 * @return the file menu
	 */
	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu(getCaption("tab_file"));
		
		// Input folder
		folder = new JMenuItem(getCaption("tab_item_choose_input"));
		folder.addActionListener(new FileInputListener(new UiCallback(), frame));
		
		// Output folder
		outputFolder = new JMenuItem(getCaption("tab_item_choose_output"));
		outputFolder.addActionListener(new FileOutputListener(frame));		
		
		// Exit button
		JMenuItem exit = new JMenuItem(getCaption("tab_item_exit"));
		exit.addActionListener(ae -> shutdown());
		
		fileMenu.add(folder);
		fileMenu.add(outputFolder);
		fileMenu.add(new Separator());
		fileMenu.add(exit);
		return fileMenu;
	}
	
	/**
	 * Creates the options menu
	 * 
	 * @return the options menu
	 */
	private JMenu createOptionsMenu() {
		final JMenu optionsMenu = new JMenu(getCaption("tab_option"));
		JMenuItem preferencesItem = new JMenuItem(getCaption("tab_item_control_panel"));
		preferencesItem.addActionListener(ae -> {
            if (!preferences.isShowing()) {
                preferences.setVisible(true);
            }
            preferences.toFront();
        });
		final JMenuItem previewItem = new JMenuItem(getCaption("tab_item_view_preview"));
		previewItem.addActionListener(ae -> {
            if (!preview.isShowing()) {
                preview.setVisible(true);
                workManager.processPreview(new UiCallback(), preview, inFiles);
            }
            preview.toFront();
        });
		optionsMenu.add(preferencesItem);
		optionsMenu.addSeparator();
		optionsMenu.add(previewItem);
		return optionsMenu;
	}

	/**
	 * Creates the about menu
	 * 
	 * @return the about menu
	 */
	private JMenu createAboutMenu() {
		JMenu about = new JMenu(getCaption("tab_about"));
		JMenuItem aboutZx = new JMenuItem(getCaption("tab_item_about"));
		about.add(aboutZx);
		aboutZx.addActionListener(new AboutListener());
		return about;
	}

	/**
	 * Creates the status box at the bottom on the frame
	 * 
	 * @return the status box
	 */
	private Component createStatusBox() {
		statusBox = new JTextField(DEFAULT_STATUS_MESSAGE);
	    statusBox.setEditable(false);
	    return statusBox;
	}

	/**
	 * Creates the main dialog's main panel for rendering the preview
	 * Nb. Needs to be a Swing Jpanel inside an AWT panel since image
	 * rendering differs from the popup preview frame otherwise!
	 *
	 * @return the render panel
	 */
	private Panel createRenderPanel() {

		Panel outer = new Panel();
		JPanel inner = new JPanel() {
			static final long serialVersionUID = 0;

			@Override
			public void paint(Graphics g) {
				if (mainPreviewImage != null) {
					// Draw rendered preview image
					g.drawImage(mainPreviewImage, 0, 0, null);
				} else if (specLogo != null) {
					g.drawImage(specLogo, 0, 0, null);
				}
			}
		};
		outer.add(inner);
		outer.setLayout(new GridLayout(1,1));
		return outer;
	}

	/**
	 * Create the toolbar on the main dialog
	 * 
	 * @return the toolbar
	 */
	private JToolBar createToolbar() {
		toolBar = new JToolBar();
		toolBar.add(createConvertButton());
		toolBar.add(createOpenButton());
		toolBar.add(createExportDirButton());
		toolBar.add(createSettingsButton());
		toolBar.add(createPreviewButton());
		toolBar.add(createDithersPanel());
		toolBar.setFloatable(false);
		return toolBar;
	}
	
	/**
	 * Creates a panel with a combobox containing the list of supported dithers
	 * 
	 * @return the panel
	 */
	private Component createDithersPanel() {
		JPanel panel = new JPanel();
		final JComboBox<DitherStrategy> dithers = new JComboBox<>();
		preferences.createDitherComboBox(dithers, dithers::setSelectedItem);
		panel.setLayout(new GridLayout(1,1));	
		panel.add(dithers);
		return panel;
	}

	/**
	 * Creates a preview button
	 * 
	 * @return the preview button
	 */
	private Component createPreviewButton() {
		JButton previewButton = new JButton(PREVIEW_ICON);
		previewButton.addActionListener(ae -> {
            if (!preview.isShowing()) {
                preview.setVisible(true);
                workManager.processPreview(new UiCallback(), preview, inFiles);
            }
            preview.toFront();
        });
		return previewButton;
	}

	/**
	 * Creates the settings button
	 * 
	 * @return the settings button
	 */
	private Component createSettingsButton() {
		JButton settingsButton = new JButton(SETTINGS_ICON);
		settingsButton.addActionListener(ae -> {
            if (!preferences.isShowing()) {
                preferences.setVisible(true);
            }
            preferences.toFront();
        });
		return settingsButton;
	}

	/**
	 * Creates the export dir button
	 * 
	 * @return export dir button
	 */
	private Component createExportDirButton() {
		JButton exportDirButton = new JButton(EXPORT_DIR_ICON);
		exportDirButton.addActionListener(new FileOutputListener(frame));
		return exportDirButton;
	}

	/**
	 * Creates the open file button
	 * 
	 * @return the open file button
	 */
	private Component createOpenButton() {
		JButton openButton = new JButton(OPEN_FILE_ICON);
		openButton.addActionListener(new FileInputListener(new UiCallback(), frame));
		return openButton;
	}

	/**
	 * Creates the convert button and a listener for initialising and cancelling the workmanager
	 * 
	 * @return the convert button
	 */
	private Component createConvertButton() {
		convertButton = new JButton(CONVERT_ICON);
		convertButton.addActionListener(new FileReadyListener(success -> {
            if (!success) {
                return;
            }
            boolean isConverting = convertButton.getIcon().equals(CONVERT_ICON);
            if (isConverting){
                try {
                    workManager.startFpsCalculator();
                    workManager.processFiles(new UiCallback(), inFiles, outFolder);
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(null, getCaption("dialog_error")+e.getMessage(), getCaption("dialog_error_title"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                convertButton.setEnabled(false);
                workManager.cancel();
            }
        }));
		return convertButton;
	}

	/**
	 * Creates and pops up the coffee dialog if necessary
	 */
	private void coffeeDialog() {
		// Local instance only to save needless waste!
		new CoffeeDialog().showPopupIfNecessary();	
	}
	
	/**
	 * Determines whether the app supports a file's media format
	 * 
	 * @param f the file to test
	 * @return true if supported
	 */
	public static boolean isSupported(File f) {
		// TODO: Consider moving to workmanager, doesn't seem right here
	 	String name = f.getAbsolutePath().toLowerCase();
		return (f.isDirectory()
				|| name.endsWith(".gif") 
				|| name.endsWith(".png")
				|| name.endsWith(".jpg")
				|| name.endsWith(".jpeg") 
				|| name.endsWith(".avi") 
				|| name.endsWith(".mov")
				|| name.endsWith(".mp4")
				|| name.endsWith(".mpg") 
				&& !name.contains(SaveHelper.FILE_SUFFIX));
	}
	
	/**
	 * Class to allow call backs to the main UI for
	 * rendering, control enablement, and message showing
	 */
	public class UiCallback {
	
		/**
		 * Repaints the frame
		 */
		public void repaint() {
			frame.repaint();
		}
		
		/**
		 * Reset's the UI state to enable input
		 */
		public void enableInput() {
			convertButton.setEnabled(true);
			convertButton.setIcon(CONVERT_ICON);
			folder.setEnabled(true);
			outputFolder.setEnabled(true);
		}
		
		/**
		 * Disable the UI state to prevent input
		 */
		public void disableInput() {
			convertButton.setIcon(CONVERT_CANCEL_ICON);
			folder.setEnabled(false);
			outputFolder.setEnabled(false);
		}
		
		/**
		 * Sets a status or error message above the convert button
		 */
		public void resetStatusMessage() {
			statusBox.setText(ImageToZxSpec.DEFAULT_STATUS_MESSAGE);
			statusBox.repaint(1000);
		}
		
		/**
		 * Sets a status or error message above the convert button
		 * @param message the message to display
		 */
		public void setStatusMessage(final String message) {
			statusBox.setText(message);
			statusBox.repaint(1000);
		}

		/**
		 * Resizes the main frame for the supplied image
		 * 
		 * @param image the image to resize the frame for
		 */
		private void resizeFrame(BufferedImage image) {
			Dimension dim = new Dimension(image.getWidth() + frame.getInsets().left + frame.getInsets().right,
					image.getHeight() + statusBox.getHeight() + toolBar.getHeight() + frame.getInsets().top + frame.getInsets().bottom
							+ menubar.getHeight());
			if (!frame.getSize().equals(dim)) {
				frame.setSize(dim);
				frame.setLocationRelativeTo(null);
			}
		}

		/**
		 * Updates the main image ready for the next paint refresh
		 * 
		 * @param previewImage the image to preview
		 */
		public void updateMainImage(BufferedImage previewImage) {
			mainPreviewImage = previewImage;
			renderPanel.repaint();
			resizeFrame(previewImage);
			frame.repaint();
		}

		/**
		 * Processes the popup preview dialog work
		 */
		public void processPopupPreview() {
			preview.setVisible(true);
			workManager.processPreview(new UiCallback(), preview, inFiles);			
		}
		
		/**
		 * Displays a warning message dialog
		 * 
		 * @param title the dialog title
		 * @param message the dialog message
		 */
		public void displayWarning(String title, String message) {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
		}
	}	
	
	/**
	 * Returns the input files
	 * 
	 * @return the input files
	 */
	public static File[] getInFiles() {
		return ImageToZxSpec.inFiles;
	}
	
	/**
	 * Sets the input files
	 * 
	 * @param inFiles the input files
	 */
	public static void setInFiles(File[] inFiles) {
		ImageToZxSpec.inFiles = inFiles;
	}
	
	/**
	 * Sets the output folder
	 * 
	 * @param outFolder the output folder
	 */
	public static void setOutFolder(File outFolder) {
		ImageToZxSpec.outFolder = outFolder;
	}
	
	/**
	 * Gets the output folder
	 * 
	 * @return the output folder
	 */
	public static File getOutFolder() {
		return outFolder;
	}
}
