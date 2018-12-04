package edu.utdallas.cs6360.davisbase.utils;

import edu.utdallas.cs6360.davisbase.trees.DataCell;
import edu.utdallas.cs6360.davisbase.trees.DataType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * A class of static helper functions for various byte operations
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public final class ByteHelpers {
	
	/**
	 * Private constructor so the class cannot be instantiated
	 */
	private ByteHelpers() { throw new IllegalStateException("ByteHelpers Utility Class");}
	
	/**
	 * A static method that accpets a 2's completment
	 * signed byte and returns the unsigned value
	 * @param b a 2's complement byte
	 * @return the unsigned value
	 */
	public static int byteToUnSignedInt(byte b) {
		return b & 0xFF;
	}
	
	/**
	 * Converts a short to an array of bytes
	 * @param value a short value
	 * @return an array of bytes
	 */
	public static byte[] shortToBytes(short value) {
		return ByteBuffer.allocate(Short.BYTES).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
	}
	
	/**
	 * Converts an integer to an array of bytes
	 * @param value an integer value
	 * @return an array of bytes
	 */
	public static byte[] intToBytes(int value) {
		return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
	}
	
	/**
	 * Converts a long to an array of bytes
	 * @param value a long value
	 * @return an array of bytes
	 */
	public static byte[] longToBytes(long value) {
		return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(value).array();
	}
	
	/**
	 * Converts a double to an array of bytes
	 * @param value a double value
	 * @return an array of bytes
	 */
	public static byte[] doubleToBytes(double value) {
		return ByteBuffer.allocate(Double.BYTES).putDouble(value).array();
	}
	
	/**
	 * Converts a float to an array of bytes
	 * @param value a float value
	 * @return an array of bytes
	 */
	public static byte[] floatToByte (float value)  {
		return ByteBuffer.allocate(Float.BYTES).putFloat(value).array();
	}
	
	/**
	 * Takes an ArrayList and returns the primitive byte type
	 * @param bytesList an ArrayList containing bytes
	 * @return an array of bytes from the ArrayList
	 */
	public static byte[] byteArrayListToArray(List<Byte> bytesList) {
		byte[] bytes = new byte[bytesList.size()];
		for(int i = 0; i < bytesList.size(); i++) {
			bytes[i] = bytesList.get(i);
		}
		return bytes;
	}
	
	/**
	 * Reverses a byte array. Used by the various page classes to reverse/unreverse
	 * data cells for writing to and reading from the page respectively
	 * @param data the an array of data cell bytes to reverse
	 * @return the reversed version of the array
	 * @see edu.utdallas.cs6360.davisbase.trees.TableLeafPage
	 * @see edu.utdallas.cs6360.davisbase.trees.TableInteriorCell
	 */
	public static byte[] reverseByteArray(byte[] data) {
		for(int i = 0; i< data.length/2; i++) {
			byte temp;
			temp = data[i];
			data[i] = data[data.length - 1 - i];
			data[data.length - i - 1] = temp;
		}
		return data;
	}
	
	/**
	 * Reverses a byte array. Used by the various page classes to reverse/unreverse
	 * data cells for writing to and reading from the page respectively
	 * @param data the an array of data cell bytes to reverse
	 * @return the reversed version of the array
	 * @see edu.utdallas.cs6360.davisbase.trees.TableLeafPage
	 * @see edu.utdallas.cs6360.davisbase.trees.TableInteriorCell
	 */
	public static int[] reverseIntArray(int[] data) {
		for(int i = 0; i< data.length/2; i++) {
			int temp;
			temp = data[i];
			data[i] = data[data.length - 1 - i];
			data[data.length - i - 1] = temp;
		}
		return data;
	}
	
	public static DataCell[] reverseDataCellArray(DataCell[] data) {
		for(int i = 0; i< data.length/2; i++) {
			DataCell temp;
			temp = data[i];
			data[i] = data[data.length - 1 - i];
			data[data.length - i - 1] = temp;
		}
		return data;
	}
	
	/**
	 * Mostly used for debugging and logging purposes
	 * @param bytes an array of bytes to convert into a hexadecimal string
	 * @return a String in hexadecimal notation representing the array of bytes
	 */
	public static String getHexString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02X ", b));
		}
		return builder.toString();
	}
}