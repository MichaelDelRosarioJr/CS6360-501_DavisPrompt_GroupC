package edu.utdallas.cs6360.davisbase.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;

/**
 * Enum class to hold information about DataRecord column types
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public enum DataType {
	NULL1_TYPE_CODE((byte)0x00, (byte)0x01, NULL1),
	NULL2_TYPE_CODE((byte)0x01, (byte)0x02, NULL2),
	NULL4_TYPE_CODE((byte)0x02, (byte)0x04, NULL4),
	NULL8_TYPE_CODE((byte)0x03, (byte)0x08, NULL8),
	TINY_INT_TYPE_CODE((byte)0x04, (byte)0x01, TINYINT_STRING),
	SHORT_TYPE_CODE((byte)0x05, (byte)0x02, SMALLINT_STRING),
	INT_TYPE_CODE((byte)0x06, (byte)0x04, INT_STRING),
	LONG_TYPE_CODE((byte)0x07, (byte)0x08, BIGINT_STRING),
	REAL_TYPE_CODE((byte)0x08, (byte)0x04, REAL_STRING),
	DOUBLE_TYPE_CODE((byte)0x09, (byte)0x08, DOUBLE_STRING),
	DATETIME_TYPE_CODE((byte)0x0A, (byte)0x08, DATETIME_STRING),
	DATE_TYPE_CODE((byte)0x0B, (byte)0x08, DATE_STRING),
	TEXT_TYPE_CODE((byte)0x0C, (byte)0x7F, TEXT_STRING);
	
	
	private final byte typeCode;
	private final byte dataSize;
	private final String dataTypeName;
	
	/**
	 * Sets the typeCode and size of the datatype in bytes, for TEXT the size is
	 * the maximum text length
	 * @param code a byte representing a data type
	 * @param size the size of the data type in bytes
	 */
	DataType(byte code, byte size, String name) {
		this.typeCode = code;
		this.dataSize = size;
		this.dataTypeName = name;
	}
	
	/**
	 * Returns the type code
	 * @return type code for the DataType
	 */
	public byte getTypeCode() { return this.typeCode; }
	
	/**
	 * Method that returns the name of the DataType for logging purposes
	 * @return the name of the DataType
	 */
	public String toString() {
		return this.dataTypeName;
	}
	
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

	public static String getDataTypeString(DataType data) {
		switch (data) {
			case INT_TYPE_CODE:
				return "int";
			case TINY_INT_TYPE_CODE:
				return "tinyint";
			case SHORT_TYPE_CODE:
				return "smallint";
			case LONG_TYPE_CODE:
				return "bigint";
			case REAL_TYPE_CODE:
				return "real";
			case DOUBLE_TYPE_CODE:
				return "double";
			case DATETIME_TYPE_CODE:
				return "datetime";
			case DATE_TYPE_CODE:
				return "date";
			case TEXT_TYPE_CODE:
				return "text";
			default:
				return "NULL";
		}
	}
	
	public static boolean sameColTypes(ArrayList<DataType> typeList1, ArrayList<DataType> typeList2) {
		if(typeList1 == typeList2)
			return true;
		
		if (!Optional.ofNullable(typeList1).isPresent() ||
				!Optional.ofNullable(typeList2).isPresent() ||
				typeList1.size() != typeList2.size()) {
			return false;
		}
		
		Collections.sort(typeList1);
		Collections.sort(typeList2);
		
		return typeList1.equals(typeList2);
	}
}