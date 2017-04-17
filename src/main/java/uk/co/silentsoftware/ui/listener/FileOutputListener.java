/* Image to ZX Spec
 * Copyright (C) 2017 Silent Software Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.ui.listener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.silentsoftware.ui.ImageToZxSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

/**
 * Listener to show the Image To ZX Spec file output selection dialog
 */
public class FileOutputListener implements ActionListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Component parentComponent;

	public FileOutputListener(Component parentComponent) {
		this.parentComponent = parentComponent;
	}

	/**
	 * Opens a directory output choice dialog, has workarounds
	 * for Java MacOS bugs of selecting the wrong folder and duplicating
	 * the final bit of the path.
	 */
	public void actionPerformed(ActionEvent ae) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(getCaption("dialog_choose_output"));
		if (ImageToZxSpec.getOutFolder() != null) {
			jfc.setCurrentDirectory(ImageToZxSpec.getOutFolder());
		}
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.addPropertyChangeListener(e -> {
            if (SystemUtils.IS_OS_MAC_OSX) {
                String name = e.getPropertyName();
                if (name.equalsIgnoreCase("directoryChanged")) {
                    log.debug("Working around Java MacOS file chooser bug");
                    jfc.setSelectedFile(null);
                }
            }
        });
		if (JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(parentComponent)) {
			File file = jfc.getSelectedFile();
			if (log.isDebugEnabled()) {
				log.debug("Chosen output folder {}", file);
			}
			if (SystemUtils.IS_OS_MAC_OSX) {
				if (isDuplicatedDirectory(file)) {
					file = file.getParentFile();
					log.debug("MacOS file selector Java bug fixed - output folder {}", file);
				} else {
					log.debug("MacOS file selector Java bug not found - output folder {}", file);
				}
			}
			ImageToZxSpec.setOutFolder(file);
		}
	}

	/**
	 * Java MacOS workaround to detect the duplicated entry at the end of the path
	 * 
	 * @param file the file to check
	 * @return whether the directory at the end of the path is duplicated
	 */
	private boolean isDuplicatedDirectory(File file) {
		String path = file.getAbsolutePath();
		log.debug("Path: {}", path);
		String lastDir = File.separator+StringUtils.substringAfterLast(path, File.separator);
		log.debug("Last dir: {}", lastDir);
		String pathWithoutLastDir = StringUtils.substringBeforeLast(path, lastDir);
		log.debug("Path without Last dir: {}", pathWithoutLastDir);
		boolean eval = lastDir.length()>1 && pathWithoutLastDir.endsWith(lastDir);
		log.debug("Eval {}", eval);
		return eval;
	}
}
