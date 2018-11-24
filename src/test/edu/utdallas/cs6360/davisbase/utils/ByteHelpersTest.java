package edu.utdallas.cs6360.davisbase.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class ByteHelpersTest {
	
	private static final byte[] SHORT_TEST_VAL = {(byte) 0xFC, 0x18};
	private static final byte[] INT_TEST_VAL = {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x03};
	private static final byte[] LONG_TEST_VAL = {(byte)0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF,
						  (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFD};
	private static final byte[] FLOAT_TEST_VAL = {(byte)0x40, (byte)0x49, (byte)0x0F, (byte)0xCF};
	private static final byte[] DOUBLE_TEST_VAL = {(byte)0x40, (byte)0x09, (byte)0x21, (byte)0xFB,
									(byte)0x54, (byte)0x44, (byte)0x2D, (byte)0x18};
	
	
	@Test
	void byteToUnSignedInt() {
		byte val = (byte) 0xF5;
		int testVal = 245;
		assertEquals(ByteHelpers.byteToUnSignedInt(val), testVal);
	}
	
	@Test
	void shortToBytes() {
		short val = -1000;
		assertArrayEquals(ByteHelpers.shortToBytes(val), SHORT_TEST_VAL);
	}
	
	@Test
	void intToBytes() {
		int val = -2147483645;
		assertArrayEquals(ByteHelpers.intToBytes(val), INT_TEST_VAL);
	}
	
	@Test
	void longToBytes() {
		long val = 9223372036854775805L;
		assertArrayEquals(ByteHelpers.longToBytes(val), LONG_TEST_VAL);
	}
	
	@Test
	void floatToByte() {
		float val = 3.1415899F;
		assertArrayEquals(ByteHelpers.floatToByte(val), FLOAT_TEST_VAL);
	}
	
	@Test
	void doubleToBytes() {
		assertArrayEquals(ByteHelpers.doubleToBytes(Math.PI), DOUBLE_TEST_VAL);
	}
}