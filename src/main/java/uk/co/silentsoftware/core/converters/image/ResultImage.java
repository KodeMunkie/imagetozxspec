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
package uk.co.silentsoftware.core.converters.image;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Processed output buffered image result with the type of image, such as
 * supporting (e.g. gigascreen image 1 of 2) or final image (e.g. combined
 * gigascreen image).
 */
public class ResultImage {

	private volatile ResultImageType resultImageType;
	private volatile BufferedImage image;

	public ResultImage(ResultImageType resultImageType, BufferedImage image) {
		this.resultImageType = resultImageType;
		this.image = image;
	}

	public ResultImageType getResultImageType() {
		return resultImageType;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public enum ResultImageType {
		FINAL_IMAGE, SUPPORTING_IMAGE
	}

	/**
	 * Utility method to retrieve the final image from an array of result images
	 * 
	 * @param images the array of ResultImages
	 * @return a single ResultImage, if it exists
	 */
	public static Optional<ResultImage> getFinalImage(ResultImage[] images) {
		try {
			for (ResultImage ri : images) {
				if (ResultImageType.FINAL_IMAGE == ri.getResultImageType() && ri.image != null) {
					return Optional.of(ri);
				}
			}
		} catch (NullPointerException npe) {
			// Occurs if settings changed mid run
		}
		return Optional.empty();
	}
}
