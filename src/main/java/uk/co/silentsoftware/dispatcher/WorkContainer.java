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
package uk.co.silentsoftware.dispatcher;

import java.awt.image.BufferedImage;

import uk.co.silentsoftware.core.converters.image.ResultImage;

/**
 * Wrapper container for holding processed and completed work ready for output
 * and display
 */
class WorkContainer {
	
	private String imageId = "(not set)";

	/**
	 * The final result images
	 */
	private ResultImage[] resultImage;

	/**
	 * The preprocessed image
	 */
	private BufferedImage preProcessedImage;

	/**
	 * The equivalent SCR representation of this image
	 */
	private byte[] scrData;
	
	WorkContainer(ResultImage[] resultImage) {
		this.resultImage = resultImage;
	}
	
	WorkContainer(ResultImage[] resultImage, BufferedImage preProcessedImage, String imageId) {
		this.resultImage = resultImage;
		this.preProcessedImage = preProcessedImage;
		this.imageId = imageId;
	}
	
	String getImageId() {
		return imageId;
	}

	ResultImage[] getResultImage() {
		return resultImage;
	}

	BufferedImage getPreprocessedImageResult() {
		return preProcessedImage;
	}

	byte[] getScrData() {
		return scrData;
	}

	void setScrData(byte[] scrData) {
		this.scrData = scrData;
	}
}
