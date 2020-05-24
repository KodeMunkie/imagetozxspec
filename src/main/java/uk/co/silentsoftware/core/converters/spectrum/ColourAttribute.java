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
package uk.co.silentsoftware.core.converters.spectrum;


/**
 * Small bean to associate RGB values
 * used for Spectrum ink and paper
 */
public class ColourAttribute {
	
	/**
	 * The colour for ink
	 */
	private Integer inkRGB = null;
	
	/**
	 * The colour for paper
	 */
	private Integer paperRGB = null;
	
	/**
	 * Whether this colour belongs to the Spectrum bright colour set
	 */
	private boolean isBrightSet = true;
	
	void setBrightSet(boolean isBrightSet) {
		this.isBrightSet = isBrightSet;
	}
	
	public boolean isBrightSet() {
		return isBrightSet;
	}
	
	Integer getInkRGB() {
		return inkRGB;
	}
	void setInkRGB(Integer inkRGB) {
		this.inkRGB = inkRGB;
	}
	Integer getPaperRGB() {
		return paperRGB;
	}
	void setPaperRGB(Integer paperRGB) {
		this.paperRGB = paperRGB;
	}

	@Override
	public String toString() {
		return "Ink:"+Integer.toHexString(inkRGB)+" Paper:"+Integer.toHexString(paperRGB)+" Bright:"+isBrightSet;
	}
	
	
}
