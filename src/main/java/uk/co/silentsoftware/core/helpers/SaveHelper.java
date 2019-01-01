/* Image to ZX Spec
 * Copyright (C) 2019 Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveHelper {
	
	private static final Logger log = LoggerFactory.getLogger(SaveHelper.class);
	
	/**
	 * The default file format and pre-suffix ".zx."
	 */
	public static final String FILE_SUFFIX = ".zx.";
	
	/**
	 * Write an image directly to the specified file deleting
	 * any file with the same name
	 * 
	 * Follows the format [destFolder]/[fileName].zx.[formatName]
	 * 
	 * @param output the image to output
	 * @param destFolder the folder to write the image to
	 * @param fileName the file name of the image
	 * @param formatName the file format suffix
	 * 
	 * @throws IOException thrown if output fails
	 */
	public static void saveImage(final BufferedImage output, final File destFolder, final String fileName, final String formatName) throws IOException {
		log.debug("Dest folder {}, filename {}, formatName {}", destFolder, fileName, formatName);
		String baseFileName = getBaseName(fileName);
		File destFile = new File(destFolder.getAbsolutePath()+"/"+baseFileName+FILE_SUFFIX+formatName);
		deleteFileIfExists(destFile);
		log.debug("Writing image to {} with format {}", destFile, formatName);
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile))) {
			ImageIO.write(output, formatName, out);
		} catch(IOException io) {
			log.error("Unable to save byte data for file {}", destFile, io);	
			throw io;
		}
	}

	private static String getBaseName(String fileName) {
		if (fileName.contains(".")) {
			return fileName.substring(0, fileName.lastIndexOf("."));
		}
		return fileName;
	}
	
	/**
	 * Saves raw byte data to the given file deleting any
	 * file with the same name
	 * 
	 * @param bytes the byte data to write out
	 * @param file the file to write it to
	 * 
	 * @throws IOException if output fails
	 */
	public static void saveBytes(byte[] bytes, File file) throws IOException {
		deleteFileIfExists(file);
		log.debug("Writing bytes to {}", file);
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(bytes);
		} catch(IOException io) {
			log.error("Unable to save byte data for file {}", file, io);
			throw io;
		}
	}
	
	/**
	 * Deletes the file if it exists
	 * 
	 * @param file the file to delete
	 */
	private static void deleteFileIfExists(File file) {
		if (file.exists()) {
			boolean success = file.delete();
			log.debug("File deletion {} for {}", success, file.getPath());
		}
	}
}
