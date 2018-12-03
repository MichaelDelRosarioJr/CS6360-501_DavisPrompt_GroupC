package edu.utdallas.cs6360.davisbase.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

/**
 * Enum class to hold information about DataRecord column types
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
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
	REAL_TYPE_CODE((byte)0x08, (byte)0x04),
	DOUBLE_TYPE_CODE((byte)0x09, (byte)0x08),
	DATETIME_TYPE_CODE((byte)0x0A, (byte)0x08),
	DATE_TYPE_CODE((byte)0x0B, (byte)0x08),
	TEXT_TYPE_CODE((byte)0x0C, (byte)0x7F);
	
	public static final String INT_STRING = "int";
	public static final String TINYINT_STRING = "tinyint";
	public static final String SMALLINT_STRING = "smallint";
	public static final String BIGINT_STRING = "bigint";
	public static final String REAL_STRING = "real";
	public static final String DOUBLE_STRING = "double";
	public static final String DATETIME_STRING = "datetime";
	public static final String DATE_STRING = "date";
	public static final String TEXT_STRING = "text";
	private final byte typeCode;
	private final byte dataSize;
	
	/**
	 * Sets the typeCode and size of the datatype in bytes, for TEXT the size is
	 * the maximum text length
	 * @param code a byte representing a data type
	 * @param size the size of the data type in bytes
	 */
	DataType(byte code, byte size) {
		this.typeCode = code;
		this.dataSize = size;
	}
	
	/**
	 * Returns the type code
	 * @return type code for the DataType
	 */
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
	
	/**
	 * Return the largest size of a DataType, used for determining how many cells can fit in a leaf page
	 * @param typeCode the type code representing the data type
	 * @return the largest amount of space the data type will take up, for Text it is 0x7C
	 */
	public static byte getMaxSize(byte typeCode) {
		for(DataType value: values()) {
			if(value.getTypeCode() == typeCode) { return value.dataSize; }
		}
		if(typeCode > TEXT_TYPE_CODE.getTypeCode()) {
			return TEXT_TYPE_CODE.dataSize;
		}
		throw new IllegalArgumentException("Invaild data type byte code");
	}

	public static DataType getDataTypeCodeFromString(String datatype) {
		switch (datatype) {
			case INT_STRING:
				return INT_TYPE_CODE;
			case TINYINT_STRING:
				return TINY_INT_TYPE_CODE;
			case SMALLINT_STRING:
				return SHORT_TYPE_CODE;
			case BIGINT_STRING:
				return LONG_TYPE_CODE;
			case REAL_STRING:
				return REAL_TYPE_CODE;
			case DOUBLE_STRING:
				return DOUBLE_TYPE_CODE;
			case DATETIME_STRING:
				return DATETIME_TYPE_CODE;
			case DATE_STRING:
				return DATE_TYPE_CODE;
			case TEXT_STRING:
				return TEXT_TYPE_CODE;
			default:
				return NULL1_TYPE_CODE;
		}
	}
	
	public static boolean sameColTypes(ArrayList<DataType> typeList1, ArrayList<DataType> typeList2) {
		if(typeList1 == typeList2)
			return true;
		
		if (Optional.ofNullable(typeList1).isPresent() ||
				Optional.ofNullable(typeList2).isPresent() ||
				typeList1.size() != typeList2.size()) {
			return false;
		}
		
		Collections.sort(typeList1);
		Collections.sort(typeList2);
		
		return typeList1.equals(typeList2);
	}
}