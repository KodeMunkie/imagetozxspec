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
package uk.co.silentsoftware.core.converters.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.magicwerk.brownies.collections.BigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import uk.co.silentsoftware.config.OptionsObject;

/**
 * Simple ImageIO based GIF converter.
 * 
 * Note this converter holds a state and thus must be
 * initialised via createSequence and reset with 
 * createGif (which is intended to be the final call). 
 * This class is NOT thread safe!
 * 
 * TODO: Fix performance
 */
public class GifConverter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The number of gif loop iterations (0 = forever)
	 */
	private final int LOOP_COUNT = 0;

	/**
	 * The anim gif's buffered frames
	 */
	private List<BufferedImage> frames;
	
	/**
	 * Byte array required to save the created gif
	 */
	private ByteArrayOutputStream baos;
	
	/**
	 * Create the gif and clear down afterwards.
	 * 
	 * @return the gif byte data
	 * @throws IOException if the output fails
	 */
	public byte[] createGif() throws IOException {
		try {
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos); // N.b. not try with closure because we need to close the stream before returning
			ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();	
			iw.setOutput(ios);
			iw.prepareWriteSequence(null);
			int index = 0;
			ImageWriteParam iwp = iw.getDefaultWriteParam();
			String frameDelay = String.valueOf(OptionsObject.getInstance().getGifDisplayTimeMillis() / 10L);
			log.debug("Frames size: {}, Free memory {}", frames.size(), Runtime.getRuntime().freeMemory());
			for (BufferedImage frame : frames) {
				IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frame), iwp);
				configureMetaData(metadata, frameDelay, index++);
				iw.writeToSequence(new IIOImage(frame, null, metadata), null);
			}
			iw.endWriteSequence();
			ios.close();
			return baos.toByteArray();
		} finally {
			baos = null;
			frames = null;
		}
	}
	
	/**
	 * Adds a single buffered image to the gif being created. 
	 * 
	 * @param image the image to add to the gif
	 */
	public void addFrame(BufferedImage image) {
		frames.add(image);	   
	}

	/**
	 * Create a new gif sequence, initialises in memory storage
	 */
	public void createSequence() {
		baos = new ByteArrayOutputStream();
		
		// BigList is concurrent and has low memory usage.
		frames = new BigList<>();
	}
	
	/**
	 * Configures the per frame metadata
	 * 
	 * @param meta the default meta data
	 * @param delayTime the amount of time a frame is to stay on screen in hundreds of a second (millisecond value/10)
	 * @param imageIndex the index of this frame
	 * @throws IIOInvalidTreeException if the meta data cannot be set
	 */
	private void configureMetaData(IIOMetadata meta, String delayTime, int imageIndex) throws IIOInvalidTreeException {
		String metaFormat = meta.getNativeMetadataFormatName();
		Node root = meta.getAsTree(metaFormat);
		Node child = root.getFirstChild();
		while (child != null) {
			if ("GraphicControlExtension".equals(child.getNodeName())) {
				break;
			}
			child = child.getNextSibling();
		}
		IIOMetadataNode gce = (IIOMetadataNode) child;
		gce.setAttribute("userDelay", "FALSE");
		gce.setAttribute("delayTime", delayTime);
		gce.setAttribute("disposalMethod", "none");

		if (imageIndex == 0) {
			IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
			IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
			ae.setAttribute("applicationID", "NETSCAPE");
	        ae.setAttribute("authenticationCode", "2.0");
			byte[] uo = new byte[] { 0x1, (byte) (LOOP_COUNT & 0xFF), (byte) ((LOOP_COUNT >> 8) & 0xFF) };
			ae.setUserObject(uo);
			aes.appendChild(ae);
			root.appendChild(aes);
		}
		meta.setFromTree(metaFormat, root);		
	}
}
