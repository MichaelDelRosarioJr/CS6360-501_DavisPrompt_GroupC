package edu.utdallas.cs6360.davisbase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Charles Krol
 */
class ByteHelpersTest {
	
	private byte[] shortTestVal = {(byte) 0xFC, 0x18};
	private byte[] intTestVal = {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x03};
	private byte[] longTestVal = {(byte)0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF,
						  (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFD};
	private byte[] floatTestVal = {(byte)0x40, (byte)0x49, (byte)0x0F, (byte)0xCF};
	private byte[] doubleTestVal = {(byte)0x40, (byte)0x09, (byte)0x21, (byte)0xF9,
							(byte)0xF0, (byte)0x1B, (byte)0x86, (byte)0x6E};
	
	
	@Test
	void byteToUnSignedInt() {
		byte val = (byte) 0xF5;
		int testVal = 245;
		assertEquals(ByteHelpers.byteToUnSignedInt(val), testVal);
	}
	
	@Test
	void shortToBytes() {
		short val = -1000;
		assertArrayEquals(ByteHelpers.shortToBytes(val), shortTestVal);
	}
	
	@Test
	void intToBytes() {
		int val = -2147483645;
		assertArrayEquals(ByteHelpers.intToBytes(val), intTestVal);
	}
	
	@Test
	void longToBytes() {
		long val = 9223372036854775805L;
		assertArrayEquals(ByteHelpers.longToBytes(val), longTestVal);
	}
	
	@Test
	void floatToByte() {
		float val = 3.1415899F;
		assertArrayEquals(ByteHelpers.floatToByte(val), floatTestVal);
	}
	
	@Test
	void doubleToBytes() {
		double val = 3.14159;
		assertArrayEquals(ByteHelpers.doubleToBytes(val), doubleTestVal);
	}
}