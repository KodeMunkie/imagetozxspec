/* Image to ZX Spec
 * Copyright (C) 2018 Silent Software Silent Software (Benjamin Brown)
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
package uk.co.silentsoftware.core.converters.spectrum;

import static uk.co.silentsoftware.core.helpers.ByteHelper.copyBytes;
import static uk.co.silentsoftware.core.helpers.ByteHelper.getChecksum;
import static uk.co.silentsoftware.core.helpers.ByteHelper.put;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.helpers.ByteHelper;

/**
 * Converter to output a Tape image format file (.tap)
 */
public class TapeConverter {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Outputs an SCR format image into a tap file part
	 * (i.e. a standard data block). 
	 * 
	 * @see <a href="http://www.zx-modules.de/fileformats/tapformat.html">http://www.zx-modules.de/fileformats/tapformat.html</a>
	 * 
	 * @param image the SCR image data to add to the tap file
	 * @return the tap file section containing the Spectrum encoded image
	 */
	public byte[] createTapPart(byte[] image) {
		
		// Standard ROM data block header
		ByteBuffer imageData = ByteBuffer.allocate(2+image.length);
		imageData.order(ByteOrder.LITTLE_ENDIAN);
		imageData.put(0, (byte)255); // 255 indicates ROM loading block - cast just for show
		put(imageData, image, 1); // SCR image data
		imageData.put(6913, getChecksum(imageData.array())); // XOR checksum
		
		// Screen header
		ByteBuffer imageHeader = ByteBuffer.allocate(19);
		imageHeader.order(ByteOrder.LITTLE_ENDIAN);
		imageHeader.put(0, (byte)0); // Indicates ROM header
		imageHeader.put(1, (byte)3); // Indicates BYTE header
		put(imageHeader, "Loading...".getBytes(), 2); // Program loading name
		imageHeader.putShort(12, (short)6912); // Length of data (should be 6912)
		imageHeader.putShort(14, (short)16384); // Start address to import to
		imageHeader.putShort(16, (short)32768); // Unused, must be 32768
		imageHeader.put(18, getChecksum(imageHeader.array())); // XOR checksum
		
		// Copy image header block into data block sub block
		ByteBuffer imageHeaderBlock = ByteBuffer.allocate(2+imageHeader.array().length);
		imageHeaderBlock.order(ByteOrder.LITTLE_ENDIAN);
		imageHeaderBlock.putShort((short)(imageHeader.array().length));
		put(imageHeaderBlock, imageHeader.array(), 2);
		
		// Copy image data into data block sub block
		ByteBuffer imageDataBlock = ByteBuffer.allocate(2+imageData.array().length);
		imageDataBlock.order(ByteOrder.LITTLE_ENDIAN);
		imageDataBlock.putShort((short)(imageData.array().length));
		put(imageDataBlock, imageData.array(), 2);
		
		// Combined the header sub block and image data sub block and return the bytes
		byte[] b = new byte[imageHeaderBlock.array().length+imageDataBlock.array().length];
		b = copyBytes(imageHeaderBlock.array(), b, 0);
		b = copyBytes(imageDataBlock.array(), b, imageHeaderBlock.array().length);
		return b;
	}
	
	/**
	 * Returns a the new tap file bytes containing
	 * a basic SCR loader followed by the SCR images
	 * that have been already converted to TAP parts
	 * 
	 * @param parts the TAP parts (data blocks) contain SCR images
	 * @return the entire tap file as bytes
	 */
	public byte[] createTap(List<byte[]> parts) {
		byte[] loader = createLoader();
		int size = loader.length;
		for (byte[] b: parts) {
			size+=b.length;
		}
		byte[] result = new byte[size];
		ByteHelper.copyBytes(loader, result, 0);
		int index = loader.length;
		for (byte[] b: parts) {
			ByteHelper.copyBytes(b, result, index);
			index += b.length;
		}
		return result;
	}
	
	/**
	 * Creates and returns the TAP loader from the options
	 * selected loader file. N.b this byte data will already
	 * be in little endian order. 
	 * 
	 * @return the byte data contained in the loader file
	 */
	private byte[] createLoader() {
		OptionsObject oo = OptionsObject.getInstance();
		ByteBuffer b = null;
		BufferedInputStream bis = null;
		try {
			InputStream fis = (TapeConverter.class.getResourceAsStream(oo.getBasicLoader().getPath()));
			if (fis == null) {
				fis = new FileInputStream(oo.getBasicLoader().getPath());
			}
			bis = new BufferedInputStream(fis);
			b = ByteBuffer.allocate(4096);
			int data;
			while((data = bis.read()) != -1) {
				b.put((byte)data);
			}	
		} catch(Exception e) {
			log.error("Unable to create loader", e);
		} finally {
			if (bis != null) {
				try {bis.close();}catch(IOException io){log.error("Unable to close load stream");}
			}
		}
		// Create a new array the length of the buffer
		byte[] sized = new byte[b.position()];
		
		// Rewind the buffer and copy the contents to the sized array
		b.rewind();
		b.get(sized);
		return sized;
	}
}
