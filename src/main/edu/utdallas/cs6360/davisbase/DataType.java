package edu.utdallas.cs6360.davisbase;

/**
 * Enum class to hold information about DataRecord column types
 * @author Charles Krol
 */
public enum DataType {
	NULL1_TYPE_CODE((byte)0x00, (byte)0x01),
	NULL2_TYPE_CODE((byte)0x01, (byte)0x02),
	NULL4_TYPE_CODE((byte)0x02, (byte)0x04),
	NULL8_TYPE_CODE((byte)0x03, (byte)0x08),
	TINY_INT_TYPE_CODE((byte)0x04, (byte)0x01),
	SHORT_TYPE_CODE((byte)0x05, (byte)0x02),
	INT_TYPE_CODE((byte)0x06, (byte)0x04),
	LONG_TYPE_CODE((byte)0x07, (byte)0x08),
	FLOAT_TYPE_CODE((byte)0x08, (byte)0x04),
	DOUBLE_TYPE_CODE((byte)0x09, (byte)0x08),
	DATETIME_TYPE_CODE((byte)0x0A, (byte)0x08),
	DATE_TYPE_CODE((byte)0x0B, (byte)0x08),
	TEXT_TYPE_CODE((byte)0x0C, (byte)0x7F);
	
	private final byte typeCode;
	private final byte dataSize;
	
	DataType(byte code, byte size) {
		this.typeCode = code;
		this.dataSize = size;
	}
	
	public byte getTypeCode() { return this.typeCode; }
	
	/**
	 * Returns the data type when given the byte code <br>
	 *
	 * If the code is larger than TEXT_TYPE_CODE then it is assumed
	 * to be TEXT_TYPE_CODE
	 * @param typeCode the code code representing the data tape
	 * @return the enum value for the data type
	 */
	public static DataType getEnum(byte typeCode) {
		for(DataType value : values()) {
			if(value.getTypeCode() == typeCode) { return value; }
		}
		
		if (typeCode > TEXT_TYPE_CODE.getTypeCode()) { return TEXT_TYPE_CODE; }
		throw new IllegalArgumentException();
	}
	
	/**
	 * Return the size of the data type
	 * @return the side of the data type
	 */
	public static byte getDataTypeSize(byte typeCode) {
		if(typeCode >= TEXT_TYPE_CODE.getTypeCode()) {
			return (byte)(typeCode - TEXT_TYPE_CODE.getTypeCode());
		}
		for(DataType value: values()) {
			if(value.getTypeCode() == typeCode) { return value.dataSize; }
		}
		throw new IllegalArgumentException();
	}
}
