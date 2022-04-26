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
package uk.co.silentsoftware.core.helpers;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Byte and BitSet manipulation and conversion class
 * 
 * Disclaimer: I understand bytes and bits, but my
 * manipulation of them is shockingly bad so I wrote
 * this class to help me with the high level Java 
 * objects.
 * 
 * TODO: Find and use suitable third party library
 */
public final class ByteHelper {

	/**
	 * Private constructor since we want static use only
	 */
	private ByteHelper(){}

	/**
	 * Noddy method to reverse a BitSet. 
	 * Basically this is just to fix the endianness of
	 * the image bits since bitset doesn't provide a
	 * means of changing the bit (not byte) endianness
	 * 
	 * @param bs the bitset to change the endianness of (by reference)
	 * @return the reversed bitset
	 */
	public static BitSet reverseBitSet(BitSet bs) {
		final BitSet copy = new BitSet(8);
		int counter = 0;
		int size = bs.length();
		// For all bits
		for (int i = 0; i < size; ++i) {
			
			// If at the end of a byte
			if (i % 8 == 0 && i != 0) {
				
				// Read all the bits in reverse order
				for (int j=8; j>=1; j--) {
					
					// Zero the original range 
					bs.clear(i-j);
					
					// Copy the current byte (in reverse order) back to the original
					if (copy.get(j-1)){
						bs.set(i-j);
					}
				}
				// Zero the copy completely to start on the next byte
				copy.clear();
				counter = 0;
			}
			
			// Copy the bits in the same order into a copy
			if (bs.get(i)) {
				copy.set(counter);
			}
			++counter;		
		}
		return bs;
	}
	
	/**
	 * Copy bytes completely from a source byte array to 
	 * a destination byte array starting at the fromIndex

	 * @param from the array to copy from 
	 * @param to the array to copy to
	 * @param fromIndex the destination array index to start copy to
	 * @return the copied byte array
	 */
	public static byte[] copyBytes(byte[] from, byte[] to, int fromIndex) {
		System.arraycopy(from, 0, to, fromIndex, from.length);
		return to;
	}
	
	/**
	 * The missing method on a ByteBuffer - allows copying
	 * of multiple bytes into the buffer at a buffer offset
	 * 
	 * @param bb the byte buffer to copy the array into
	 * @param bytes the bytes to copy
	 * @param from the offset into the bytebuffer from which to copy
	 */
	public static void put(ByteBuffer bb, byte[] bytes, int from) {
		int counter = 0;
		for (byte b : bytes) {
			bb.put(from+counter, b);
			++counter;
		}
	}
	
	/**
	 * Implementation of an XOR checksum
	 *  
	 * @param bytes the bytes to xor
	 * @return the xor result
	 */
	public static byte getChecksum(byte[] bytes) {
		int checksum = 0;
		for (byte  b: bytes) {
			checksum^=b;
		}
		return (byte)checksum;
	}	
}