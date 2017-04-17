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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.ui.ImageToZxSpec;
import uk.co.silentsoftware.ui.ImageToZxSpec.UiCallback;

/**
 * Custom dnd listener to allow file drop to open preview window.
 * Uses a hack to check for broken drag and drop java support.
 */
public class CustomDropTargetListener implements DropTargetListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";
	private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085\u000C]";
	private DataFlavor uriListFlavor;
	private final UiCallback uiCallback;

	public CustomDropTargetListener(UiCallback uiCallback) {
		this.uiCallback = uiCallback;
		try {
			uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
		} catch (ClassNotFoundException e) {
			log.error("Data flavor not supported",e);
			uriListFlavor = null;
		}
	}
	
	/**
	 * Converts a URI string to a file List
	 * 
	 * @param data the dnd string
	 * @return the file list
	 * @throws URISyntaxException if a dnd uri is invalid
	 */
	private static List<File> textURIListToFileList(String data) throws URISyntaxException {
		List<File> list = new ArrayList<>(1);
		String[] lines = data.split(LINE_SEPARATOR_PATTERN);
		for (String line : lines) {
			
			// Comment line - useless for us.
			if (line.startsWith("#")) {
				continue;
			}
			list.add(new File(new URI(line)));
		}
		return list;
	}

	/*
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavours = tr.getTransferDataFlavors();
			
			// Iterate the flavours we have for this event
			for (DataFlavor flavour : flavours) {
			
				List<File> files = new ArrayList<>(0);
				
				// Is the OS playing nicely? This is the correct data flavour
				// for what we want
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					if (flavour.isFlavorJavaFileListType()) {
						dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
						files = (List<File>) tr.getTransferData(flavour);
					}
				}
				// Check if this is the URI way of providing the dnd data
				else if (uriListFlavor != null && dtde.isDataFlavorSupported(uriListFlavor)) {
					if (flavour.isMimeTypeEqual(uriListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
				        String data = (String) tr.getTransferData(uriListFlavor);
				        files = textURIListToFileList(data);
					}
				} else {
					log.error("No supported data flavors!");
				}
				
				// We didn't get the desired flavour, keep trying until we do
				// (or run out).
				if (files.size() == 0) {
					continue;
				}
				
				// Filter unsupported files from our list of files.
				for (File f : files) {
					if (!ImageToZxSpec.isSupported(f)) {
						files.remove(f);
					}
				}
				
				// Only convert the first file.
				File[] inFiles = files.toArray(new File[0]);
				ImageToZxSpec.setInFiles(inFiles);
				if (inFiles[0].isDirectory()) {
					ImageToZxSpec.setOutFolder(inFiles[0]);
				} else {
					ImageToZxSpec.setOutFolder(inFiles[0].getParentFile());
				}

				// For drag and drop we want feedback so we're showing the
				// preview dialog which will show the first file.
				uiCallback.processPopupPreview();
				
				// We got what we came for - don't bother to keep checking
				break;
			}
		} catch (Exception io) {
			log.error("Drag and drop error {}", io);
		} finally {
			dtde.dropComplete(true);
		}
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void dragExit(DropTargetEvent dte) {
	}
	/*
	 * {@inheritDoc}
	 */
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}
}
