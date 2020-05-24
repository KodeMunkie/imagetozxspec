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
package uk.co.silentsoftware.ui.listener;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Listener that wraps the input and output file listeners to provide
 * an indication as to whether it is okay to start processing (i.e.
 * input and output folders have been correctly set).
 */
public class FileReadyListener implements ActionListener {

	private final OperationFinishedListener operationFinishedListener;

	public FileReadyListener(OperationFinishedListener operationFinishedListener) {
		this.operationFinishedListener = operationFinishedListener;
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (ImageToZxSpec.getInFiles() == null || ImageToZxSpec.getInFiles().length == 0) {
			JOptionPane.showMessageDialog(null, getCaption("dialog_choose_input_first"), getCaption("dialog_files_not_selected"), JOptionPane.INFORMATION_MESSAGE);
		}
		if (ImageToZxSpec.getOutFolder() == null) {
			JOptionPane.showMessageDialog(null, getCaption("dialog_choose_folder_first"), getCaption("dialog_folder_not_selected"), JOptionPane.INFORMATION_MESSAGE);
		}
		if (operationFinishedListener != null) {
			operationFinishedListener.operationFinished(ImageToZxSpec.getInFiles() != null 
					&& ImageToZxSpec.getInFiles().length >0 && ImageToZxSpec.getOutFolder() != null);
		}
	}
}
